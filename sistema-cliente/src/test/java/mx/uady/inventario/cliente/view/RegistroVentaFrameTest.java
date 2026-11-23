package mx.uady.inventario.cliente.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import mx.uady.inventario.cliente.controller.VentaOperaciones;
import mx.uady.inventario.cliente.model.MercanciaDTO;
import mx.uady.inventario.cliente.model.TiendaDTO;
import mx.uady.inventario.cliente.model.UsuarioDTO;

/**
 * Suite de pruebas de interfaz de usuario para {@link RegistroVentaFrame} en el Sistema Proveedor (Cliente).
 * Valida la interactividad de la tabla de inventario (RF-26), el filtrado dinámico (RF-27),
 * el selector de tiendas (RF-28), el selector de usuarios proveedores (RF-29), el botón de actualización (RF-30)
 * y las reglas de habilitación del formulario de venta (RF-31).
 */
class RegistroVentaFrameTest {

    private static final MercanciaDTO LAPTOP = new MercanciaDTO("SN-CLI-001", 1, "Laptop", "EN_ALMACEN");
    private static final TiendaDTO TIENDA = new TiendaDTO(1L, "Almacén Central UADY", "Calle 60");
    private static final UsuarioDTO USUARIO = new UsuarioDTO(1L, "Pedro", "Gómez");

    private RegistroVentaFrame frame;

