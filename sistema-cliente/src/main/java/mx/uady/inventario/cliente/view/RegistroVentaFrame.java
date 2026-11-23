package mx.uady.inventario.cliente.view;

import com.formdev.flatlaf.FlatClientProperties;
import mx.uady.inventario.cliente.controller.RegistroVentaController;
import mx.uady.inventario.cliente.controller.VentaOperaciones;
import mx.uady.inventario.cliente.controller.VistaController;
import mx.uady.inventario.cliente.model.*;
import mx.uady.inventario.cliente.view.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Ventana principal modularizada para el registro de ventas del proveedor.
 * Compone la barra de herramientas y filtros, la tabla de inventario disponible y el formulario de venta,
 * delegando la visualización modal a los diálogos de QR e historial de precios.
 *
 * <p>Trazabilidad con Requisitos:
 * <ul>
 *   <li><b>RF-25:</b> Apertura de ventana, visualización de estado («Cargando» / «Listo») y despliegue de catálogos e inventario.</li>
 *   <li><b>RF-26:</b> Despliegue de la tabla de mercancía disponible en almacén.</li>
 *   <li><b>RF-27:</b> Filtrado de inventario en tiempo real por número de serie o nombre de producto.</li>
 *   <li><b>RF-28:</b> Selector de tiendas destino.</li>
 *   <li><b>RF-29:</b> Selector de encargados de ventas (usuarios proveedores).</li>
 *   <li><b>RF-30:</b> Botón «Actualizar Listas» para reconsultar tiendas, inventario y usuarios.</li>
 *   <li><b>RF-31:</b> Habilitación condicional del botón «Registrar venta de unidad seleccionada» solo ante selección completa.</li>
 *   <li><b>RF-32:</b> Despliegue de confirmación de venta exitosa y limpieza del formulario.</li>
 *   <li><b>RF-34:</b> Apertura modal del visor de código QR al realizar doble clic sobre una fila del inventario.</li>
 *   <li><b>RF-36:</b> Apertura modal del visor de historial de precios al presionar «Ver historial de precios».</li>
 *   <li><b>RF-37:</b> Notificación de errores de red o servidor.</li>
 *   <li><b>RF-38:</b> Presentación de ventana de confirmación interactiva para reintentar operaciones fallidas.</li>
 *   <li><b>RNF-23:</b> Desacoplamiento de la interfaz gráfica respecto a la lógica de red y serialización.</li>
 * </ul>
 * </p>
 */
public class RegistroVentaFrame extends JFrame implements VistaController {

    private final JTextField txtBuscarMercancia = new JTextField(20);
    private final JButton btnRefrescar = new JButton("Actualizar Listas");
    private final JLabel lblIndicadorCarga = new JLabel("Listo");
    private final MercanciaTabla tabla = new MercanciaTabla(txtBuscarMercancia);
    private final VentaFormulario formulario = new VentaFormulario();
    private final OperacionDialog dialogos = new OperacionDialog(this);

    private VentaOperaciones operaciones = VentaOperaciones.OPERACIONES_BASE;
    private boolean cargando;

    /**
     * Construye la ventana principal e inicializa sus paneles y gestores de eventos.
     * Hasta que se asigne el controlador con {@link #setOperaciones(VentaOperaciones)},
     * los eventos de usuario operan sobre la implementación base nula.
     *
     * <p>Requisito: RF-25.</p>
     */
    public RegistroVentaFrame() {
        super("Sistema Proveedor - Registro de Ventas (UADY)");
        setSize(950, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        add(crearPanelPrincipal());
        conectarEventos();
        actualizarHabilitacion();
    }

    /**
     * Conecta la ventana con el controlador que atiende sus acciones de negocio.
     *
     * <p>Requisito: RNF-23.</p>
     *
     * @param operaciones controlador de la pantalla
     */
    public void setOperaciones(VentaOperaciones operaciones) {
        this.operaciones = operaciones != null ? operaciones : VentaOperaciones.OPERACIONES_BASE;
    }

    /**
     * Obtiene la tabla de inventario (útil para pruebas unitarias y de interfaz).
     *
     * <p>Requisito: RF-26.</p>
     *
     * @return componente con la tabla de inventario
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
        JLabel subtitulo = new JLabel("Seleccione una unidad disponible, verifique su código QR con doble clic y registre la venta.");
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

    /**
     * Conecta los componentes a eventos
     */
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