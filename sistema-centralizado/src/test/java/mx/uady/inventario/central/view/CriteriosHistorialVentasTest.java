package mx.uady.inventario.central.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import mx.uady.inventario.central.model.VentaHistorial;

/** Regla de filtrado del historial de ventas, sin Swing (RF-14). */
class CriteriosHistorialVentasTest {

    private static final VentaHistorial VENTA = new VentaHistorial(1, "SN-Álamo-01", "Laptop", "Centro", "Ana",
            LocalDateTime.of(2026, 3, 10, 12, 0), 1);

    @Test
    @Tag("RF-14")
    @DisplayName("RF-14 · Cada filtro por separado y combinados")
    void cadaFiltroPorSeparadoYCombinados() {
        assertTrue(criterios("alamo", null, null, "", "").cumple(VENTA));
        assertFalse(criterios("SN-999", null, null, "", "").cumple(VENTA));
        assertTrue(criterios("", "Centro", null, "", "").cumple(VENTA));
        assertFalse(criterios("", "Norte", null, "", "").cumple(VENTA));
        assertTrue(criterios("", null, "Laptop", "", "").cumple(VENTA));
        assertFalse(criterios("", null, "Monitor", "", "").cumple(VENTA));

        assertTrue(criterios("sn-", "Centro", "Laptop", "2026-03-01", "2026-03-31").cumple(VENTA));
        assertFalse(criterios("sn-", "Centro", "Monitor", "2026-03-01", "2026-03-31").cumple(VENTA),
                "basta con que un criterio falle");
    }

    @Test
    @Tag("RF-14")
    @DisplayName("RF-14 · Sin criterios todas las ventas pasan")
    void sinCriteriosTodasPasan() {
        CriteriosHistorialVentas vacios = criterios(null, null, null, null, null);

        assertTrue(vacios.cumple(VENTA));
        assertEquals("", vacios.serie());
        assertNull(vacios.desde());
        assertNull(vacios.hasta());
        assertFalse(vacios.fechaInvalida());
        assertFalse(criterios("   ", null, null, "  ", "\t").fechaInvalida(), "solo espacios = vacío");
    }

    @Test
    @Tag("RF-14")
    @DisplayName("RF-14 · El rango incluye el primer y el último día, a cualquier hora")
    void elRangoIncluyeLosExtremos() {
        VentaHistorial medianoche = conFecha(LocalDateTime.of(2026, 3, 10, 0, 0));
        VentaHistorial ultimoMinuto = conFecha(LocalDateTime.of(2026, 3, 10, 23, 59, 59));
        CriteriosHistorialVentas mismoDia = criterios("", null, null, "2026-03-10", "2026-03-10");

        assertTrue(mismoDia.cumple(medianoche));
        assertTrue(mismoDia.cumple(ultimoMinuto));
        assertFalse(criterios("", null, null, "2026-03-11", "").cumple(ultimoMinuto), "día anterior a Desde");
        assertFalse(criterios("", null, null, "", "2026-03-09").cumple(medianoche), "día posterior a Hasta");
        assertFalse(criterios("", null, null, "2026-03-11", "2026-03-09").cumple(VENTA), "Desde posterior a Hasta");
    }

    @Test
    @Tag("RF-14")
    @DisplayName("RF-14 · Las fechas se leen como aaaa-mm-dd y se recortan espacios")
    void lasFechasSeRecortan() {
        CriteriosHistorialVentas c = criterios("  SN ", null, null, " 2026-03-01 ", "2026-03-31  ");

        assertEquals("SN", c.serie());
        assertEquals(LocalDate.of(2026, 3, 1), c.desde());
        assertEquals(LocalDate.of(2026, 3, 31), c.hasta());
        assertFalse(c.fechaInvalida());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-13-01", "2026-02-30", "10/03/2026", "abc", "2026-3-1"})
    @Tag("RF-14")
    @DisplayName("RF-14 · Una fecha mal escrita se ignora y se marca como inválida")
    void unaFechaMalEscritaSeIgnoraYSeMarca(String fecha) {
        CriteriosHistorialVentas enDesde = criterios("", null, null, fecha, "");
        CriteriosHistorialVentas enHasta = criterios("", null, null, "", fecha);

        assertTrue(enDesde.fechaInvalida());
        assertTrue(enHasta.fechaInvalida());
        assertNull(enDesde.desde());
        assertNull(enHasta.hasta());
        assertTrue(enDesde.cumple(VENTA), "se filtra como si no hubiera fecha");
    }

    @Test
    @Tag("RF-14")
    @DisplayName("RF-14 · Una venta sin tienda o sin serie solo pasa si ese filtro está vacío")
    void ventaConDatosNulos() {
        VentaHistorial incompleta = new VentaHistorial(2, null, "Laptop", null, "Ana",
                LocalDateTime.of(2026, 3, 10, 12, 0), 1);

        assertTrue(criterios("", null, null, "", "").cumple(incompleta));
        assertFalse(criterios("", "Centro", null, "", "").cumple(incompleta));
        assertFalse(criterios("SN", null, null, "", "").cumple(incompleta));
    }

    private static CriteriosHistorialVentas criterios(String serie, String tienda, String producto,
            String desde, String hasta) {
        return CriteriosHistorialVentas.de(serie, tienda, producto, desde, hasta);
    }

    private static VentaHistorial conFecha(LocalDateTime fecha) {
        return new VentaHistorial(9, "SN-009", "Laptop", "Centro", "Ana", fecha, 1);
    }
}
