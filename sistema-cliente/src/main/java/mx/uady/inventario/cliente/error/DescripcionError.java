package mx.uady.inventario.cliente.error;

/**
 * Presentación de un error para el encargado de ventas.
 *
 * <p>Requisitos: RF-37 (mensaje con la causa), RF-38 (ventana con el tipo de error y decisión de
 * reintento) y RNF-33 (mensajes en español, sin JSON ni trazas).</p>
 *
 * @param titulo       tipo de error en pocas palabras (título de la ventana de reintento, RF-38)
 * @param mensaje      explicación en español de qué falló
 * @param reintentable {@code true} si volver a intentar podría resolver el problema (RF-38)
 */
public record DescripcionError(String titulo, String mensaje, boolean reintentable) {
}
