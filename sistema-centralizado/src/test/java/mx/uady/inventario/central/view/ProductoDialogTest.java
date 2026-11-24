package mx.uady.inventario.central.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class ProductoDialogTest {

    @Test
    @Tag("RF-01")
    @Tag("RNF-30")
    @DisplayName("RF-01, RNF-30 · Interpreta precios escritos de formas habituales")
    void interpretaPreciosEscritosDeFormasHabituales() {
        assertEquals(new BigDecimal("1250.50"), ProductoDialog.parsearPrecio("1250.50"));
        assertEquals(new BigDecimal("1250.50"), ProductoDialog.parsearPrecio(" $1,250.50 "));
        assertEquals(new BigDecimal("1250000"), ProductoDialog.parsearPrecio("1,250,000"));
        assertEquals(new BigDecimal("-3"), ProductoDialog.parsearPrecio("-3"));
    }

    @Test
    @Tag("RF-01")
    @Tag("RNF-30")
    @DisplayName("RF-01, RNF-30 · Rechaza textos que no son precios")
    void rechazaTextosQueNoSonPrecios() {
        assertNull(ProductoDialog.parsearPrecio(""));
        assertNull(ProductoDialog.parsearPrecio("   "));
        assertNull(ProductoDialog.parsearPrecio(null));
        assertNull(ProductoDialog.parsearPrecio("abc"));
        assertNull(ProductoDialog.parsearPrecio("1.2.3"));
    }

    @Test
    @Tag("RF-01")
    @Tag("RNF-30")
    @DisplayName("RF-01, RNF-30 · No confunde la coma decimal con el separador de miles")
    void noConfundeLaComaDecimalConSeparadorDeMiles() {
        // "1250,50" es ambiguo: se rechaza en lugar de interpretarlo como 125050.
        assertNull(ProductoDialog.parsearPrecio("1250,50"));
        assertNull(ProductoDialog.parsearPrecio("12,50"));
    }
}
