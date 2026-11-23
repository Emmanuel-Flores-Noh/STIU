package mx.uady.inventario.central.view.comun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Regla común del texto que resume las filas visibles de una tabla filtrable. */
class ResumenTablaTest {

    private static final ResumenTabla UNIDADES = new ResumenTabla("No hay unidades.",
            "Ninguna unidad coincide con «%s».", "%d de %d unidades", "1 unidad", "%d unidades");

    @Test
    @Tag("RF-09")
    @Tag("RF-14")
    @DisplayName("RF-09, RF-14 · Con todas las filas visibles indica el total")
    void conTodasLasFilasVisiblesIndicaElTotal() {
        assertEquals("10 unidades", UNIDADES.texto(10, 10, ""));
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-14")
    @DisplayName("RF-09, RF-14 · Con parte de las filas visibles indica cuántas de cuántas")
    void conParteDeLasFilasVisiblesIndicaCuantasDeCuantas() {
        assertEquals("3 de 10 unidades", UNIDADES.texto(10, 3, "sn"));
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-14")
    @DisplayName("RF-09, RF-14 · Una sola fila usa el texto en singular")
    void unaSolaFilaUsaElTextoEnSingular() {
        assertEquals("1 unidad", UNIDADES.texto(1, 1, null));
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-14")
    @DisplayName("RF-09, RF-14 · Una tabla vacía usa el texto de vacío aunque haya búsqueda")
    void unaTablaVaciaUsaElTextoDeVacio() {
        assertEquals("No hay unidades.", UNIDADES.texto(0, 0, "sn"));
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-14")
    @DisplayName("RF-09, RF-14 · Sin coincidencias muestra la búsqueda sin espacios sobrantes")
    void sinCoincidenciasMuestraLaBusquedaSinEspacios() {
        assertEquals("Ninguna unidad coincide con «zzz».", UNIDADES.texto(5, 0, "  zzz "));
        assertEquals("Ninguna unidad coincide con «».", UNIDADES.texto(5, 0, null));
    }

    @Test
    @Tag("RF-14")
    @DisplayName("RF-14 · El texto sin coincidencias puede no incluir la búsqueda")
    void elTextoSinCoincidenciasPuedeNoIncluirLaBusqueda() {
        ResumenTabla ventas = new ResumenTabla("Sin ventas.", "Ninguna venta coincide con los filtros.",
                "%d de %d ventas", "1 venta", "%d ventas");

        assertEquals("Ninguna venta coincide con los filtros.", ventas.texto(2, 0, "SN"));
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-14")
    @DisplayName("RF-09, RF-14 · Los totales grandes se escriben sin separador de miles")
    void losTotalesGrandesSeEscribenSinSeparadorDeMiles() {
        assertEquals("999 de 1000 unidades", UNIDADES.texto(1000, 999, ""));
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-14")
    @DisplayName("RF-09, RF-14 · Rechaza conteos negativos o más filas visibles que el total")
    void rechazaConteosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> UNIDADES.texto(-1, 0, ""));
        assertThrows(IllegalArgumentException.class, () -> UNIDADES.texto(3, -1, ""));
        assertThrows(IllegalArgumentException.class, () -> UNIDADES.texto(3, 4, ""));
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-14")
    @DisplayName("RF-09, RF-14 · Rechaza textos nulos al construirse")
    void rechazaTextosNulosAlConstruirse() {
        assertThrows(NullPointerException.class, () -> new ResumenTabla(null, "", "", "", ""));
        assertThrows(NullPointerException.class, () -> new ResumenTabla("", "", "", "", null));
    }
}
