package mx.uady.inventario.cliente.controller;

import mx.uady.inventario.cliente.error.ComunicacionError;
import mx.uady.inventario.cliente.error.ComunicacionException;
import mx.uady.inventario.cliente.error.ErrorServicioException;
import mx.uady.inventario.cliente.model.MercanciaDTO;
import mx.uady.inventario.cliente.model.TiendaDTO;
import mx.uady.inventario.cliente.model.UsuarioDTO;
import mx.uady.inventario.cliente.util.PantallaTipo;
import mx.uady.inventario.common.error.CodigoError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de pruebas unitarias para {@link RegistroVentaController}.
 * Valida la orquestación de la carga de listas (RF-25, RF-30), validación de venta completa (RF-31),
 * registro de ventas exitoso (RF-32), rechazo sin reintento (RF-33) y manejo de reintentos (RF-38).
 */
class RegistroVentaControllerTest {

    private VistaFalsa vista;
    private ServicioFalso servicio;
    private RegistroVentaController controlador;

    private final MercanciaDTO LAPTOP = new MercanciaDTO("SN-001", 101, "Laptop Lenovo", "EN_ALMACEN");
    private final TiendaDTO TIENDA = new TiendaDTO(1L, "Tienda Central UADY", "Calle 60");
    private final UsuarioDTO USUARIO = new UsuarioDTO(1L, "Carlos", "Pérez");

    @BeforeEach
    void preparar() {
        vista = new VistaFalsa();
        servicio = new ServicioFalso();
        EjecutorAsincrono ejecutorSincrono = new EjecutorAsincrono() {
            @Override
            public <T> void ejecutar(
                    Callable<T> trabajo,
                    Consumer<T> alTerminar,
                    Consumer<Throwable> alFallar) {
                try {
                    alTerminar.accept(trabajo.call());
                } catch (Throwable t) {
                    alFallar.accept(t);
                }
            }
        };
        controlador = new RegistroVentaController(vista, servicio, ejecutorSincrono);
    }

    @Test
    @Tag("RF-25")
    @DisplayName("RF-25 · Caso Normal: Carga de listas e inventario al abrir la ventana")
    void cargarListas_CasoNormal_CargaExitosa() {
        controlador.cargarListas();

        assertTrue(vista.eventos.contains("estado:" + PantallaTipo.ESTADO_CARGANDO.getMensaje()));
        assertTrue(vista.eventos.contains("combos"), "Debe actualizar selectores de tiendas y usuarios");
        assertTrue(vista.eventos.contains("tabla:1"), "Debe poblar la tabla con el inventario disponible");
        assertTrue(vista.eventos.contains("estado:" + PantallaTipo.ESTADO_LISTO.getMensaje()));
        assertEquals(1, vista.ultimasTiendas.size());
        assertEquals(1, vista.ultimosUsuarios.size());
        assertEquals(1, vista.ultimaMercancia.size());
    }

    @Test
    @Tag("RF-25")
    @DisplayName("RF-25 · Caso Error: Fallo al cargar listas muestra mensaje de error y ofrece reintentar")
    void cargarListas_CasoError_FalloAlCargar() {
        servicio.falloCatalogos = new ComunicacionException(
                ComunicacionError.SIN_CONEXION,
                "Sin conexión al Broker",
                null
        );

        controlador.cargarListas();

        assertTrue(vista.eventos.contains("estado:" + PantallaTipo.ERROR_LISTAS.getMensaje()));
        assertTrue(vista.eventos.stream().anyMatch(e -> e.startsWith("reintentar:")));
        assertNotNull(vista.reintentoOfrecido);
    }

    @Test
    @Tag("RF-30")
    @DisplayName("RF-30 · Caso Normal: Recarga manual de tiendas, usuarios e inventario a petición del usuario")
    void cargarListas_CasoNormal_RecargaManual() {
        // Primera carga
        controlador.cargarListas();
        int eventosIniciales = vista.eventos.size();

        // Segunda carga (simulando clic en Actualizar Listas)
        servicio.inventario = List.of(
                LAPTOP,
                new MercanciaDTO("SN-002", 102, "Proyector Epson", "EN_ALMACEN")
        );
        controlador.cargarListas();

        assertTrue(vista.eventos.size() > eventosIniciales);
        assertTrue(vista.eventos.contains("tabla:2"), "La tabla debe reflejar el inventario actualizado");
    }

    @Test
    @Tag("RF-31")
    @DisplayName("RF-31 · Caso Normal: esVentaCompleta retorna true cuando unidad, tienda y usuario están seleccionados")
    void esVentaCompleta_CasoNormal_TodosLosDatosPresentes() {
        assertTrue(RegistroVentaController.esVentaCompleta(LAPTOP, TIENDA, USUARIO));
    }

