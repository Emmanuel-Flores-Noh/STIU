package mx.uady.inventario.central.view.comun;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import mx.uady.inventario.central.view.FiltroTexto;

/**
 * Tabla de las pantallas con la configuración común: selección de una sola fila, alto de fila
 * uniforme, ordenamiento por columnas y filtros por texto o por fila.
 *
 * <p>Traduce entre los índices de la vista (ordenada y filtrada) y los del modelo, de modo que las
 * pantallas trabajan siempre con las filas del modelo.</p>
 *
 * @param <M> tipo del modelo de la tabla
 * @param <F> tipo de cada fila del modelo
 */
public final class TablaFiltrable<M extends TableModel, F> {

    /** Alto de fila de todas las tablas de las pantallas. */
    public static final int ALTO_FILA = 26;

    private final M modelo;
    private final IntFunction<F> filaDelModelo;
    private final JTable tabla;
    private final TableRowSorter<M> ordenador;

    /**
     * Crea la tabla sobre el modelo indicado.
     *
     * @param modelo        modelo con los datos
     * @param filaDelModelo función que devuelve la fila del modelo en un índice, p. ej. {@code modelo::getFila}
     * @throws NullPointerException si algún argumento es {@code null}
     */
    public TablaFiltrable(M modelo, IntFunction<F> filaDelModelo) {
        this.modelo = Objects.requireNonNull(modelo, "modelo");
        this.filaDelModelo = Objects.requireNonNull(filaDelModelo, "filaDelModelo");
        this.tabla = new JTable(modelo);
        this.ordenador = new TableRowSorter<>(modelo);
        tabla.setRowSorter(ordenador);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(ALTO_FILA);
    }

    /**
     * Devuelve el componente Swing para agregarlo a la pantalla.
     *
     * @return la tabla
     */
    public JTable tabla() {
        return tabla;
    }

    /**
     * Ordena la tabla de forma ascendente por la columna indicada.
     *
     * @param columna índice de la columna en el modelo
     * @return esta misma tabla, para encadenar la configuración
     */
    public TablaFiltrable<M, F> ordenarPor(int columna) {
        ordenador.setSortKeys(List.of(new RowSorter.SortKey(columna, SortOrder.ASCENDING)));
        return this;
    }

    /**
     * Devuelve la fila elegida.
     *
     * @return la fila del modelo seleccionada, o vacío si no hay ninguna
     */
    public Optional<F> seleccionada() {
        int enVista = tabla.getSelectedRow();
        return enVista < 0 ? Optional.empty() : Optional.of(filaDelModelo.apply(tabla.convertRowIndexToModel(enVista)));
    }

    /**
     * Selecciona una fila del modelo y desplaza la tabla para que quede visible.
     *
     * @param indice índice de la fila en el modelo
     * @return {@code true} si la fila existe, no está oculta por el filtro y quedó seleccionada
     */
    public boolean seleccionarFilaDelModelo(int indice) {
        if (indice < 0 || indice >= modelo.getRowCount()) {
            return false;
        }
        int enVista = tabla.convertRowIndexToView(indice);
        if (enVista < 0) {
            return false;
        }
        tabla.setRowSelectionInterval(enVista, enVista);
        tabla.scrollRectToVisible(tabla.getCellRect(enVista, 0, true));
        return true;
    }

    /**
     * Ejecuta la acción cada vez que cambia la fila elegida.
     *
     * @param accion acción a ejecutar
     */
    public void alSeleccionar(Runnable accion) {
        Objects.requireNonNull(accion, "accion");
        tabla.getSelectionModel().addListSelectionListener(e -> accion.run());
    }

    /**
     * Ejecuta la acción al hacer doble clic sobre una fila (no sobre el espacio vacío de la tabla).
     *
     * @param accion acción a ejecutar; puede comprobar por su cuenta si la acción está disponible
     */
    public void alDobleClic(Runnable accion) {
        Objects.requireNonNull(accion, "accion");
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabla.rowAtPoint(e.getPoint()) >= 0) {
                    accion.run();
                }
            }
        });
    }

    /**
     * Muestra solo las filas cuya columna contiene el texto, sin distinguir mayúsculas ni acentos
     * (ver {@link FiltroTexto}). Un texto vacío o {@code null} quita el filtro.
     *
     * @param columna índice de la columna en el modelo
     * @param texto   texto buscado
     */
    public void filtrarPorTexto(int columna, String texto) {
        ordenador.setRowFilter(texto == null || texto.isBlank() ? null : new RowFilter<M, Integer>() {
            @Override
            public boolean include(Entry<? extends M, ? extends Integer> fila) {
                return FiltroTexto.contiene(fila.getStringValue(columna), texto);
            }
        });
    }

    /**
     * Muestra solo las filas del modelo que cumplen el criterio.
     *
     * @param criterio condición que debe cumplir cada fila
     */
    public void filtrar(Predicate<? super F> criterio) {
        Objects.requireNonNull(criterio, "criterio");
        ordenador.setRowFilter(new RowFilter<M, Integer>() {
            @Override
            public boolean include(Entry<? extends M, ? extends Integer> fila) {
                return criterio.test(filaDelModelo.apply(fila.getIdentifier()));
            }
        });
    }

    /**
     * Cuenta las filas del modelo.
     *
     * @return número de filas sin filtrar
     */
    public int totalFilas() {
        return modelo.getRowCount();
    }

    /**
     * Cuenta las filas que deja ver el filtro.
     *
     * @return número de filas visibles
     */
    public int filasVisibles() {
        return tabla.getRowCount();
    }
}
