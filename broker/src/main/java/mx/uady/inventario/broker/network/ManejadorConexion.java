package mx.uady.inventario.broker.network;

import com.google.gson.JsonSyntaxException;
import mx.uady.inventario.broker.registry.AdministradorRegistro;
import mx.uady.inventario.broker.registry.RegistroServicios;
import mx.uady.inventario.broker.routing.Enrutador;
import mx.uady.inventario.broker.util.BitacoraBroker;
import mx.uady.inventario.broker.util.JsonBroker;
import mx.uady.inventario.common.contrato.Respuesta;
import mx.uady.inventario.common.contrato.Solicitud;
import mx.uady.inventario.common.error.CodigoError;
import mx.uady.inventario.common.json.GsonFactory;
import mx.uady.inventario.common.servicio.Servicios;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Atiende una conexion TCP del broker.
 * Implementa Runnable para que pueda ser ejecutada por un thread
 *
 * <p>Cada solicitud atendida y cada cambio en el registro de servicios se anotan
 * en la {@link BitacoraBroker}.</p>
 */
public class ManejadorConexion implements Runnable {

    /** Destino que se anota en la bitacora para las operaciones internas del broker. */
    private static final String DESTINO_BROKER = "broker";

    private final Socket socket;
    private final RegistroServicios registro;
    private final Enrutador enrutador;
    private final int timeoutMillis;
    private final BitacoraBroker bitacora;
    private final AdministradorRegistro administrador;

    /**
     * Crear un manejador de conexion para atender una peticion hecha por TCP.
     * Los eventos se escriben en la bitacora de consola.
     * @param socket El socket de la peticion
     * @param registro El registro de servicios del broker
     * @param enrutador El enrutador (Encargado de reenviar la peticion al servicio correspondiente)
     * @param timeoutMillis El tiempo en el que la peticion se marca como timeout en milisegundos
     */
    public ManejadorConexion(Socket socket, RegistroServicios registro, Enrutador enrutador, int timeoutMillis) {
        this(socket, registro, enrutador, timeoutMillis, BitacoraBroker.consola());
    }

    /**
     * Crear un manejador de conexion que anota cada solicitud en la bitacora indicada.
     * @param socket El socket de la peticion
     * @param registro El registro de servicios del broker
     * @param enrutador El enrutador (Encargado de reenviar la peticion al servicio correspondiente)
     * @param timeoutMillis El tiempo en el que la peticion se marca como timeout en milisegundos
     * @param bitacora La bitacora de solicitudes del broker
     * @throws NullPointerException si la bitacora es nula
     */
    public ManejadorConexion(Socket socket, RegistroServicios registro, Enrutador enrutador, int timeoutMillis,
                             BitacoraBroker bitacora) {
        this.socket = socket;
        this.registro = registro;
        this.enrutador = enrutador;
        this.timeoutMillis = timeoutMillis;
        this.bitacora = Objects.requireNonNull(bitacora, "La bitacora es obligatoria");
        this.administrador = new AdministradorRegistro(registro, bitacora);
    }

    /**
     * Logica que el thread debe de ejecutar para manejar la conexion
     */
    @Override
    public void run() {
        // Se separan las declaraciones del try-with-resources en múltiples líneas para evitar el desplazamiento horizontal
        try (
            socket;
            InputStreamReader isr = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
            BufferedReader entrada = new BufferedReader(isr);
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)
        ) {
            // Configuración de resiliencia de red requerida por el nivel 2
            socket.setSoTimeout(timeoutMillis);

            String linea;
            while ((linea = entrada.readLine()) != null) {
                if (linea.isBlank()) {
                    continue;
                }

                // 1. Interpretar la solicitud entrante
                Respuesta respuesta = procesarSolicitud(linea);

                // 2. Marshalling: Convertir el objeto Respuesta a formato JSON
                String respuestaJson = JsonBroker.serializarRespuesta(respuesta);

                // 3. Enviar la respuesta al cliente y vaciar el buffer
                salida.println(respuestaJson);
                salida.flush();
            }

        } catch (IOException e) {
            // Manejo de desconexiones: se captura la excepción silenciosamente
            // para no tumbar la atención concurrente del Broker.
        }

    }

    /**
     * Procesa una solicitud entrante, la enruta si los parametros son correctos y
     * anota en la bitacora el resultado y el tiempo de atencion.
     * @param linea La cadena que contenga un Json valido de {@code solicitud}
     * @return La respuesta que produjo el servicio
     */
    private Respuesta procesarSolicitud(String linea) {
        long inicio = System.nanoTime();
        Solicitud solicitud = null;
        Respuesta respuesta;
        try {
            solicitud = GsonFactory.DEFAULT.fromJson(linea, Solicitud.class);
            respuesta = atenderSolicitud(solicitud);
        } catch (JsonSyntaxException | IllegalStateException | NullPointerException e) {
            respuesta = Respuesta.error(null, CodigoError.PARAMETROS_INVALIDOS, "JSON malformado");
        } catch (RuntimeException e) {
            respuesta = Respuesta.error(null, CodigoError.ERROR_INTERNO, "Error interno del broker: " + e.getMessage());
        }
        long millis = (System.nanoTime() - inicio) / 1_000_000;
        bitacora.registrarSolicitud(solicitud, respuesta, destinoDe(solicitud), millis);
        return respuesta;
    }

    /**
     * Atiende una solicitud ya interpretada: la resuelve el propio broker o la enruta al proveedor.
     * @param solicitud La solicitud interpretada; puede ser nula si el JSON estaba vacio
     * @return La respuesta para el cliente
     * @throws IllegalArgumentException si la solicitud no tiene id, servicio u operacion
     */
    private Respuesta atenderSolicitud(Solicitud solicitud) {
        if ( solicitud == null ) {
            return Respuesta.error(null, CodigoError.PARAMETROS_INVALIDOS, "JSON malformado");
        }

        // Hacer una operacion del broker
        if ( Servicios.BROKER.equals(solicitud.getNombreServicio()) ) {
            return administrador.atender(solicitud);
        }

        return enrutador.enrutar(solicitud, registro);
    }

    /**
     * Determina el destino que se anota en la bitacora para una solicitud.
     * @param solicitud La solicitud atendida; puede ser nula
     * @return {@code broker} para operaciones internas, {@code host:puerto} del proveedor
     *         registrado, o {@code null} si no hay proveedor
     */
    private String destinoDe(Solicitud solicitud) {
        if (solicitud == null || solicitud.getNombreServicio() == null) {
            return null;
        }
        if (Servicios.BROKER.equals(solicitud.getNombreServicio())) {
            return DESTINO_BROKER;
        }
        return registro.buscar(solicitud.getNombreServicio()).map(BitacoraBroker::direccion).orElse(null);
    }
}
