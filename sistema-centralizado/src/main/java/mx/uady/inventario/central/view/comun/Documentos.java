package mx.uady.inventario.central.view.comun;

import java.util.Objects;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Utilidades para reaccionar a los cambios de texto de los campos de las pantallas. */
public final class Documentos {

    private Documentos() {
    }

    /**
     * Crea un listener que ejecuta la acción cada vez que el texto del documento cambia
     * (se inserta, se borra o cambian sus atributos).
     *
     * @param accion acción a ejecutar en el hilo de Swing tras cada cambio
     * @return listener listo para registrar con {@code campo.getDocument().addDocumentListener(...)}
     * @throws NullPointerException si {@code accion} es {@code null}
     */
    public static DocumentListener alCambiar(Runnable accion) {
        Objects.requireNonNull(accion, "accion");
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                accion.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                accion.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                accion.run();
            }
        };
    }
}
