package mx.uady.inventario.central.service.memoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import mx.uady.inventario.central.model.ProductoConUnidades;
import mx.uady.inventario.central.service.NegocioException;
import mx.uady.inventario.central.service.ValidacionException;
import mx.uady.inventario.common.modelo.HistorialPrecio;
import mx.uady.inventario.common.modelo.Producto;

class ProductoServiceMemoriaTest {

    private AlmacenMemoria almacen;
    private ProductoServiceMemoria servicio;

    @BeforeEach
    void preparar() {
        almacen = AlmacenMemoria.conDatosDeEjemplo(Duration.ZERO);
        servicio = new ProductoServiceMemoria(almacen);
    }

    @Test
    @Tag("RF-04")
    @DisplayName("RF-04 · Lista los productos del seed ordenados con sus unidades")
    void listaLosProductosDelSeedOrdenadosConSusUnidades() {
        List<ProductoConUnidades> lista = servicio.listarConUnidades();

        assertEquals(5, lista.size());
        assertEquals("Impresora laser HP", lista.get(0).producto().nombre());
        assertTrue(lista.stream().allMatch(fila -> fila.unidades() == 2));
    }

    @Test
    @Tag("RF-01")
    @DisplayName("RF-01 · Crea un producto nuevo con id asignado y sin unidades")
    void creaUnProductoNuevoConIdAsignadoYSinUnidades() {
        Producto nuevo = servicio.guardar(new Producto(0, "  Monitor Dell  ", "  ", new BigDecimal("3200")));

        assertEquals(6, nuevo.idProducto());
        assertEquals("Monitor Dell", nuevo.nombre());
        assertNull(nuevo.descripcion());
        assertTrue(servicio.listarConUnidades().stream()
                .anyMatch(f -> f.producto().idProducto() == 6 && f.unidades() == 0));
    }

    @Test
    @Tag("RF-03")
    @DisplayName("RF-03 · Al cambiar el precio guarda el anterior en el historial")
    void alCambiarElPrecioGuardaElAnteriorEnElHistorial() {
        servicio.guardar(new Producto(3, "Silla ergonomica", "Silla", new BigDecimal("3999.00")));

        List<HistorialPrecio> historial = almacen.historial(3);
        assertEquals(1, historial.size());
        assertEquals(0, new BigDecimal("3499.00").compareTo(historial.get(0).precioHistorico()));
    }

    @Test
    @Tag("RF-03")
    @DisplayName("RF-03 · Sin cambio de precio no agrega historial")
    void sinCambioDePrecioNoAgregaHistorial() {
        servicio.guardar(new Producto(3, "Silla renombrada", "Silla", new BigDecimal("3499")));

        assertTrue(almacen.historial(3).isEmpty());
    }

    @Test
    @Tag("RF-01")
    @DisplayName("RF-01 · Rechaza datos inválidos sin modificar nada")
    void rechazaDatosInvalidosSinModificarNada() {
        assertThrows(ValidacionException.class,
                () -> servicio.guardar(new Producto(3, "Silla", null, BigDecimal.ZERO)));
        assertTrue(almacen.historial(3).isEmpty());
    }

    @Test
    @Tag("RF-02")
    @DisplayName("RF-02 · Rechaza editar un producto inexistente")
    void rechazaEditarUnProductoInexistente() {
        assertThrows(NegocioException.class,
                () -> servicio.guardar(new Producto(99, "Fantasma", null, BigDecimal.ONE)));
    }

    @Test
    @Tag("RF-03")
    @DisplayName("RF-03 · Cada cambio de precio agrega un registro con el precio anterior")
    void cadaCambioDePrecioAgregaUnRegistroConElPrecioAnterior() {
        servicio.guardar(new Producto(3, "Silla ergonomica", "Silla", new BigDecimal("3600.00")));
        servicio.guardar(new Producto(3, "Silla ergonomica", "Silla", new BigDecimal("3700.00")));
        servicio.guardar(new Producto(3, "Silla ergonomica", "Silla", new BigDecimal("3800.00")));

        List<BigDecimal> anteriores = almacen.historial(3).stream().map(HistorialPrecio::precioHistorico).toList();
        assertEquals(3, anteriores.size());
        assertEquals(0, new BigDecimal("3499.00").compareTo(anteriores.get(0)));
        assertEquals(0, new BigDecimal("3600.00").compareTo(anteriores.get(1)));
        assertEquals(0, new BigDecimal("3700.00").compareTo(anteriores.get(2)));
    }

    @Test
    @Tag("RF-03")
    @DisplayName("RF-03 · El mismo precio con otra escala no se considera un cambio")
    void elMismoPrecioConOtraEscalaNoSeConsideraUnCambio() {
        servicio.guardar(new Producto(3, "Silla ergonomica", "Silla", new BigDecimal("3499.0000")));
        servicio.guardar(new Producto(3, "Silla ergonomica", "Silla", new BigDecimal("3499")));

        assertTrue(almacen.historial(3).isEmpty());
    }

    @Test
    @Tag("RF-03")
    @DisplayName("RF-03 · Cambiar solo la descripción no crea registro en el historial")
    void cambiarSoloLaDescripcionNoCreaRegistroEnElHistorial() {
        servicio.guardar(new Producto(3, "Silla ergonomica", "Nueva descripción", new BigDecimal("3499.00")));

        assertTrue(almacen.historial(3).isEmpty());
    }

    @Test
    @Tag("RF-03")
    @DisplayName("RF-03 · Un cambio de precio inválido no agrega historial ni cambia el precio")
    void unCambioDePrecioInvalidoNoAgregaHistorialNiCambiaElPrecio() {
        assertThrows(ValidacionException.class,
                () -> servicio.guardar(new Producto(3, "Silla ergonomica", "Silla", new BigDecimal("-5"))));

        assertTrue(almacen.historial(3).isEmpty());
        assertTrue(servicio.listarConUnidades().stream()
                .anyMatch(f -> f.producto().idProducto() == 3
                        && new BigDecimal("3499.00").compareTo(f.producto().precioActual()) == 0));
    }
}
