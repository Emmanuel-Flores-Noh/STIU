package mx.uady.inventario.cliente.controller;


import mx.uady.inventario.cliente.model.MercanciaDTO;
import mx.uady.inventario.cliente.model.TiendaDTO;
import mx.uady.inventario.cliente.model.UsuarioDTO;
import mx.uady.inventario.cliente.network.VentaService;
import mx.uady.inventario.cliente.util.FormateadorTexto;
import mx.uady.inventario.cliente.util.PantallaTipo;

import java.util.concurrent.atomic.AtomicBoolean;
import mx.uady.inventario.cliente.util.ConfiguracionNetwork;
import mx.uady.inventario.cliente.util.TraductorErrores;
import mx.uady.inventario.cliente.view.RegistroVentaFrame;
import mx.uady.inventario.common.contrato.EstadoRespuesta;
import mx.uady.inventario.common.contrato.Respuesta;
import mx.uady.inventario.common.contrato.Solicitud;
import mx.uady.inventario.common.json.GsonFactory;
import mx.uady.inventario.common.json.LocalDateTimeAdapter;
import mx.uady.inventario.common.servicio.Operaciones;
import mx.uady.inventario.common.servicio.Servicios;

import javax.swing.*;
import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Controlador encargado de coordinar la lógica de negocio para el registro de ventas
 * y la comunicación asíncrona con el Broker de servicios.
 *
 * <p>Trazabilidad con Requisitos:
 * <ul>
 *   <li><b>RF-25:</b> Carga en segundo plano de listas de tiendas, usuarios proveedores e inventario disponible al iniciar la vista.</li>
 *   <li><b>RF-26:</b> Actualización de la tabla con unidades disponibles en almacén.</li>
 *   <li><b>RF-28:</b> Poblado del selector de tiendas destino.</li>
 *   <li><b>RF-29:</b> Poblado del selector de encargados de venta (usuarios proveedores).</li>
 *   <li><b>RF-30:</b> Recarga manual de catálogos e inventario con indicador visual de carga.</li>
 *   <li><b>RF-31:</b> Habilitación y validación estricta de que exista unidad, tienda y usuario antes de permitir la venta.</li>
 *   <li><b>RF-32:</b> Registro de la venta con actualización de vista, limpieza de campos y recarga de listas.</li>
 *   <li><b>RF-33:</b> Notificación clara cuando el servidor rechaza la venta sin reintento automático.</li>
 *   <li><b>RF-34:</b> Delegación de la consulta del código QR de la unidad seleccionada.</li>
 *   <li><b>RF-36:</b> Delegación de la consulta del historial de precios del producto.</li>
 *   <li><b>RF-37:</b> Presentación individualizada y entendible de fallos de red o servidor.</li>
 *   <li><b>RF-38:</b> Opción de reintento controlado sin retransmisión automática espontánea.</li>
 * </ul>
 * </p>
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
     * <p>Requisitos: RF-25, RF-31, RF-32, RF-34, RF-36.</p>
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
     * <p>Requisitos: RF-25, RF-31, RF-32, RF-34, RF-36.</p>
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
     * RF-31: una venta solo puede enviarse con una unidad, una tienda y un usuario elegidos.
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
    /**
     * Obtiene la fecha de venta de la confirmación del servidor con formato legible.
     *
     * @param resultado resultado de {@code actualizarTiendaVenta}
     * @return fecha en formato {@code dd/MM/yyyy HH:mm}, el texto original si no se puede
     *         interpretar, o "N/D" si no viene
     */
    private static String extraerFecha(JsonElement resultado) {
        if (resultado == null || !resultado.isJsonObject() || !resultado.getAsJsonObject().has("fecha_venta")) {
            return "N/D";
        }
        String texto = resultado.getAsJsonObject().get("fecha_venta").getAsString();
        try {
            return LocalDateTimeAdapter.parsear(texto).format(FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            return texto;
        }
    }
}