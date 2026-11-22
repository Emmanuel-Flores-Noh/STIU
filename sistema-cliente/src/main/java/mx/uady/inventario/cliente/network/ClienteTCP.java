package mx.uady.inventario.cliente.network;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import mx.uady.inventario.cliente.error.ComunicacionException;
import mx.uady.inventario.cliente.error.ComunicacionError;
import mx.uady.inventario.common.contrato.EstadoRespuesta;
import mx.uady.inventario.common.contrato.Respuesta;
import mx.uady.inventario.common.contrato.Solicitud;
import mx.uady.inventario.common.json.GsonFactory;

import java.util.UUID;

/**
 * Cliente del protocolo del Broker: arma la {@link Solicitud} con un idSolicitud único, la serializa
 * a JSON, la envía por {@link ConexionTCP} (un mensaje por línea) e interpreta y valida la {@link Respuesta}.
 *
 * <p>No reintenta automáticamente: si se pierde la respuesta de una venta ya procesada, reenviarla
 * podría registrarla dos veces. Los reintentos hacia el Sistema 1 los hace el Broker y, ante un fallo,
 * el controlador ofrece al encargado reintentar de forma explícita.</p>
 */
public class ClienteTCP {

    private final ConexionTCP conexion;
    private final Gson gson = GsonFactory.DEFAULT;

    /**
     * Crea un cliente que se conecta al Broker indicado.
     *
     * @param host      host o IP del Broker
     * @param puerto    puerto del Broker
     * @param timeoutMs tiempo límite total por solicitud, en milisegundos
     */
    public ClienteTCP(String host, int puerto, int timeoutMs) {
        this(new ConexionTCP(host, puerto, timeoutMs));
    }

    /**
     * Crea un cliente con un transporte ya construido.
     *
     * @param conexion transporte por el que viajan las líneas JSON
     */
    public ClienteTCP(ConexionTCP conexion) {
        this.conexion = conexion;
    }

    /**
     * Ejecuta una operación en el Broker y devuelve su resultado.
     *
     * @param servicio   nombre del servicio destino
     * @param operacion  nombre de la operación
     * @param parametros parámetros de la operación; puede ser nulo
     * @return resultado de la respuesta exitosa (puede ser nulo)
     * @throws ComunicacionException si falla la red, se agota el tiempo, la respuesta es inválida
     *                                o el servidor responde con estado de error
     */
    public JsonElement invocar(String servicio, String operacion, JsonObject parametros)
            throws ComunicacionException {
        Solicitud solicitud = Solicitud.crear(UUID.randomUUID().toString(), servicio, operacion, parametros);
        Respuesta respuesta = enviarSolicitud(solicitud);
        if (respuesta.getEstado() != EstadoRespuesta.EXITO) {
            throw ComunicacionException.servidor(respuesta.getCodigoError(), respuesta.getMensajeError());
        }
        return respuesta.getResultado();
    }

    /**
     * Envía una solicitud al Broker y devuelve su respuesta ya validada.
     *
     * @param solicitud datos de la operación a ejecutar
     * @return respuesta correspondiente a la solicitud
     * @throws ComunicacionException si falla la red, se agota el tiempo, la respuesta no es JSON
     *                                válido o su idSolicitud no coincide con el de la solicitud
     */
    public Respuesta enviarSolicitud(Solicitud solicitud) throws ComunicacionException {
        String lineaRespuesta = conexion.intercambiar(gson.toJson(solicitud));
        Respuesta respuesta = interpretar(lineaRespuesta);
        validarCorrelacion(solicitud, respuesta);
        return respuesta;
    }

    private Respuesta interpretar(String linea) throws ComunicacionException {
        try {
            Respuesta respuesta = gson.fromJson(linea, Respuesta.class);
            if (respuesta == null) {
                throw new ComunicacionException(ComunicacionError.RESPUESTA_INVALIDA, "El Broker envió una respuesta vacía.", null);
            }
            return respuesta;
        } catch (JsonParseException e) {
            throw new ComunicacionException(ComunicacionError.RESPUESTA_INVALIDA, "El Broker envió JSON inválido.", e);
        }
    }

    /** Rechaza una respuesta cuyo idSolicitud sea distinto al de la solicitud enviada. */
    private static void validarCorrelacion(Solicitud solicitud, Respuesta respuesta) throws ComunicacionException {
        String esperado = solicitud.getIdSolicitud();
        String recibido = respuesta.getIdSolicitud();
        if (esperado != null && recibido != null && !esperado.equals(recibido)) {
            throw new ComunicacionException(ComunicacionError.RESPUESTA_INVALIDA,
                    "El idSolicitud de la respuesta no coincide con el de la solicitud.", null);
        }
    }
}
