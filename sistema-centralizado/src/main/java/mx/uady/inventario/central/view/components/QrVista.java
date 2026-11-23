package mx.uady.inventario.central.view.components;

import java.awt.Component;

import javax.swing.SwingUtilities;

import mx.uady.inventario.central.view.Mensajes;
import mx.uady.inventario.common.modelo.Mercancia;

/**
 * Abre la vista previa del código QR de una unidad (RF-11) desde cualquier pantalla. Si el PNG
 * no se puede leer, muestra el error en lugar del diálogo.
 */
public final class QrVista {

    private QrVista() {
    }

    /**
     * Muestra el diálogo modal con el QR de la unidad.
     *
     * @param origen         componente desde el que se pide; su ventana es la dueña del diálogo
     * @param unidad         unidad con la ruta de su PNG
     * @param nombreProducto nombre del producto de la unidad
     */
    public static void mostrar(Component origen, Mercancia unidad, String nombreProducto) {
        try {
            new QrPreviewDialog(SwingUtilities.getWindowAncestor(origen), unidad.numeroSerie(),
                    unidad.idProducto(), nombreProducto, unidad.rutaQr()).setVisible(true);
        } catch (IllegalArgumentException error) {
            Mensajes.mostrarError(origen, error);
        }
    }
}
