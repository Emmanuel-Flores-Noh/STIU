package mx.uady.inventario.central.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import mx.uady.inventario.central.view.comun.ResumenTabla;
import mx.uady.inventario.central.view.comun.TablaFiltrable;
import mx.uady.inventario.central.view.components.QrVista;
import mx.uady.inventario.common.modelo.EstadoMercancia;
import mx.uady.inventario.common.modelo.Mercancia;
import mx.uady.inventario.common.modelo.Producto;

/**
 * Pantalla de mercancía: registro de unidades físicas y tabla filtrable por producto y estado.
 * Reúne el formulario de registro ({@link RegistroMercanciaForm}), la barra de filtros
 * ({@link FiltrosMercanciaBar}) y la tabla de unidades. No contiene lógica de negocio;
 * {@code MercanciaController} reacciona a sus eventos.
 */
public class MercanciaPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    static final String TODOS = FiltrosMercanciaBar.TODOS;
    static final String SIN_PRODUCTOS = RegistroMercanciaForm.SIN_PRODUCTOS;
    private static final String SIN_RESULTADOS_FILTRADOS = "No hay unidades con los filtros elegidos.";
    private static final ResumenTabla RESUMEN = new ResumenTabla("No hay unidades registradas.",
            "Ninguna unidad coincide con «%s».", "%d de %d unidades", "1 unidad", "%d unidades");

    private final RegistroMercanciaForm registro = new RegistroMercanciaForm();
    private final FiltrosMercanciaBar filtros = new FiltrosMercanciaBar();
    private final MercanciaTableModel modelo = new MercanciaTableModel();
    private final TablaFiltrable<MercanciaTableModel, Mercancia> tablaFiltrable =
            new TablaFiltrable<MercanciaTableModel, Mercancia>(modelo, modelo::getFila)
                    .ordenarPor(MercanciaTableModel.COL_SERIE);
    private final JTable tabla = tablaFiltrable.tabla();
    private final JLabel estado = new JLabel(" ");

    private Runnable alVerQr = () -> { };
    private boolean cargado;

    public MercanciaPanel() {
        super(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        tabla.setName("tablaUnidades");
        estado.setName("estadoUnidades");

        filtros.alBuscar(this::aplicarBusqueda);
        filtros.alVerQr(() -> alVerQr.run());

        add(registro, BorderLayout.NORTH);
        JPanel consulta = new JPanel(new BorderLayout(0, 8));
        consulta.add(filtros, BorderLayout.NORTH);
        consulta.add(new JScrollPane(construirTabla()), BorderLayout.CENTER);
        estado.putClientProperty("FlatLaf.styleClass", "small");
        consulta.add(estado, BorderLayout.SOUTH);
        add(consulta, BorderLayout.CENTER);
    }

    private JTable construirTabla() {
        tabla.getColumnModel().getColumn(MercanciaTableModel.COL_SERIE).setPreferredWidth(180);
        tabla.getColumnModel().getColumn(MercanciaTableModel.COL_PRODUCTO).setPreferredWidth(320);
        tabla.getColumnModel().getColumn(MercanciaTableModel.COL_ESTADO).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(MercanciaTableModel.COL_QR).setPreferredWidth(90);

        tablaFiltrable.alSeleccionar(() -> filtros.setVerQrHabilitado(seleccionada().isPresent()));
        tablaFiltrable.alDobleClic(() -> {
            if (filtros.verQrHabilitado()) {
                alVerQr.run();
            }
        });
        return tabla;
    }

    private void aplicarBusqueda() {
        tablaFiltrable.filtrarPorTexto(MercanciaTableModel.COL_SERIE, filtros.textoBusqueda());
        actualizarResumen();
    }

    private void actualizarResumen() {
        if (!cargado) {
            return;
        }
        int total = tablaFiltrable.totalFilas();
        int visibles = tablaFiltrable.filasVisibles();
        estado.setText(total == 0 && filtros.hayFiltros()
                ? SIN_RESULTADOS_FILTRADOS
                : RESUMEN.texto(total, visibles, filtros.textoBusqueda()));
    }

    // ---- API para el controlador ----

    public void alRegistrar(Runnable accion) {
        registro.alRegistrar(accion);
    }

    public void alCambiarFiltros(Runnable accion) {
        filtros.alCambiarFiltros(accion);
    }

    public void alVerQr(Runnable accion) {
        this.alVerQr = accion;
    }

    /** Rellena ambos combos conservando la selección actual cuando el producto sigue existiendo. */
    public void mostrarProductos(List<Producto> productos) {
        registro.mostrarProductos(productos);
        filtros.mostrarProductos(productos);
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
        return filtros.productoSeleccionado();
    }

    /** Estado elegido en el filtro, o {@code null} para todos. */
    public EstadoMercancia filtroEstadoSeleccionado() {
        return filtros.estadoSeleccionado();
    }

    /** Abre la vista previa del código QR de la unidad, con el nombre de su producto. */
    public void mostrarQr(Mercancia unidad) {
        QrVista.mostrar(this, unidad, nombreProducto(unidad.idProducto()));
    }

    public void setEstado(String texto) {
        estado.setText(texto);
    }

    /** Controles que se bloquean mientras hay una operación en curso. */
    public Component[] controles() {
        return Stream.concat(Arrays.stream(registro.controles()), Arrays.stream(filtros.controles()))
                .toArray(Component[]::new);
    }
}
