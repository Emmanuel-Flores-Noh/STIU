package mx.uady.inventario.central.view;

import static mx.uady.inventario.central.view.MercanciaPanelTest.buscar;
import static mx.uady.inventario.central.view.MercanciaPanelTest.enEdt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import mx.uady.inventario.common.modelo.Producto;

/** Formulario de registro de unidades (RF-06) y su mensaje de error junto al campo (RNF-30). */
class RegistroMercanciaFormTest {

    private static final Producto LAPTOP = new Producto(1, "Laptop", null, BigDecimal.TEN);
    private static final Producto SILLA = new Producto(2, "Silla", null, BigDecimal.ONE);

    @Test
    @Tag("RF-06")
    @DisplayName("RF-06 · Enter en la serie y el botón Registrar avisan; se leen el producto y la serie")
    void enterYElBotonRegistran() throws Exception {
        AtomicInteger registros = new AtomicInteger();
        enEdt(() -> {
            RegistroMercanciaForm form = new RegistroMercanciaForm();
            form.alRegistrar(registros::incrementAndGet);
            form.mostrarProductos(List.of(LAPTOP, SILLA));
            buscar(form, "productoRegistro", JComboBox.class).setSelectedItem(SILLA);
            JTextField serie = buscar(form, "serie", JTextField.class);
            serie.setText("SN-0001");

            serie.postActionEvent();
            buscar(form, "registrar", JButton.class).doClick();

            assertEquals(2, registros.get());
            assertEquals(SILLA, form.productoElegido().orElseThrow());
            assertEquals("SN-0001", form.serieCapturada());
            form.limpiarSerie();
            assertEquals("", form.serieCapturada());
        });
    }

    @Test
    @Tag("RF-06")
    @DisplayName("RF-06 · Antes de cargar no hay aviso; sin productos se deshabilita y lo indica")
    void sinProductosSeDeshabilitaYLoIndica() throws Exception {
        AtomicInteger registros = new AtomicInteger();
        enEdt(() -> {
            RegistroMercanciaForm form = new RegistroMercanciaForm();
            form.alRegistrar(registros::incrementAndGet);
            JLabel error = buscar(form, "errorSerie", JLabel.class);
            assertEquals(" ", error.getText(), "sin cargar todavía no hay aviso");
            assertFalse(buscar(form, "registrar", JButton.class).isEnabled());

            form.mostrarProductos(List.of());
            assertEquals(RegistroMercanciaForm.SIN_PRODUCTOS, error.getText());
            assertTrue(form.productoElegido().isEmpty());
            for (var control : form.controles()) {
                assertFalse(control.isEnabled(), control.getName());
            }
            buscar(form, "serie", JTextField.class).postActionEvent();
            assertEquals(0, registros.get());

            form.mostrarProductos(List.of(LAPTOP));
            assertEquals(" ", error.getText());
            for (var control : form.controles()) {
                assertTrue(control.isEnabled(), control.getName());
            }
        });
    }

    @Test
    @Tag("RF-06")
    @Tag("RNF-30")
    @DisplayName("RF-06, RNF-30 · Escribir en la serie borra el error; otro error no se borra al tener productos")
    void escribirBorraElError() throws Exception {
        enEdt(() -> {
            RegistroMercanciaForm form = new RegistroMercanciaForm();
            form.mostrarProductos(List.of(LAPTOP));
            JLabel error = buscar(form, "errorSerie", JLabel.class);

            form.mostrarError("El número de serie es obligatorio.");
            form.mostrarProductos(List.of(LAPTOP, SILLA));
            assertEquals("El número de serie es obligatorio.", error.getText(), "solo se quita el aviso de productos");

            buscar(form, "serie", JTextField.class).setText("S");
            assertEquals(" ", error.getText());
        });
    }

    @Test
    @Tag("RF-06")
    @Tag("RNF-30")
    @DisplayName("RF-06, RNF-30 · Un error resalta el campo y selecciona la serie; null lo limpia")
    void unErrorResaltaElCampoYNullLoLimpia() throws Exception {
        enEdt(() -> {
            RegistroMercanciaForm form = new RegistroMercanciaForm();
            form.mostrarProductos(List.of(LAPTOP));
            JTextField serie = buscar(form, "serie", JTextField.class);
            serie.setText("SN-UADY-0001");

            form.mostrarError("Ya existe una unidad con ese número de serie.");
            assertEquals("error", serie.getClientProperty("JComponent.outline"));
            assertEquals("SN-UADY-0001", serie.getSelectedText());

            form.mostrarError(null);
            assertNull(serie.getClientProperty("JComponent.outline"));
            assertEquals(" ", buscar(form, "errorSerie", JLabel.class).getText());
        });
    }

    @Test
    @Tag("RF-06")
    @DisplayName("RF-06 · Rechaza una acción de registro nula")
    void rechazaUnaAccionNula() throws Exception {
        enEdt(() -> assertThrows(NullPointerException.class, () -> new RegistroMercanciaForm().alRegistrar(null)));
    }
}
