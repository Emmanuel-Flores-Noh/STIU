package mx.uady.inventario.cliente.view.components;

import mx.uady.inventario.cliente.model.TiendaDTO;
import mx.uady.inventario.cliente.model.UsuarioDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JComboBox;
import java.awt.Component;
import java.awt.Container;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el componente de formulario de venta {@link VentaFormulario}.
 * Valida la gestión de selectores de tiendas (RF-28), usuarios proveedores (RF-29),
 * habilitación condicional de botones (RF-31) y limpieza de campos (RF-32).
 */
class VentaFormularioComponentTest {

    private final TiendaDTO TIENDA_1 = new TiendaDTO(1L, "Tienda Central UADY", "Calle 60");
    private final TiendaDTO TIENDA_2 = new TiendaDTO(2L, "Tienda Campus Ciencias Exactas", "Periférico Norte");
    private final UsuarioDTO USUARIO_1 = new UsuarioDTO(10L, "Mario", "Zapata");
    private final UsuarioDTO USUARIO_2 = new UsuarioDTO(20L, "Elena", "Reyes");

    @SuppressWarnings("unchecked")
    private static <T extends Component> T buscar(Container raiz, String nombre, Class<T> tipo) {
        for (Component hijo : raiz.getComponents()) {
            if (nombre.equals(hijo.getName()) && tipo.isInstance(hijo)) {
                return tipo.cast(hijo);
            }
            if (hijo instanceof Container contenedorHijo) {
                T encontrado = buscar(contenedorHijo, nombre, tipo);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }

    @Test
    @Tag("RF-28")
    @DisplayName("RF-28 · Caso Normal: Despliega tiendas en el orden recibido y permite selección")
    void ventaFormulario_CasoNormal_SelectorTiendas() {
        VentaFormulario formulario = new VentaFormulario();
        formulario.actualizarCatalogos(List.of(TIENDA_1, TIENDA_2), List.of());

        // Inicialmente no hay tienda seleccionada
        assertNull(formulario.getTienda());

        @SuppressWarnings("unchecked")
        JComboBox<TiendaDTO> combo = buscar(formulario, "comboTiendas", JComboBox.class);
        assertNotNull(combo);
        assertEquals(2, combo.getItemCount());
        assertEquals(TIENDA_1, combo.getItemAt(0));
        assertEquals(TIENDA_2, combo.getItemAt(1));

        combo.setSelectedItem(TIENDA_1);
        assertEquals(TIENDA_1, formulario.getTienda());
        assertEquals("Tienda Central UADY", formulario.getTienda().getNombre());
    }

    @Test
    @Tag("RF-29")
    @DisplayName("RF-29 · Caso Normal: Despliega usuarios proveedores mostrando nombres y apellido paterno")
    void ventaFormulario_CasoNormal_SelectorUsuarios() {
        VentaFormulario formulario = new VentaFormulario();
        formulario.actualizarCatalogos(List.of(), List.of(USUARIO_1, USUARIO_2));

        // Inicialmente no hay usuario seleccionado
        assertNull(formulario.getUsuario());

        @SuppressWarnings("unchecked")
        JComboBox<UsuarioDTO> combo = buscar(formulario, "comboUsuarios", JComboBox.class);
        assertNotNull(combo);
        assertEquals(2, combo.getItemCount());
        assertEquals(USUARIO_1, combo.getItemAt(0));
        assertEquals(USUARIO_2, combo.getItemAt(1));

        combo.setSelectedItem(USUARIO_1);
        assertEquals(USUARIO_1, formulario.getUsuario());
        assertEquals("Mario Zapata", formulario.getUsuario().toString());
    }

    @Test
    @Tag("RF-31")
    @DisplayName("RF-31 · Caso Normal: Control de habilitación de botones de registrar venta e historial")
    void ventaFormulario_CasoNormal_HabilitacionBotones() {
        VentaFormulario formulario = new VentaFormulario();

        JButton btnRegistrar = buscar(formulario, "btnRegistrarVenta", JButton.class);
        JButton btnHistorial = buscar(formulario, "btnHistorialPrecios", JButton.class);
        assertNotNull(btnRegistrar);
        assertNotNull(btnHistorial);

        // Inicialmente deshabilitados por defecto (RF-31)
        assertFalse(btnRegistrar.isEnabled());
        assertFalse(btnHistorial.isEnabled());

        // Habilitación dinámica por el controlador
        formulario.setRegistrarHabilitado(true);
        formulario.setHistorialHabilitado(true);
        assertTrue(btnRegistrar.isEnabled());
        assertTrue(btnHistorial.isEnabled());

        formulario.setSelectoresHabilitados(false);
        JComboBox<?> comboTiendas = buscar(formulario, "comboTiendas", JComboBox.class);
        assertNotNull(comboTiendas);
        assertFalse(comboTiendas.isEnabled());
    }

    @Test
    @Tag("RF-32")
    @DisplayName("RF-32 · Caso Normal: Limpieza de formulario deselecciona tienda y usuario")
    void ventaFormulario_CasoNormal_LimpiarFormulario() {
        VentaFormulario formulario = new VentaFormulario();
        formulario.actualizarCatalogos(List.of(TIENDA_1), List.of(USUARIO_1));

        @SuppressWarnings("unchecked")
        JComboBox<TiendaDTO> comboTiendas = buscar(formulario, "comboTiendas", JComboBox.class);
        @SuppressWarnings("unchecked")
        JComboBox<UsuarioDTO> comboUsuarios = buscar(formulario, "comboUsuarios", JComboBox.class);
        assertNotNull(comboTiendas);
        assertNotNull(comboUsuarios);

        comboTiendas.setSelectedItem(TIENDA_1);
        comboUsuarios.setSelectedItem(USUARIO_1);

        assertNotNull(formulario.getTienda());
        assertNotNull(formulario.getUsuario());

        formulario.limpiar();

        assertNull(formulario.getTienda());
        assertNull(formulario.getUsuario());
    }

    @Test
    @Tag("RF-28")
    @Tag("RF-29")
    @DisplayName("RF-28 / RF-29 · Caso Límite: Catálogos vacíos dejan selectores sin selección")
    void ventaFormulario_CasoLimite_CatalogosVacios() {
        VentaFormulario formulario = new VentaFormulario();
        formulario.actualizarCatalogos(List.of(), List.of());

        assertNull(formulario.getTienda());
        assertNull(formulario.getUsuario());
    }

    @Test
    @Tag("RF-32")
    @Tag("RF-36")
    @DisplayName("RF-32 / RF-36 · Caso Normal: Disparo de listeners al cambiar selección o presionar botones habilitados")
    void ventaFormulario_CasoNormal_Listeners() {
        VentaFormulario formulario = new VentaFormulario();
        AtomicBoolean seleccionCambiada = new AtomicBoolean(false);
        AtomicBoolean registroPulsado = new AtomicBoolean(false);
        AtomicBoolean historialPulsado = new AtomicBoolean(false);

        formulario.setAlCambiarSeleccion(() -> seleccionCambiada.set(true));
        formulario.setAlRegistrar(() -> registroPulsado.set(true));
        formulario.setAlVerHistorial(() -> historialPulsado.set(true));

        formulario.actualizarCatalogos(List.of(TIENDA_1), List.of(USUARIO_1));
        @SuppressWarnings("unchecked")
        JComboBox<TiendaDTO> comboTiendas = buscar(formulario, "comboTiendas", JComboBox.class);
        assertNotNull(comboTiendas);

        seleccionCambiada.set(false);
        comboTiendas.setSelectedItem(TIENDA_1);
        assertTrue(seleccionCambiada.get());

        JButton btnRegistrar = buscar(formulario, "btnRegistrarVenta", JButton.class);
        JButton btnHistorial = buscar(formulario, "btnHistorialPrecios", JButton.class);
        assertNotNull(btnRegistrar);
        assertNotNull(btnHistorial);

        // Deshabilitados por defecto (RF-31): no deben disparar evento al hacer click
        btnRegistrar.doClick();
        assertFalse(registroPulsado.get());

        // Al ser habilitados, disparan las acciones correspondientes
        formulario.setRegistrarHabilitado(true);
        formulario.setHistorialHabilitado(true);

        btnRegistrar.doClick();
        assertTrue(registroPulsado.get());

        btnHistorial.doClick();
        assertTrue(historialPulsado.get());
    }
}
