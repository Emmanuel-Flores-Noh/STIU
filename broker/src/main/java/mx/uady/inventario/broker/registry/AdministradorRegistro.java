package mx.uady.inventario.broker.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import mx.uady.inventario.broker.model.Proveedor;
import mx.uady.inventario.broker.util.BitacoraBroker;
import mx.uady.inventario.common.contrato.Respuesta;
import mx.uady.inventario.common.contrato.Solicitud;
import mx.uady.inventario.common.error.CodigoError;
import mx.uady.inventario.common.json.GsonFactory;
import mx.uady.inventario.common.servicio.Operaciones;

import java.util.Objects;

/**
 * Atiende las operaciones de administracion del broker: registrar, actualizar y
 * remover proveedores en el {@link RegistroServicios}.
 *
 * <p>Cada cambio en el registro, aplicado o no, se anota en la {@link BitacoraBroker}.
 * Las respuestas de error conservan el id de la solicitud.</p>
 */
public class AdministradorRegistro {

    private final RegistroServicios registro;
    private final BitacoraBroker bitacora;

    /**
     * Crea el administrador del registro de servicios.
     * @param registro El registro de servicios del broker
     * @param bitacora La bitacora donde se anotan los cambios del registro
     * @throws NullPointerException si el registro o la bitacora son nulos
     */
    public AdministradorRegistro(RegistroServicios registro, BitacoraBroker bitacora) {
        this.registro = Objects.requireNonNull(registro, "El registro es obligatorio");
        this.bitacora = Objects.requireNonNull(bitacora, "La bitacora es obligatoria");
    }

    /**
     * Ejecuta una operacion propia del broker (registrar, actualizar o remover proveedores).
     * @param solicitud La solicitud dirigida al servicio {@code broker}
     * @return La respuesta del propio broker; {@code OPERACION_NO_SOPORTADA} si la operacion no existe
     */
    public Respuesta atender(Solicitud solicitud) {
        if( solicitud.getNombreOperacion().equals(Operaciones.REGISTRAR_PROVEEDOR) ) {
            return registrarDesdeSolicitud(solicitud);
        }

        if( solicitud.getNombreOperacion().equals(Operaciones.ACTUALIZAR_PROVEEDOR) ) {
            return modificarDesdeSolicitud(solicitud);
        }

        if( solicitud.getNombreOperacion().equals(Operaciones.REMOVER_PROVEEDOR) ) {
            return removerDesdeSolicitud(solicitud);
        }

        return Respuesta.error(solicitud.getIdSolicitud(), CodigoError.OPERACION_NO_SOPORTADA, "Operacion no encontrada en el broker");
    }

    /**
     * Registra un servicio al broker desde una solicitud
     * @param solicitud La solicitud
     * @return La respuesta del propio broker
     */
    private Respuesta registrarDesdeSolicitud(Solicitud solicitud) {
        try {
            Proveedor proveedor = leerProveedor(solicitud);
            registro.registrar(proveedor);
            bitacora.registrarCambioRegistro("registrar", proveedor.servicio(), proveedor, true);
            return Respuesta.exito(solicitud.getIdSolicitud(), new JsonPrimitive("Proveedor registrado"));
        } catch (IllegalArgumentException e) {
            return parametrosInvalidos(solicitud, "registro", e);
        }
    }

    /**
     * Sustituye el proveedor de un servicio desde una solicitud.
     * Si el servicio no esta registrado no se agrega y se responde {@code PROVEEDOR_NO_REGISTRADO}.
     * @param solicitud La solicitud con el servicio, host y puerto del nuevo proveedor
     * @return La respuesta del propio broker
     */
    private Respuesta modificarDesdeSolicitud(Solicitud solicitud) {
        try {
            Proveedor proveedor = leerProveedor(solicitud);
            boolean sustituido = registro.sustituir(proveedor);
            bitacora.registrarCambioRegistro("sustituir", proveedor.servicio(), proveedor, sustituido);
            if (!sustituido) {
                return proveedorNoRegistrado(solicitud, proveedor.servicio());
            }
            return Respuesta.exito(solicitud.getIdSolicitud(), new JsonPrimitive("Proveedor modificado"));
        } catch (IllegalArgumentException e) {
            return parametrosInvalidos(solicitud, "actualizacion", e);
        }
    }

