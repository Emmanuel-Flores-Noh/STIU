package mx.uady.inventario.central.network;

import java.util.Map;
import java.util.Objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mx.uady.inventario.central.service.MercanciaService;
import mx.uady.inventario.central.service.ProductoService;
import mx.uady.inventario.common.modelo.EstadoMercancia;
import mx.uady.inventario.common.modelo.Mercancia;

/**
 * Manejador de la operación {@code inventario.listarInventario} (RF-21).
 *
 * <p>Devuelve las unidades {@code EN_ALMACEN} con el nombre de su producto. No incluye la ruta
 * del QR, que es interna al contenedor del Sistema 1 (RNF-27).</p>
 */
final class ListarInventarioManejador implements ManejadorOperacion {

    private final MercanciaService mercancia;
    private final ProductoService productos;

    /**
     * Crea el manejador.
     *
     * @param mercancia servicio de unidades físicas
     * @param productos servicio de productos, para resolver los nombres
     */
    ListarInventarioManejador(MercanciaService mercancia, ProductoService productos) {
        this.mercancia = Objects.requireNonNull(mercancia, "mercancia");
        this.productos = Objects.requireNonNull(productos, "productos");
    }

    /**
     * Lista el inventario disponible. No usa parámetros.
     *
     * @param parametros parámetros de la solicitud (se ignoran)
     * @return arreglo de {@code {numeroSerie, idProducto, nombreProducto, estado}}
     */
    @Override
    public JsonElement ejecutar(JsonObject parametros) {
        Map<Integer, String> nombres = NombresProducto.porId(productos);
        JsonArray resultado = new JsonArray();
        for (Mercancia unidad : mercancia.listar(null, EstadoMercancia.EN_ALMACEN)) {
            JsonObject fila = new JsonObject();
            fila.addProperty("numeroSerie", unidad.numeroSerie());
            fila.addProperty("idProducto", unidad.idProducto());
            fila.addProperty("nombreProducto", nombres.getOrDefault(unidad.idProducto(), NombresProducto.DESCONOCIDO));
            fila.addProperty("estado", unidad.estado().name());
            resultado.add(fila);
        }
        return resultado;
    }
}
