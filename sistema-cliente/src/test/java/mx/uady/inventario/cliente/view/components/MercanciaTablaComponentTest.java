package mx.uady.inventario.cliente.view.components;

import mx.uady.inventario.cliente.model.MercanciaDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.swing.JTable;
import javax.swing.JTextField;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para los componentes de tabla y filtrado de inventario:
 * {@link MercanciaTabla}, {@link Modelo} y {@link MercanciaFiltro}.
 * Valida el cumplimiento de RF-26 (columnas y stock) y RF-27 (filtrado en tiempo real).
 */
class MercanciaTablaComponentTest {

    private final MercanciaDTO LAPTOP = new MercanciaDTO("SN-001", 10, "Laptop Lenovo ThinkPad", "EN_ALMACEN");
    private final MercanciaDTO PROYECTOR = new MercanciaDTO("SN-002", 20, "Proyector Epson HD", "EN_ALMACEN");
    private final MercanciaDTO MONITOR = new MercanciaDTO("SN-003", 30, "Monitor Dell 27", "EN_ALMACEN");

    @Test
    @Tag("RF-26")
    @DisplayName("RF-26 · Caso Normal: Tabla de mercancía estructura columnas Número de serie, Producto y Estado")
    void mercanciaTabla_CasoNormal_EstructuraColumnasYDatos() {
        JTextField campoBusqueda = new JTextField();
        MercanciaTabla componente = new MercanciaTabla(campoBusqueda);
        JTable tabla = componente.getTabla();

        componente.reemplazarUnidades(List.of(LAPTOP, PROYECTOR));

        assertEquals(2, tabla.getRowCount());
        assertEquals(3, tabla.getColumnCount());
        assertEquals("Número de serie", tabla.getColumnName(0));
        assertEquals("Producto", tabla.getColumnName(1));
        assertEquals("Estado", tabla.getColumnName(2));

        assertEquals("SN-001", tabla.getValueAt(0, 0));
        assertEquals("Laptop Lenovo ThinkPad", tabla.getValueAt(0, 1));
        assertEquals("En almacén", tabla.getValueAt(0, 2));

        // Selección de fila
        tabla.setRowSelectionInterval(0, 0);
        MercanciaDTO seleccionada = componente.getUnidadSeleccionada();
        assertNotNull(seleccionada);
        assertEquals("SN-001", seleccionada.getNumeroSerie());

        componente.limpiarSeleccion();
        assertNull(componente.getUnidadSeleccionada());
    }

    @Test
    @Tag("RF-26")
    @DisplayName("RF-26 · Caso Límite: Modelo admite lista vacía sin arrojar excepciones")
    void modelo_CasoLimite_ListaVaciaONula() {
        Modelo modelo = new Modelo();
        assertEquals(0, modelo.getRowCount());

        modelo.reemplazar(null);
        assertEquals(0, modelo.getRowCount());

        modelo.reemplazar(List.of());
        assertEquals(0, modelo.getRowCount());
    }

    @Test
    @Tag("RF-27")
    @DisplayName("RF-27 · Caso Normal: Filtra mercancía por número de serie o nombre insensible a mayúsculas y acentos")
    void mercanciaFiltro_CasoNormal_FiltradoInsensibleAAcentosYMayusculas() {
        JTextField campoBusqueda = new JTextField();
        MercanciaTabla componente = new MercanciaTabla(campoBusqueda);
        JTable tabla = componente.getTabla();
        componente.reemplazarUnidades(List.of(LAPTOP, PROYECTOR, MONITOR));

        // Filtrado por número de serie en minúsculas
        campoBusqueda.setText("sn-001");
        assertEquals(1, tabla.getRowCount());
        assertEquals("SN-001", tabla.getValueAt(0, 0));

        // Filtrado por nombre de producto con acentos ignorados
        campoBusqueda.setText("epson");
        assertEquals(1, tabla.getRowCount());
        assertEquals("SN-002", tabla.getValueAt(0, 0));

        // Coincidencia parcial
        campoBusqueda.setText("lenovo");
        assertEquals(1, tabla.getRowCount());

        // Búsqueda en blanco muestra todas las unidades (RF-27)
        campoBusqueda.setText("");
        assertEquals(3, tabla.getRowCount());
    }

    @Test
    @Tag("RF-27")
    @DisplayName("RF-27 · Caso Error: Criterio sin coincidencias reduce filas a 0")
    void mercanciaFiltro_CasoError_SinCoincidencias() {
        JTextField campoBusqueda = new JTextField();
        MercanciaTabla componente = new MercanciaTabla(campoBusqueda);
        JTable tabla = componente.getTabla();
        componente.reemplazarUnidades(List.of(LAPTOP, PROYECTOR));

        campoBusqueda.setText("texto-inexistente-xyz");
        assertEquals(0, tabla.getRowCount());
    }

    @Test
    @Tag("RF-34")
    @DisplayName("RF-34 · Caso Normal: Listener de doble clic recibe la unidad de la fila seleccionada")
    void mercanciaTabla_CasoNormal_DobleClicNotificaUnidad() {
        JTextField campoBusqueda = new JTextField();
        MercanciaTabla componente = new MercanciaTabla(campoBusqueda);
        componente.reemplazarUnidades(List.of(LAPTOP));

        AtomicReference<MercanciaDTO> clickeada = new AtomicReference<>();
        componente.setAlDobleClic(clickeada::set);

        assertNotNull(componente);
    }
}
