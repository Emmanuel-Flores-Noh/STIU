package mx.uady.inventario.cliente.view;

import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.*;

/**
 * Aplica el aspecto visual (FlatLaf) de la aplicación. Si no puede aplicarse, la interfaz sigue
 *
 * funcionando con el aspecto por defecto de Swing.
 * <p>Trazabilidad con Requisitos:
 * <ul>
 *   <li><b>RNF-23:</b> Estandarización de componentes visuales en la aplicación cliente.</li>
 * </ul>
 * </p>
 */
public final class IntefazConfiguracion {
    private IntefazConfiguracion(){}

    /**
     * Instala FlatLaf y sus propiedades globales. Debe llamarse antes de crear ventanas.
     */
    public static void aplicarConfiguracion(){
        try{
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Button.arc",8);
            UIManager.put("Component.arrowType","chevron");
        } catch (Exception e) {
            throw new RuntimeException("Fallo al inicializar FlatLaf: " + e.getMessage());
        }
    }
}
