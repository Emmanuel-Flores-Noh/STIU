package mx.uady.inventario.cliente.error;

/**
 * Clasificación de los fallos al comunicarse con el Broker. Se usa para elegir el mensaje que ve el
 * encargado y para decidir si se ofrece reintentar.
 *
 * <p>Trazabilidad con Requisitos:
 * <ul>
 *   <li><b>RF-37:</b> Presentación de cada tipo de error o excepción de red con un mensaje descriptivo propio.</li>
 *   <li><b>RF-38:</b> Discriminación de errores que permiten ofrecer el reintento interactivo de la operación.</li>
 *   <li><b>RF-39:</b> Detección de fallos en la comunicación exclusiva con el Broker y rechazo de respuestas no correlacionadas.</li>
 *   <li><b>RF-40:</b> Detección de tiempo límite de respuesta agotado.</li>
 *   <li><b>RNF-28:</b> Tratamiento de respuestas inválidas como fallos de comunicación.</li>
 * </ul>
 * </p>
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
