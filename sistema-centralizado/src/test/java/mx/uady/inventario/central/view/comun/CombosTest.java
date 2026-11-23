package mx.uady.inventario.central.view.comun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.function.Function;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Llenado de combos conservando la selección y texto mostrado por el renderer (RF-09, RF-13, RF-14). */
class CombosTest {

    /** Elemento de prueba: dos instancias con el mismo id representan el mismo elemento. */
    private record Elemento(int id, String nombre) {
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-13")
    @DisplayName("RF-09, RF-13 · Al rellenar se conserva el elegido aunque sea otra instancia con el mismo id")
    void alRellenarSeConservaElElegidoPorSuClave() {
        JComboBox<Elemento> combo = new JComboBox<>();
        Combos.rellenarConservando(combo, List.of(new Elemento(1, "A"), new Elemento(2, "B")), Elemento::id);
        combo.setSelectedIndex(1);

        Combos.rellenarConservando(combo,
                List.of(new Elemento(3, "C"), new Elemento(1, "A"), new Elemento(2, "B renombrado")), Elemento::id);

        assertEquals(3, combo.getItemCount());
        assertEquals(new Elemento(2, "B renombrado"), combo.getSelectedItem());
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-13")
    @DisplayName("RF-09, RF-13 · Si el elegido ya no existe queda seleccionado el primero")
    void siElElegidoYaNoExisteQuedaElPrimero() {
        JComboBox<Elemento> combo = new JComboBox<>();
        Combos.rellenarConservando(combo, List.of(new Elemento(1, "A"), new Elemento(2, "B")), Elemento::id);
        combo.setSelectedIndex(1);

        Combos.rellenarConservando(combo, List.of(new Elemento(5, "E"), new Elemento(6, "F")), Elemento::id);

        assertEquals(0, combo.getSelectedIndex());
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-13")
    @DisplayName("RF-09, RF-13 · Una lista vacía deja el combo vacío y sin selección, aunque hubiera un elegido")
    void unaListaVaciaDejaElComboSinSeleccion() {
        JComboBox<Elemento> combo = new JComboBox<>();
        Combos.rellenarConservando(combo, List.of(new Elemento(1, "A")), Elemento::id);

        Combos.rellenarConservando(combo, List.of(), Elemento::id);

        assertEquals(0, combo.getItemCount());
        assertNull(combo.getSelectedItem());
    }

    @Test
    @Tag("RF-14")
    @DisplayName("RF-14 · Con textos como clave conserva la opción elegida o vuelve a la primera")
    void conTextosComoClaveConservaLaOpcionOVuelveALaPrimera() {
        JComboBox<String> combo = new JComboBox<>();
        Combos.rellenarConservando(combo, List.of("Todas", "Centro", "Norte"), Function.identity());
        combo.setSelectedItem("Norte");

        Combos.rellenarConservando(combo, List.of("Todas", "Norte", "Sur"), Function.identity());
        assertEquals("Norte", combo.getSelectedItem());

        Combos.rellenarConservando(combo, List.of("Todas", "Sur"), Function.identity());
        assertEquals("Todas", combo.getSelectedItem());
    }

    @Test
    @Tag("RF-09")
    @DisplayName("RF-09 · Seleccionar o primero elige la primera coincidencia y, sin ella, el primer elemento")
    void seleccionarOPrimeroEligeLaCoincidenciaOElPrimero() {
        JComboBox<String> combo = new JComboBox<>(new String[] {"a", "b", "b", "c"});

        Combos.seleccionarOPrimero(combo, "b"::equals);
        assertEquals(1, combo.getSelectedIndex());

        Combos.seleccionarOPrimero(combo, "z"::equals);
        assertEquals(0, combo.getSelectedIndex());

        JComboBox<String> vacio = new JComboBox<>();
        Combos.seleccionarOPrimero(vacio, "a"::equals);
        assertEquals(-1, vacio.getSelectedIndex());
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-13")
    @DisplayName("RF-09, RF-13 · El renderer muestra el texto calculado, también para el valor nulo")
    void elRendererMuestraElTextoCalculado() {
        var render = Combos.renderTexto(v -> v instanceof Elemento e ? e.nombre() : "—");
        JList<Object> lista = new JList<>();

        JLabel conValor = (JLabel) render.getListCellRendererComponent(lista, new Elemento(1, "Laptop"), 0, false, false);
        assertEquals("Laptop", conValor.getText());
        JLabel sinValor = (JLabel) render.getListCellRendererComponent(lista, null, -1, false, false);
        assertEquals("—", sinValor.getText());
    }

    @Test
    @Tag("RF-09")
    @DisplayName("RF-09 · Rechaza argumentos nulos")
    void rechazaArgumentosNulos() {
        JComboBox<String> combo = new JComboBox<>();
        assertThrows(NullPointerException.class,
                () -> Combos.rellenarConservando(null, List.of(), Function.identity()));
        assertThrows(NullPointerException.class,
                () -> Combos.rellenarConservando(combo, null, Function.identity()));
        assertThrows(NullPointerException.class, () -> Combos.rellenarConservando(combo, List.of(), null));
        assertThrows(NullPointerException.class, () -> Combos.seleccionarOPrimero(combo, null));
        assertThrows(NullPointerException.class, () -> Combos.renderTexto(null));
    }
}