    @Test
    @Tag("RF-31")
    @DisplayName("RF-31 · Caso Límite: esVentaCompleta retorna false si falta cualquiera de los tres elementos")
    void esVentaCompleta_CasoLimite_FaltaAlgunDato() {
        assertFalse(RegistroVentaController.esVentaCompleta(null, TIENDA, USUARIO), "Sin unidad debe ser false");
        assertFalse(RegistroVentaController.esVentaCompleta(new MercanciaDTO("", 1, "P", "EN_ALMACEN"), TIENDA, USUARIO));
        assertFalse(RegistroVentaController.esVentaCompleta(LAPTOP, null, USUARIO), "Sin tienda debe ser false");
        assertFalse(RegistroVentaController.esVentaCompleta(LAPTOP, new TiendaDTO(null, "T", "D"), USUARIO));
        assertFalse(RegistroVentaController.esVentaCompleta(LAPTOP, TIENDA, null), "Sin usuario debe ser false");
        assertFalse(RegistroVentaController.esVentaCompleta(LAPTOP, TIENDA, new UsuarioDTO(null, "U", "P")));
    }

    @Test
    @Tag("RF-31")
    @DisplayName("RF-31 · Caso Error: Intento de registrar venta incompleta es rechazado con mensaje de advertencia")
    void registrarVenta_CasoError_DatosIncompletosRechazaEnvio() {
        controlador.registrarVenta(null, TIENDA, USUARIO);

        assertEquals(0, servicio.ventas.get(), "No debe invocar el servicio si los datos están incompletos");
        assertTrue(vista.eventos.contains("error"));
        assertTrue(vista.mensajes.contains(PantallaTipo.ERROR_DATOS_INCOMPLETOS.getMensaje()));
    }

    @Test
    @Tag("RF-32")
    @DisplayName("RF-32 · Caso Normal: Venta exitosa limpia formulario, muestra confirmación y recarga listas")
    void registrarVenta_CasoNormal_VentaExitosa() {
        controlador.registrarVenta(LAPTOP, TIENDA, USUARIO);

        assertEquals(1, servicio.ventas.get());
        assertTrue(vista.eventos.contains("exito"));
        assertTrue(vista.eventos.contains("limpiar"), "Debe limpiar el formulario tras venta exitosa (RF-32)");
        assertTrue(vista.eventos.contains("combos"), "Debe recargar las listas tras venta exitosa");

        String mensajeExito = vista.mensajes.stream()
                .filter(m -> m.contains("Venta registrada con éxito"))
                .findFirst()
                .orElse("");
        assertTrue(mensajeExito.contains("SN-001"));
        assertTrue(mensajeExito.contains("Tienda Central UADY"));
    }

    @Test
    @Tag("RF-33")
    @DisplayName("RF-33 · Caso Error: Servidor rechaza venta (PARAMETROS_INVALIDOS) e informa causa sin ofrecer reintento")
    void registrarVenta_CasoError_ServidorRechazaVentaSinReintento() {
        servicio.falloVenta = new ErrorServicioException(
                CodigoError.PARAMETROS_INVALIDOS,
                "La mercancía con serie 'SN-001' ya fue vendida"
        );

        controlador.registrarVenta(LAPTOP, TIENDA, USUARIO);

        assertTrue(vista.eventos.contains("error"));
        assertFalse(vista.eventos.stream().anyMatch(e -> e.startsWith("reintentar:")),
                "No debe ofrecerse reintento ante PARAMETROS_INVALIDOS (RF-33)");
        assertTrue(vista.mensajes.stream().anyMatch(m -> m.contains("ya fue vendida")));
    }

    @Test
    @Tag("RF-38")
    @DisplayName("RF-38 · Caso Error: Fallo de red al registrar venta ofrece reintento sin reenviar automáticamente")
    void registrarVenta_CasoError_FalloRedOfreceReintentoManual() {
        servicio.falloVenta = new ComunicacionException(
                ComunicacionError.TIEMPO_AGOTADO,
                "Conexión perdida",
                null
        );

        controlador.registrarVenta(LAPTOP, TIENDA, USUARIO);

        assertEquals(1, servicio.ventas.get(), "No debe retransmitir automáticamente");
        assertTrue(vista.eventos.stream().anyMatch(e -> e.startsWith("reintentar:")),
                "Fallo de red en venta debe ofrecer diálogo interactivo de reintento (RF-38)");
        assertNotNull(vista.reintentoOfrecido);
    }
}