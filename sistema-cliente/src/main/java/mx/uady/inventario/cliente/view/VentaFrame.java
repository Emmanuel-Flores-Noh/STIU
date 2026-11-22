package mx.uady.inventario.cliente.view;

import com.formdev.flatlaf.FlatClientProperties;
import mx.uady.inventario.cliente.controller.RegistroVentaController;
import mx.uady.inventario.cliente.controller.VentaOperaciones;
import mx.uady.inventario.cliente.controller.VistaController;
import mx.uady.inventario.cliente.model.*;
import mx.uady.inventario.cliente.view.components.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

/**
 * Ventana principal del Sistema Proveedor: compone la barra de herramientas, la tabla de inventario
 * y el formulario de venta, y decide qué botones se habilitan.
 *
 * <p>No conoce el socket ni el JSON: pide las operaciones a {@link RegistroVentaController} y recibe objetos
 * de datos y mensajes ya traducidos a través de {@link VistaController}.</p>
 */
public class VentaFrame extends JFrame implements VistaController {

    private final JTextField txtBuscarMercancia = new JTextField(20);
    private final JButton btnRefrescar = new JButton("Actualizar Listas");
    private final JLabel lblIndicadorCarga = new JLabel("Listo");
    private final MercanciaTabla tabla = new MercanciaTabla(txtBuscarMercancia);
    private final VentaFormulario formulario = new VentaFormulario();
    private final OperacionDialog dialogos = new OperacionDialog(this);

    private VentaOperaciones operaciones = VentaOperaciones.OPERACIONES_BASE;
    private boolean cargando;

    /**
     * Construye la ventana. Hasta que se llame a {@link #(RegistroVentaController)}, los botones
     * no ejecutan ninguna operación.
     */
    public VentaFrame() {
        super("Sistema Proveedor - Registro de Ventas (UADY)");
        setSize(950, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        add(crearPanelPrincipal());
        conectarEventos();
        actualizarHabilitacion();
    }

    /**
     * Conecta la ventana con el controlador que atiende sus acciones.
     *
     * @param operaciones controlador de la pantalla
     */
    public void setOperaciones(VentaOperaciones operaciones) {
        this.operaciones = operaciones != null ? operaciones : VentaOperaciones.OPERACIONES_BASE;
    }

    /**
     * Obtiene la tabla de inventario (para pruebas de interfaz).
     *
     * @return panel con la tabla de inventario
     */
    public MercanciaTabla getTablaMercancia() {
        return tabla;
    }

    private JPanel crearPanelPrincipal() {
        JPanel norte = new JPanel(new BorderLayout(10, 15));
        norte.add(crearPanelTitulo(), BorderLayout.NORTH);
        norte.add(crearBarraHerramientas(), BorderLayout.SOUTH);

        JPanel principal = new JPanel(new BorderLayout(15, 15));
        principal.setBorder(new EmptyBorder(20, 20, 20, 20));
        principal.add(norte, BorderLayout.NORTH);
        principal.add(tabla.getTabla(), BorderLayout.CENTER);
        principal.add(formulario, BorderLayout.SOUTH);
        return principal;
    }

    private JPanel crearPanelTitulo() {
        JLabel titulo = new JLabel("Módulo de Proveedor - Registro y Trazabilidad de Ventas");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        JLabel subtitulo = new JLabel(
                "Seleccione una unidad disponible, verifique su código QR con doble clic y registre la venta.");
        subtitulo.setForeground(new Color(110, 110, 110));
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.add(titulo, BorderLayout.NORTH);
        panel.add(subtitulo, BorderLayout.SOUTH);
        return panel;
    }

    /** Barra con búsqueda, botón de actualizar y barra de estado. El campo de búsqueda nunca se deshabilita (RNF-08). */
    private JPanel crearBarraHerramientas() {
        txtBuscarMercancia.setName("txtBuscarMercancia");
        txtBuscarMercancia.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar por serie o producto...");
        btnRefrescar.setName("btnRefrescar");
        lblIndicadorCarga.setName("lblIndicadorCarga");
        lblIndicadorCarga.setForeground(new Color(100, 100, 100));

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        busqueda.add(new JLabel("Filtrar:"));
        busqueda.add(txtBuscarMercancia);
        busqueda.add(btnRefrescar);

        JPanel estado = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        estado.add(new JLabel("Estado:"));
        estado.add(lblIndicadorCarga);

        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.add(busqueda, BorderLayout.WEST);
        barra.add(estado, BorderLayout.EAST);
        return barra;
    }

    private void conectarEventos() {
        btnRefrescar.addActionListener(e -> operaciones.cargarListas());
        tabla.setAlCambiarSeleccion(this::actualizarHabilitacion);
        tabla.setAlDobleClic(unidad -> operaciones.consultarQr(unidad.getNumeroSerie()));
        formulario.setAlCambiarSeleccion(this::actualizarHabilitacion);
        formulario.setAlRegistrar(() -> operaciones.registrarVenta(
                tabla.getUnidadSeleccionada(), formulario.getTienda(), formulario.getUsuario()));
        formulario.setAlVerHistorial(() -> {
            MercanciaDTO unidad = tabla.getUnidadSeleccionada();
            if (unidad != null) {
                operaciones.consultarHistorialPrecios(unidad.getIdProducto());
            }
        });
    }

    /** Habilita el registro solo con unidad, tienda y usuario elegidos, y el historial con una unidad elegida. */
    private void actualizarHabilitacion() {
        MercanciaDTO unidad = tabla.getUnidadSeleccionada();
        boolean completa = RegistroVentaController.esVentaCompleta(unidad, formulario.getTienda(), formulario.getUsuario());
        formulario.setRegistrarHabilitado(!cargando && completa);
        formulario.setHistorialHabilitado(!cargando && unidad != null && unidad.getIdProducto() != null);
    }

    @Override
    public void mostrarIndicadorCarga(String mensaje, boolean cargando) {
        this.cargando = cargando;
        lblIndicadorCarga.setText(mensaje);
        btnRefrescar.setEnabled(!cargando);
        formulario.setSelectoresHabilitados(!cargando);
        actualizarHabilitacion();
    }

    @Override
    public void actualizarListasCombos(List<TiendaDTO> tiendas, List<UsuarioDTO> usuarios) {
        formulario.actualizarCatalogos(tiendas, usuarios);
        actualizarHabilitacion();
    }

    @Override
    public void actualizarTablaMercancia(List<MercanciaDTO> unidades) {
        tabla.reemplazarUnidades(unidades);
        actualizarHabilitacion();
    }

    @Override
    public void limpiarFormulario() {
        tabla.limpiarSeleccion();
        formulario.limpiar();
        txtBuscarMercancia.setText("");
        actualizarHabilitacion();
    }

    @Override
    public void mostrarMensajeExito(String mensaje) {
        dialogos.exito(mensaje);
    }

    @Override
    public void mostrarMensajeError(String mensaje) {
        dialogos.error(mensaje);
    }

    @Override
    public void mostrarDialogoReintentar(String tipoError, String mensaje, Runnable reintento) {
        if (dialogos.preguntarReintento(tipoError, mensaje)) {
            reintento.run();
        }
    }

    @Override
    public void mostrarQr(QrDTO qr) {
        new QrPreviewDialog(this, qr).setVisible(true);
    }

    @Override
    public void mostrarHistorialPrecios(PrecioHistorialDTO historial) {
        new HistorialPrecioDialog(this, historial).setVisible(true);
    }
}