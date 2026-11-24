package mx.uady.inventario.broker.routing;

import mx.uady.inventario.broker.model.Proveedor;
import mx.uady.inventario.broker.registry.RegistroServicios;
import mx.uady.inventario.broker.util.BitacoraBroker;
import mx.uady.inventario.broker.util.ConfiguracionBroker;
import mx.uady.inventario.common.contrato.Respuesta;
import mx.uady.inventario.common.contrato.Solicitud;
import mx.uady.inventario.common.error.CodigoError;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.Optional;

/**
 * Enruta una solicitud al proveedor registrado y devuelve la respuesta.
 *
 * <p>La resiliencia vive aqui: el timeout de conexion y el de lectura se
 * configuran por separado, los fallos de red se reintentan de forma acotada y
 * cada fallo se reporta al {@link RegistroServicios} para que un proveedor que
 * falla de forma repetida quede marcado como no disponible.</p>
 *
 * <p>Una respuesta de error valida del proveedor no es un fallo de red: no se
 * reintenta y ademas cuenta como comunicacion exitosa.</p>
 *
 * <p>Cada intento fallido y cada proveedor que queda marcado como no disponible
 * se registran en la {@link BitacoraBroker}.</p>
 *
 * <p>El envio por socket lo hace el {@link ConectorProveedor} y la repeticion de
 * intentos la decide la {@link PoliticaReintentos}.</p>
 */
public class Enrutador {
    private final ConectorProveedor conector;
    private final PoliticaReintentos politica;
    private final BitacoraBroker bitacora;

    /**
     * Constructor compatible con el comportamiento anterior: el mismo timeout
     * para conexion y lectura, sin reintentos.
     *
     * @param timeoutMillis El tiempo en milisegundos para marcar una peticion como timeout
     */
    public Enrutador(int timeoutMillis) {
        this(timeoutMillis, timeoutMillis, 0, 0);
    }

    /**
     * Constructor con la configuracion de resiliencia del broker. Los eventos se
     * escriben en la bitacora de consola.
     *
     * @param configuracion timeouts, reintentos y espera entre intentos
     */
    public Enrutador(ConfiguracionBroker configuracion) {
        this(configuracion, BitacoraBroker.consola());
    }

    /**
     * Constructor con la configuracion de resiliencia y la bitacora donde se
     * registran los intentos fallidos y los proveedores marcados como no disponibles.
     *
     * @param configuracion timeouts, reintentos y espera entre intentos
     * @param bitacora      bitacora de eventos del broker
     * @throws NullPointerException si la bitacora es nula
     */
    public Enrutador(ConfiguracionBroker configuracion, BitacoraBroker bitacora) {
        this(configuracion.timeoutConexionMillis(),
                configuracion.timeoutLecturaMillis(),
                configuracion.reintentos(),
                configuracion.esperaReintentoMillis(),
                bitacora);
    }

    /**
     * Constructor con todos los parametros de red. Los eventos se escriben en la
     * bitacora de consola.
     *
     * @param timeoutConexionMillis timeout de conexion al proveedor
     * @param timeoutLecturaMillis timeout de lectura de la respuesta
     * @param reintentos reintentos adicionales tras el primer intento
     * @param esperaReintentoMillis espera entre intentos
     * @throws IllegalArgumentException si algun valor esta fuera de rango
     */
    public Enrutador(int timeoutConexionMillis, int timeoutLecturaMillis, int reintentos, long esperaReintentoMillis) {
        this(timeoutConexionMillis, timeoutLecturaMillis, reintentos, esperaReintentoMillis,
                BitacoraBroker.consola());
    }

    /**
     * Constructor con todos los parametros de red y la bitacora de eventos.
     *
     * @param timeoutConexionMillis timeout de conexion al proveedor
     * @param timeoutLecturaMillis timeout de lectura de la respuesta
     * @param reintentos reintentos adicionales tras el primer intento
     * @param esperaReintentoMillis espera entre intentos
     * @param bitacora bitacora donde se registran los intentos fallidos
     * @throws IllegalArgumentException si algun valor esta fuera de rango
     * @throws NullPointerException si la bitacora es nula
     */
    public Enrutador(int timeoutConexionMillis, int timeoutLecturaMillis, int reintentos, long esperaReintentoMillis,
                     BitacoraBroker bitacora) {
        this(new ConectorProveedor(timeoutConexionMillis, timeoutLecturaMillis),
                new PoliticaReintentos(reintentos, esperaReintentoMillis),
                bitacora);
    }

