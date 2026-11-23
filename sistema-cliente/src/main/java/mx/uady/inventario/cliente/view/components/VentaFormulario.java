package mx.uady.inventario.cliente.view.components;

import com.formdev.flatlaf.FlatClientProperties;
import mx.uady.inventario.cliente.model.TiendaDTO;
import mx.uady.inventario.cliente.model.UsuarioDTO;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Formulario de venta: lista de tiendas, lista de usuarios proveedores y los botones de registrar
 * la venta y de consultar el historial de precios. No decide cuándo habilitarse: la ventana se lo indica.
 *
 * <p>Trazabilidad con Requisitos:
 * <ul>
 *   <li><b>RF-28:</b> Despliegue de la lista de tiendas para elegir el destino de la venta.</li>
 *   <li><b>RF-29:</b> Despliegue de la lista de encargados de ventas (usuarios con rol proveedor).</li>
 *   <li><b>RF-31:</b> Habilitación del botón «Registrar venta» únicamente cuando unidad, tienda y usuario están seleccionados.</li>
 *   <li><b>RF-32:</b> Disparo de la acción de venta y limpieza de los selectores tras éxito.</li>
 *   <li><b>RF-36:</b> Disparo de la consulta de historial de precios para la unidad seleccionada.</li>
 * </ul>
 * </p>
 */
public class VentaFormulario extends JPanel {

    private final JComboBox<TiendaDTO> comboTiendas = new JComboBox<>();
    private final JComboBox<UsuarioDTO> comboUsuarios = new JComboBox<>();
    private final JButton btnRegistrarVenta = new JButton("Registrar venta de unidad seleccionada");
    private final JButton btnHistorialPrecios = new JButton("Ver historial de precios");

    /**
     * Construye el formulario con ambos botones deshabilitados.
     */
    public VentaFormulario() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Datos de Registro de Venta"));

        comboTiendas.setName("comboTiendas");
        comboUsuarios.setName("comboUsuarios");

        btnRegistrarVenta.setName("btnRegistrarVenta");
        btnRegistrarVenta.putClientProperty(FlatClientProperties.STYLE,
                "background: $accentColor; " +
                "foreground: #555555ff; " +
                "font: bold; " +
                "arc: 6");

        btnHistorialPrecios.setName("btnHistorialPrecios");
        btnRegistrarVenta.setEnabled(false);
        btnHistorialPrecios.setEnabled(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1;
        add(new JLabel("Tienda Destino:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.9;
        add(comboTiendas, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.1;
        add(new JLabel("Usuario Proveedor:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.9;
        add(comboUsuarios, gbc);

        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 10, 0));
        panelBotones.add(btnRegistrarVenta);
        panelBotones.add(btnHistorialPrecios);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        add(panelBotones, gbc);
    }

    /**
     * Obtiene la tienda elegida.
     *
     * @return tienda seleccionada, o {@code null} si no hay
     */
    public TiendaDTO getTienda() {
        return (TiendaDTO) comboTiendas.getSelectedItem();
    }

    /**
     * Obtiene el usuario proveedor elegido.
     *
     * @return usuario seleccionado, o {@code null} si no hay
     */
    public UsuarioDTO getUsuario() {
        return (UsuarioDTO) comboUsuarios.getSelectedItem();
    }

    /**
     * Reemplaza las opciones de ambas listas conservando la elección previa si sigue existiendo.
     *
     * @param tiendas  tiendas destino, en el orden recibido
     * @param usuarios usuarios con rol de proveedor
     */
    public void actualizarCatalogos(List<TiendaDTO> tiendas, List<UsuarioDTO> usuarios) {
        reemplazarOpciones(comboTiendas, tiendas, TiendaDTO::getIdTienda);
        reemplazarOpciones(comboUsuarios, usuarios, UsuarioDTO::getIdUsuario);
    }

    /** Quita la tienda y el usuario elegidos. */
    public void limpiar() {
        comboTiendas.setSelectedIndex(-1);
        comboUsuarios.setSelectedIndex(-1);
    }

    /**
     * Registra la acción que se ejecuta al cambiar la tienda o el usuario elegidos.
     *
     * @param accion acción a ejecutar
     */
    public void setAlCambiarSeleccion(Runnable accion) {
        comboTiendas.addActionListener(e -> accion.run());
        comboUsuarios.addActionListener(e -> accion.run());
    }

    /**
     * Registra la acción del botón de registrar la venta.
     *
     * @param accion acción a ejecutar
     */
    public void setAlRegistrar(Runnable accion) {
        btnRegistrarVenta.addActionListener(e -> accion.run());
    }

    /**
     * Registra la acción del botón de historial de precios.
     *
     * @param accion acción a ejecutar
     */
    public void setAlVerHistorial(Runnable accion) {
        btnHistorialPrecios.addActionListener(e -> accion.run());
    }

    /**
     * Habilita o deshabilita el botón de registrar la venta.
     *
     * @param habilitado {@code true} para permitir registrar
     */
    public void setRegistrarHabilitado(boolean habilitado) {
        btnRegistrarVenta.setEnabled(habilitado);
    }

    /**
     * Habilita o deshabilita el botón de historial de precios.
     *
     * @param habilitado {@code true} para permitir consultar
     */
    public void setHistorialHabilitado(boolean habilitado) {
        btnHistorialPrecios.setEnabled(habilitado);
    }

    /**
     * Habilita o deshabilita las listas de tienda y de usuario.
     *
     * @param habilitado {@code true} para permitir elegir
     */
    public void setSelectoresHabilitados(boolean habilitado) {
        comboTiendas.setEnabled(habilitado);
        comboUsuarios.setEnabled(habilitado);
    }

    private static <T> void reemplazarOpciones(JComboBox<T> combo, List<T> opciones, Function<T, Object> id) {
        int indicePrevio = combo.getSelectedIndex();
        T previo = indicePrevio >= 0 ? combo.getItemAt(indicePrevio) : null;
        combo.removeAllItems();
        int indiceNuevo = -1;
        for (T opcion : opciones) {
            combo.addItem(opcion);
            if (previo != null && Objects.equals(id.apply(opcion), id.apply(previo))) {
                indiceNuevo = combo.getItemCount() - 1;
            }
        }
        combo.setSelectedIndex(indiceNuevo);
    }
}
