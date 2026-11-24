package mx.uady.inventario.cliente.view;

import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Aplica el aspecto visual (FlatLaf) de la aplicación. Si no puede aplicarse, la interfaz sigue
 */
public final class InterfazConfiguracion {
    private static final Logger LOGGER = Logger.getLogger(InterfazConfiguracion.class.getName());
    private InterfazConfiguracion(){}

    /**
     * Instala FlatLaf y sus propiedades globales. Debe llamarse antes de crear ventanas.
     * Si falla, solo no se utiliza simplemente
     */
    public static void aplicarConfiguracion(){
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arrowType", "chevron");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,"No se pudo aplicar FlatLaf; se continuará con el Look & Feel por defecto de Swing.", e);
        }
    }
}
