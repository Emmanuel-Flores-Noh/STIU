package mx.uady.inventario.cliente.controller;

import mx.uady.inventario.cliente.network.VentaService;
import mx.uady.inventario.cliente.util.PantallaTipo;

/**
 * Casos de uso sobre el producto de una unidad: consulta de su código QR y de su historial de precios.
 * Las consultas corren en segundo plano y los errores se informan con {@link PresentadorErrores}.
 */
public class ProductoController {

    private final VistaController vista;
    private final VentaService servicio;
    private final EjecutorAsincrono ejecutor;
    private final PresentadorErrores errores;

    /**
     * Crea el controlador.
     *
     * <p>Requisitos: RF-34, RF-36.</p>
     * @param vista    pantalla donde se muestran los resultados
     * @param servicio acceso al Sistema 1 a través del Broker
     * @param ejecutor ejecutor de trabajos en segundo plano
     */
    public ProductoController(VistaController vista, VentaService servicio, EjecutorAsincrono ejecutor) {
        this.vista = vista;
        this.servicio = servicio;
        this.ejecutor = ejecutor;
        this.errores = new PresentadorErrores(vista);
    }

    /**
     * Obtiene y muestra el código QR de una unidad. La imagen se pide codificada en Base64; si no se
     * puede mostrar, la ventana del QR lo indica.
     *
     * <p>Requisito: RF-34 (código QR en Base64 por doble clic).</p>
     * @param numeroSerie número de serie de la unidad; si está vacío no hace nada
     */
    public void consultarQr(String numeroSerie) {
        if (numeroSerie == null || numeroSerie.isBlank()) {
            return;
        }
        vista.mostrarIndicadorCarga("Obteniendo código QR de " + numeroSerie + "...", true);
        ejecutor.ejecutar(() -> servicio.obtenerQr(numeroSerie),
                qr -> {
                    vista.mostrarIndicadorCarga(PantallaTipo.ESTADO_LISTO.getMensaje(), false);
                    vista.mostrarQr(qr);
                },
                error -> {
                    vista.mostrarIndicadorCarga("No se pudo obtener el QR.", false);
                    errores.informar("No se pudo obtener el código QR.", error, () -> consultarQr(numeroSerie));
                });
    }

    /**
     * Obtiene y muestra el historial de precios de un producto.
     *
     * <p>Requisito: RF-36 (consulta de historial de precios).</p>
     * @param idProducto identificador del producto; si es nulo no hace nada
     */
    public void consultarHistorialPrecios(Integer idProducto) {
        if (idProducto == null) {
            return;
        }
        vista.mostrarIndicadorCarga("Consultando historial de precios...", true);
        ejecutor.ejecutar(() -> servicio.obtenerHistorialPrecios(idProducto),
                historial -> {
                    vista.mostrarIndicadorCarga(PantallaTipo.ESTADO_LISTO.getMensaje(), false);
                    vista.mostrarHistorialPrecios(historial);
                },
                error -> {
                    vista.mostrarIndicadorCarga("No se pudo obtener el historial.", false);
                    errores.informar("No se pudo obtener el historial de precios.", error,
                            () -> consultarHistorialPrecios(idProducto));
                });
    }
}
