package mx.uady.inventario.cliente.error;

import mx.uady.inventario.common.error.CodigoError;

/**
 * Fallo de una operación contra el Broker: problema de red, tiempo de espera agotado o respuesta
 * inválida. Su especialización {@link ErrorServicioException} cubre las respuestas de error del servidor.
 *
 * <p>Requisitos: RF-37 (errores de red con mensaje propio), RF-38 (reintento explícito) y
 * RF-40 (tiempo de espera agotado).</p>
 */
public class ComunicacionException extends Exception {

    private final ComunicacionError tipo;
    private final CodigoError codigoError;

    /**
     * Crea un fallo de comunicación (sin código de error del contrato).
     *
     * @param tipo    clasificación del fallo
     * @param mensaje descripción técnica (para bitácora, no para el encargado)
     * @param causa   excepción original; puede ser nula
     */
    public ComunicacionException(ComunicacionError tipo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.tipo = tipo;
        this.codigoError = null;
    }

    private ComunicacionException(CodigoError codigoError, String mensaje) {
        super(mensaje);
        this.tipo = ComunicacionError.ERROR_SERVIDOR;
        this.codigoError = codigoError;
    }

    /**
     * Crea el fallo correspondiente a una respuesta de error del servidor.
     *
     * @param codigoError código de error del contrato; puede ser nulo
     * @param mensaje     mensaje enviado por el servidor; puede ser nulo
     * @return excepción de tipo {@link ComunicacionError#ERROR_SERVIDOR}
     */
    public static ComunicacionException servidor(CodigoError codigoError, String mensaje) {
        return new ComunicacionException(codigoError, mensaje);
    }

    /**
     * Obtiene la clasificación del fallo.
     *
     * @return tipo de fallo
     */
    public ComunicacionError getTipo() {
        return tipo;
    }

    /**
     * Obtiene el código de error del contrato.
     *
     * @return código, o {@code null} si el fallo no vino de una respuesta del servidor o no lo indicó
     */
    public CodigoError getCodigoError() {
        return codigoError;
    }
}

