package mx.uady.inventario.central.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import mx.uady.inventario.central.view.comun.Combos;
import mx.uady.inventario.central.view.comun.Documentos;
import mx.uady.inventario.common.modelo.Producto;

/**
 * Formulario "Registrar unidad" de la pantalla de Mercancía: producto, número de serie, botón
 * Registrar y el error junto al campo. Sin productos registrados se deshabilita y lo indica.
 */
final class RegistroMercanciaForm extends JPanel {

    private static final long serialVersionUID = 1L;
    static final String SIN_PRODUCTOS = "Primero registra un producto en la pestaña Productos.";

    private final JComboBox<Producto> producto = new JComboBox<>();
    private final JTextField serie = new JTextField(22);
    private final JButton registrar = new JButton("Registrar");
    private final JLabel error = new JLabel(" ");

    private Runnable alRegistrar = () -> { };
    private boolean cargado;
    private boolean hayProductos;

    RegistroMercanciaForm() {
        super(new GridBagLayout());
        producto.setName("productoRegistro");
        serie.setName("serie");
        registrar.setName("registrar");
        error.setName("errorSerie");

        producto.setRenderer(Combos.renderTexto(v -> v instanceof Producto p ? p.nombre() : v));
        producto.setPrototypeDisplayValue(new Producto(0, "Laptop Lenovo ThinkPad E14 extra", null, null));
        serie.putClientProperty("JTextField.placeholderText", "Escanea o escribe el número de serie");
        serie.addActionListener(e -> {
            if (registrar.isEnabled()) {
                alRegistrar.run();
            }
        });
        serie.getDocument().addDocumentListener(Documentos.alCambiar(() -> mostrarError(null)));
        registrar.addActionListener(e -> alRegistrar.run());
        Color rojo = UIManager.getColor("Component.error.focusedBorderColor");
        error.setForeground(rojo != null ? rojo : new Color(0xC62828));
        error.putClientProperty("FlatLaf.styleClass", "small");

        construir();
        actualizarHabilitado();
    }

    private void construir() {
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Registrar unidad"),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 0, 0, 8);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        add(new JLabel("Producto"), c);
        c.gridx = 1;
        add(producto, c);
        c.gridx = 2;
        add(new JLabel("Número de serie"), c);
        c.gridx = 3; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        add(serie, c);
        c.gridx = 4; c.weightx = 0; c.fill = GridBagConstraints.NONE; c.insets = new Insets(4, 0, 0, 0);
        add(registrar, c);

        c.gridx = 3; c.gridy = 1; c.gridwidth = 2; c.insets = new Insets(2, 0, 0, 0);
        add(error, c);
    }

    private void actualizarHabilitado() {
        registrar.setEnabled(hayProductos);
        producto.setEnabled(hayProductos);
        serie.setEnabled(hayProductos);
        if (!hayProductos && cargado) {
            mostrarError(SIN_PRODUCTOS);
        } else if (SIN_PRODUCTOS.equals(error.getText())) {
            mostrarError(null);
        }
    }

    /** Acción al pulsar Registrar o Enter en la serie (Enter solo si se puede registrar). */
    void alRegistrar(Runnable accion) {
        this.alRegistrar = Objects.requireNonNull(accion, "accion");
    }

    /** Rellena el combo conservando el producto elegido si sigue existiendo. */
    void mostrarProductos(List<Producto> productos) {
        Combos.rellenarConservando(producto, productos, Producto::idProducto);
        hayProductos = !productos.isEmpty();
        cargado = true;
        actualizarHabilitado();
    }

    Optional<Producto> productoElegido() {
        return Optional.ofNullable((Producto) producto.getSelectedItem());
    }

    String serieCapturada() {
        return serie.getText();
    }

    void limpiarSerie() {
        serie.setText("");
        serie.requestFocusInWindow();
    }

    /** Muestra (o borra, con {@code null}) el error del número de serie y resalta el campo. */
    void mostrarError(String texto) {
        serie.putClientProperty("JComponent.outline", texto == null ? null : "error");
        error.setText(texto == null ? " " : texto);
        if (texto != null) {
            serie.selectAll();
            serie.requestFocusInWindow();
        }
    }

    /** Controles que se bloquean mientras hay una operación en curso. */
    Component[] controles() {
        return new Component[] {producto, serie, registrar};
    }
}
