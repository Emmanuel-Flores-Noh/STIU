package mx.uady.inventario.cliente.error;

import mx.uady.inventario.common.error.CodigoError;

/**
 * Señala que el Broker o el Sistema 1 respondieron la solicitud con estado de error. Conserva el
 * código y el mensaje del contrato para traducirlos a un texto entendible.
 *
 * <p>Trazabilidad con Requisitos:
 * <ul>
 *   <li><b>RF-33:</b> Rechazo de venta con la causa descriptiva enviada por el servidor remoto sin reintentar.</li>
 *   <li><b>RF-37:</b> Presentación individualizada de cada código de error del catálogo estandarizado.</li>
 *   <li><b>RF-38:</b> Discriminación de errores que permiten ofrecer el reintento de la operación.</li>
 * </ul>
 * </p>
 */
public class ErrorServicioException extends ComunicacionException {

    private final CodigoError codigoError;

    /**
     * Crea la excepción a partir de los datos de una respuesta de error.
     *
     * @param codigoError código de error del contrato; puede ser nulo
     * @param mensaje     mensaje de error enviado por el servidor; puede ser nulo
     */
    public ErrorServicioException(CodigoError codigoError, String mensaje) {
        super(ComunicacionError.ERROR_SERVIDOR, mensaje, null);
        this.codigoError = codigoError;
    }

    /**
     * Obtiene el código de error del contrato.
     *
     * @return código de error, o {@code null} si el servidor no lo indicó
     */
    public CodigoError getCodigoError() {
        return codigoError;
    }
}