    /**
     * Elimina del registro el proveedor de un servicio desde una solicitud.
     * Solo se necesita el nombre del servicio; si no esta registrado se responde
     * {@code PROVEEDOR_NO_REGISTRADO}.
     * @param solicitud La solicitud con el servicio a eliminar
     * @return La respuesta del propio broker
     */
    private Respuesta removerDesdeSolicitud(Solicitud solicitud) {
        try {
            String servicio = leerServicio(solicitud);
            boolean eliminado = registro.eliminar(servicio);
            bitacora.registrarCambioRegistro("eliminar", servicio, null, eliminado);
            if (!eliminado) {
                return proveedorNoRegistrado(solicitud, servicio);
            }
            return Respuesta.exito(solicitud.getIdSolicitud(), new JsonPrimitive("Proveedor eliminado"));
        } catch (IllegalArgumentException e) {
            return parametrosInvalidos(solicitud, "eliminacion", e);
        }
    }

    /**
     * Obtiene los parametros de una operacion de administracion.
     * @param solicitud La solicitud dirigida al broker
     * @return Los parametros como objeto JSON
     * @throws IllegalArgumentException si la solicitud no trae parametros o no son un objeto JSON
     */
    private static JsonObject leerParametros(Solicitud solicitud) {
        JsonElement params = solicitud.getParametros();
        if (params == null || !params.isJsonObject()) {
            throw new IllegalArgumentException("se esperaba un objeto con los datos del proveedor");
        }
        return params.getAsJsonObject();
    }

    /**
     * Obtiene el nombre del servicio de los parametros de una operacion de administracion.
     * @param solicitud La solicitud dirigida al broker
     * @return El nombre del servicio
     * @throws IllegalArgumentException si no hay parametros o el servicio es nulo o vacio
     */
    private static String leerServicio(Solicitud solicitud) {
        JsonElement servicio = leerParametros(solicitud).get("servicio");
        if (servicio == null || !servicio.isJsonPrimitive() || servicio.getAsString().isBlank()) {
            throw new IllegalArgumentException("servicio es obligatorio");
        }
        return servicio.getAsString();
    }

    /**
     * Construye el proveedor descrito en los parametros de una operacion de administracion.
     * @param solicitud La solicitud con el servicio, host y puerto del proveedor
     * @return El proveedor leido
     * @throws IllegalArgumentException si falta el servicio, el host o el puerto no es valido
     */
    private static Proveedor leerProveedor(Solicitud solicitud) {
        leerServicio(solicitud);
        try {
            return GsonFactory.DEFAULT.fromJson(leerParametros(solicitud), Proveedor.class);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } catch (RuntimeException e) {
            // Gson envuelve la validacion del constructor de Proveedor; se recupera su mensaje.
            for (Throwable causa = e.getCause(); causa != null; causa = causa.getCause()) {
                if (causa instanceof IllegalArgumentException invalido) {
                    throw invalido;
                }
            }
            throw e;
        }
    }

    /**
     * Respuesta de error para parametros invalidos en una operacion de administracion.
     * @param solicitud La solicitud atendida
     * @param operacion El nombre de la operacion para el mensaje
     * @param causa La validacion que fallo
     * @return La respuesta {@code PARAMETROS_INVALIDOS} con el id de la solicitud
     */
    private static Respuesta parametrosInvalidos(Solicitud solicitud, String operacion, IllegalArgumentException causa) {
        return Respuesta.error(solicitud.getIdSolicitud(), CodigoError.PARAMETROS_INVALIDOS,
                "Parametros invalidos para " + operacion + ": " + causa.getMessage());
    }

    /**
     * Respuesta de error para un servicio que no esta en el registro.
     * @param solicitud La solicitud atendida
     * @param servicio El servicio buscado
     * @return La respuesta {@code PROVEEDOR_NO_REGISTRADO} con el id de la solicitud
     */
    private static Respuesta proveedorNoRegistrado(Solicitud solicitud, String servicio) {
        return Respuesta.error(solicitud.getIdSolicitud(), CodigoError.PROVEEDOR_NO_REGISTRADO,
                "Proveedor no registrado: " + servicio);
    }
}
