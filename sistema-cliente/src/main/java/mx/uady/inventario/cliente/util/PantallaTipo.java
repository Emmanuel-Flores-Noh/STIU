package mx.uady.inventario.cliente.util;

/**
 * Catálogo de mensajes estándar y estados visuales del Sistema Cliente (Proveedor).
 * Centraliza las cadenas de texto exactas especificadas en los requisitos del sistema
 * para las barras de estado, validaciones de formularios y mensajes de error.
 *
 * <p>Trazabilidad con Requisitos:
 * <ul>
 *   <li><b>RF-25:</b> Estados de barra de estado «Cargando» y «Listo», y mensaje de error «No se pudieron cargar las listas.» ante fallos.</li>
 *   <li><b>RF-30:</b> Indicador de carga activo durante la actualización manual de catálogos.</li>
 *   <li><b>RF-31:</b> Mensaje de validación «Debe seleccionar una unidad de mercancía, una tienda y un usuario proveedor.» si faltan datos.</li>
 *   <li><b>RF-33:</b> Encabezado de error «No se pudo registrar la venta» cuando el servidor rechaza la transacción.</li>
 *   <li><b>RF-37:</b> Notificación descriptiva de fallos en pantalla.</li>
 * </ul>
 * </p>
 */
public enum PantallaTipo {
    /** Mensaje de la barra de estado mientras se cargan las listas. */
    ESTADO_CARGANDO("Cargando..."),
    /** Mensaje de la barra de estado cuando termina una operación. */
    ESTADO_LISTO("Listo"),
    /** Mensaje cuando fallan las consultas de las listas. */
    ERROR_LISTAS("No se pudieron cargar las listas."),
    /** Mensaje cuando falta la unidad, la tienda o el usuario. */
    ERROR_DATOS_INCOMPLETOS("Debe seleccionar una unidad de mercancía, una tienda y un usuario proveedor."),
    /** Mensaje cuando el Sistema 1 o la red impiden registrar la venta. */
    ERROR_VENTA("No se pudo registrar la venta.");
    private String mensaje;

    PantallaTipo(String mensaje) {
        this.mensaje = mensaje;
    }

    /**
     * Obtiene el mensaje textual asociado al estado o error.
     *
     * @return texto del mensaje
     */
    public String getMensaje() {
        return mensaje;
    }
    @Override
    public String toString(){
        return getMensaje();
    }
}
