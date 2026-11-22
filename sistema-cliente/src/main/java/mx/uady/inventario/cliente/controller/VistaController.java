package mx.uady.inventario.cliente.controller;

import mx.uady.inventario.cliente.model.*;

import java.util.List;

/**
 * Contrato entre la pantalla de ventas y sus controladores. La interfaz principal son las acciones
 * que la vista puede pedir; {@link VistaController} es lo que los controladores necesitan de la pantalla, que
 * recibe objetos de datos y mensajes ya traducidos y nunca conoce el socket ni el JSON (RNF-23).
 */
public interface VistaController {


    /**
     * Muestra el estado de la operación en la barra de estado y bloquea o desbloquea los controles
     * que no deben usarse mientras hay una operación de red en curso.
     *
     * @param mensaje  texto de la barra de estado (p. ej. «Cargando...» o «Listo»)
     * @param cargando {@code true} si hay una operación en curso
     */
    void mostrarIndicadorCarga(String mensaje, boolean cargando);

    /**
     * Reemplaza las opciones de tienda y de usuario proveedor.
     *
     * @param tiendas  tiendas destino
     * @param usuarios usuarios con rol de proveedor
     */
    void actualizarListasCombos(List<TiendaDTO> tiendas, List<UsuarioDTO> usuarios);

    /**
     * Reemplaza las unidades de la tabla de inventario.
     *
     * @param unidades unidades disponibles
     */
    void actualizarTablaMercancia(List<MercanciaDTO> unidades);

    /** Deja el formulario sin unidad, tienda ni usuario elegidos y sin texto de búsqueda. */
    void limpiarFormulario();

    /**
     * Informa que una operación terminó con éxito.
     *
     * @param mensaje texto a mostrar
     */
    void mostrarMensajeExito(String mensaje);

    /**
     * Informa un error que no se resuelve reintentando.
     *
     * @param mensaje texto a mostrar
     */
    void mostrarMensajeError(String mensaje);

    /**
     * Informa un error de red o del servidor y pregunta si se desea reintentar.
     *
     * @param tipoError tipo de error (título de la ventana)
     * @param mensaje   explicación del fallo
     * @param reintento acción que se ejecuta solo si el encargado elige reintentar
     */
    void mostrarDialogoReintentar(String tipoError, String mensaje, Runnable reintento);

    /**
     * Muestra el código QR de una unidad.
     *
     * @param qr datos y imagen del QR
     */
    void mostrarQr(QrDTO qr);

    /**
     * Muestra el historial de precios de un producto.
     *
     * @param historial nombre, precio actual y precios anteriores
     */
    void mostrarHistorialPrecios(PrecioHistorialDTO historial);
}
