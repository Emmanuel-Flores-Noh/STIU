package mx.uady.inventario.cliente.view.components;

import mx.uady.inventario.cliente.model.PrecioDTO;
import mx.uady.inventario.cliente.model.PrecioHistorialDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la visualización del historial de precios (RF-36).
 */
class HistorialPrecioTest {

    @Test
    @Tag("RF-36")
    @DisplayName("RF-36 · Caso Normal: Historial con cambios cronológicos previos")
    void historialPrecios_CasoNormal_MuestraPreciosAnteriores() throws Exception {
        PrecioDTO p1 = new PrecioDTO(new BigDecimal("12000.00"), "2025-01-15T09:00:00");
        PrecioDTO p2 = new PrecioDTO(new BigDecimal("13500.00"), "2025-06-20T14:30:00");
        PrecioHistorialDTO dto = new PrecioHistorialDTO(1, "Laptop HP", new BigDecimal("15000.00"), List.of(p1, p2));

        assertEquals("Laptop HP", dto.getNombre());
        assertEquals(new BigDecimal("15000.00"), dto.getPrecioActual());
        assertEquals(2, dto.getHistorialPrecios().size());
        assertEquals(new BigDecimal("12000.00"), dto.getHistorialPrecios().get(0).getPrecio());
        assertEquals(new BigDecimal("13500.00"), dto.getHistorialPrecios().get(1).getPrecio());

        // Verificación en EDT de la ventana modal si hay entorno con pantalla
        if (!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeAndWait(() -> {
                HistorialPrecioDialog dialog = new HistorialPrecioDialog(null, dto);
                assertNotNull(dialog);
                dialog.dispose();
            });
        }
    }

    @Test
    @Tag("RF-36")
    @DisplayName("RF-36 · Caso Límite: Producto que nunca ha cambiado de precio muestra lista vacía")
    void historialPrecios_CasoLimite_SinHistorialPrevio() throws Exception {
        PrecioHistorialDTO dtoSinCambios = new PrecioHistorialDTO(2, "Mouse Óptico", new BigDecimal("150.00"), List.of());

        assertEquals(new BigDecimal("150.00"), dtoSinCambios.getPrecioActual());
        assertNotNull(dtoSinCambios.getHistorialPrecios());
        assertTrue(dtoSinCambios.getHistorialPrecios().isEmpty(),
                "Si nunca cambió de precio, el historial debe ser una lista vacía (RF-36)");

        if (!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeAndWait(() -> {
                HistorialPrecioDialog dialog = new HistorialPrecioDialog(null, dtoSinCambios);
                assertNotNull(dialog);
                dialog.dispose();
            });
        }
    }

    @Test
    @Tag("RF-36")
    @DisplayName("RF-36 · Caso Error: Atributos nulos no provocan excepciones en la visualización")
    void historialPrecios_CasoError_DatosNulosOMalformados() throws Exception {
        PrecioHistorialDTO dtoNulo = new PrecioHistorialDTO(null, null, null, null);

        assertNull(dtoNulo.getIdProducto());
        assertNull(dtoNulo.getNombre());
        assertNull(dtoNulo.getPrecioActual());
        assertNull(dtoNulo.getHistorialPrecios());

        if (!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeAndWait(() -> {
                HistorialPrecioDialog dialog = new HistorialPrecioDialog(null, dtoNulo);
                assertNotNull(dialog);
                dialog.dispose();
            });
        }
    }
}
