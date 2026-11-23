package mx.uady.inventario.cliente.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link FormateadorTexto}.
 * Valida la normalización de texto sin acentos (RF-27), detección de estado «En almacén» (RF-26),
 * formato de fechas dd/MM/yyyy HH:mm (RF-32) y formateo de precios en moneda (RF-36).
 */
class FormateadorTextoTest {

    @Test
    @Tag("RF-27")
    @DisplayName("RF-27 · Caso Normal: Normaliza texto removiendo mayúsculas y acentos diacríticos")
    void normalizar_CasoNormal_QuitaAcentosYMayusculas() {
        assertEquals("laptop epson", FormateadorTexto.normalizar("Láptop Épson"));
        assertEquals("cancion", FormateadorTexto.normalizar("Canción"));
        assertEquals("arbol", FormateadorTexto.normalizar("ÁRBOL"));
    }

    @Test
    @Tag("RF-27")
    @DisplayName("RF-27 · Caso Límite: Normalizar texto nulo o vacío retorna cadena vacía")
    void normalizar_CasoLimite_TextoNuloOVacio() {
        assertEquals("", FormateadorTexto.normalizar(null));
        assertEquals("", FormateadorTexto.normalizar(""));
        assertEquals("   ", FormateadorTexto.normalizar("   "));
    }

    @Test
    @Tag("RF-26")
    @DisplayName("RF-26 · Caso Normal: Identifica correctamente variantes del estado 'En almacén'")
    void esEnAlmacen_CasoNormal_DetectaEstadoEnAlmacen() {
        assertTrue(FormateadorTexto.esEnAlmacen("EN_ALMACEN"));
        assertTrue(FormateadorTexto.esEnAlmacen("En almacén"));
        assertTrue(FormateadorTexto.esEnAlmacen("en almacen"));
        assertTrue(FormateadorTexto.esEnAlmacen("  EN_ALMACÉN  "));
    }

    @Test
    @Tag("RF-26")
    @DisplayName("RF-26 · Caso Error: Rechaza estados distintos a 'En almacén'")
    void esEnAlmacen_CasoError_RechazaOtrosEstados() {
        assertFalse(FormateadorTexto.esEnAlmacen("VENDIDO"));
        assertFalse(FormateadorTexto.esEnAlmacen("BAJA"));
        assertFalse(FormateadorTexto.esEnAlmacen(null));
        assertFalse(FormateadorTexto.esEnAlmacen(""));
    }

    @Test
    @Tag("RF-32")
    @DisplayName("RF-32 · Caso Normal: Formatea fecha ISO a dd/MM/yyyy HH:mm")
    void fecha_CasoNormal_FormateaCorrectamente() {
        String resultado = FormateadorTexto.fecha("2026-11-21T10:30:00", ZoneId.of("UTC"));
        assertEquals("21/11/2026 10:30", resultado);
    }

    @Test
    @Tag("RF-32")
    @DisplayName("RF-32 · Caso Límite: Fecha nula o vacía devuelve 'N/D'")
    void fecha_CasoLimite_TextoVacioDevuelveND() {
        assertEquals("N/D", FormateadorTexto.fecha(null));
        assertEquals("N/D", FormateadorTexto.fecha(""));
        assertEquals("N/D", FormateadorTexto.fecha("   "));
    }

    @Test
    @Tag("RF-36")
    @DisplayName("RF-36 · Caso Normal: Formatea precios como moneda con dos decimales")
    void precio_CasoNormal_FormatoMoneda() {
        String precioFormateado = FormateadorTexto.precio(new BigDecimal("1500.50"));
        assertNotNull(precioFormateado);
        assertTrue(precioFormateado.contains("1,500.50") || precioFormateado.contains("1500.50"));
        assertTrue(precioFormateado.contains("$"));
    }

    @Test
    @Tag("RF-36")
    @DisplayName("RF-36 · Caso Límite: Precio nulo devuelve 'N/D'")
    void precio_CasoLimite_NuloDevuelveND() {
        assertEquals("N/D", FormateadorTexto.precio(null));
    }
}
