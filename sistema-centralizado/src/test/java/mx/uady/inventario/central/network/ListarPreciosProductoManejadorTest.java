package mx.uady.inventario.central.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import mx.uady.inventario.central.model.ProductoConUnidades;
import mx.uady.inventario.central.service.ProductoService;
import mx.uady.inventario.central.service.memoria.AlmacenMemoria;
import mx.uady.inventario.central.service.memoria.ProductoServiceMemoria;
import mx.uady.inventario.common.modelo.HistorialPrecio;
import mx.uady.inventario.common.modelo.Producto;

/**
 * Pruebas de {@link ListarPreciosProductoManejador}: operación {@code listarPreciosProducto} (RF-23).
 *
 * <p>Usa los productos de {@code 02_seed.sql} en memoria. El producto 2 (Proyector Epson PowerLite)
 * cuesta 12500.50 al iniciar.</p>
 */
@Tag("RF-23")
class ListarPreciosProductoManejadorTest {

    private ProductoService productos;
    private ListarPreciosProductoManejador manejador;

    @BeforeEach
    void crearManejador() {
        AlmacenMemoria almacen = AlmacenMemoria.conDatosDeEjemplo(Duration.ZERO);
        ProductoServiceMemoria base = new ProductoServiceMemoria(almacen);
        // La versión en memoria no implementa el historial; se toma directo del almacén.
        productos = new ProductoService() {
            @Override
            public List<ProductoConUnidades> listarConUnidades() {
                return base.listarConUnidades();
            }

            @Override
            public Producto guardar(Producto producto) {
                return base.guardar(producto);
            }

            @Override
            public List<HistorialPrecio> listarHistorialPrecios(int idProducto) {
                return almacen.historial(idProducto);
            }
        };
        manejador = new ListarPreciosProductoManejador(productos);
    }

    private static JsonObject producto(Number idProducto) {
        JsonObject parametros = new JsonObject();
        parametros.addProperty("id_producto", idProducto);
        return parametros;
    }

    @Test
    @DisplayName("RF-23: tras un cambio de precio devuelve el precio actual y el anterior en el historial")
    void productoConCambioDePrecio_devuelvePrecioActualEHistorial() {
        productos.guardar(new Producto(2, "Proyector Epson PowerLite", "Proyector portatil",
                new BigDecimal("13000.00")));

        JsonObject resultado = manejador.ejecutar(producto(2)).getAsJsonObject();

        assertEquals(2, resultado.get("idProducto").getAsInt());
        assertEquals("Proyector Epson PowerLite", resultado.get("nombre").getAsString());
        assertEquals(0, new BigDecimal("13000.00").compareTo(resultado.get("precioActual").getAsBigDecimal()));
        JsonArray historial = resultado.getAsJsonArray("historial");
        assertEquals(1, historial.size());
        assertEquals(0, new BigDecimal("12500.50")
                .compareTo(historial.get(0).getAsJsonObject().get("precioHistorico").getAsBigDecimal()));
    }

    @Test
    @DisplayName("RF-23: el id 1 (mínimo válido) sin cambios de precio devuelve el historial vacío")
    void idMinimoSinCambios_devuelveHistorialVacio() {
        JsonObject resultado = manejador.ejecutar(producto(1)).getAsJsonObject();

        assertEquals(0, new BigDecimal("18999.00").compareTo(resultado.get("precioActual").getAsBigDecimal()));
        assertTrue(resultado.getAsJsonArray("historial").isEmpty());
    }

    @Test
    @DisplayName("RF-23: id_producto 0 se rechaza por no ser positivo")
    void idCero_lanzaExcepcion() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> manejador.ejecutar(producto(0)));
        assertEquals("El parámetro id_producto debe ser positivo", error.getMessage());
    }

    @Test
    @DisplayName("RF-23: un producto que no existe se rechaza con su id en el mensaje")
    void productoInexistente_lanzaExcepcion() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> manejador.ejecutar(producto(999)));
        assertEquals("El producto 999 no existe", error.getMessage());
    }

    @Test
    @DisplayName("RF-23: un id_producto que no es número se rechaza")
    void idNoNumerico_lanzaExcepcion() {
        JsonObject parametros = new JsonObject();
        parametros.addProperty("id_producto", "abc");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> manejador.ejecutar(parametros));
        assertEquals("El parámetro id_producto debe ser un entero", error.getMessage());
    }

    @Test
    @DisplayName("RF-23: sin id_producto se rechaza la solicitud")
    void sinIdProducto_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> manejador.ejecutar(new JsonObject()));
    }
}
