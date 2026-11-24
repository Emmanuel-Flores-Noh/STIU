package mx.uady.inventario.central.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import mx.uady.inventario.common.modelo.TiendaAlmacen;

/** Pruebas de {@link ListarTiendasManejador}: operación {@code listarTiendas} (RF-19). */
@Tag("RF-19")
class ListarTiendasManejadorTest {

    @Test
    @DisplayName("RF-19: devuelve cada tienda con identificador, nombre y dirección")
    void catalogoConTiendas_devuelveCadaTiendaConSusCampos() {
        ConsultasVentaFalsa consultas = ConsultasVentaFalsa.con(List.of(
                new TiendaAlmacen(1, "Almacen Central UADY", "Calle 60 s/n, Centro, Merida"),
                new TiendaAlmacen(3, "Tienda Facultad de Matematicas", "Campus de Ciencias Exactas")), List.of());

        JsonArray resultado = new ListarTiendasManejador(consultas).ejecutar(new JsonObject()).getAsJsonArray();

        assertEquals(2, resultado.size());
        JsonObject primera = resultado.get(0).getAsJsonObject();
        assertEquals(1, primera.get("idTienda").getAsInt());
        assertEquals("Almacen Central UADY", primera.get("nombre").getAsString());
        assertEquals("Calle 60 s/n, Centro, Merida", primera.get("direccion").getAsString());
    }

    @Test
    @DisplayName("RF-19: conserva el orden por nombre en que el servicio entrega las tiendas")
    void variasTiendas_conservaElOrdenDelServicio() {
        ConsultasVentaFalsa consultas = ConsultasVentaFalsa.con(List.of(
                new TiendaAlmacen(2, "A", "x"), new TiendaAlmacen(1, "B", "y")), List.of());

        JsonArray resultado = new ListarTiendasManejador(consultas).ejecutar(new JsonObject()).getAsJsonArray();

        assertEquals(2, resultado.get(0).getAsJsonObject().get("idTienda").getAsInt());
        assertEquals(1, resultado.get(1).getAsJsonObject().get("idTienda").getAsInt());
    }

    @Test
    @Tag("RF-13")
    @DisplayName("RF-19, RF-13: sin tiendas en el catálogo inicial devuelve un arreglo vacío")
    void catalogoSinTiendas_devuelveArregloVacio() {
        ConsultasVentaFalsa consultas = ConsultasVentaFalsa.con(List.of(), List.of());

        JsonArray resultado = new ListarTiendasManejador(consultas).ejecutar(new JsonObject()).getAsJsonArray();

        assertTrue(resultado.isEmpty());
    }

    @Test
    @Tag("RF-24")
    @DisplayName("RF-24: si la consulta falla, la excepción llega al despachador (ERROR_INTERNO)")
    void servicioFalla_propagaLaExcepcion() {
        IllegalStateException fallo = new IllegalStateException("No se pudo consultar las tiendas");
        ListarTiendasManejador manejador = new ListarTiendasManejador(ConsultasVentaFalsa.queFalla(fallo));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> manejador.ejecutar(new JsonObject()));
        assertSame(fallo, error);
    }
}
