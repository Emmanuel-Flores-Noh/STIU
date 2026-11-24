package mx.uady.inventario.cliente.controller;

import mx.uady.inventario.cliente.model.*;

import java.util.List;

/**
 * Contrato entre la pantalla de ventas (vista) y sus controladores.
 * Define los métodos necesarios para que los controladores actualicen componentes gráficos,
 * indicadores de estado, tablas, selectores y cuadros de diálogo sin acoplar la vista al transporte TCP ni al formato JSON.
 */
public interface VistaController {


    /**
     * Muestra el estado de la operación en la barra de estado y bloquea o desbloquea los controles
     * que no deben usarse mientras hay una operación de red en curso.
     *
     * <p>Requisitos: RF-25, RF-30.</p>
     * @param mensaje  texto de la barra de estado (p. ej. «Cargando...» o «Listo»)
     * @param cargando {@code true} si hay una operación en curso
     */
    void mostrarIndicadorCarga(String mensaje, boolean cargando);

    /**
     * Reemplaza las opciones de tienda y de usuario proveedor.
     *
     * <p>Requisitos: RF-25, RF-28, RF-29.</p>
     * @param tiendas  tiendas destino
     * @param usuarios usuarios con rol de proveedor
     */
    void actualizarListasCombos(List<TiendaDTO> tiendas, List<UsuarioDTO> usuarios);

    /**
     * Reemplaza las unidades de la tabla de inventario.
     *
     * <p>Requisitos: RF-25, RF-26.</p>
     * @param unidades unidades disponibles
     */
    void actualizarTablaMercancia(List<MercanciaDTO> unidades);

    /** Deja el formulario sin unidad, tienda ni usuario elegidos y sin texto de búsqueda.
     * <p>Requisito: RF-32 (limpieza tras venta exitosa).</p>
     * */
    void limpiarFormulario();

    /**
     * Informa que una operación terminó con éxito.
     *
     * <p>Requisito: RF-32.</p>
     * @param mensaje texto a mostrar
     */
    void mostrarMensajeExito(String mensaje);

    /**
     * Informa un error que no se resuelve reintentando.
     *
     * <p>Requisitos: RF-33, RF-37.</p>
     * @param mensaje texto a mostrar
     */
    void mostrarMensajeError(String mensaje);

    /**
     * Informa un error de red o del servidor y pregunta si se desea reintentar.
     *
     * <p>Requisito: RF-38.</p>
     * @param tipoError tipo de error (título de la ventana)
     * @param mensaje   explicación del fallo
     * @param reintento acción que se ejecuta solo si el encargado elige reintentar
     */
    void mostrarDialogoReintentar(String tipoError, String mensaje, Runnable reintento);

    /**
     * Muestra el código QR de una unidad.
     *
     * <p>Requisito: RF-34.</p>
     * @param qr datos y imagen del QR
     */
    void mostrarQr(QrDTO qr);

    /**
     * Muestra el historial de precios de un producto.
     *
     * <p>Requisito: RF-36.</p>
     * @param historial nombre, precio actual y precios anteriores
     */
    void mostrarHistorialPrecios(PrecioHistorialDTO historial);
}
