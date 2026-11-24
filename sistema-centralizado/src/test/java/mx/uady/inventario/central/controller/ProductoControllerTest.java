package mx.uady.inventario.central.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import mx.uady.inventario.central.service.memoria.AlmacenMemoria;
import mx.uady.inventario.central.service.memoria.ProductoServiceMemoria;
import mx.uady.inventario.central.view.ProductosPanel;

class ProductoControllerTest {

    @Test
    @Tag("RF-04")
    @Tag("RNF-12")
    @DisplayName("RF-04, RNF-12 · Cargar llena la tabla con los productos del servicio")
    void cargarLlenaLaTablaConLosProductosDelServicio() throws Exception {
        ProductosPanel panel = new ProductosPanel();
        ProductoServiceMemoria servicio = new ProductoServiceMemoria(AlmacenMemoria.conDatosDeEjemplo(Duration.ZERO));

        SwingUtilities.invokeAndWait(() -> new ProductoController(servicio, panel).cargar());

        JTable tabla = tablaDe(panel);
        AtomicInteger filas = new AtomicInteger();
        long limite = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        do {
            SwingUtilities.invokeAndWait(() -> filas.set(tabla.getRowCount()));
        } while (filas.get() == 0 && System.nanoTime() < limite);

        assertEquals(5, filas.get());
        SwingUtilities.invokeAndWait(() -> assertTrue(panel.controles()[0].isEnabled()));
    }

    private static JTable tablaDe(ProductosPanel panel) {
        for (var componente : panel.getComponents()) {
            if (componente instanceof JScrollPane scroll) {
                JViewport viewport = scroll.getViewport();
                return (JTable) viewport.getView();
            }
        }
        throw new AssertionError("El panel no contiene una tabla");
    }
}
