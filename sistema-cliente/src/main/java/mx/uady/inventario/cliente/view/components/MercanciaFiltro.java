package mx.uady.inventario.cliente.view.components;


import mx.uady.inventario.cliente.util.FormateadorTexto;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

/**
 * Filtro en tiempo real del inventario sin distinguir mayúsculas ni acentos.
 *
 * <p>Trazabilidad con Requisitos:
 * <ul>
 *   <li><b>RF-27:</b> Filtrado de inventario por número de serie o nombre de producto (búsqueda parcial insensible a mayúsculas y acentos, actualización en tiempo real).</li>
 *   <li><b>RNF-07:</b> Actualización fluida y eficiente por cada evento del teclado sobre el inventario.</li>
 * </ul>
 * </p>
 */
public class MercanciaFiltro {

    private final TableRowSorter<Modelo> sorter;
    private final JTextField campoBusqueda;

    /**
     * Instala el filtro en la tabla y escucha los cambios del campo de búsqueda.
     *
     * @param tabla         tabla sobre la que se filtran las filas
     * @param modelo        modelo que respalda la tabla
     * @param campoBusqueda campo donde se escribe el texto a buscar
     */
    public MercanciaFiltro(JTable tabla, Modelo modelo, JTextField campoBusqueda) {
        this.sorter = new TableRowSorter<>(modelo);
        this.campoBusqueda = campoBusqueda;
        tabla.setRowSorter(sorter);
        campoBusqueda.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizar();
            }
        });
    }

    private void actualizar() {
        String texto = campoBusqueda.getText();
        sorter.setRowFilter(texto.isBlank() ? null : crearFiltro(texto));
    }

    /**
     * Crea el filtro de filas para un texto de búsqueda.
     *
     * <p>Requisito: RF-27 (coincidencia parcial sin mayúsculas ni acentos en serie o producto).</p>
     * @param texto texto buscado; no debe estar vacío
     * @return filtro que acepta las filas cuyo serie o producto contienen el texto
     */
    public static RowFilter<Modelo, Integer> crearFiltro(String texto) {
        String buscado = FormateadorTexto.normalizar(texto.trim());
        return new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends Modelo, ? extends Integer> fila) {
                return FormateadorTexto.normalizar(fila.getStringValue(Modelo.COLUMNA_SERIE)).contains(buscado)
                        || FormateadorTexto.normalizar(fila.getStringValue(Modelo.COLUMNA_PRODUCTO)).contains(buscado);
            }
        };
    }
}
