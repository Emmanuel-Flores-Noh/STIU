package mx.uady.inventario.central.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import mx.uady.inventario.central.service.ProductoValidator.Campo;
import mx.uady.inventario.common.modelo.Producto;

class ProductoValidatorTest {

    @Test
    @Tag("RF-01")
    @Tag("RF-02")
    @DisplayName("RF-01, RF-02 · Acepta un producto válido")
    void aceptaUnProductoValido() {
        assertTrue(ProductoValidator.errores("Silla", new BigDecimal("10.50")).isEmpty());
        assertDoesNotThrow(() -> ProductoValidator.exigirValido(new Producto(0, "Silla", null, new BigDecimal("1"))));
    }

    @Test
    @Tag("RF-01")
    @Tag("RF-02")
    @DisplayName("RF-01, RF-02 · Exige nombre")
    void exigeNombre() {
        assertTrue(ProductoValidator.errores(null, BigDecimal.ONE).containsKey(Campo.NOMBRE));
        assertTrue(ProductoValidator.errores("   ", BigDecimal.ONE).containsKey(Campo.NOMBRE));
    }

    @Test
    @Tag("RF-01")
    @Tag("RF-02")
    @DisplayName("RF-01, RF-02 · Limita el nombre al tamaño de la columna")
    void limitaElNombreAlTamanoDeLaColumna() {
        String largo = "a".repeat(ProductoValidator.LONGITUD_MAXIMA_NOMBRE + 1);
        assertTrue(ProductoValidator.errores(largo, BigDecimal.ONE).containsKey(Campo.NOMBRE));
        String justo = "a".repeat(ProductoValidator.LONGITUD_MAXIMA_NOMBRE);
        assertTrue(ProductoValidator.errores(justo, BigDecimal.ONE).isEmpty());
    }

    @Test
    @Tag("RF-01")
    @Tag("RF-02")
    @DisplayName("RF-01, RF-02 · Exige precio mayor que cero")
    void exigePrecioMayorQueCero() {
        assertEquals("El precio debe ser mayor que 0.",
                ProductoValidator.errores("Silla", BigDecimal.ZERO).get(Campo.PRECIO));
        assertTrue(ProductoValidator.errores("Silla", new BigDecimal("-1")).containsKey(Campo.PRECIO));
        assertTrue(ProductoValidator.errores("Silla", null).containsKey(Campo.PRECIO));
    }

    @Test
    @Tag("RF-01")
    @Tag("RF-02")
    @DisplayName("RF-01, RF-02 · Respeta la precisión decimal de la columna")
    void respetaLaPrecisionDecimalDeLaColumna() {
        assertTrue(ProductoValidator.errores("Silla", new BigDecimal("1.999")).containsKey(Campo.PRECIO));
        assertTrue(ProductoValidator.errores("Silla", new BigDecimal("1.500")).isEmpty());
        assertTrue(ProductoValidator.errores("Silla", new BigDecimal("100000000")).containsKey(Campo.PRECIO));
    }

    @Test
    @Tag("RF-01")
    @Tag("RF-02")
    @Tag("RNF-30")
    @DisplayName("RF-01, RF-02, RNF-30 · Reporta todos los campos con error")
    void reportaTodosLosCamposConError() {
        Map<Campo, String> errores = ProductoValidator.errores("", BigDecimal.ZERO);
        assertEquals(2, errores.size());
        assertThrows(ValidacionException.class,
                () -> ProductoValidator.exigirValido(new Producto(0, "", null, BigDecimal.ZERO)));
    }

    @Test
    @Tag("RF-01")
    @Tag("RF-02")
    @DisplayName("RF-01, RF-02 · Acepta el precio mínimo de un centavo")
    void aceptaElPrecioMinimoDeUnCentavo() {
        assertTrue(ProductoValidator.errores("Silla", new BigDecimal("0.01")).isEmpty());
    }

    @Test
    @Tag("RF-01")
    @Tag("RF-02")
    @DisplayName("RF-01, RF-02 · Acepta el precio máximo exacto y rechaza el siguiente centavo")
    void aceptaElPrecioMaximoExactoYRechazaElSiguienteCentavo() {
        assertTrue(ProductoValidator.errores("Silla", new BigDecimal("99999999.99")).isEmpty());
        assertEquals("El precio no puede ser mayor que 99999999.99.",
                ProductoValidator.errores("Silla", new BigDecimal("100000000.00")).get(Campo.PRECIO));
    }

    @Test
    @Tag("RF-01")
    @Tag("RF-02")
    @DisplayName("RF-01, RF-02 · Los espacios al inicio y al final no cuentan para el largo del nombre")
    void losEspaciosAlInicioYAlFinalNoCuentanParaElLargoDelNombre() {
        String justo = "a".repeat(ProductoValidator.LONGITUD_MAXIMA_NOMBRE);

        assertTrue(ProductoValidator.errores("   " + justo + "   ", BigDecimal.ONE).isEmpty());
        Producto guardado = ProductoValidator.normalizar(
                new Producto(0, "   " + justo + "   ", null, BigDecimal.ONE));
        assertEquals(justo, guardado.nombre());
    }

    @Test
    @Tag("RF-01")
    @DisplayName("RF-01 · Una descripción vacía se guarda como nula y una con texto se recorta")
    void unaDescripcionVaciaSeGuardaComoNulaYUnaConTextoSeRecorta() {
        assertNull(ProductoValidator.normalizar(new Producto(0, "Silla", "   ", BigDecimal.ONE)).descripcion());
        assertEquals("Ergonómica",
                ProductoValidator.normalizar(new Producto(0, "Silla", "  Ergonómica ", BigDecimal.ONE)).descripcion());
    }

    @Test
    @Tag("RF-01")
    @Tag("RNF-30")
    @DisplayName("RF-01, RNF-30 · Normalizar rechaza un producto nulo con un mensaje claro")
    void normalizarRechazaUnProductoNuloConUnMensajeClaro() {
        ValidacionException error = assertThrows(ValidacionException.class,
                () -> ProductoValidator.normalizar(null));
        assertEquals("No se indicó el producto.", error.getMessage());
    }
}
