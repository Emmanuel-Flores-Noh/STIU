package mx.uady.inventario.central.view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class FiltroTextoTest {

    @Test
    @Tag("RF-04")
    @Tag("RF-09")
    @Tag("RF-14")
    @DisplayName("RF-04, RF-09, RF-14 · Ignora mayúsculas, acentos y espacios")
    void ignoraMayusculasAcentosYEspacios() {
        assertTrue(FiltroTexto.contiene("Silla ergonómica", "ERGONOMICA"));
        assertTrue(FiltroTexto.contiene("Silla ergonomica", "  ergonómica "));
    }

    @Test
    @Tag("RF-04")
    @Tag("RF-09")
    @Tag("RF-14")
    @DisplayName("RF-04, RF-09, RF-14 · Una búsqueda vacía coincide con todo")
    void unaBusquedaVaciaCoincideConTodo() {
        assertTrue(FiltroTexto.contiene("Laptop", ""));
        assertTrue(FiltroTexto.contiene("Laptop", null));
    }

    @Test
    @Tag("RF-04")
    @Tag("RF-09")
    @Tag("RF-14")
    @DisplayName("RF-04, RF-09, RF-14 · No coincide si no contiene el texto")
    void noCoincideSiNoContieneElTexto() {
        assertFalse(FiltroTexto.contiene("Laptop", "proyector"));
        assertFalse(FiltroTexto.contiene(null, "x"));
    }
}
