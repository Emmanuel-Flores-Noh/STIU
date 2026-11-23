package mx.uady.inventario.cliente.controller;

import mx.uady.inventario.cliente.model.MercanciaDTO;
import mx.uady.inventario.cliente.model.TiendaDTO;
import mx.uady.inventario.cliente.model.UsuarioDTO;

/**
 * Define las acciones que los componentes de la vista pueden solicitar al controlador.
 *
 * <p>Trazabilidad con Requisitos:
 * <ul>
 *   <li><b>RF-25:</b> Carga inicial de listas y mercancía al abrir la pantalla.</li>
 *   <li><b>RF-30:</b> Recarga manual de catálogos e inventario a través de {@link #cargarListas()}.</li>
 *   <li><b>RF-31:</b> Validación de datos completos para registrar venta.</li>
 *   <li><b>RF-32:</b> Registro de venta de la unidad seleccionada mediante {@link #registrarVenta(MercanciaDTO, TiendaDTO, UsuarioDTO)}.</li>
 *   <li><b>RF-34:</b> Consulta y visualización del código QR mediante {@link #consultarQr(String)}.</li>
 *   <li><b>RF-36:</b> Consulta del historial de precios de un producto mediante {@link #consultarHistorialPrecios(Integer)}.</li>
 * </ul>
 * </p>
 */
public interface VentaOperaciones {

    /** Acciones que no hacen nada; se usan mientras la vista aún no tiene controlador. */
    VentaOperaciones OPERACIONES_BASE = new VentaOperaciones() {
        @Override
        public void cargarListas() {
        }

        @Override
        public void registrarVenta(MercanciaDTO unidad, TiendaDTO tienda, UsuarioDTO usuario) {
        }

        @Override
        public void consultarQr(String numeroSerie) {
        }

        @Override
        public void consultarHistorialPrecios(Integer idProducto) {
        }
    };

    /** Consulta tiendas, usuarios proveedores e inventario disponible y los muestra.
     * <p>Requisitos: RF-25, RF-30.</p>
     * */
    void cargarListas();

    /**
     * Registra la venta de una unidad a una tienda. Si falta algún dato no envía nada y lo informa.
     *
     * <p>Requisitos: RF-31, RF-32.</p>
     * @param unidad  unidad seleccionada; puede ser nula
     * @param tienda  tienda destino; puede ser nula
     * @param usuario usuario proveedor; puede ser nulo
     */
    void registrarVenta(MercanciaDTO unidad, TiendaDTO tienda, UsuarioDTO usuario);

    /**
     * Obtiene y muestra el código QR de una unidad.
     *
     * <p>Requisito: RF-34.</p>
     * @param numeroSerie número de serie de la unidad
     */
    void consultarQr(String numeroSerie);

    /**
     * Obtiene y muestra el historial de precios de un producto.
     *
     * <p>Requisito: RF-36.</p>
     * @param idProducto identificador del producto
     */
    void consultarHistorialPrecios(Integer idProducto);
}
