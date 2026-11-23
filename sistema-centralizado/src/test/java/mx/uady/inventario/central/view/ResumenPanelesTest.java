package mx.uady.inventario.central.view;

import static mx.uady.inventario.central.view.MercanciaPanelTest.buscar;
import static mx.uady.inventario.central.view.MercanciaPanelTest.enEdt;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import mx.uady.inventario.central.model.VentaHistorial;
import mx.uady.inventario.common.modelo.EstadoMercancia;
import mx.uady.inventario.common.modelo.Mercancia;
import mx.uady.inventario.common.modelo.Producto;

/** Textos exactos de la barra de estado de cada tabla filtrable. */
class ResumenPanelesTest {

    private static final Producto LAPTOP = new Producto(1, "Laptop", null, BigDecimal.TEN);
    private static final Mercancia UNA = new Mercancia("SN-001", 1, EstadoMercancia.EN_ALMACEN, null);
    private static final Mercancia OTRA = new Mercancia("SN-002", 1, EstadoMercancia.EN_ALMACEN, null);
    private static final VentaHistorial VENTA = new VentaHistorial(1, "SN-001", "Laptop",
            "Centro", "Ana", LocalDateTime.of(2026, 1, 10, 12, 0), 1);
    private static final VentaHistorial OTRA_VENTA = new VentaHistorial(2, "SN-002", "Laptop",
            "Norte", "Luis", LocalDateTime.of(2026, 2, 10, 12, 0), 1);

    @Test
    @Tag("RF-09")
    @DisplayName("RF-09 · La barra de Mercancía indica cuántas unidades muestra y por qué no hay")
    void laBarraDeMercanciaIndicaCuantasUnidadesMuestra() throws Exception {
        enEdt(() -> {
            MercanciaPanel panel = new MercanciaPanel();
            JLabel estado = buscar(panel, "estadoUnidades", JLabel.class);
            JTextField busqueda = buscar(panel, "busquedaSerie", JTextField.class);
            panel.mostrarProductos(List.of(LAPTOP));

            panel.mostrarUnidades(List.of(), List.of(LAPTOP));
            assertEquals("No hay unidades registradas.", estado.getText());

            panel.mostrarUnidades(List.of(UNA), List.of(LAPTOP));
            assertEquals("1 unidad", estado.getText());

            panel.mostrarUnidades(List.of(UNA, OTRA), List.of(LAPTOP));
            assertEquals("2 unidades", estado.getText());

            busqueda.setText("001");
            assertEquals("1 de 2 unidades", estado.getText());

            busqueda.setText("  zzz ");
            assertEquals("Ninguna unidad coincide con «zzz».", estado.getText());

            busqueda.setText("");
            buscar(panel, "filtroProducto", JComboBox.class).setSelectedIndex(1);
            panel.mostrarUnidades(List.of(), List.of(LAPTOP));
            assertEquals("No hay unidades con los filtros elegidos.", estado.getText());
        });
    }

    @Test
    @Tag("RF-15")
    @DisplayName("RF-15 · La barra de Ventas indica cuántas mercancías hay disponibles")
    void laBarraDeVentasIndicaCuantasMercanciasHayDisponibles() throws Exception {
        enEdt(() -> {
            VentaPanel panel = new VentaPanel();
            JLabel estado = buscar(panel, "estadoVentas", JLabel.class);
            JTextField busqueda = buscar(panel, "busquedaVentas", JTextField.class);
            Map<Integer, String> nombres = Map.of(1, "Laptop");

            panel.mostrarMercancias(List.of(), nombres);
            assertEquals("No hay mercancías disponibles para vender.", estado.getText());

            panel.mostrarMercancias(List.of(UNA), nombres);
            assertEquals("1 mercancía disponible", estado.getText());

            panel.mostrarMercancias(List.of(UNA, OTRA), nombres);
            assertEquals("2 mercancías disponibles", estado.getText());

            busqueda.setText("002");
            assertEquals("1 de 2 mercancías", estado.getText());

            busqueda.setText(" zzz ");
            assertEquals("Ninguna mercancía coincide con «zzz».", estado.getText());
        });
    }

    @Test
    @Tag("RF-14")
    @DisplayName("RF-14 · La barra del historial indica cuántas ventas muestra y avisa de fechas inválidas")
    void laBarraDelHistorialIndicaCuantasVentasMuestra() throws Exception {
        enEdt(() -> {
            VentaHistorialPanel panel = new VentaHistorialPanel();
            JLabel estado = buscar(panel, "estadoHistorialVentas", JLabel.class);
            JTextField serie = buscar(panel, "busquedaSerieHistorial", JTextField.class);

            panel.mostrarVentas(List.of());
            assertEquals("Todavía no hay ventas registradas.", estado.getText());

            panel.mostrarVentas(List.of(VENTA));
            assertEquals("1 venta", estado.getText());

            panel.mostrarVentas(List.of(VENTA, OTRA_VENTA));
            assertEquals("2 ventas", estado.getText());

            serie.setText("SN-001");
            assertEquals("1 de 2 ventas", estado.getText());

            serie.setText("zzz");
            assertEquals("Ninguna venta coincide con los filtros.", estado.getText());

            serie.setText("");
            buscar(panel, "filtroDesdeVentas", JTextField.class).setText("2026-13-01");
            assertEquals("Las fechas deben escribirse como aaaa-mm-dd, por ejemplo 2026-11-12.", estado.getText());
        });
    }
}
