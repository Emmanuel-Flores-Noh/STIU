package mx.uady.inventario.central.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import mx.uady.inventario.central.service.ConsultasVentaService;
import mx.uady.inventario.central.view.comun.Combos;
import mx.uady.inventario.central.view.comun.Documentos;
import mx.uady.inventario.central.view.comun.ResumenTabla;
import mx.uady.inventario.central.view.comun.TablaFiltrable;
import mx.uady.inventario.common.modelo.Mercancia;
import mx.uady.inventario.common.modelo.TiendaAlmacen;
import mx.uady.inventario.common.modelo.Usuario;

/**
 * Pantalla visual para seleccionar una mercancía y preparar una venta.
 * La carga de datos y el registro de la venta pertenecen al controlador.
 */
public final class VentaPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final ResumenTabla RESUMEN = new ResumenTabla(
            "No hay mercancías disponibles para vender.", "Ninguna mercancía coincide con «%s».",
            "%d de %d mercancías", "1 mercancía disponible", "%d mercancías disponibles");

    private final VentaTableModel modelo = new VentaTableModel();
    private final TablaFiltrable<VentaTableModel, Mercancia> tablaFiltrable =
            new TablaFiltrable<VentaTableModel, Mercancia>(modelo, modelo::getFila)
                    .ordenarPor(VentaTableModel.COL_SERIE);
    private final JTable tabla = tablaFiltrable.tabla();
    private final JTextField busqueda = new JTextField(18);
    private final JComboBox<TiendaAlmacen> tienda = new JComboBox<>();
    private final JComboBox<Usuario> usuario = new JComboBox<>();
    private final JButton actualizar = new JButton("Actualizar");
    private final JButton vender = new JButton("Registrar venta");
    private final JLabel estado = new JLabel(" ");

    private Runnable alActualizar = () -> { };
    private Runnable alVender = () -> { };

    public VentaPanel() {
        super(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        nombrarComponentes();

        add(construirFormulario(), BorderLayout.NORTH);
        add(new JScrollPane(construirTabla()), BorderLayout.CENTER);
        estado.putClientProperty("FlatLaf.styleClass", "small");
        add(estado, BorderLayout.SOUTH);
        actualizarDisponibilidad();
    }

    private void nombrarComponentes() {
        busqueda.setName("busquedaVentas");
        tienda.setName("tiendaVenta");
        usuario.setName("usuarioVenta");
        actualizar.setName("actualizarVentas");
        vender.setName("registrarVenta");
        tabla.setName("tablaVentas");
        estado.setName("estadoVentas");
    }

    private JPanel construirFormulario() {
        busqueda.putClientProperty("JTextField.placeholderText", "Buscar número de serie");
        busqueda.putClientProperty("JTextField.showClearButton", true);
        busqueda.getDocument().addDocumentListener(Documentos.alCambiar(this::aplicarBusqueda));

        tienda.setRenderer(Combos.renderTexto(VentaPanel::textoDe));
        tienda.addActionListener(e -> actualizarDisponibilidad());
        usuario.setRenderer(Combos.renderTexto(VentaPanel::textoDe));
        usuario.addActionListener(e -> actualizarDisponibilidad());
        actualizar.addActionListener(e -> alActualizar.run());
        vender.addActionListener(e -> alVender.run());

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Registrar venta"),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 0, 0, 8);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        formulario.add(new JLabel("Tienda"), c);
        c.gridx = 1;
        formulario.add(tienda, c);
        c.gridx = 2;
        formulario.add(new JLabel("Usuario"), c);
        c.gridx = 3;
        formulario.add(usuario, c);

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izquierda.add(busqueda);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        derecha.add(actualizar);
        derecha.add(vender);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.insets = new Insets(8, 0, 0, 0);
        formulario.add(izquierda, c);
        c.gridx = 2;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.EAST;
        formulario.add(derecha, c);
        return formulario;
    }

    private JTable construirTabla() {
        tablaFiltrable.alSeleccionar(this::actualizarDisponibilidad);
        return tabla;
    }

    private void aplicarBusqueda() {
        tablaFiltrable.filtrarPorTexto(VentaTableModel.COL_SERIE, busqueda.getText());
        actualizarResumen();
    }

    private void actualizarResumen() {
        estado.setText(RESUMEN.texto(tablaFiltrable.totalFilas(), tablaFiltrable.filasVisibles(), busqueda.getText()));
    }

    private void actualizarDisponibilidad() {
        vender.setEnabled(seleccionada().isPresent() && tienda.getSelectedItem() != null
                && usuario.getSelectedItem() != null);
    }

    // ---- API para el controlador ----

    public void alActualizar(Runnable accion) {
        alActualizar = accion;
    }

    public void alVender(Runnable accion) {
        alVender = accion;
    }

    public void mostrarMercancias(List<Mercancia> mercancias, Map<Integer, String> nombresProducto) {
        modelo.setFilas(mercancias, nombresProducto);
        actualizarResumen();
        actualizarDisponibilidad();
    }

    public void mostrarTiendas(List<TiendaAlmacen> tiendas) {
        Combos.rellenarConservando(tienda, tiendas, TiendaAlmacen::idTienda);
        actualizarDisponibilidad();
    }

    public void mostrarUsuarios(List<Usuario> usuarios) {
        Combos.rellenarConservando(usuario, usuarios, Usuario::idUsuario);
        actualizarDisponibilidad();
    }

    public Optional<Mercancia> seleccionada() {
        return tablaFiltrable.seleccionada();
    }

    public boolean seleccionarMercancia(String numeroSerie) {
        return tablaFiltrable.seleccionarFilaDelModelo(modelo.indiceDe(numeroSerie));
    }

    public Optional<TiendaAlmacen> tiendaSeleccionada() {
        return Optional.ofNullable((TiendaAlmacen) tienda.getSelectedItem());
    }

    public String nombreProducto(int idProducto) {
        return modelo.nombreProducto(idProducto);
    }

    public Optional<Usuario> usuarioSeleccionado() {
        return Optional.ofNullable((Usuario) usuario.getSelectedItem());
    }

    public void setEstado(String texto) {
        estado.setText(texto);
    }

    public Component[] controles() {
        return new Component[] {busqueda, tienda, usuario, actualizar, vender, tabla};
    }

    /** Muestra tiendas y usuarios por nombre. */
    private static Object textoDe(Object valor) {
        return valor instanceof TiendaAlmacen t ? t.nombre()
                : valor instanceof Usuario u ? ConsultasVentaService.nombreCompleto(u)
                : valor;
    }
}
