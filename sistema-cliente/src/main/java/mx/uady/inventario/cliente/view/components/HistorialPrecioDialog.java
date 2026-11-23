package mx.uady.inventario.cliente.view.components;

import mx.uady.inventario.cliente.model.PrecioDTO;
import mx.uady.inventario.cliente.model.PrecioHistorialDTO;
import mx.uady.inventario.cliente.util.FormateadorTexto;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.Collections;
import java.util.List;

/**
 * Ventana «Historial de precios»: muestra el nombre y el precio actual del producto y la lista de
 * precios anteriores con su fecha, del más antiguo al más reciente. Si el producto nunca cambió de
 * precio, muestra solo el precio actual y lo indica.
 *
 * <p>Trazabilidad con Requisitos:
 * <ul>
 *   <li><b>RF-36:</b> Consulta y despliegue del historial de precios de un producto (nombre, precio actual y tabla cronológica de precios anteriores).</li>
 *   <li><b>RNF-23:</b> Desacoplamiento de componentes de presentación visual.</li>
 * </ul>
 * </p>
 */
public class HistorialPrecioDialog extends JDialog {

    private final PrecioHistorialDTO historial;

    /**
     * Crea la ventana modal.
     *
     * <p>Requisito: RF-36.</p>
     * @param parent    ventana propietaria
     * @param historial datos del historial a mostrar
     */
    public HistorialPrecioDialog(Window parent, PrecioHistorialDTO historial) {
        super(parent, "Historial de precios", ModalityType.APPLICATION_MODAL);
        this.historial = historial;
        setSize(450, 400);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel principal = new JPanel(new BorderLayout(15, 15));
        principal.setBorder(new EmptyBorder(20, 20, 20, 20));
        principal.add(crearPanelResumen(), BorderLayout.NORTH);
        principal.add(crearPanelPrecios(), BorderLayout.CENTER);
        principal.add(crearPanelBotones(), BorderLayout.SOUTH);
        add(principal);
    }

    /**
     * Panel para datos base del historial de precios
     * @return Panel de resumen para historial de precios
     */
    private JPanel crearPanelResumen() {
        JPanel resumen = new JPanel(new GridLayout(2, 1, 5, 5));

        // Por nombre nulo
        String nombre = historial.getNombre() != null ? historial.getNombre() : "N/D";
        JLabel lblNombre = new JLabel("Producto: " + nombre);
        lblNombre.setFont(lblNombre.getFont().deriveFont(Font.BOLD, 14f));

        // Mantener precio si es nulo
        Boolean precioNulo = historial.getPrecioActual() != null;
        String precioTexto = precioNulo ? FormateadorTexto.precio(historial.getPrecioActual()) : "N/D";
        JLabel lblPrecio = new JLabel("Precio actual: " + precioTexto);
        lblPrecio.setName("lblPrecioActual");
        resumen.add(lblNombre);
        resumen.add(lblPrecio);
        return resumen;
    }

    /**
     * Crea panel tipo tabla con el historial de precios
     * @return
     */
    private Component crearPanelPrecios() {
        List<PrecioDTO> historialPrecios = historial.getHistorialPrecios();

        // Maneja si lista es vacia o no
        if (historialPrecios == null || historialPrecios.isEmpty()) {
            JLabel vacio = new JLabel("Este producto no ha cambiado de precio.", SwingConstants.CENTER);
            vacio.setName("lblSinHistorial");
            return vacio;
        }

        DefaultTableModel modelo = new DefaultTableModel(new String[]{"Fecha", "Precio anterior"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        for (PrecioDTO anterior : historialPrecios) {
            modelo.addRow(new Object[]{
                    FormateadorTexto.fecha(anterior.getFecha()),
                    FormateadorTexto.precio(anterior.getPrecio())});
        }
        JTable tabla = new JTable(modelo);
        tabla.setName("tablaHistorialPrecios");
        tabla.setRowHeight(25);
        tabla.getTableHeader().setReorderingAllowed(false);
        return new JScrollPane(tabla);
    }

    private JPanel crearPanelBotones() {
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(btnCerrar);
        return panel;
    }
}
