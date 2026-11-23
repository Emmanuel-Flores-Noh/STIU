package mx.uady.inventario.cliente.view.components;

import com.formdev.flatlaf.FlatClientProperties;
import mx.uady.inventario.cliente.model.QrDTO;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Ventana «Código QR»: muestra la imagen del QR, el número de serie y el nombre del producto, y
 * permite guardar la imagen como PNG. Si la imagen no puede mostrarse, lo indica en la misma ventana.
 *
 * <p>Trazabilidad con Requisitos:
 * <ul>
 *   <li><b>RF-34:</b> Despliegue de ventana modal «Código QR» con imagen en Base64, serie y producto. En caso de fallo muestra «No se pudo mostrar la imagen del código QR.».</li>
 *   <li><b>RF-35:</b> Guardado del código QR mostrado como archivo PNG local mediante botón «Guardar png como».</li>
 *   <li><b>RNF-23:</b> Desacoplamiento de componentes de interfaz gráfica.</li>
 * </ul>
 * </p>
 */
public class QrPreviewDialog extends JDialog {

    /** Texto que se muestra cuando la imagen no puede mostrarse. */
    public static final String TEXTO_SIN_IMAGEN = "No se pudo mostrar la imagen del código QR.";

    private static final int TAMANO_IMAGEN = 250;

    private final QrDTO qr;
    private final byte[] png;
    private final OperacionDialog mensajes;

    /**
     * Crea la ventana modal.
     *
     * @param parent ventana propietaria
     * @param qr     datos del QR a mostrar
     */
    public QrPreviewDialog(Window parent, QrDTO qr) {
        super(parent, "Código QR", ModalityType.APPLICATION_MODAL);
        this.qr = qr;
        this.png = qr.decodificarPng();
        this.mensajes = new OperacionDialog(this);
        setSize(400, 500);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        BufferedImage imagen = leerImagen(png);
        JPanel principal = new JPanel(new BorderLayout(15, 15));
        principal.setBorder(new EmptyBorder(20, 20, 20, 20));
        principal.add(crearPanelInfo(), BorderLayout.NORTH);
        principal.add(crearPanelImagen(imagen), BorderLayout.CENTER);
        principal.add(crearPanelBotones(imagen != null), BorderLayout.SOUTH);
        add(principal);
    }

    private JPanel crearPanelInfo() {
        JPanel info = new JPanel(new GridLayout(2, 1, 5, 5));
        JLabel lblSerie = new JLabel("Número de serie: " + qr.getNumeroSerie());
        lblSerie.setFont(lblSerie.getFont().deriveFont(Font.BOLD, 14f));
        info.add(lblSerie);
        info.add(new JLabel("Producto: " + textoProducto()));
        return info;
    }

    private String textoProducto() {
        boolean hayNombre = qr.getNombreProducto() != null && !qr.getNombreProducto().isBlank();
        String id = qr.getIdProducto() != null ? "ID " + qr.getIdProducto() : null;
        if (hayNombre) {
            return id != null ? qr.getNombreProducto() + " (" + id + ")" : qr.getNombreProducto();
        }
        return id != null ? id : "N/D";
    }

    private JPanel crearPanelImagen(BufferedImage imagen) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #FFFFFF;");
        panel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        JLabel lblImagen = new JLabel();
        lblImagen.setName("lblImagenQr");
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        if (imagen != null) {
            lblImagen.setIcon(new ImageIcon(imagen.getScaledInstance(TAMANO_IMAGEN, TAMANO_IMAGEN, Image.SCALE_SMOOTH)));
        } else {
            lblImagen.setText(TEXTO_SIN_IMAGEN);
        }
        panel.add(lblImagen);
        return panel;
    }

    private JPanel crearPanelBotones(boolean puedeGuardar) {
        JButton btnGuardar = new JButton("Guardar png como");
        btnGuardar.setName("btnGuardarPng");
        btnGuardar.setEnabled(puedeGuardar);
        btnGuardar.addActionListener(e -> guardarComoPng());

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(btnGuardar);
        panel.add(btnCerrar);
        return panel;
    }

    /** Pide la ruta, agrega «.png» si falta, confirma antes de reemplazar y guarda. Si se cancela, no crea archivo. */
    private void guardarComoPng() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar código QR");
        selector.setFileFilter(new FileNameExtensionFilter("Imagen PNG", "png"));
        selector.setSelectedFile(new File(nombreSeguro(qr.getNumeroSerie()) + ".png"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File destino = selector.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".png")) {
            destino = new File(destino.getParentFile(), destino.getName() + ".png");
        }
        if (destino.exists() && JOptionPane.showConfirmDialog(this,
                "El archivo " + destino.getName() + " ya existe.\n¿Desea reemplazarlo?",
                "Archivo existente", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Files.write(destino.toPath(), png);
            mensajes.informar("QR guardado", "Código QR guardado en:\n" + destino.getAbsolutePath());
        } catch (IOException e) {
            mensajes.error("Error al guardar", "No se pudo guardar el archivo:\n" + e.getMessage());
        }
    }

    /** Evita que caracteres no válidos en nombres de archivo (p. ej. «/») rompan el nombre sugerido. */
    private static String nombreSeguro(String nombre) {
        return nombre == null || nombre.isBlank() ? "codigo-qr" : nombre.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /** Decodifica los bytes PNG; devuelve {@code null} si no hay bytes o no son una imagen válida. */
    private static BufferedImage leerImagen(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            return null;
        }
    }
}
