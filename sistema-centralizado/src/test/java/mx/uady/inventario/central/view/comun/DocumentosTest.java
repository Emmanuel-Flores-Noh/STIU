package mx.uady.inventario.central.view.comun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JTextField;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** El listener que usan las búsquedas para filtrar mientras se escribe (RF-09, RF-14). */
class DocumentosTest {

    @Test
    @Tag("RF-09")
    @Tag("RF-14")
    @DisplayName("RF-09, RF-14 · Escribir y borrar texto ejecuta la acción en cada cambio")
    void escribirYBorrarTextoEjecutaLaAccion() {
        AtomicInteger llamadas = new AtomicInteger();
        JTextField campo = new JTextField();
        campo.getDocument().addDocumentListener(Documentos.alCambiar(llamadas::incrementAndGet));

        campo.setText("SN-1");
        assertEquals(1, llamadas.get(), "insertar");
        campo.setText("");
        assertEquals(2, llamadas.get(), "borrar");
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-14")
    @DisplayName("RF-09, RF-14 · Un cambio de atributos del texto también ejecuta la acción")
    void unCambioDeAtributosTambienEjecutaLaAccion() throws Exception {
        AtomicInteger llamadas = new AtomicInteger();
        DefaultStyledDocument documento = new DefaultStyledDocument();
        documento.insertString(0, "texto", null);
        documento.addDocumentListener(Documentos.alCambiar(llamadas::incrementAndGet));

        SimpleAttributeSet negritas = new SimpleAttributeSet();
        StyleConstants.setBold(negritas, true);
        documento.setCharacterAttributes(0, 5, negritas, false);

        assertEquals(1, llamadas.get());
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-14")
    @DisplayName("RF-09, RF-14 · Vaciar un campo que ya está vacío no ejecuta la acción")
    void vaciarUnCampoQueYaEstaVacioNoEjecutaLaAccion() {
        AtomicInteger llamadas = new AtomicInteger();
        JTextField campo = new JTextField();
        campo.getDocument().addDocumentListener(Documentos.alCambiar(llamadas::incrementAndGet));

        campo.setText("");

        assertEquals(0, llamadas.get());
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-14")
    @DisplayName("RF-09, RF-14 · Una acción nula se rechaza al crear el listener")
    void unaAccionNulaSeRechazaAlCrearElListener() {
        assertThrows(NullPointerException.class, () -> Documentos.alCambiar(null));
    }
}
