package mx.uady.inventario.cliente.controller;

import mx.uady.inventario.cliente.error.DescripcionError;
import mx.uady.inventario.cliente.error.TraductorErrores;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Decide cómo se informa un error al encargado: un mensaje simple si reintentar no sirve, o la
 * pregunta «¿Desea reintentar la operación?» si el fallo fue de red o del servidor. Nunca reintenta
 * por su cuenta.
 */
public class PresentadorErrores {

    private static final Logger BITACORA = Logger.getLogger(PresentadorErrores.class.getName());

    private final VistaController vista;

    /**
     * Crea el presentador.
     *
     * @param vista vista donde se muestran los errores
     */
    public PresentadorErrores(VistaController vista) {
        this.vista = vista;
    }

    /**
     * Informa un error. El mensaje empieza con la frase que dice qué falló.
     *
     * @param contexto  frase que indica qué operación falló (p. ej. «No se pudo registrar la venta.»)
     * @param error     causa del fallo
     * @param reintento acción a ejecutar si el encargado elige reintentar
     */
    public void informar(String contexto, Throwable error, Runnable reintento) {
        BITACORA.log(Level.WARNING, contexto, error);
        DescripcionError descripcion = TraductorErrores.describir(error);
        String mensaje = contexto + "\n" + descripcion.mensaje();
        if (descripcion.reintentable()) {
            vista.mostrarDialogoReintentar(descripcion.titulo(), mensaje, reintento);
        } else {
            vista.mostrarMensajeError(mensaje);
        }
    }
}
