package mx.uady.inventario.central.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import mx.uady.inventario.central.view.comun.Combos;
import mx.uady.inventario.central.view.comun.Documentos;
import mx.uady.inventario.common.modelo.EstadoMercancia;
import mx.uady.inventario.common.modelo.Producto;

/**
 * Barra de consulta de la pantalla de Mercancía: filtros por producto y estado, búsqueda por
 * número de serie y los botones Actualizar y Ver QR.
 *
 * <p>Cambiar un filtro avisa para recargar desde el servicio; la búsqueda solo filtra en pantalla.
 * Rellenar los combos por código no avisa.</p>
 */
final class FiltrosMercanciaBar extends JPanel {

    private static final long serialVersionUID = 1L;
    static final String TODOS = "Todos";

    private final JComboBox<Object> producto = new JComboBox<>();
    private final JComboBox<Object> estado = new JComboBox<>();
    private final JTextField busqueda = new JTextField(16);
    private final JButton actualizar = new JButton("Actualizar");
    private final JButton verQr = new JButton("Ver QR");

    private Runnable alCambiarFiltros = () -> { };
    private Runnable alBuscar = () -> { };
    private Runnable alVerQr = () -> { };
    /** Evita recargar mientras se rellenan los combos por código. */
    private boolean ajustandoCombos;

    FiltrosMercanciaBar() {
        super(new BorderLayout());
        producto.setName("filtroProducto");
        estado.setName("filtroEstado");
        busqueda.setName("busquedaSerie");
        actualizar.setName("actualizar");
        verQr.setName("verQr");

        producto.setRenderer(Combos.renderTexto(FiltrosMercanciaBar::textoDe));
        estado.setRenderer(Combos.renderTexto(FiltrosMercanciaBar::textoDe));
        producto.addItem(TODOS);
        estado.addItem(TODOS);
        for (EstadoMercancia e : EstadoMercancia.values()) {
            estado.addItem(e);
        }
        producto.addActionListener(e -> notificarFiltros());
        estado.addActionListener(e -> notificarFiltros());

        busqueda.putClientProperty("JTextField.placeholderText", "Buscar número de serie");
        busqueda.putClientProperty("JTextField.showClearButton", true);
        busqueda.getDocument().addDocumentListener(Documentos.alCambiar(() -> alBuscar.run()));

        actualizar.addActionListener(e -> alCambiarFiltros.run());
        verQr.addActionListener(e -> alVerQr.run());
        verQr.setEnabled(false);

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        izquierda.add(new JLabel("Producto"));
        izquierda.add(producto);
        izquierda.add(new JLabel("Estado"));
        izquierda.add(estado);
        izquierda.add(busqueda);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        derecha.add(actualizar);
        derecha.add(verQr);

        add(izquierda, BorderLayout.WEST);
        add(derecha, BorderLayout.EAST);
    }

    private void notificarFiltros() {
        if (!ajustandoCombos) {
            alCambiarFiltros.run();
        }
    }

    /** Acción al cambiar el producto o el estado, o al pulsar Actualizar. */
    void alCambiarFiltros(Runnable accion) {
        this.alCambiarFiltros = Objects.requireNonNull(accion, "accion");
    }

    /** Acción al escribir en la búsqueda por número de serie. */
    void alBuscar(Runnable accion) {
        this.alBuscar = Objects.requireNonNull(accion, "accion");
    }

    /** Acción al pulsar Ver QR. */
    void alVerQr(Runnable accion) {
        this.alVerQr = Objects.requireNonNull(accion, "accion");
    }

    /** Rellena el filtro de producto ("Todos" primero) conservando el elegido si sigue existiendo, sin avisar. */
    void mostrarProductos(List<Producto> productos) {
        List<Object> opciones = new ArrayList<>();
        opciones.add(TODOS);
        opciones.addAll(productos);
        ajustandoCombos = true;
        try {
            Combos.rellenarConservando(producto, opciones, FiltrosMercanciaBar::claveProducto);
        } finally {
            ajustandoCombos = false;
        }
    }

    /** Producto elegido en el filtro, o {@code null} para todos. */
    Integer productoSeleccionado() {
        return producto.getSelectedItem() instanceof Producto p ? p.idProducto() : null;
    }

    /** Estado elegido en el filtro, o {@code null} para todos. */
    EstadoMercancia estadoSeleccionado() {
        return estado.getSelectedItem() instanceof EstadoMercancia e ? e : null;
    }

    /** @return {@code true} si se eligió un producto o un estado distinto de "Todos" */
    boolean hayFiltros() {
        return producto.getSelectedIndex() > 0 || estado.getSelectedIndex() > 0;
    }

    String textoBusqueda() {
        return busqueda.getText();
    }

    void setVerQrHabilitado(boolean habilitado) {
        verQr.setEnabled(habilitado);
    }

    boolean verQrHabilitado() {
        return verQr.isEnabled();
    }

    /** Controles que se bloquean mientras hay una operación en curso (la búsqueda queda libre). */
    Component[] controles() {
        return new Component[] {producto, estado, actualizar, verQr};
    }

    /** Identifica las opciones del filtro: los productos por su id y la opción "Todos" por sí misma. */
    private static Object claveProducto(Object opcion) {
        return opcion instanceof Producto p ? p.idProducto() : opcion;
    }

    /** Muestra productos por nombre y estados con texto legible. */
    private static Object textoDe(Object valor) {
        return valor instanceof Producto p ? p.nombre()
                : valor instanceof EstadoMercancia e ? MercanciaTableModel.texto(e)
                : valor;
    }
}
