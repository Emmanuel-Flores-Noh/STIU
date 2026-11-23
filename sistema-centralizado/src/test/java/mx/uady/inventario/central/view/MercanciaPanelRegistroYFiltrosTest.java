package mx.uady.inventario.central.view;

import static mx.uady.inventario.central.view.MercanciaPanelTest.buscar;
import static mx.uady.inventario.central.view.MercanciaPanelTest.enEdt;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Component;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import mx.uady.inventario.common.modelo.EstadoMercancia;
import mx.uady.inventario.common.modelo.Producto;

/** Registro y filtros de la pantalla de Mercancía vistos desde fuera del panel. */
class MercanciaPanelRegistroYFiltrosTest {

    private static final Producto LAPTOP = new Producto(1, "Laptop", null, BigDecimal.TEN);

    @Test
    @Tag("RF-06")
    @Tag("RNF-30")
    @DisplayName("RF-06, RNF-30 · El error de la serie resalta el campo, selecciona el texto y se borra al escribir")
    void elErrorDeLaSerieResaltaElCampoYSeBorraAlEscribir() throws Exception {
        enEdt(() -> {
            MercanciaPanel panel = new MercanciaPanel();
            panel.mostrarProductos(List.of(LAPTOP));
            JTextField serie = buscar(panel, "serie", JTextField.class);
            JLabel error = buscar(panel, "errorSerie", JLabel.class);
            serie.setText("SN-0001");

            panel.mostrarErrorSerie("Ya existe una unidad con ese número de serie.");
            assertEquals("Ya existe una unidad con ese número de serie.", error.getText());
            assertEquals("error", serie.getClientProperty("JComponent.outline"));
            assertEquals("SN-0001", serie.getSelectedText());

            serie.setText("SN-0002");
            assertEquals(" ", error.getText());
            assertNull(serie.getClientProperty("JComponent.outline"));

            panel.mostrarErrorSerie("Otro error");
            panel.mostrarErrorSerie(null);
            assertEquals(" ", error.getText());
            assertNull(serie.getClientProperty("JComponent.outline"));
        });
    }

    @Test
    @Tag("RF-06")
    @DisplayName("RF-06 · Registrar avisa con el botón, limpiar vacía la serie y sin productos Enter no registra")
    void registrarAvisaYLimpiarVaciaLaSerie() throws Exception {
        AtomicInteger registros = new AtomicInteger();
        enEdt(() -> {
            MercanciaPanel panel = new MercanciaPanel();
            panel.alRegistrar(registros::incrementAndGet);
            JTextField serie = buscar(panel, "serie", JTextField.class);

            panel.mostrarProductos(List.of());
            assertFalse(serie.isEnabled());
            assertFalse(buscar(panel, "productoRegistro", JComboBox.class).isEnabled());
            serie.postActionEvent();
            assertEquals(0, registros.get(), "sin productos Enter no registra");

            panel.mostrarProductos(List.of(LAPTOP));
            buscar(panel, "registrar", JButton.class).doClick();
            assertEquals(1, registros.get());

            serie.setText("SN-0001");
            assertEquals("SN-0001", panel.serieCapturada());
            panel.limpiarSerie();
            assertEquals("", panel.serieCapturada());
        });
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-11")
    @DisplayName("RF-09, RF-11 · Filtros: estados en orden, avisan una vez por cambio, la búsqueda no recarga")
    void losFiltrosAvisanUnaVezYLaBusquedaNoRecarga() throws Exception {
        AtomicInteger cambios = new AtomicInteger();
        AtomicInteger verQr = new AtomicInteger();
        enEdt(() -> {
            MercanciaPanel panel = new MercanciaPanel();
            panel.mostrarProductos(List.of(LAPTOP));
            panel.alCambiarFiltros(cambios::incrementAndGet);
            panel.alVerQr(verQr::incrementAndGet);
            JComboBox<?> estados = buscar(panel, "filtroEstado", JComboBox.class);

            assertEquals(3, estados.getItemCount());
            assertEquals(MercanciaPanel.TODOS, estados.getItemAt(0));
            assertEquals(EstadoMercancia.EN_ALMACEN, estados.getItemAt(1));
            assertEquals(EstadoMercancia.VENDIDO, estados.getItemAt(2));

            estados.setSelectedIndex(2);
            assertEquals(1, cambios.get());
            buscar(panel, "filtroProducto", JComboBox.class).setSelectedIndex(1);
            assertEquals(2, cambios.get());
            buscar(panel, "actualizar", JButton.class).doClick();
            assertEquals(3, cambios.get());

            buscar(panel, "busquedaSerie", JTextField.class).setText("SN");
            assertEquals(3, cambios.get(), "buscar filtra en pantalla sin recargar");

            JButton boton = buscar(panel, "verQr", JButton.class);
            boton.setEnabled(true);
            boton.doClick();
            assertEquals(1, verQr.get());
        });
    }

    @Test
    @Tag("RNF-12")
    @DisplayName("RNF-12 · Los controles que se bloquean durante una tarea son los de registro y filtros, en orden")
    void losControlesQueSeBloqueanSonLosDeRegistroYFiltros() throws Exception {
        enEdt(() -> {
            MercanciaPanel panel = new MercanciaPanel();
            String[] nombres = Arrays.stream(panel.controles()).map(Component::getName).toArray(String[]::new);

            assertArrayEquals(new String[] {"productoRegistro", "serie", "registrar", "filtroProducto",
                    "filtroEstado", "actualizar", "verQr"}, nombres);
            assertEquals(panel.controles()[1], buscar(panel, "serie", JTextField.class));
        });
    }
}
