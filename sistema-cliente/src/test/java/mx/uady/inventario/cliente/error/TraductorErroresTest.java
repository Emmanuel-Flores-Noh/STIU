package mx.uady.inventario.cliente.error;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el Traductor de Errores.
 * Criterio ERS: RF-14.
 */
public class TraductorErroresTest {

    @Test
    void traducirError_CasoNormal_ManejaCodigoE005() {
        // Caso normal: Con Sistema 1 caído, se recibe E005 del Broker
        String mensaje = TraductorErrores.traducir("E005");
        assertNotNull(mensaje);
        assertTrue(mensaje.contains("Sistema 1") || mensaje.contains("Timeout") || mensaje.contains("Broker"));
    }

    @Test
    void traducirError_CasoLimite_CodigoVacio() {
        // Caso límite: Entrada vacía o nula devuelva mensaje genérico
        String mensaje = TraductorErrores.traducir("");
        assertEquals("Error desconocido", mensaje);
    }

    @Test
    void traducirError_CasoError_CodigoInvalido() {
        // Caso de error: Código no registrado en el catálogo
        String mensaje = TraductorErrores.traducir("E999");
        assertTrue(mensaje.contains("E999") || mensaje.contains("No reconocido"));
    }
}
