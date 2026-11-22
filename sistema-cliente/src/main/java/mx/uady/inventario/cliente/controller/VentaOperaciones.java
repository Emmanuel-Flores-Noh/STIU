package mx.uady.inventario.cliente.controller;

import mx.uady.inventario.cliente.model.MercanciaDTO;
import mx.uady.inventario.cliente.model.TiendaDTO;
import mx.uady.inventario.cliente.model.UsuarioDTO;

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

    /** Consulta tiendas, usuarios proveedores e inventario disponible y los muestra. */
    void cargarListas();

    /**
     * Registra la venta de una unidad a una tienda. Si falta algún dato no envía nada y lo informa.
     *
     * @param unidad  unidad seleccionada; puede ser nula
     * @param tienda  tienda destino; puede ser nula
     * @param usuario usuario proveedor; puede ser nulo
     */
    void registrarVenta(MercanciaDTO unidad, TiendaDTO tienda, UsuarioDTO usuario);

    /**
     * Obtiene y muestra el código QR de una unidad.
     *
     * @param numeroSerie número de serie de la unidad
     */
    void consultarQr(String numeroSerie);

    /**
     * Obtiene y muestra el historial de precios de un producto.
     *
     * @param idProducto identificador del producto
     */
    void consultarHistorialPrecios(Integer idProducto);
}
