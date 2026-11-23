package mx.uady.inventario.central.view;

import static mx.uady.inventario.central.view.MercanciaPanelTest.buscar;
import static mx.uady.inventario.central.view.MercanciaPanelTest.enEdt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import mx.uady.inventario.common.modelo.EstadoMercancia;
import mx.uady.inventario.common.modelo.Producto;

/** Barra de filtros de Mercancía: producto, estado, búsqueda por serie (RF-09) y Ver QR (RF-11). */
class FiltrosMercanciaBarTest {

    private static final Producto LAPTOP = new Producto(1, "Laptop", null, BigDecimal.TEN);
    private static final Producto SILLA = new Producto(2, "Silla", null, BigDecimal.ONE);

    @Test
    @Tag("RF-09")
    @DisplayName("RF-09 · Elegir producto o estado avisa una vez y devuelve el valor elegido")
    void elegirProductoOEstadoAvisaYDevuelveElValor() throws Exception {
        AtomicInteger cambios = new AtomicInteger();
        enEdt(() -> {
            FiltrosMercanciaBar barra = new FiltrosMercanciaBar();
            barra.mostrarProductos(List.of(LAPTOP, SILLA));
            barra.alCambiarFiltros(cambios::incrementAndGet);

            buscar(barra, "filtroProducto", JComboBox.class).setSelectedItem(SILLA);
            assertEquals(1, cambios.get());
            assertEquals(2, barra.productoSeleccionado());

            buscar(barra, "filtroEstado", JComboBox.class).setSelectedItem(EstadoMercancia.EN_ALMACEN);
            assertEquals(2, cambios.get());
            assertEquals(EstadoMercancia.EN_ALMACEN, barra.estadoSeleccionado());
            assertTrue(barra.hayFiltros());

            buscar(barra, "actualizar", JButton.class).doClick();
            assertEquals(3, cambios.get());
        });
    }

    @Test
    @Tag("RF-09")
    @DisplayName("RF-09 · Todos equivale a sin filtro y recargar productos conserva la elección sin avisar")
    void todosEsSinFiltroYRecargarNoAvisa() throws Exception {
        AtomicInteger cambios = new AtomicInteger();
        enEdt(() -> {
            FiltrosMercanciaBar barra = new FiltrosMercanciaBar();
            barra.alCambiarFiltros(cambios::incrementAndGet);
            assertNull(barra.productoSeleccionado());
            assertNull(barra.estadoSeleccionado());
            assertFalse(barra.hayFiltros());

            barra.mostrarProductos(List.of(LAPTOP, SILLA));
            buscar(barra, "filtroProducto", JComboBox.class).setSelectedItem(SILLA);
            int antes = cambios.get();

            barra.mostrarProductos(List.of(SILLA, LAPTOP, new Producto(3, "Proyector", null, BigDecimal.ONE)));
            assertEquals(2, barra.productoSeleccionado());
            assertEquals(antes, cambios.get(), "rellenar por código no recarga");

            barra.mostrarProductos(List.of(LAPTOP));
            assertNull(barra.productoSeleccionado(), "producto eliminado: vuelve a Todos");
            assertEquals(FiltrosMercanciaBar.TODOS, buscar(barra, "filtroProducto", JComboBox.class).getSelectedItem());
            assertEquals(antes, cambios.get());

            barra.mostrarProductos(List.of());
            assertEquals(1, buscar(barra, "filtroProducto", JComboBox.class).getItemCount(), "solo queda Todos");
        });
    }

    @Test
    @Tag("RF-09")
    @Tag("RF-11")
    @DisplayName("RF-09, RF-11 · La búsqueda solo avisa a alBuscar y Ver QR empieza deshabilitado")
    void laBusquedaSoloAvisaABuscarYVerQrEmpiezaDeshabilitado() throws Exception {
        AtomicInteger cambios = new AtomicInteger();
        AtomicInteger busquedas = new AtomicInteger();
        AtomicInteger verQr = new AtomicInteger();
        enEdt(() -> {
            FiltrosMercanciaBar barra = new FiltrosMercanciaBar();
            barra.alCambiarFiltros(cambios::incrementAndGet);
            barra.alBuscar(busquedas::incrementAndGet);
            barra.alVerQr(verQr::incrementAndGet);
            JButton boton = buscar(barra, "verQr", JButton.class);

            buscar(barra, "busquedaSerie", JTextField.class).setText("SN-01");
            assertTrue(busquedas.get() > 0);
            assertEquals(0, cambios.get());
            assertEquals("SN-01", barra.textoBusqueda());

            assertFalse(barra.verQrHabilitado());
            boton.doClick();
            assertEquals(0, verQr.get(), "deshabilitado no abre el QR");

            barra.setVerQrHabilitado(true);
            assertTrue(boton.isEnabled());
            boton.doClick();
            assertEquals(1, verQr.get());
        });
    }

    @Test
    @Tag("RF-09")
    @DisplayName("RF-09 · Rechaza acciones nulas")
    void rechazaAccionesNulas() throws Exception {
        enEdt(() -> {
            FiltrosMercanciaBar barra = new FiltrosMercanciaBar();
            assertThrows(NullPointerException.class, () -> barra.alCambiarFiltros(null));
            assertThrows(NullPointerException.class, () -> barra.alBuscar(null));
            assertThrows(NullPointerException.class, () -> barra.alVerQr(null));
        });
    }
}
