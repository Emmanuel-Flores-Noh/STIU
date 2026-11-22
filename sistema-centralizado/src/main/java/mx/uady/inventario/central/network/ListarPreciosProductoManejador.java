package mx.uady.inventario.central.network;

import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mx.uady.inventario.central.model.ProductoConUnidades;
import mx.uady.inventario.central.service.ProductoService;
import mx.uady.inventario.common.json.GsonFactory;
import mx.uady.inventario.common.modelo.Producto;

/**
 * Manejador de la operación {@code gestorPrecios.listarPreciosProducto} (RF-23).
 *
 * <p>Devuelve el precio actual del producto y su historial de precios.</p>
 */
final class ListarPreciosProductoManejador implements ManejadorOperacion {

    private final ProductoService productos;

    /**
     * Crea el manejador.
     *
     * @param productos servicio de productos
     */
    ListarPreciosProductoManejador(ProductoService productos) {
        this.productos = Objects.requireNonNull(productos, "productos");
    }

    /**
     * Lista los precios de un producto.
     *
     * @param parametros objeto con {@code id_producto}
     * @return {@code {idProducto, nombre, precioActual, historial: [...]}}
     * @throws IllegalArgumentException si el id no es un entero positivo o el producto no existe
     */
    @Override
    public JsonElement ejecutar(JsonObject parametros) {
        int idProducto = ParametrosSolicitud.enteroPositivo(parametros, "id_producto");
        Producto producto = productos.listarConUnidades().stream()
                .map(ProductoConUnidades::producto)
                .filter(p -> p.idProducto() == idProducto)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El producto " + idProducto + " no existe"));
        JsonObject resultado = new JsonObject();
        resultado.addProperty("idProducto", producto.idProducto());
        resultado.addProperty("nombre", producto.nombre());
        resultado.addProperty("precioActual", producto.precioActual());
        resultado.add("historial", GsonFactory.DEFAULT.toJsonTree(productos.listarHistorialPrecios(idProducto)));
        return resultado;
    }
}
