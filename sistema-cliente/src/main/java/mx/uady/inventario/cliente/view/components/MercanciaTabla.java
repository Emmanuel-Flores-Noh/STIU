package mx.uady.inventario.cliente.view.components;

import com.formdev.flatlaf.FlatClientProperties;
import mx.uady.inventario.cliente.model.MercanciaDTO;
import mx.uady.inventario.cliente.util.FormateadorTexto;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tabla de inventario con desplazamiento: muestra las unidades disponibles (Número de serie, las filtra con el campo de búsqueda y
 * avisa cuando cambia la selección o se hace doble clic sobre una unidad.
 *
 */
public class MercanciaTabla extends JScrollPane{
    private final Modelo modelo = new Modelo();
    private final JTable tabla = new JTable(modelo);
    private Runnable alCambiarSeleccion = () -> { };
    private Consumer<MercanciaDTO> alDobleClic = unidad -> { };

    /**
     * Construye la tabla y la enlaza con el campo de búsqueda.
     *
     * @param campoBusqueda campo cuyo texto filtra las filas
     */
    public MercanciaTabla(JTextField campoBusqueda) {
        tabla.setName("tablaMercancia");
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(25);
        tabla.getTableHeader().setReorderingAllowed(false);
        new MercanciaFiltro(tabla, modelo, campoBusqueda);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                alCambiarSeleccion.run();
            }
        });
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = tabla.rowAtPoint(e.getPoint());
                    if (fila >= 0) {
                        alDobleClic.accept(unidadEnFila(fila));
                    }
                }
            }
        });
        setViewportView(tabla);
        putClientProperty(FlatClientProperties.STYLE, "arc: 8");
    }

    /**
     * Obtiene la tabla (para pruebas de interfaz).
     *
     * @return tabla de inventario
     */
    public JTable getTabla() {
        return tabla;
    }

    /**
     * Reemplaza las unidades mostradas.
     *
     * @param unidades unidades disponibles
     */
    public void reemplazarUnidades(List<MercanciaDTO> unidades) {
        modelo.reemplazar(unidades);
    }

    /**
     * Obtiene la unidad seleccionada, considerando el filtro activo.
     *
     * @return unidad seleccionada, o {@code null} si no hay selección
     */
    public MercanciaDTO getUnidadSeleccionada() {
        int fila = tabla.getSelectedRow();
        return fila >= 0 ? unidadEnFila(fila) : null;
    }

    /** Quita la selección de la tabla. */
    public void limpiarSeleccion() {
        tabla.clearSelection();
    }

    /**
     * Registra la acción que se ejecuta al cambiar la selección.
     *
     * @param accion acción a ejecutar
     */
    public void setAlCambiarSeleccion(Runnable accion) {
        this.alCambiarSeleccion = accion;
    }

    /**
     * Registra la acción que se ejecuta al hacer doble clic sobre una unidad.
     *
     * @param accion recibe la unidad sobre la que se hizo doble clic
     */
    public void setAlDobleClic(Consumer<MercanciaDTO> accion) {
        this.alDobleClic = accion;
    }

    private MercanciaDTO unidadEnFila(int filaVista) {
        return modelo.getUnidad(tabla.convertRowIndexToModel(filaVista));
    }
}
