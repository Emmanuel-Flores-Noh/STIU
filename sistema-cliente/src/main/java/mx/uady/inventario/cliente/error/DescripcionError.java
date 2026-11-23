package mx.uady.inventario.cliente.error;

/**
 * Presentación de un error para el encargado de ventas.
 *
 * <p>Trazabilidad con Requisitos:
 * <ul>
 *   <li><b>RF-37:</b> Presentación de cada error del servidor o de red mediante un mensaje explicativo de la causa.</li>
 *   <li><b>RF-38:</b> Ventana con el tipo de error y la decisión interactiva de reintento («¿Desea reintentar la operación?»).</li>
 *   <li><b>RNF-33:</b> Mensajes en español comprensibles para el usuario, sin fugas de JSON, trazas ni datos técnicos crudos.</li>
 * </ul>
 * </p>
 *
 * @param titulo       tipo de error en pocas palabras (título de la ventana de reintento, RF-38)
 * @param mensaje      explicación en español de qué falló
 * @param reintentable {@code true} si volver a intentar podría resolver el problema (RF-38)
 */
public record DescripcionError(String titulo, String mensaje, boolean reintentable) {
}
