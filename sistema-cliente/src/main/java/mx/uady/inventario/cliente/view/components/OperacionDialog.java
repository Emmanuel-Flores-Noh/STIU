package mx.uady.inventario.cliente.view.components;

import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * Cuadros de diálogo con el resultado de una operación: éxito, error y la pregunta de reintento.
 */
public class OperacionDialog {

    /** Pregunta que se agrega al mensaje cuando se ofrece reintentar. */
    public static final String PREGUNTA_REINTENTO = "¿Desea reintentar la operación?";

    private static final String[] OPCIONES_REINTENTO = {"Sí", "No"};

    private final Component padre;

    /**
     * Crea el conjunto de diálogos.
     *
     * @param padre componente sobre el que se centran los diálogos; puede ser nulo
     */
    public OperacionDialog(Component padre) {
        this.padre = padre;
    }

    /**
     * Arma el texto de la ventana de reintento: el mensaje del fallo seguido de la pregunta.
     *
     * <p>Requisito: RF-38.</p>
     * @param mensaje explicación del fallo
     * @return texto completo de la ventana
     */
    public static String textoReintento(String mensaje) {
        return mensaje + "\n" + PREGUNTA_REINTENTO;
    }

    /**
     * Muestra un mensaje de éxito.
     *
     * <p>Requisito: RF-32.</p>
     * @param mensaje texto a mostrar
     */
    public void exito(String mensaje) {
        informar("Éxito en el Registro", mensaje);
    }

    /**
     * Muestra un mensaje informativo con título propio.
     *
     * @param titulo  título de la ventana
     * @param mensaje texto a mostrar
     */
    public void informar(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Muestra un mensaje de error.
     *
     * <p>Requisitos: RF-33, RF-37.</p>
     * @param mensaje texto a mostrar
     */
    public void error(String mensaje) {
        error("Error de Operación", mensaje);
    }

    /**
     * Muestra un mensaje de error con título propio.
     *
     * <p>Requisitos: RF-33, RF-37.</p>
     * @param titulo  título de la ventana
     * @param mensaje texto a mostrar
     */
    public void error(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Muestra el tipo de error y pregunta «¿Desea reintentar la operación?».
     *
     * <p>Requisito: RF-38.</p>
     * @param tipoError tipo de error (título de la ventana)
     * @param mensaje   explicación del fallo
     * @return {@code true} solo si el encargado elige «Sí»
     */
    public boolean preguntarReintento(String tipoError, String mensaje) {
        int opcion = JOptionPane.showOptionDialog(padre, textoReintento(mensaje), tipoError,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, OPCIONES_REINTENTO, OPCIONES_REINTENTO[1]);
        return opcion == 0;
    }
}
