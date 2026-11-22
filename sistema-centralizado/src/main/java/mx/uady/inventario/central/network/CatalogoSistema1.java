package mx.uady.inventario.central.network;

import mx.uady.inventario.central.service.ConsultasVentaService;
import mx.uady.inventario.central.service.MercanciaService;
import mx.uady.inventario.central.service.ProductoService;
import mx.uady.inventario.central.service.QrService;
import mx.uady.inventario.central.service.VentaService;
import mx.uady.inventario.common.servicio.Operaciones;
import mx.uady.inventario.common.servicio.Servicios;

/**
 * Catálogo de operaciones que el Sistema 1 expone por red.
 *
 * <p>Conecta cada operación del contrato con el manejador que la atiende, usando los servicios
 * de negocio del Sistema 1. Así {@link DespachadorSolicitudes} solo mapea operación → manejador
 * y no conoce la capa de servicios.</p>
 */
final class CatalogoSistema1 {

    private CatalogoSistema1() {
    }

    /**
     * Crea el despachador con el catálogo completo y conecta cada operación cuyo servicio
     * se proporcione. Un servicio nulo deja sus operaciones declaradas pero pendientes.
     *
     * @param ventaService     registra ventas ({@code actualizarTiendaVenta})
     * @param consultasVenta   catálogos de tiendas y usuarios ({@code listarTiendas}, {@code listarUsuarios})
     * @param mercanciaService unidades físicas ({@code listarInventario}, {@code obtenerQr})
     * @param productoService  productos; necesario para nombres y precios
     *                         ({@code listarInventario}, {@code obtenerQr}, {@code listarPreciosProducto})
     * @param qrService        lectura de los PNG ({@code obtenerQr})
     * @return despachador con el catálogo estándar
     */
    static DespachadorSolicitudes crear(
            VentaService ventaService,
            ConsultasVentaService consultasVenta,
            MercanciaService mercanciaService,
            ProductoService productoService,
            QrService qrService) {

        DespachadorSolicitudes despachador = new DespachadorSolicitudes();
        despachador.registrarServicio(Servicios.GESTOR_TIENDAS,
                Operaciones.LISTAR_TIENDAS, Operaciones.ACTUALIZAR_TIENDA_VENTA, Operaciones.LISTAR_USUARIOS);
        despachador.registrarServicio(Servicios.INVENTARIO,
                Operaciones.LISTAR_INVENTARIO, Operaciones.OBTENER_QR);
        despachador.registrarServicio(Servicios.GESTOR_PRECIOS,
                Operaciones.LISTAR_PRECIOS_PRODUCTO);

        if (ventaService != null) {
            despachador.registrarOperacion(Servicios.GESTOR_TIENDAS,
                    Operaciones.ACTUALIZAR_TIENDA_VENTA, ventaService::registrarVenta);
        }
        if (consultasVenta != null) {
            despachador.registrarOperacion(Servicios.GESTOR_TIENDAS,
                    Operaciones.LISTAR_TIENDAS, ManejadoresConsulta.listarTiendas(consultasVenta));
            despachador.registrarOperacion(Servicios.GESTOR_TIENDAS,
                    Operaciones.LISTAR_USUARIOS, ManejadoresConsulta.listarUsuariosProveedores(consultasVenta));
        }
        if (mercanciaService != null && productoService != null) {
            despachador.registrarOperacion(Servicios.INVENTARIO, Operaciones.LISTAR_INVENTARIO,
                    ManejadoresConsulta.listarInventarioDisponible(mercanciaService, productoService));
            if (qrService != null) {
                despachador.registrarOperacion(Servicios.INVENTARIO, Operaciones.OBTENER_QR,
                        ManejadoresConsulta.obtenerQr(mercanciaService, productoService, qrService));
            }
        }
        if (productoService != null) {
            despachador.registrarOperacion(Servicios.GESTOR_PRECIOS, Operaciones.LISTAR_PRECIOS_PRODUCTO,
                    ManejadoresConsulta.listarPreciosProducto(productoService));
        }
        return despachador;
    }
}
