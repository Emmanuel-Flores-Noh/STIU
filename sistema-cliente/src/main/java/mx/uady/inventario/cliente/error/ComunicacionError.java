package mx.uady.inventario.cliente.error;

/**
 * Clasificación de los fallos al comunicarse con el Broker. Se usa para elegir el mensaje que ve el
 * encargado y para decidir si se ofrece reintentar.
 */
public enum ComunicacionError {
    /** No se pudo abrir la conexión con el Broker (apagado, host o puerto incorrectos). */
    SIN_CONEXION,
    /** Se agotó el tiempo límite al conectar o al esperar la respuesta (RF-40). */
    TIEMPO_AGOTADO,
    /** La conexión se cerró o se perdió antes de recibir la respuesta completa. */
    CONEXION_INTERRUMPIDA,
    /** La respuesta no es JSON válido o su idSolicitud no corresponde a la solicitud enviada (RF-39, RNF-28). */
    RESPUESTA_INVALIDA,
    /** El Broker o el Sistema 1 respondieron con estado de error; ver {@link ErrorServicioException}. */
    ERROR_SERVIDOR
}
