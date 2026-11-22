package mx.uady.inventario.cliente.util;

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

    public String getMensaje() {
        return mensaje;
    }
    @Override
    public String toString(){
        return getMensaje();
    }
}
