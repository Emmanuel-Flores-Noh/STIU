package mx.uady.inventario.cliente.controller;


import mx.uady.inventario.cliente.model.MercanciaDTO;
import mx.uady.inventario.cliente.model.TiendaDTO;
import mx.uady.inventario.cliente.model.UsuarioDTO;
import mx.uady.inventario.cliente.network.VentaService;
import mx.uady.inventario.cliente.util.FormateadorTexto;
import mx.uady.inventario.cliente.util.PantallaTipo;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controlador encargado de coordinar la lógica de negocio, la comunicación de red
 * con ClienteTCP y la actualización asíncrona de la interfaz gráfica
 * en RegistroVentaFrame.
 *
 * <p>La vista nunca conoce el socket ni el JSON: recibe DTOs y mensajes ya traducidos
 * con el TraductorErrores.</p>
 */
public class RegistroVentaController implements VentaOperaciones {
    private final VistaController vista;
    private final VentaService servicio;
    private final EjecutorAsincrono ejecutor;
    private final ProductoController productos;
    private final PresentadorErrores errores;
    private final AtomicBoolean ventaEnCurso = new AtomicBoolean(false);

    /**
     * Crea el controlador con su propio {@link ProductoController}.
     *
     * @param vista    pantalla que se coordina
     * @param servicio acceso al Sistema 1 a través del Broker
     * @param ejecutor ejecutor de trabajos en segundo plano
     */
    public RegistroVentaController(VistaController vista, VentaService servicio, EjecutorAsincrono ejecutor) {
        this(vista, servicio, ejecutor, new ProductoController(vista, servicio, ejecutor));
    }

    /**
     * Crea el controlador con un {@link ProductoController} ya construido.
     *
     * @param vista     pantalla que se coordina
     * @param servicio  acceso al Sistema 1 a través del Broker
     * @param ejecutor  ejecutor de trabajos en segundo plano
     * @param productos controlador de las consultas de QR e historial
     */
    public RegistroVentaController(VistaController vista, VentaService servicio, EjecutorAsincrono ejecutor, ProductoController productos) {
        this.vista = vista;
        this.servicio = servicio;
        this.ejecutor = ejecutor;
        this.productos = productos;
        this.errores = new PresentadorErrores(vista);
    }

    /**
     * Regla RF-31: una venta solo puede enviarse con una unidad, una tienda y un usuario elegidos.
     * La usan la vista (para habilitar el botón) y este controlador (para rechazar el envío).
     *
     * @param unidad  unidad seleccionada; puede ser nula
     * @param tienda  tienda destino; puede ser nula
     * @param usuario usuario proveedor; puede ser nulo
     * @return {@code true} si hay número de serie, id de tienda e id de usuario
     */
    public static boolean esVentaCompleta(MercanciaDTO unidad, TiendaDTO tienda, UsuarioDTO usuario) {
        boolean productoValido = unidad != null && unidad.getNumeroSerie() != null && !unidad.getNumeroSerie().isBlank();
        boolean tiendaValida = tienda != null && tienda.getIdTienda() != null;
        boolean usuarioValido = usuario != null && usuario.getIdUsuario() != null;
        return productoValido && tiendaValida && usuarioValido;
    }

    @Override
    public void cargarListas() {
        vista.mostrarIndicadorCarga(PantallaTipo.ESTADO_CARGANDO.getMensaje(), true);
        ejecutor.ejecutar(servicio::cargarCatalogos,
                catalogos -> {
                    vista.actualizarListasCombos(catalogos.tiendas(), catalogos.usuarios());
                    vista.actualizarTablaMercancia(catalogos.mercancia());
                    vista.mostrarIndicadorCarga(PantallaTipo.ESTADO_LISTO.getMensaje(), false);
                },
                error -> {
                    vista.mostrarIndicadorCarga(PantallaTipo.ERROR_LISTAS.getMensaje(), false);
                    errores.informar(PantallaTipo.ERROR_LISTAS.getMensaje(), error, this::cargarListas);
                });
    }

    @Override
    public void registrarVenta(MercanciaDTO unidad, TiendaDTO tienda, UsuarioDTO usuario) {
        if (!esVentaCompleta(unidad, tienda, usuario)) {
            vista.mostrarMensajeError(PantallaTipo.ERROR_DATOS_INCOMPLETOS.getMensaje());
            return;
        }
        if (!ventaEnCurso.compareAndSet(false, true)) {
            return; // ya hay una venta enviándose: nunca se envía dos veces la misma
        }
        vista.mostrarIndicadorCarga("Registrando venta de la unidad...", true);
        ejecutor.ejecutar(
                () -> servicio.registrarVenta(unidad.getNumeroSerie(), tienda.getIdTienda(), usuario.getIdUsuario()),
                fechaVenta -> {
                    ventaEnCurso.set(false);
                    alVenderConExito(unidad, tienda, fechaVenta);
                },
                error -> {
                    ventaEnCurso.set(false);
                    vista.mostrarIndicadorCarga("No se registró la venta.", false);
                    errores.informar(PantallaTipo.ERROR_VENTA.getMensaje(), error, () -> registrarVenta(unidad, tienda, usuario));
                });
    }

    @Override
    public void consultarQr(String numeroSerie) {
        productos.consultarQr(numeroSerie);
    }

    @Override
    public void consultarHistorialPrecios(Integer idProducto) {
        productos.consultarHistorialPrecios(idProducto);
    }

    /** Tras una venta exitosa: confirma al encargado, limpia el formulario y recarga las listas. */
    private void alVenderConExito(MercanciaDTO unidad, TiendaDTO tienda, String fechaVenta) {
        vista.mostrarIndicadorCarga("Venta registrada", false);
        vista.mostrarMensajeExito("Venta registrada con éxito"
                + "\nUnidad: " + unidad.getNumeroSerie()
                + (unidad.getNombreProducto() != null ? " (" + unidad.getNombreProducto() + ")" : "")
                + "\nTienda: " + tienda.getNombre()
                + "\nFecha: " + FormateadorTexto.fecha(fechaVenta));
        vista.limpiarFormulario();
        cargarListas();
    }
}