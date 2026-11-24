package mx.uady.inventario.central.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mx.uady.inventario.central.service.MercanciaService;
import mx.uady.inventario.central.service.QrService;
import mx.uady.inventario.central.service.memoria.AlmacenMemoria;
import mx.uady.inventario.central.service.memoria.MercanciaServiceMemoria;
import mx.uady.inventario.central.service.memoria.ProductoServiceMemoria;
import mx.uady.inventario.central.service.memoria.VentasMemoria;
import mx.uady.inventario.common.modelo.EstadoMercancia;
import mx.uady.inventario.common.modelo.Mercancia;

/**
 * Pruebas de {@link ListarInventarioManejador}: operación {@code listarInventario} (RF-21).
 *
 * <p>Usa los datos de ejemplo de {@code 02_seed.sql} en memoria: 10 unidades, 6 de ellas
 * {@code EN_ALMACEN}.</p>
 */
@Tag("RF-21")
class ListarInventarioManejadorTest {

    @TempDir
    Path directorioQr;

    private ListarInventarioManejador manejadorConDatosDeEjemplo() {
        AlmacenMemoria almacen = AlmacenMemoria.conDatosDeEjemplo(Duration.ZERO);
        return new ListarInventarioManejador(
                new MercanciaServiceMemoria(almacen, new QrService(directorioQr)),
                new ProductoServiceMemoria(almacen));
    }

    /** Servicio cuya consulta de unidades devuelve la lista indicada o lanza la excepción indicada. */
    private static MercanciaService mercancia(List<Mercancia> unidades, RuntimeException fallo) {
        return new MercanciaService() {
            @Override
            public List<Mercancia> listar(Integer idProducto, EstadoMercancia estado) {
                if (fallo != null) {
                    throw fallo;
                }
                return unidades;
            }

            @Override
            public Mercancia registrar(int idProducto, String numeroSerie) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Mercancia asegurarQr(String numeroSerie) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Test
    @DisplayName("RF-21: devuelve solo las unidades en almacén con numeroSerie, idProducto, nombreProducto y estado")
    void inventarioDeEjemplo_devuelveSoloUnidadesEnAlmacen() {
        JsonArray resultado = manejadorConDatosDeEjemplo().ejecutar(new JsonObject()).getAsJsonArray();

        assertEquals(6, resultado.size());
        for (JsonElement elemento : resultado) {
            assertEquals("EN_ALMACEN", elemento.getAsJsonObject().get("estado").getAsString());
        }
        JsonObject primera = resultado.get(0).getAsJsonObject();
        assertEquals("SN-UADY-0001", primera.get("numeroSerie").getAsString());
        assertEquals(1, primera.get("idProducto").getAsInt());
        assertEquals("Laptop Lenovo ThinkPad E14", primera.get("nombreProducto").getAsString());
    }

    @Test
    @DisplayName("RF-21: una unidad vendida no aparece en el inventario disponible")
    void unidadVendida_noApareceEnInventario() {
        JsonArray resultado = manejadorConDatosDeEjemplo().ejecutar(new JsonObject()).getAsJsonArray();

        for (JsonElement elemento : resultado) {
            assertFalse("SN-UADY-0002".equals(elemento.getAsJsonObject().get("numeroSerie").getAsString()),
                    "SN-UADY-0002 está VENDIDO en los datos de ejemplo");
        }
    }

    @Test
    @DisplayName("RF-21: si todas las unidades están vendidas devuelve un arreglo vacío")
    void todasLasUnidadesVendidas_devuelveArregloVacio() {
        AlmacenMemoria almacen = AlmacenMemoria.conDatosDeEjemplo(Duration.ZERO);
        MercanciaServiceMemoria mercancia = new MercanciaServiceMemoria(almacen, new QrService(directorioQr));
        VentasMemoria ventas = new VentasMemoria(almacen);
        for (Mercancia unidad : mercancia.listar(null, EstadoMercancia.EN_ALMACEN)) {
            ventas.registrar(unidad.numeroSerie(), 1, 2);
        }
        ListarInventarioManejador manejador = new ListarInventarioManejador(mercancia,
                new ProductoServiceMemoria(almacen));

        JsonArray resultado = manejador.ejecutar(new JsonObject()).getAsJsonArray();

        assertTrue(resultado.isEmpty());
    }

    @Test
    @Tag("RNF-27")
    @DisplayName("RF-21, RNF-27: la respuesta no incluye la ruta interna del archivo QR")
    void unidadConQr_noEnviaLaRutaDelArchivo() {
        Mercancia conQr = new Mercancia("SN-QR-1", 1, EstadoMercancia.EN_ALMACEN, "/app/storage/qr/SN-QR-1.png");
        ListarInventarioManejador manejador = new ListarInventarioManejador(mercancia(List.of(conQr), null),
                new ProductoServiceMemoria(AlmacenMemoria.conDatosDeEjemplo(Duration.ZERO)));

        JsonObject fila = manejador.ejecutar(new JsonObject()).getAsJsonArray().get(0).getAsJsonObject();

        assertEquals(4, fila.size(), "solo numeroSerie, idProducto, nombreProducto y estado");
        assertFalse(fila.has("rutaQr"));
        assertFalse(fila.toString().contains("/app/storage"));
    }

    @Test
    @Tag("RF-24")
    @DisplayName("RF-24: si la consulta de unidades falla, la excepción llega al despachador (ERROR_INTERNO)")
    void servicioFalla_propagaLaExcepcion() {
        IllegalStateException fallo = new IllegalStateException("No se pudo consultar la mercancía");
        ListarInventarioManejador manejador = new ListarInventarioManejador(mercancia(List.of(), fallo),
                new ProductoServiceMemoria(new AlmacenMemoria(Duration.ZERO)));

        assertSame(fallo, assertThrows(IllegalStateException.class, () -> manejador.ejecutar(new JsonObject())));
    }
}