    @BeforeEach
    void preparar() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeFalse(
                java.awt.GraphicsEnvironment.isHeadless(),
                "Requiere entorno gráfico con display X11 para inicializar JFrame");
        SwingUtilities.invokeAndWait(() -> {
            frame = new RegistroVentaFrame();
        });
    }

    @Test
    @Tag("RF-26")
    @DisplayName("RF-26 · Caso Normal: Tabla muestra columnas requeridas y desactiva botón de venta sin selección")
    void componentesInicializanYBloqueanRegistroSinSeleccion() throws Exception {
        enEdt(() -> {
            frame.actualizarListasCombos(List.of(TIENDA), List.of(USUARIO));
            frame.actualizarTablaMercancia(List.of(LAPTOP));

            JTable tabla = componente("tablaMercancia", JTable.class);
            assertEquals(1, tabla.getRowCount());
            assertEquals(3, tabla.getColumnCount());
            assertEquals("Número de serie", tabla.getColumnName(0));
            assertEquals("Producto", tabla.getColumnName(1));
            assertEquals("Estado", tabla.getColumnName(2));

            // Aún con catálogos cargados, si no hay fila seleccionada, el botón debe estar deshabilitado (RF-31)
            assertFalse(componente("btnRegistrarVenta", JButton.class).isEnabled());
            return null;
        });
    }

    @Test
    @Tag("RF-31")
    @DisplayName("RF-31 · Caso Normal: Habilitar botón de venta al seleccionar fila, tienda y usuario")
    void habilitarRegistroAlSeleccionarFilaYCatalogos() throws Exception {
        enEdt(() -> {
            frame.actualizarListasCombos(List.of(TIENDA), List.of(USUARIO));
            frame.actualizarTablaMercancia(List.of(LAPTOP));

            JTable tabla = componente("tablaMercancia", JTable.class);
            tabla.setRowSelectionInterval(0, 0);

            JComboBox<?> comboTiendas = componente("comboTiendas", JComboBox.class);
            JComboBox<?> comboUsuarios = componente("comboUsuarios", JComboBox.class);
            comboTiendas.setSelectedItem(TIENDA);
            comboUsuarios.setSelectedItem(USUARIO);

            assertTrue(componente("btnRegistrarVenta", JButton.class).isEnabled());
            return null;
        });
    }

    @Test
    @Tag("RF-27")
    @DisplayName("RF-27 · Caso Normal: Filtra mercancía en tiempo real por número de serie o nombre de producto")
    void filtraMercanciaPorTexto() throws Exception {
        MercanciaDTO monitor = new MercanciaDTO("AB-CLI-002", 2, "Monitor", "EN_ALMACEN");
        enEdt(() -> {
            frame.actualizarTablaMercancia(List.of(LAPTOP, monitor));
            JTextField campoBusqueda = componente("txtBuscarMercancia", JTextField.class);
            JTable tabla = componente("tablaMercancia", JTable.class);

            // Búsqueda por serie sin distinguir mayúsculas
            campoBusqueda.setText("sn-cli");
            assertEquals(1, tabla.getRowCount());

            // Búsqueda por nombre de producto
            campoBusqueda.setText("monitor");
            assertEquals(1, tabla.getRowCount());

            // Búsqueda vacía muestra todas las unidades (RF-27)
            campoBusqueda.setText("");
            assertEquals(2, tabla.getRowCount());
            return null;
        });
    }

    @Test
    @Tag("RF-28")
    @Tag("RF-29")
    @DisplayName("RF-28 / RF-29 · Caso Normal: Combos muestran lista de tiendas y lista de usuarios proveedores")
    void combosMuestranOpcionesDeTiendasYUsuarios() throws Exception {
        TiendaDTO tienda2 = new TiendaDTO(2L, "Tienda Campus Poniente", "Periférico");
        UsuarioDTO usuario2 = new UsuarioDTO(2L, "Ana", "López");

        enEdt(() -> {
            frame.actualizarListasCombos(List.of(TIENDA, tienda2), List.of(USUARIO, usuario2));

            JComboBox<?> comboTiendas = componente("comboTiendas", JComboBox.class);
            JComboBox<?> comboUsuarios = componente("comboUsuarios", JComboBox.class);

            assertEquals(2, comboTiendas.getItemCount(), "Debe desplegar las 2 tiendas registradas (RF-28)");
            assertEquals(2, comboUsuarios.getItemCount(), "Debe desplegar los 2 usuarios proveedores (RF-29)");
            return null;
        });
    }

    @Test
    @Tag("RF-30")
    @DisplayName("RF-30 · Caso Normal: Clic en 'Actualizar Listas' invoca cargarListas en el controlador")
    void botonActualizarDisparaRecarga() throws Exception {
        AtomicBoolean llamado = new AtomicBoolean(false);
        frame.setOperaciones(new VentaOperaciones() {
            @Override public void cargarListas() { llamado.set(true); }
            @Override public void registrarVenta(MercanciaDTO u, TiendaDTO t, UsuarioDTO us) {}
            @Override public void consultarQr(String s) {}
            @Override public void consultarHistorialPrecios(Integer id) {}
        });

        enEdt(() -> {
            JButton btnRefrescar = componente("btnRefrescar", JButton.class);
            btnRefrescar.doClick();
            return null;
        });

        assertTrue(llamado.get(), "Hacer clic en 'Actualizar Listas' debe llamar a cargarListas (RF-30)");
    }

    // ---- Métodos Auxiliares para pruebas de Swing ----

    private static <T extends Component> T buscar(Container raiz, String nombre, Class<T> tipo) {
        for (Component c : raiz.getComponents()) {
            if (nombre.equals(c.getName()) && tipo.isInstance(c)) {
                return tipo.cast(c);
            }
            if (c instanceof Container hijo) {
                T encontrado = buscar(hijo, nombre, tipo);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }

    private <T extends Component> T componente(String nombre, Class<T> tipo) {
        T encontrado = buscar(frame, nombre, tipo);
        if (encontrado == null) {
            throw new AssertionError("No se encontró el componente gráfico: " + nombre);
        }
        return encontrado;
    }

    private static <T> T enEdt(Callable<T> consulta) throws Exception {
        AtomicReference<T> resultado = new AtomicReference<>();
        AtomicReference<Exception> fallo = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                resultado.set(consulta.call());
            } catch (Exception e) {
                fallo.set(e);
            }
        });
        if (fallo.get() != null) {
            throw fallo.get();
        }
        return resultado.get();
    }
}
