package mx.uady.inventario.central.network;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import mx.uady.inventario.central.service.VentaService;
import mx.uady.inventario.central.service.memoria.AlmacenMemoria;
import mx.uady.inventario.central.service.memoria.VentasMemoria;

/**
 * Pruebas de {@link ActualizarTiendaVentaManejador}: operación {@code actualizarTiendaVenta} (RF-17).
 *
 * <p>El manejador delega en {@link VentaService}. Las reglas de existencia y de unidad ya vendida
 * las aplica el registrador en memoria ({@link VentasMemoria}), que replica a {@code VentaRepository};
 * estas pruebas comprueban que el manejador <b>propaga</b> esos rechazos. La regla en la base de datos
 * real la cubre {@code VentaRepositoryIntegracionTest} (requiere MariaDB).</p>
 */
@Tag("RF-17")
class ActualizarTiendaVentaManejadorTest {

    private VentasMemoria ventas;
    private ActualizarTiendaVentaManejador manejador;

    @BeforeEach
    void crearManejador() {
        ventas = new VentasMemoria(AlmacenMemoria.conDatosDeEjemplo(Duration.ZERO));
        manejador = new ActualizarTiendaVentaManejador(new VentaService(ventas));
    }

    private static JsonObject venta(String numeroSerie, int idTienda, int idUsuario) {
        JsonObject parametros = new JsonObject();
        parametros.addProperty("numero_serie", numeroSerie);
        parametros.addProperty("id_tienda", idTienda);
        parametros.addProperty("id_usuario", idUsuario);
        return parametros;
    }

    @Test
    @DisplayName("RF-17: registra la venta y devuelve numero_serie, id_tienda y fecha_venta")
    void unidadEnAlmacen_registraLaVenta() throws Exception {
        JsonObject resultado = manejador.ejecutar(venta("SN-UADY-0001", 2, 4)).getAsJsonObject();

        assertEquals("SN-UADY-0001", resultado.get("numero_serie").getAsString());
        assertEquals(2, resultado.get("id_tienda").getAsInt());
        assertDoesNotThrow(() -> LocalDateTime.parse(resultado.get("fecha_venta").getAsString()));
        assertEquals(1, ventas.listarHistorial().size());
    }

    @Test
    @DisplayName("RF-17: los identificadores mínimos válidos (1) se aceptan")
    void identificadoresMinimos_registraLaVenta() throws Exception {
        JsonObject resultado = manejador.ejecutar(venta("SN-UADY-0003", 1, 1)).getAsJsonObject();

        assertEquals(1, resultado.get("id_tienda").getAsInt());
    }

    @Test
    @Tag("RF-18")
    @DisplayName("RF-18: propaga el rechazo de una unidad ya vendida y no se registra otra venta")
    void unidadVendida_lanzaExcepcion() throws Exception {
        manejador.ejecutar(venta("SN-UADY-0001", 1, 2));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> manejador.ejecutar(venta("SN-UADY-0001", 2, 4)));
        assertEquals("La mercancía ya fue vendida", error.getMessage());
        assertEquals(1, ventas.listarHistorial().size());
    }

    @Test
    @DisplayName("RF-17: id_tienda 0 se rechaza y no se registra la venta")
    void tiendaCero_lanzaExcepcion() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> manejador.ejecutar(venta("SN-UADY-0001", 0, 2)));
        assertEquals("El parámetro id_tienda debe ser positivo", error.getMessage());
        assertEquals(0, ventas.listarHistorial().size());
    }

    @Test
    @DisplayName("RF-17: propaga el rechazo de una tienda que no existe")
    void tiendaInexistente_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> manejador.ejecutar(venta("SN-UADY-0001", 99, 2)));
        assertEquals(0, ventas.listarHistorial().size());
    }

    @Test
    @DisplayName("RF-17: sin numero_serie se rechaza la solicitud")
    void sinNumeroSerie_lanzaExcepcion() {
        JsonObject parametros = venta("x", 1, 2);
        parametros.remove("numero_serie");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> manejador.ejecutar(parametros));
        assertEquals("Falta el parámetro numero_serie", error.getMessage());
    }
}
