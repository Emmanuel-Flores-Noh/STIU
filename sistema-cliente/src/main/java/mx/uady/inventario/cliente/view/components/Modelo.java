package mx.uady.inventario.cliente.view.components;

import mx.uady.inventario.cliente.model.MercanciaDTO;
import mx.uady.inventario.cliente.util.FormateadorTexto;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de solo lectura que guarda las unidades como {@link MercanciaDTO}.
 */
public class Modelo extends AbstractTableModel {

    /** Índice de la columna del número de serie. */
    public static final int COLUMNA_SERIE = 0;
    /** Índice de la columna del nombre del producto. */
    public static final int COLUMNA_PRODUCTO = 1;
    /** Índice de la columna del estado. */
    public static final int COLUMNA_ESTADO = 2;

    private static final String[] COLUMNAS = {"Número de serie", "Producto", "Estado"};

    private final List<MercanciaDTO> unidades = new ArrayList<>();

    /**
     * Reemplaza todas las unidades del modelo.
     *
     * @param nuevas unidades a mostrar; puede ser nula (equivale a vacía)
     */
    public void reemplazar(List<MercanciaDTO> nuevas) {
        unidades.clear();
        if (nuevas != null) {
            unidades.addAll(nuevas);
        }
        fireTableDataChanged();
    }

    /**
     * Obtiene la unidad de una fila del modelo.
     *
     * @param filaModelo índice de la fila en el modelo (no en la vista)
     * @return unidad de esa fila
     */
    public MercanciaDTO getUnidad(int filaModelo) {
        return unidades.get(filaModelo);
    }

    @Override
    public int getRowCount() {
        return unidades.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public String getColumnName(int columna) {
        return COLUMNAS[columna];
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        MercanciaDTO unidad = unidades.get(fila);
        return switch (columna) {
            case COLUMNA_SERIE -> unidad.getNumeroSerie();
            case COLUMNA_PRODUCTO -> unidad.getNombreProducto();
            case COLUMNA_ESTADO -> FormateadorTexto.estadoATexto(unidad.getEstado());
            default -> null;
        };
    }
}
