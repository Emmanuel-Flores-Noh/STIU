package mx.uady.inventario.cliente.view.components;


import mx.uady.inventario.cliente.util.FormateadorTexto;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

/**
 * Filtro en tiempo real del inventario: en cada tecla deja las unidades cuyo número de serie o
 * nombre de producto contienen el texto buscado, sin distinguir mayúsculas ni acentos. El texto se
 * toma literalmente (no es una expresión regular), la columna Estado no cuenta y una búsqueda vacía
 * muestra todas las unidades.
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
