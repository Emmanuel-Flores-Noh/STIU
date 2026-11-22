package mx.uady.inventario.cliente;

import com.formdev.flatlaf.FlatIntelliJLaf;
import mx.uady.inventario.cliente.controller.EjecutorAsincrono;
import mx.uady.inventario.cliente.controller.RegistroVentaController;
import mx.uady.inventario.cliente.network.ClienteTCP;
import mx.uady.inventario.cliente.network.ConfiguracionNetwork;
import mx.uady.inventario.cliente.network.VentaService;
import mx.uady.inventario.cliente.view.IntefazConfiguracion;
import mx.uady.inventario.cliente.view.RegistroVentaFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Punto de entrada principal para el Sistema 2 (Cliente del Proveedor).
 * 
 * <p>Valida la configuración de red.</p>
 */
/**
 * Punto de entrada del Sistema 2 (Cliente del Proveedor). Valida la configuración de red, arma los
 * objetos de la aplicación (cliente TCP, servicio, controlador y ventana) y abre la ventana.
 */
public final class Main {

    private Main() {
    }

    /**
     * Inicia el sistema. Si la configuración de red no es válida, informa el motivo y termina con código 1.
     *
     * @param args argumentos de línea de comandos (no se usan)
     */
    public static void main(String[] args) {
        ConfiguracionNetwork configuracion = leerConfiguracionOSalir();
        imprimirResumen(configuracion);
        IntefazConfiguracion.aplicarConfiguracion();
        SwingUtilities.invokeLater(() -> abrirVentana(configuracion));
    }

    private static ConfiguracionNetwork leerConfiguracionOSalir() {
        try {
            return ConfiguracionNetwork.desdeEntorno();
        } catch (RuntimeException e) {
            String mensaje = "Configuración de red inválida en el Sistema Cliente: " + e.getMessage();
            System.err.println(mensaje);
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(null, mensaje, "No se pudo iniciar", JOptionPane.ERROR_MESSAGE);
            }
            System.exit(1);
            throw e;
        }
    }

    private static void imprimirResumen(ConfiguracionNetwork configuracion) {
        System.out.println("==================================================");
        System.out.println("  Iniciando Sistema Cliente del Proveedor");
        System.out.println("  Sistema de Trazabilidad e Inventarios UADY");
        System.out.println("  Broker Destino: " + configuracion.brokerHost() + ":" + configuracion.brokerPuerto());
        System.out.println("  Timeout configurado: " + configuracion.timeoutMs() + " ms");
        System.out.println("==================================================");
        if (configuracion.timeoutInferiorAlRecomendado()) {
            System.err.println("ADVERTENCIA: " + ConfiguracionNetwork.VAR_CLIENT_TIMEOUT + " es menor a "
                    + ConfiguracionNetwork.TIMEOUT_MINIMO_RECOMENDADO_MS
                    + " ms; el cliente podría rendirse antes de recibir la respuesta del Broker.");
        }
    }

    private static void abrirVentana(ConfiguracionNetwork configuracion) {
        ClienteTCP cliente = new ClienteTCP(
                configuracion.brokerHost(), configuracion.brokerPuerto(), configuracion.timeoutMs());
        VentaService servicio = new VentaService(cliente);

        RegistroVentaFrame ventana = new RegistroVentaFrame();
        RegistroVentaController controlador = new RegistroVentaController(ventana, servicio, EjecutorAsincrono.swing());
        ventana.setOperaciones(controlador);
        ventana.setVisible(true);
        controlador.cargarListas();
    }
}