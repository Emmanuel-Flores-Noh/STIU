package mx.uady.inventario.central.view;

import static mx.uady.inventario.central.view.MercanciaPanelTest.buscar;
import static mx.uady.inventario.central.view.MercanciaPanelTest.enEdt;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import mx.uady.inventario.central.model.VentaHistorial;

/** Límites de los filtros del historial de ventas vistos desde la pantalla (RF-14). */
class VentaHistorialFiltrosTest {

    private static final String FECHA_INVALIDA = "Las fechas deben escribirse como aaaa-mm-dd, por ejemplo 2026-11-12.";

    private static final VentaHistorial PRIMERA_HORA = venta(1, "SN-Álamo-01", "Centro", LocalDateTime.of(2026, 3, 10, 0, 0));
    private static final VentaHistorial ULTIMA_HORA = venta(2, "SN-002", "Centro", LocalDateTime.of(2026, 3, 10, 23, 59));
    private static final VentaHistorial DIA_SIGUIENTE = venta(3, "SN-003", "Norte", LocalDateTime.of(2026, 3, 11, 0, 0));

    @Test
    @Tag("RF-14")
    @DisplayName("RF-14 · El rango de fechas incluye el día de inicio y el de fin completos")
    void elRangoIncluyeLosDiasExtremosCompletos() throws Exception {
        enEdt(() -> {
            VentaHistorialPanel panel = panelCon(PRIMERA_HORA, ULTIMA_HORA, DIA_SIGUIENTE);

            texto(panel, "filtroDesdeVentas").setText("2026-03-10");
            texto(panel, "filtroHastaVentas").setText("2026-03-10");
            assertEquals(List.of(1, 2), visibles(panel));

            texto(panel, "filtroDesdeVentas").setText("2026-03-11");
            texto(panel, "filtroHastaVentas").setText("2026-03-11");
            assertEquals(List.of(3), visibles(panel));
        });
    }

    @Test
    @Tag("RF-14")
    @DisplayName("RF-14 · Si Desde es posterior a Hasta no queda ninguna venta")
    void desdePosteriorAHastaNoDejaNinguna() throws Exception {
        enEdt(() -> {
            VentaHistorialPanel panel = panelCon(PRIMERA_HORA, ULTIMA_HORA, DIA_SIGUIENTE);

            texto(panel, "filtroDesdeVentas").setText("2026-03-11");
            texto(panel, "filtroHastaVentas").setText("2026-03-10");

            assertEquals(List.of(), visibles(panel));
            assertEquals("Ninguna venta coincide con los filtros.", estado(panel));
        });
    }

    @Test
    @Tag("RF-14")
    @DisplayName("RF-14 · Una fecha mal escrita se ignora y avisa, pero los demás filtros siguen aplicando")
    void unaFechaInvalidaSeIgnoraYLosDemasFiltrosAplican() throws Exception {
        enEdt(() -> {
            VentaHistorialPanel panel = panelCon(PRIMERA_HORA, ULTIMA_HORA, DIA_SIGUIENTE);
            buscar(panel, "filtroTiendaVentas", JComboBox.class).setSelectedItem("Norte");

            texto(panel, "filtroDesdeVentas").setText("10/03/2026");
            assertEquals(List.of(3), visibles(panel));
            assertEquals(FECHA_INVALIDA, estado(panel));

            texto(panel, "filtroDesdeVentas").setText("");
            texto(panel, "filtroHastaVentas").setText("2026-02-30");
            assertEquals(List.of(3), visibles(panel));
            assertEquals(FECHA_INVALIDA, estado(panel));
        });
    }

    @Test
    @Tag("RF-14")
    @DisplayName("RF-14 · Las fechas admiten espacios alrededor y la serie ignora mayúsculas y acentos")
    void fechasConEspaciosYSerieSinMayusculasNiAcentos() throws Exception {
        enEdt(() -> {
            VentaHistorialPanel panel = panelCon(PRIMERA_HORA, ULTIMA_HORA, DIA_SIGUIENTE);

            texto(panel, "filtroDesdeVentas").setText("  2026-03-11 ");
            assertEquals(List.of(3), visibles(panel));
            assertEquals("1 de 3 ventas", estado(panel));

            texto(panel, "filtroDesdeVentas").setText("");
            texto(panel, "busquedaSerieHistorial").setText("  sn-alamo ");
            assertEquals(List.of(1), visibles(panel));
        });
    }

    @Test
    @Tag("RF-14")
    @DisplayName("RF-14 · Los combos omiten valores nulos y las ventas sin tienda solo se ven con Todas")
    void losCombosOmitenNulos() throws Exception {
        enEdt(() -> {
            VentaHistorialPanel panel = panelCon(PRIMERA_HORA, venta(4, "SN-004", null, LocalDateTime.of(2026, 3, 12, 9, 0)));
            JComboBox<?> tienda = buscar(panel, "filtroTiendaVentas", JComboBox.class);

            assertEquals(2, tienda.getItemCount());
            assertEquals(List.of(1, 4), visibles(panel));
            tienda.setSelectedItem("Centro");
            assertEquals(List.of(1), visibles(panel));
        });
    }

    private static VentaHistorial venta(int id, String serie, String tienda, LocalDateTime fecha) {
        return new VentaHistorial(id, serie, "Laptop", tienda, "Ana", fecha, 1);
    }

    private static VentaHistorialPanel panelCon(VentaHistorial... ventas) {
        VentaHistorialPanel panel = new VentaHistorialPanel();
        panel.mostrarVentas(List.of(ventas));
        return panel;
    }

    private static JTextField texto(VentaHistorialPanel panel, String nombre) {
        return buscar(panel, nombre, JTextField.class);
    }

    private static String estado(VentaHistorialPanel panel) {
        return buscar(panel, "estadoHistorialVentas", JLabel.class).getText();
    }

    /** Ids de las ventas visibles, en el orden en que se muestran. */
    private static List<Integer> visibles(VentaHistorialPanel panel) {
        JTable tabla = buscar(panel, "tablaHistorialVentas", JTable.class);
        VentaHistorialTableModel modelo = (VentaHistorialTableModel) tabla.getModel();
        List<Integer> ids = new ArrayList<>();
        for (int fila = 0; fila < tabla.getRowCount(); fila++) {
            ids.add(modelo.getFila(tabla.convertRowIndexToModel(fila)).idVenta());
        }
        return ids;
    }
}
