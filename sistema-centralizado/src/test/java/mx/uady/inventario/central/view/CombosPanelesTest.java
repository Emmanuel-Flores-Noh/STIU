package mx.uady.inventario.central.view;

import static mx.uady.inventario.central.view.MercanciaPanelTest.buscar;
import static mx.uady.inventario.central.view.MercanciaPanelTest.enEdt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import mx.uady.inventario.central.model.VentaHistorial;
import mx.uady.inventario.common.modelo.EstadoMercancia;
import mx.uady.inventario.common.modelo.Producto;
import mx.uady.inventario.common.modelo.TiendaAlmacen;
import mx.uady.inventario.common.modelo.Usuario;

/** Cómo se muestran y se vuelven a llenar los combos de las pantallas. */
class CombosPanelesTest {

    private static final Producto LAPTOP = new Producto(1, "Laptop", null, BigDecimal.TEN);
    private static final Producto SILLA = new Producto(2, "Silla", null, BigDecimal.ONE);
    private static final TiendaAlmacen CENTRO = new TiendaAlmacen(7, "Tienda Centro", "Calle 1");
    private static final TiendaAlmacen NORTE = new TiendaAlmacen(9, "Tienda Norte", "Calle 2");
    private static final Usuario LUCERO = new Usuario(3, 3, "Lucero", "Chan", "Canul");
    private static final Usuario LUIS = new Usuario(5, 2, "Luis", "Can", null);

    @Test
    @Tag("RF-08")
    @Tag("RF-09")
    @DisplayName("RF-08, RF-09 · Los combos de Mercancía muestran el nombre del producto y el estado legible")
    void losCombosDeMercanciaMuestranNombresYEstados() throws Exception {
        enEdt(() -> {
            MercanciaPanel panel = new MercanciaPanel();
            panel.mostrarProductos(List.of(LAPTOP));

            assertEquals("Laptop", textoMostrado(buscar(panel, "productoRegistro", JComboBox.class), LAPTOP));
            assertEquals("Laptop", textoMostrado(buscar(panel, "filtroProducto", JComboBox.class), LAPTOP));
            assertEquals(MercanciaPanel.TODOS,
                    textoMostrado(buscar(panel, "filtroProducto", JComboBox.class), MercanciaPanel.TODOS));
            JComboBox<?> estados = buscar(panel, "filtroEstado", JComboBox.class);
            assertEquals("En almacén", textoMostrado(estados, EstadoMercancia.EN_ALMACEN));
            assertEquals("Vendido", textoMostrado(estados, EstadoMercancia.VENDIDO));
        });
    }

    @Test
    @Tag("RF-09")
    @DisplayName("RF-09 · Si el producto elegido desaparece, los combos vuelven al primero y a Todos")
    void siElProductoElegidoDesapareceLosCombosVuelvenAlPrimero() throws Exception {
        enEdt(() -> {
            MercanciaPanel panel = new MercanciaPanel();
            panel.mostrarProductos(List.of(LAPTOP, SILLA));
            buscar(panel, "productoRegistro", JComboBox.class).setSelectedItem(SILLA);
            buscar(panel, "filtroProducto", JComboBox.class).setSelectedItem(SILLA);

            panel.mostrarProductos(List.of(LAPTOP));

            assertEquals(1, panel.productoParaRegistrar().orElseThrow().idProducto());
            assertNull(panel.filtroProductoSeleccionado());
        });
    }

    @Test
    @Tag("RF-13")
    @Tag("RF-15")
    @DisplayName("RF-13, RF-15 · Los combos de Ventas muestran la tienda y el nombre completo del usuario")
    void losCombosDeVentasMuestranTiendaYNombreCompleto() throws Exception {
        enEdt(() -> {
            VentaPanel panel = new VentaPanel();
            panel.mostrarTiendas(List.of(CENTRO));
            panel.mostrarUsuarios(List.of(LUCERO, LUIS));

            assertEquals("Tienda Centro", textoMostrado(buscar(panel, "tiendaVenta", JComboBox.class), CENTRO));
            JComboBox<?> usuarios = buscar(panel, "usuarioVenta", JComboBox.class);
            assertEquals("Lucero Chan Canul", textoMostrado(usuarios, LUCERO));
            assertEquals("Luis Can", textoMostrado(usuarios, LUIS));
        });
    }

    @Test
    @Tag("RF-13")
    @Tag("RF-15")
    @DisplayName("RF-13, RF-15 · Al recargar Ventas sin la tienda elegida queda la primera; sin datos, ninguna")
    void alRecargarVentasSinLaTiendaElegidaQuedaLaPrimera() throws Exception {
        enEdt(() -> {
            VentaPanel panel = new VentaPanel();
            panel.mostrarTiendas(List.of(CENTRO, NORTE));
            panel.mostrarUsuarios(List.of(LUCERO, LUIS));
            buscar(panel, "tiendaVenta", JComboBox.class).setSelectedIndex(1);
            buscar(panel, "usuarioVenta", JComboBox.class).setSelectedIndex(1);

            panel.mostrarTiendas(List.of(CENTRO));
            panel.mostrarUsuarios(List.of(LUCERO));
            assertEquals(7, panel.tiendaSeleccionada().orElseThrow().idTienda());
            assertEquals(3, panel.usuarioSeleccionado().orElseThrow().idUsuario());

            panel.mostrarTiendas(List.of());
            panel.mostrarUsuarios(List.of());
            assertTrue(panel.tiendaSeleccionada().isEmpty());
            assertTrue(panel.usuarioSeleccionado().isEmpty());
        });
    }

    @Test
    @Tag("RF-14")
    @DisplayName("RF-14 · Los combos del historial ofrecen Todas/Todos y los valores ordenados sin repetir")
    void losCombosDelHistorialOfrecenTodosYValoresOrdenados() throws Exception {
        enEdt(() -> {
            VentaHistorialPanel panel = new VentaHistorialPanel();
            panel.mostrarVentas(List.of(
                    venta(1, "Silla", "Norte"), venta(2, "Laptop", "Centro"), venta(3, "Laptop", "Norte")));

            assertEquals(List.of(VentaHistorialPanel.TODAS, "Centro", "Norte"),
                    elementos(buscar(panel, "filtroTiendaVentas", JComboBox.class)));
            assertEquals(List.of(VentaHistorialPanel.TODOS, "Laptop", "Silla"),
                    elementos(buscar(panel, "filtroProductoVentas", JComboBox.class)));

            JComboBox<?> tienda = buscar(panel, "filtroTiendaVentas", JComboBox.class);
            tienda.setSelectedItem("Norte");
            panel.mostrarVentas(List.of(venta(4, "Laptop", "Centro")));
            assertEquals(VentaHistorialPanel.TODAS, tienda.getSelectedItem());
        });
    }

    private static VentaHistorial venta(int id, String producto, String tienda) {
        return new VentaHistorial(id, "SN-" + id, producto, tienda, "Ana", LocalDateTime.of(2026, 1, id, 12, 0), 1);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String textoMostrado(JComboBox combo, Object valor) {
        return ((JLabel) combo.getRenderer().getListCellRendererComponent(new JList<>(), valor, 0, false, false))
                .getText();
    }

    private static List<Object> elementos(JComboBox<?> combo) {
        List<Object> elementos = new java.util.ArrayList<>();
        for (int i = 0; i < combo.getItemCount(); i++) {
            elementos.add(combo.getItemAt(i));
        }
        return elementos;
    }
}
