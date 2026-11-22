package mx.uady.inventario.cliente.error;

import mx.uady.inventario.common.error.CodigoError;

/**
 * Señala que el Broker o el Sistema 1 respondieron la solicitud con estado de error. Conserva el
 * código y el mensaje del contrato para traducirlos a un texto entendible.
 *
 * <p>Requisitos: RF-33 (rechazo de la venta con la causa del servidor), RF-37 (mensaje por cada
 * código del catálogo de errores) y RF-38 (distinguir errores que sí permiten reintentar).</p>
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
