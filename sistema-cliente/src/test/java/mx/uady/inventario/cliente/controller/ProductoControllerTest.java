package mx.uady.inventario.cliente.controller;

import mx.uady.inventario.cliente.error.ComunicacionError;
import mx.uady.inventario.cliente.error.ComunicacionException;
import mx.uady.inventario.cliente.model.PrecioDTO;
import mx.uady.inventario.cliente.model.PrecioHistorialDTO;
import mx.uady.inventario.cliente.model.QrDTO;
import mx.uady.inventario.cliente.util.PantallaTipo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link ProductoController}.
 * Valida la consulta asíncrona y visualización del código QR (RF-34) * y del historial de precios (RF-36). */class ProductoControllerTest {

    private VistaFalsa vista;
    private ServicioFalso servicio;
    private ProductoController controller;

    @BeforeEach
    void preparar() {
        vista = new VistaFalsa();
        servicio = new ServicioFalso();
        // Ejecutor síncrono para pruebas deterministas
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
        controller = new ProductoController(vista, servicio, ejecutorSincrono);
    }

    @Test
    @Tag("RF-34")
    @DisplayName("RF-34 · Caso Normal: Consulta y muestra código QR de la unidad seleccionada")
    void consultarQr_CasoNormal_MuestraQrExitoso() {
        servicio.qrRespuesta = new QrDTO("SN-100", 1, "Proyector Epson", "iVBORw0KGgoAAAANSUhEUg==");

        controller.consultarQr("SN-100");

        assertTrue(vista.eventos.contains("qr"), "Debe notificar a la vista para mostrar el QR");
        assertNotNull(vista.qrMostrado);
        assertEquals("SN-100", vista.qrMostrado.getNumeroSerie());
        assertEquals("Proyector Epson", vista.qrMostrado.getNombreProducto());
        assertFalse(vista.cargando, "El indicador de carga debe apagarse al terminar");
        assertTrue(vista.eventos.contains("estado:" + PantallaTipo.ESTADO_LISTO.getMensaje()));
    }

    @Test
    @Tag("RF-34")
    @DisplayName("RF-34 · Caso Límite: Consulta de QR con serie vacío o nulo no realiza peticiones")
    void consultarQr_CasoLimite_SerieVacioONulo() {
        controller.consultarQr("");
        controller.consultarQr(null);
        controller.consultarQr("   ");

        assertFalse(vista.eventos.contains("qr"));
        assertEquals(0, vista.mensajes.size());
    }

    @Test
    @Tag("RF-34")
    @DisplayName("RF-34 · Caso Error: Fallo de red al obtener QR informa error y ofrece reintentar")
    void consultarQr_CasoError_FalloRedInformaYOfreceReintento() {
        servicio.falloQr = new ComunicacionException(
                ComunicacionError.TIEMPO_AGOTADO,
                "Timeout obteniendo QR",
                null
        );

        controller.consultarQr("SN-100");

        assertFalse(vista.eventos.contains("qr"));
        assertTrue(vista.eventos.stream().anyMatch(e -> e.startsWith("reintentar:")),
                "Debe desplegar diálogo de reintento para fallos de red al consultar QR (RF-38)");
        assertNotNull(vista.reintentoOfrecido);
    }

    @Test
    @Tag("RF-36")
    @DisplayName("RF-36 · Caso Normal: Consulta y muestra historial cronológico de precios")
    void consultarHistorialPrecios_CasoNormal_MuestraHistorial() {
        servicio.historialRespuesta = new PrecioHistorialDTO(
                10,
                "Monitor Dell",
                new BigDecimal("3500.00"),
                List.of(
                        new PrecioDTO(new BigDecimal("3000.00"), "2025-01-01T12:00:00"),
                        new PrecioDTO(new BigDecimal("3300.00"), "2025-06-01T12:00:00")
                )
        );

        controller.consultarHistorialPrecios(10);

        assertTrue(vista.eventos.contains("historial"));
        assertNotNull(vista.historialMostrado);
        assertEquals(10, vista.historialMostrado.getIdProducto());
        assertEquals(2, vista.historialMostrado.getHistorialPrecios().size());
        assertEquals(new BigDecimal("3500.00"), vista.historialMostrado.getPrecioActual());
    }

    @Test
    @Tag("RF-36")
    @DisplayName("RF-36 · Caso Límite: Producto sin cambios de precio devuelve historial vacío")
    void consultarHistorialPrecios_CasoLimite_SinHistorialPrevio() {
        servicio.historialRespuesta = new PrecioHistorialDTO(
                20,
                "Teclado USB",
                new BigDecimal("250.00"),
                List.of()
        );

        controller.consultarHistorialPrecios(20);

        assertTrue(vista.eventos.contains("historial"));
        assertNotNull(vista.historialMostrado);
        assertTrue(vista.historialMostrado.getHistorialPrecios().isEmpty(), "Si nunca cambió de precio, la lista previa es vacía (RF-36)");
        assertEquals(new BigDecimal("250.00"), vista.historialMostrado.getPrecioActual());

        // id nulo no debe causar excepciones
        controller.consultarHistorialPrecios(null);
    }

    @Test
    @Tag("RF-36")
    @DisplayName("RF-36 · Caso Error: Fallo de red al consultar historial informa error al usuario")
    void consultarHistorialPrecios_CasoError_FalloRedInformaError() {
        servicio.falloHistorial = new ComunicacionException(
                ComunicacionError.TIEMPO_AGOTADO,
                "Conexión perdida",
                null
        );

        controller.consultarHistorialPrecios(30);

        assertNull(vista.historialMostrado);
        assertTrue(vista.eventos.stream().anyMatch(e -> e.startsWith("reintentar:")),
                "Fallo de comunicación en historial de precios debe ofrecer reintento (RF-38)");
    }
}