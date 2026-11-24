package mx.uady.inventario.cliente.error;

/**
 * Presentación de un error para el encargado de ventas.
 *
 * @param titulo       tipo de error en pocas palabras (título de la ventana de reintento, RF-38)
 * @param mensaje      explicación en español de qué falló
 * @param reintentable {@code true} si volver a intentar podría resolver el problema (RF-38)
 */
public record DescripcionError(String titulo, String mensaje, boolean reintentable) {
}