    /**
     * Constructor con los colaboradores ya creados.
     *
     * @param conector envia la solicitud al proveedor
     * @param politica decide cuantas veces se intenta el envio
     * @param bitacora bitacora donde se registran los intentos fallidos
     * @throws NullPointerException si algun colaborador es nulo
     */
    Enrutador(ConectorProveedor conector, PoliticaReintentos politica, BitacoraBroker bitacora) {
        this.conector = Objects.requireNonNull(conector, "El conector es obligatorio");
        this.politica = Objects.requireNonNull(politica, "La politica de reintentos es obligatoria");
        this.bitacora = Objects.requireNonNull(bitacora, "La bitacora es obligatoria");
    }

    /**
     * Valida si una solicitud es valida
     * @param solicitud La solicitud a evaluar
     * @return {@code true} si la solicitud es valida
     */
    private boolean validarSolicitud(Solicitud solicitud) {
        return solicitud != null
                && solicitud.getIdSolicitud() != null && !solicitud.getIdSolicitud().isBlank()
                && solicitud.getNombreServicio() != null && !solicitud.getNombreServicio().isBlank()
                && solicitud.getNombreOperacion() != null && !solicitud.getNombreOperacion().isBlank();
    }

    /**
     * Enruta la solicitud al proveedor del servicio, con reintentos acotados.
     *
     * @param solicitud solicitud a enviar
     * @param registro registro de proveedores y su salud
     * @throws IllegalArgumentException si la solicitud no tiene id, servicio u operacion
     * @return la respuesta del servicio o una respuesta de error del broker
     */
    public Respuesta enrutar(Solicitud solicitud, RegistroServicios registro) throws IllegalArgumentException {
        if (!validarSolicitud(solicitud)) {
            throw new IllegalArgumentException("La solicitud no tiene id, servicio u operacion");
        }
        Objects.requireNonNull(registro, "El registro es obligatorio");

        Optional<Proveedor> proveedorOpt = registro.buscar(solicitud.getNombreServicio());
        if (proveedorOpt.isEmpty()) {
            return Respuesta.error(solicitud.getIdSolicitud(), CodigoError.SERVICIO_DESCONOCIDO,
                    "Servicio desconocido: " + solicitud.getNombreServicio());
        }
        if (!registro.estaDisponible(solicitud.getNombreServicio())) {
            return Respuesta.error(solicitud.getIdSolicitud(), CodigoError.PROVEEDOR_NO_REGISTRADO,
                    "Proveedor no disponible: " + solicitud.getNombreServicio());
        }

        Proveedor proveedor = proveedorOpt.get();
        String servicio = solicitud.getNombreServicio();
        try {
            Respuesta respuesta = politica.ejecutar(() -> conector.enviar(solicitud, proveedor),
                    (intento, intentosTotales, error) -> bitacora.registrarIntentoFallido(solicitud, proveedor,
                            intento, intentosTotales, codigoDe(error), mensajeDe(error)));
            // La comunicacion funciono aunque el proveedor haya respondido con error.
            registro.marcarExito(servicio);
            return respuesta;
        } catch (IOException | RuntimeException e) {
            boolean disponibleAntes = registro.estaDisponible(servicio);
            registro.marcarFallo(servicio);
            if (disponibleAntes && !registro.estaDisponible(servicio)) {
                bitacora.registrarNoDisponible(servicio, registro.fallos(servicio));
            }
            return Respuesta.error(solicitud.getIdSolicitud(), codigoDe(e), mensajeDe(e));
        }
    }

    /**
     * Codigo de error del contrato para un fallo de comunicacion.
     * @param error el fallo de un intento
     * @return {@code TIMEOUT} si se agoto el tiempo de espera; {@code ERROR_INTERNO} en otro caso
     */
    private static CodigoError codigoDe(Exception error) {
        return error instanceof SocketTimeoutException ? CodigoError.TIMEOUT : CodigoError.ERROR_INTERNO;
    }

    /**
     * Mensaje para el cliente y la bitacora de un fallo de comunicacion.
     * @param error el fallo de un intento
     * @return el mensaje del fallo
     */
    private static String mensajeDe(Exception error) {
        if (error instanceof SocketTimeoutException) {
            return "Tiempo de espera agotado al contactar al proveedor";
        }
        return "Error de comunicacion con el proveedor: " + error.getMessage();
    }
}
