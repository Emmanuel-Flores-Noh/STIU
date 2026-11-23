package mx.uady.inventario.central.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import mx.uady.inventario.central.view.comun.Combos;
import mx.uady.inventario.central.view.comun.Documentos;
import mx.uady.inventario.central.view.comun.ResumenTabla;
import mx.uady.inventario.central.view.comun.TablaFiltrable;
import mx.uady.inventario.common.modelo.EstadoMercancia;
import mx.uady.inventario.common.modelo.Mercancia;
import mx.uady.inventario.common.modelo.Producto;

/**
 * Pantalla de mercancía: registro de unidades físicas y tabla filtrable por producto y estado.
 * No contiene lógica de negocio; {@code MercanciaController} reacciona a sus eventos.
 */
public class MercanciaPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    static final String TODOS = "Todos";
    static final String SIN_PRODUCTOS = RegistroMercanciaForm.SIN_PRODUCTOS;
    private static final String SIN_RESULTADOS_FILTRADOS = "No hay unidades con los filtros elegidos.";
    private static final ResumenTabla RESUMEN = new ResumenTabla("No hay unidades registradas.",
            "Ninguna unidad coincide con «%s».", "%d de %d unidades", "1 unidad", "%d unidades");

    private final RegistroMercanciaForm registro = new RegistroMercanciaForm();

    // Consulta
    private final JComboBox<Object> filtroProducto = new JComboBox<>();
    private final JComboBox<Object> filtroEstado = new JComboBox<>();
    private final JTextField busquedaSerie = new JTextField(16);
    private final JButton actualizar = new JButton("Actualizar");
    private final JButton verQr = new JButton("Ver QR");
    private final MercanciaTableModel modelo = new MercanciaTableModel();
    private final TablaFiltrable<MercanciaTableModel, Mercancia> tablaFiltrable =
            new TablaFiltrable<MercanciaTableModel, Mercancia>(modelo, modelo::getFila)
                    .ordenarPor(MercanciaTableModel.COL_SERIE);
    private final JTable tabla = tablaFiltrable.tabla();
    private final JLabel estado = new JLabel(" ");

    private Runnable alCambiarFiltros = () -> { };
    private Runnable alVerQr = () -> { };
    /** Evita recargar mientras se rellenan los combos por código. */
    private boolean ajustandoCombos;
    private boolean cargado;

    public MercanciaPanel() {
        super(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        nombrarComponentes();

        add(registro, BorderLayout.NORTH);
        JPanel consulta = new JPanel(new BorderLayout(0, 8));
        consulta.add(construirFiltros(), BorderLayout.NORTH);
        consulta.add(new JScrollPane(construirTabla()), BorderLayout.CENTER);
        estado.putClientProperty("FlatLaf.styleClass", "small");
        consulta.add(estado, BorderLayout.SOUTH);
        add(consulta, BorderLayout.CENTER);

        verQr.setEnabled(false);
    }

    private void nombrarComponentes() {
        filtroProducto.setName("filtroProducto");
        filtroEstado.setName("filtroEstado");
        busquedaSerie.setName("busquedaSerie");
        actualizar.setName("actualizar");
        verQr.setName("verQr");
        tabla.setName("tablaUnidades");
        estado.setName("estadoUnidades");
    }

    private JPanel construirFiltros() {
        filtroProducto.setRenderer(Combos.renderTexto(MercanciaPanel::textoDe));
        filtroEstado.setRenderer(Combos.renderTexto(MercanciaPanel::textoDe));
        filtroProducto.addItem(TODOS);
        filtroEstado.addItem(TODOS);
        for (EstadoMercancia e : EstadoMercancia.values()) {
            filtroEstado.addItem(e);
        }
        filtroProducto.addActionListener(e -> notificarFiltros());
        filtroEstado.addActionListener(e -> notificarFiltros());

        busquedaSerie.putClientProperty("JTextField.placeholderText", "Buscar número de serie");
        busquedaSerie.putClientProperty("JTextField.showClearButton", true);
        busquedaSerie.getDocument().addDocumentListener(Documentos.alCambiar(this::aplicarBusqueda));

        actualizar.addActionListener(e -> alCambiarFiltros.run());
        verQr.addActionListener(e -> alVerQr.run());

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        izquierda.add(new JLabel("Producto"));
        izquierda.add(filtroProducto);
        izquierda.add(new JLabel("Estado"));
        izquierda.add(filtroEstado);
        izquierda.add(busquedaSerie);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        derecha.add(actualizar);
        derecha.add(verQr);

        JPanel barra = new JPanel(new BorderLayout());
        barra.add(izquierda, BorderLayout.WEST);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    private JTable construirTabla() {
        tabla.getColumnModel().getColumn(MercanciaTableModel.COL_SERIE).setPreferredWidth(180);
        tabla.getColumnModel().getColumn(MercanciaTableModel.COL_PRODUCTO).setPreferredWidth(320);
        tabla.getColumnModel().getColumn(MercanciaTableModel.COL_ESTADO).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(MercanciaTableModel.COL_QR).setPreferredWidth(90);

        tablaFiltrable.alSeleccionar(() -> verQr.setEnabled(seleccionada().isPresent()));
        tablaFiltrable.alDobleClic(() -> {
            if (verQr.isEnabled()) {
                alVerQr.run();
            }
        });
        return tabla;
    }

    private void notificarFiltros() {
        if (!ajustandoCombos) {
            alCambiarFiltros.run();
        }
    }

    private void aplicarBusqueda() {
        tablaFiltrable.filtrarPorTexto(MercanciaTableModel.COL_SERIE, busquedaSerie.getText());
        actualizarResumen();
    }

    private void actualizarResumen() {
        if (!cargado) {
            return;
        }
        int total = tablaFiltrable.totalFilas();
        int visibles = tablaFiltrable.filasVisibles();
        boolean hayFiltros = filtroProducto.getSelectedIndex() > 0 || filtroEstado.getSelectedIndex() > 0;
        estado.setText(total == 0 && hayFiltros
                ? SIN_RESULTADOS_FILTRADOS
                : RESUMEN.texto(total, visibles, busquedaSerie.getText()));
    }

    // ---- API para el controlador ----

    public void alRegistrar(Runnable accion) {
        registro.alRegistrar(accion);
    }

    public void alCambiarFiltros(Runnable accion) {
        this.alCambiarFiltros = accion;
    }

    public void alVerQr(Runnable accion) {
        this.alVerQr = accion;
    }

    /** Rellena ambos combos conservando la selección actual cuando el producto sigue existiendo. */
    public void mostrarProductos(List<Producto> productos) {
        List<Object> opcionesFiltro = new ArrayList<>();
        opcionesFiltro.add(TODOS);
        opcionesFiltro.addAll(productos);

        ajustandoCombos = true;
        try {
            Combos.rellenarConservando(filtroProducto, opcionesFiltro, MercanciaPanel::claveProducto);
        } finally {
            ajustandoCombos = false;
        }
        registro.mostrarProductos(productos);
        cargado = true;
    }

    public void mostrarUnidades(List<Mercancia> unidades, List<Producto> productos) {
        Map<Integer, String> nombres = productos.stream()
                .collect(Collectors.toMap(Producto::idProducto, Producto::nombre, (a, b) -> a));
        cargado = true;
        modelo.setFilas(unidades, nombres);
        actualizarResumen();
    }

    public void actualizarUnidad(Mercancia unidad) {
        modelo.actualizar(unidad);
    }

    /** @return {@code true} si la unidad está visible con los filtros actuales y quedó seleccionada */
    public boolean seleccionarUnidad(String numeroSerie) {
        return tablaFiltrable.seleccionarFilaDelModelo(modelo.indiceDe(numeroSerie));
    }

    public Optional<Mercancia> seleccionada() {
        return tablaFiltrable.seleccionada();
    }

    public String nombreProducto(int idProducto) {
        return modelo.nombreProducto(idProducto);
    }

    public Optional<Producto> productoParaRegistrar() {
        return registro.productoElegido();
    }

    public String serieCapturada() {
        return registro.serieCapturada();
    }

    public void limpiarSerie() {
        registro.limpiarSerie();
    }

    /** Muestra (o borra, con {@code null}) el error del número de serie y resalta el campo. */
    public void mostrarErrorSerie(String error) {
        registro.mostrarError(error);
    }

    /** Producto elegido en el filtro, o {@code null} para todos. */
    public Integer filtroProductoSeleccionado() {
        return filtroProducto.getSelectedItem() instanceof Producto p ? p.idProducto() : null;
    }

    /** Estado elegido en el filtro, o {@code null} para todos. */
    public EstadoMercancia filtroEstadoSeleccionado() {
        return filtroEstado.getSelectedItem() instanceof EstadoMercancia e ? e : null;
    }

    public void setEstado(String texto) {
        estado.setText(texto);
    }

    /** Controles que se bloquean mientras hay una operación en curso. */
    public Component[] controles() {
        Component[] deRegistro = registro.controles();
        Component[] todos = Arrays.copyOf(deRegistro, deRegistro.length + 4);
        todos[deRegistro.length] = filtroProducto;
        todos[deRegistro.length + 1] = filtroEstado;
        todos[deRegistro.length + 2] = actualizar;
        todos[deRegistro.length + 3] = verQr;
        return todos;
    }

    // ---- Auxiliares ----

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
