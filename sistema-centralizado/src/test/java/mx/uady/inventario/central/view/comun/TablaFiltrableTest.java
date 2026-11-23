package mx.uady.inventario.central.view.comun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Configuración, selección y filtros comunes de las tablas (RF-09, RF-14, RF-15). */
class TablaFiltrableTest {

    private record Fila(String serie, String tienda) {
    }

    /** Modelo mínimo de dos columnas: serie y tienda. */
    private static final class Modelo extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private final List<Fila> filas = new ArrayList<>();

        void setFilas(List<Fila> nuevas) {
            filas.clear();
            filas.addAll(nuevas);
            fireTableDataChanged();
        }

        Fila getFila(int indice) {
            return filas.get(indice);
        }

        @Override
        public int getRowCount() {
            return filas.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            return columna == 0 ? filas.get(fila).serie() : filas.get(fila).tienda();
        }
    }

    private Modelo modelo;
    private TablaFiltrable<Modelo, Fila> tabla;

    @BeforeEach
    void preparar() {
        modelo = new Modelo();
        modelo.setFilas(List.of(new Fila("SN-003", "Mérida"), new Fila("SN-001", "Centro"),
                new Fila("SN-002", "Norte")));
        tabla = new TablaFiltrable<Modelo, Fila>(modelo, modelo::getFila).ordenarPor(0);
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-15")
    @DisplayName("RF-09, RF-15 · La tabla permite una sola fila elegida, alto uniforme y orden por columna")
    void laTablaTieneLaConfiguracionComun() {
        JTable componente = tabla.tabla();

        assertEquals(ListSelectionModel.SINGLE_SELECTION, componente.getSelectionModel().getSelectionMode());
        assertEquals(TablaFiltrable.ALTO_FILA, componente.getRowHeight());
        assertTrue(componente.getFillsViewportHeight());
        assertEquals("SN-001", componente.getValueAt(0, 0));
        assertEquals("SN-003", componente.getValueAt(2, 0));
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-15")
    @DisplayName("RF-09, RF-15 · Seleccionar una fila del modelo la elige aunque la tabla esté ordenada")
    void seleccionarUnaFilaDelModeloLaEligeAunqueEsteOrdenada() {
        assertTrue(tabla.seleccionarFilaDelModelo(0));

        assertEquals(2, tabla.tabla().getSelectedRow());
        assertEquals(new Fila("SN-003", "Mérida"), tabla.seleccionada().orElseThrow());
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-15")
    @DisplayName("RF-09, RF-15 · Sin fila elegida no hay selección")
    void sinFilaElegidaNoHaySeleccion() {
        assertTrue(tabla.seleccionada().isEmpty());
    }

    @Test
    @Tag("RF-09")
    @DisplayName("RF-09 · Filtra por texto sin distinguir mayúsculas ni acentos")
    void filtraPorTextoSinDistinguirMayusculasNiAcentos() {
        tabla.filtrarPorTexto(1, "merida");

        assertEquals(3, tabla.totalFilas());
        assertEquals(1, tabla.filasVisibles());
        assertEquals("SN-003", tabla.tabla().getValueAt(0, 0));
    }

    @Test
    @Tag("RF-09")
    @DisplayName("RF-09 · Un texto vacío o nulo quita el filtro")
    void unTextoVacioONuloQuitaElFiltro() {
        tabla.filtrarPorTexto(0, "001");
        assertEquals(1, tabla.filasVisibles());

        tabla.filtrarPorTexto(0, "   ");
        assertEquals(3, tabla.filasVisibles());
        tabla.filtrarPorTexto(0, "001");
        tabla.filtrarPorTexto(0, null);
        assertEquals(3, tabla.filasVisibles());
    }

    @Test
    @Tag("RF-14")
    @DisplayName("RF-14 · Filtra por un criterio sobre la fila completa")
    void filtraPorUnCriterioSobreLaFila() {
        tabla.filtrar(f -> f.tienda().startsWith("N") || f.serie().endsWith("1"));

        assertEquals(2, tabla.filasVisibles());
        assertEquals("SN-001", tabla.tabla().getValueAt(0, 0));
        assertEquals("SN-002", tabla.tabla().getValueAt(1, 0));
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-15")
    @DisplayName("RF-09, RF-15 · No selecciona filas ocultas por el filtro ni índices fuera de rango")
    void noSeleccionaFilasOcultasNiIndicesFueraDeRango() {
        tabla.filtrarPorTexto(0, "001");

        assertFalse(tabla.seleccionarFilaDelModelo(0), "oculta por el filtro");
        assertFalse(tabla.seleccionarFilaDelModelo(-1));
        assertFalse(tabla.seleccionarFilaDelModelo(3));
        assertTrue(tabla.seleccionada().isEmpty());
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-14")
    @DisplayName("RF-09, RF-14 · Avisa los cambios de selección y el doble clic sobre una fila")
    void avisaLosCambiosDeSeleccionYElDobleClic() {
        AtomicInteger cambios = new AtomicInteger();
        AtomicInteger dobles = new AtomicInteger();
        tabla.alSeleccionar(cambios::incrementAndGet);
        tabla.alDobleClic(dobles::incrementAndGet);

        tabla.seleccionarFilaDelModelo(1);
        assertTrue(cambios.get() > 0);

        clic(1, 1);
        assertEquals(0, dobles.get(), "un solo clic");
        clic(1, 2);
        assertEquals(1, dobles.get(), "doble clic sobre una fila");
        clic(10, 2);
        assertEquals(1, dobles.get(), "doble clic debajo de la última fila");
    }

    @Test
    @Tag("RF-09")
    @DisplayName("RF-09 · Rechaza modelo, función de fila, acciones y criterio nulos")
    void rechazaArgumentosNulos() {
        assertThrows(NullPointerException.class, () -> new TablaFiltrable<Modelo, Fila>(null, i -> null));
        assertThrows(NullPointerException.class, () -> new TablaFiltrable<Modelo, Fila>(modelo, null));
        assertThrows(NullPointerException.class, () -> tabla.alSeleccionar(null));
        assertThrows(NullPointerException.class, () -> tabla.alDobleClic(null));
        assertThrows(NullPointerException.class, () -> tabla.filtrar(null));
    }

    private void clic(int fila, int clics) {
        JTable componente = tabla.tabla();
        int y = fila * componente.getRowHeight() + componente.getRowHeight() / 2;
        componente.dispatchEvent(new MouseEvent(componente, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0,
                5, y, clics, false, MouseEvent.BUTTON1));
    }
}
