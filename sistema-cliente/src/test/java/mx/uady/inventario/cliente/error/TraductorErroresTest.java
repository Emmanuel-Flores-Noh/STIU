package mx.uady.inventario.cliente.error;

import mx.uady.inventario.common.error.CodigoError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link TraductorErrores}.
 * Valida la traducción de errores técnicos a mensajes comprensibles para el usuario,
 * la preservación del error TIMEOUT (E005) y la determinación de reintentabilidad.
 */
class TraductorErroresTest {

    @Test
    @Tag("RF-37")
    @Tag("RF-40")
    @DisplayName("RF-40 · Caso Normal: Con Sistema 1 caído, maneja código E005 (TIMEOUT) del Broker")
    void traducirError_CasoNormal_ManejaCodigoE005() {
        ErrorServicioException errorTimeout = new ErrorServicioException(
                CodigoError.TIMEOUT, "Timeout esperando respuesta del proveedor");

        DescripcionError descripcion = TraductorErrores.describir(errorTimeout);

        assertNotNull(descripcion);
        assertEquals("Tiempo de espera agotado", descripcion.titulo());
        assertTrue(descripcion.mensaje().contains("E005"), "Debe incluir el código E005");
        assertTrue(descripcion.mensaje().contains("Timeout esperando respuesta del proveedor"));
        assertTrue(descripcion.reintentable(), "El timeout del broker debe ser reintentable");
    }

    @Test
    @Tag("RF-37")
    @DisplayName("RF-37 · Caso Normal: Traduce errores de transporte de red a mensajes comprensibles")
    void traducirError_CasoNormal_ErroresDeRed() {
        ComunicacionException sinConexion = new ComunicacionException(
                ComunicacionError.SIN_CONEXION, "Conexión rechazada", null);
        DescripcionError descSinConexion = TraductorErrores.describir(sinConexion);
        assertTrue(descSinConexion.mensaje().contains("Broker esté encendido"));
        assertTrue(descSinConexion.reintentable());

        ComunicacionException conexionPerdida = new ComunicacionException(
                ComunicacionError.CONEXION_INTERRUMPIDA, "Reset por peer", null);
        DescripcionError descPerdida = TraductorErrores.describir(conexionPerdida);
        assertTrue(descPerdida.mensaje().contains("Se perdió la conexión"));
        assertTrue(descPerdida.reintentable());
    }

    @Test
    @Tag("RF-33")
    @DisplayName("RF-33 · Caso Error: Rechazo de venta por PARAMETROS_INVALIDOS o RECURSO_NO_ENCONTRADO no es reintentable")
    void traducirError_CasoError_ParametrosInvalidosNoReintentable() {
        ErrorServicioException datosInvalidos = new ErrorServicioException(
                CodigoError.PARAMETROS_INVALIDOS, "La mercancía ya fue vendida");

        DescripcionError descripcion = TraductorErrores.describir(datosInvalidos);

        assertEquals("Operación rechazada", descripcion.titulo());
        assertEquals("La mercancía ya fue vendida", descripcion.mensaje());
        assertFalse(descripcion.reintentable(), "RF-33 exige que no se ofrezca reintentar");
        assertFalse(TraductorErrores.esReintentable(datosInvalidos));
    }

    @Test
    @Tag("RF-33")
    @DisplayName("RF-33 · Caso Error: RECURSO_NO_ENCONTRADO no es reintentable")
    void traducirError_CasoError_RecursoNoEncontradoNoReintentable() {
        ErrorServicioException noEncontrado = new ErrorServicioException(
                CodigoError.RECURSO_NO_ENCONTRADO, "La tienda indicada no existe");

        DescripcionError descripcion = TraductorErrores.describir(noEncontrado);

        assertEquals("Operación rechazada", descripcion.titulo());
        assertEquals("La tienda indicada no existe", descripcion.mensaje());
        assertFalse(descripcion.reintentable());
    }

    @Test
    @Tag("RF-38")
    @DisplayName("RF-38 · Caso Normal: Fallos de red o servidor interno son reintentables")
    void traducirError_CasoNormal_FalloServidorReintentable() {
        ErrorServicioException errorInterno = new ErrorServicioException(
                CodigoError.ERROR_INTERNO, "Falla en base de datos central");

        DescripcionError descripcion = TraductorErrores.describir(errorInterno);

        assertEquals("Error del sistema central", descripcion.titulo());
        assertTrue(descripcion.reintentable(), "RF-38 permite reintento en fallos internos del servidor");
        assertTrue(TraductorErrores.esReintentable(errorInterno));
    }

    @Test
    @Tag("RF-37")
    @DisplayName("RF-37 · Caso Límite: Error con código nulo o mensaje vacío usa valores por defecto")
    void traducirError_CasoLimite_MensajeVacioONulo() {
        ErrorServicioException sinMensaje = new ErrorServicioException(CodigoError.TIMEOUT, "");
        DescripcionError desc = TraductorErrores.describir(sinMensaje);

        assertNotNull(desc.mensaje());
        assertTrue(desc.mensaje().contains(CodigoError.TIMEOUT.getMensajePorDefecto()));

        ErrorServicioException errorNull = new ErrorServicioException(null, null);
        DescripcionError descNull = TraductorErrores.describir(errorNull);
        assertEquals("Error del servidor", descNull.titulo());
        assertEquals("El servidor rechazó la operación.", descNull.mensaje());
    }

    @Test
    @Tag("RF-37")
    @DisplayName("RF-37 · Caso Límite: Desenvolver excepciones anidadas en ExecutionException")
    void traducirError_CasoLimite_DesenvolverExcepcionesAnidadas() {
        ComunicacionException causaReal = new ComunicacionException(ComunicacionError.TIEMPO_AGOTADO, "Timeout local", null);
        ExecutionException envuelta = new ExecutionException("Fallo en tarea asíncrona", causaReal);

        DescripcionError descripcion = TraductorErrores.describir(envuelta);

        assertEquals("Tiempo de espera agotado", descripcion.titulo());
        assertTrue(descripcion.reintentable());
    }

    @Test
    @Tag("RF-37")
    @DisplayName("RF-37 · Caso Error: Excepción desconocida produce mensaje genérico seguro")
    void traducirError_CasoError_ExcepcionDesconocida() {
        RuntimeException inesperada = new RuntimeException("Error inesperado en JVM");

        DescripcionError descripcion = TraductorErrores.describir(inesperada);

        assertEquals("Error inesperado", descripcion.titulo());
        assertTrue(descripcion.mensaje().contains("Ocurrió un problema inesperado"));
        assertTrue(descripcion.reintentable());
    }
}