package mx.uady.inventario.central.view;

import static mx.uady.inventario.central.view.MercanciaPanelTest.buscar;
import static mx.uady.inventario.central.view.MercanciaPanelTest.enEdt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import mx.uady.inventario.central.model.VentaHistorial;
import mx.uady.inventario.common.modelo.EstadoMercancia;
import mx.uady.inventario.common.modelo.Mercancia;
import mx.uady.inventario.common.modelo.Producto;

/** Comportamiento común de las tablas: orden inicial, selección y doble clic. */
class TablasPanelesTest {

    private static final Producto LAPTOP = new Producto(1, "Laptop", null, BigDecimal.TEN);
    private static final Mercancia SN_001 = new Mercancia("SN-001", 1, EstadoMercancia.EN_ALMACEN, null);
    private static final Mercancia SN_002 = new Mercancia("SN-002", 1, EstadoMercancia.EN_ALMACEN, null);

    @Test
    @Tag("RF-09")
    @Tag("RF-11")
    @DisplayName("RF-09, RF-11 · Mercancía ordena por serie, activa Ver QR al seleccionar y lo abre con doble clic")
    void mercanciaOrdenaPorSerieYAbreElQrConDobleClic() throws Exception {
        AtomicInteger verQr = new AtomicInteger();
        enEdt(() -> {
            MercanciaPanel panel = new MercanciaPanel();
            panel.alVerQr(verQr::incrementAndGet);
            panel.mostrarProductos(List.of(LAPTOP));
            panel.mostrarUnidades(List.of(SN_002, SN_001), List.of(LAPTOP));
            JTable tabla = buscar(panel, "tablaUnidades", JTable.class);
            JButton boton = buscar(panel, "verQr", JButton.class);

            assertConfiguracionComun(tabla);
            assertEquals("SN-001", tabla.getValueAt(0, MercanciaTableModel.COL_SERIE));
            assertFalse(boton.isEnabled());

            dobleClic(tabla, 0);
            assertEquals(0, verQr.get(), "sin selección el doble clic no abre el QR");

            tabla.setRowSelectionInterval(1, 1);
            assertTrue(boton.isEnabled());
            assertEquals("SN-002", panel.seleccionada().orElseThrow().numeroSerie());
            clic(tabla, 1, 1);
            assertEquals(0, verQr.get(), "un solo clic no abre el QR");
            dobleClic(tabla, 1);
            assertEquals(1, verQr.get());

            tabla.clearSelection();
            assertFalse(boton.isEnabled());
            assertFalse(panel.seleccionarUnidad("SN-999"));
        });
    }

    @Test
    @Tag("RF-15")
    @DisplayName("RF-15 · Ventas ordena por serie, selecciona por serie e ignora series inexistentes")
    void ventasOrdenaPorSerieYSeleccionaPorSerie() throws Exception {
        enEdt(() -> {
            VentaPanel panel = new VentaPanel();
            panel.mostrarMercancias(List.of(SN_002, SN_001), Map.of(1, "Laptop"));
            JTable tabla = buscar(panel, "tablaVentas", JTable.class);

            assertConfiguracionComun(tabla);
            assertEquals("SN-001", tabla.getValueAt(0, VentaTableModel.COL_SERIE));
            assertTrue(panel.seleccionada().isEmpty());

            assertTrue(panel.seleccionarMercancia("SN-002"));
            assertEquals(1, tabla.getSelectedRow());
            assertEquals("SN-002", panel.seleccionada().orElseThrow().numeroSerie());
            assertFalse(panel.seleccionarMercancia("SN-999"));
        });
    }

    @Test
    @Tag("RF-14")
    @DisplayName("RF-14 · El historial conserva el orden recibido, activa Abrir QR y lo abre con doble clic")
    void historialConservaElOrdenYAbreElQrConDobleClic() throws Exception {
        AtomicInteger abrirQr = new AtomicInteger();
        VentaHistorial reciente = new VentaHistorial(2, "SN-002", "Laptop", "Centro", "Ana",
                LocalDateTime.of(2026, 2, 1, 9, 0), 1);
        VentaHistorial antigua = new VentaHistorial(1, "SN-001", "Laptop", "Centro", "Ana",
                LocalDateTime.of(2026, 1, 1, 9, 0), 1);
        enEdt(() -> {
            VentaHistorialPanel panel = new VentaHistorialPanel();
            panel.alAbrirQr(abrirQr::incrementAndGet);
            panel.mostrarVentas(List.of(reciente, antigua));
            JTable tabla = buscar(panel, "tablaHistorialVentas", JTable.class);
            JButton boton = buscar(panel, "abrirQrVenta", JButton.class);

            assertConfiguracionComun(tabla);
            assertEquals("SN-002", tabla.getValueAt(0, VentaHistorialTableModel.COL_SERIE));
            assertFalse(boton.isEnabled());

            dobleClic(tabla, 0);
            assertEquals(0, abrirQr.get());

            tabla.setRowSelectionInterval(1, 1);
            assertTrue(boton.isEnabled());
            assertEquals(1, panel.seleccionada().orElseThrow().idVenta());
            dobleClic(tabla, 1);
            assertEquals(1, abrirQr.get());
        });
    }

    private static void assertConfiguracionComun(JTable tabla) {
        assertEquals(ListSelectionModel.SINGLE_SELECTION, tabla.getSelectionModel().getSelectionMode());
        assertEquals(26, tabla.getRowHeight());
        assertTrue(tabla.getFillsViewportHeight());
    }

    private static void dobleClic(JTable tabla, int fila) {
        clic(tabla, fila, 2);
    }

    private static void clic(JTable tabla, int fila, int clics) {
        int y = fila * tabla.getRowHeight() + tabla.getRowHeight() / 2;
        tabla.dispatchEvent(new MouseEvent(tabla, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0,
                5, y, clics, false, MouseEvent.BUTTON1));
    }
}
