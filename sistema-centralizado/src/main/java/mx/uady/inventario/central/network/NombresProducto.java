package mx.uady.inventario.central.network;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import mx.uady.inventario.central.model.ProductoConUnidades;
import mx.uady.inventario.central.service.ProductoService;
import mx.uady.inventario.common.modelo.Producto;

/**
 * Resuelve el nombre de los productos para las respuestas que solo conocen su identificador
 * (inventario disponible y código QR).
 */
final class NombresProducto {

    /** Nombre que se envía cuando el producto de una unidad no existe. */
    static final String DESCONOCIDO = "Producto desconocido";

    private NombresProducto() {
    }

    /**
     * Construye el mapa id → nombre de todos los productos.
     *
     * @param productos servicio de productos
     * @return mapa con el nombre de cada producto por su id
     */
    static Map<Integer, String> porId(ProductoService productos) {
        List<ProductoConUnidades> lista = productos.listarConUnidades();
        return lista.stream()
                .map(ProductoConUnidades::producto)
                .collect(Collectors.toMap(Producto::idProducto, Producto::nombre, (a, b) -> a));
    }
}
