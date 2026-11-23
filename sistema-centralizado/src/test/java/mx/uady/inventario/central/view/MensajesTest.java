package mx.uady.inventario.central.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.ConnectException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import mx.uady.inventario.central.service.AccesoDatosException;
import mx.uady.inventario.central.service.NegocioException;

class MensajesTest {

    @Test
    @Tag("RNF-30")
    @DisplayName("RNF-30 · Usa el mensaje de la excepción de negocio")
    void usaElMensajeDeLaExcepcionDeNegocio() {
        assertEquals("El número de serie ya existe.",
                Mensajes.describir(new NegocioException("El número de serie ya existe.")));
    }

    @Test
    @Tag("RNF-30")
    @DisplayName("RNF-30 · Encuentra la excepción de negocio envuelta")
    void encuentraLaExcepcionDeNegocioEnvuelta() {
        Throwable envuelta = new ExecutionException(new RuntimeException(new NegocioException("Precio inválido.")));
        assertEquals("Precio inválido.", Mensajes.describir(envuelta));
    }

    @Test
    @Tag("RNF-18")
    @Tag("RNF-30")
    @DisplayName("RNF-18, RNF-30 · Traduce fallos de conexión a un mensaje de conexión")
    void traduceFallosDeConexionAMensajeDeConexion() {
        assertEquals(Mensajes.ERROR_CONEXION,
                Mensajes.describir(new SQLException("Socket fail to connect", "08000")));
        assertEquals(Mensajes.ERROR_CONEXION,
                Mensajes.describir(new RuntimeException(new ConnectException("Connection refused"))));
    }

    @Test
    @Tag("RNF-30")
    @DisplayName("RNF-30 · Distingue fallos de almacenamiento de los de conexión")
    void distingueFallosDeAlmacenamientoDeLosDeConexion() {
        assertEquals(Mensajes.ERROR_ALMACENAMIENTO,
                Mensajes.describir(new AccesoDatosException("QR", new java.io.IOException("disco lleno"))));
        assertEquals(Mensajes.ERROR_ALMACENAMIENTO,
                Mensajes.describir(new AccesoDatosException("guardar", new SQLException("Data too long", "22001"))));
        assertEquals(Mensajes.ERROR_CONEXION,
                Mensajes.describir(new AccesoDatosException("listar", new SQLException("Connection lost", "08S01"))));
    }

    @Test
    @Tag("RNF-30")
    @DisplayName("RNF-30 · Un error recién cerrado no se repite hasta que pasa el silencio")
    void unErrorRecienCerradoNoSeRepiteHastaQuePasaElSilencio() {
        String texto = "Error de prueba " + System.nanoTime();
        assertTrue(Mensajes.debeMostrarse(texto, 1_000_000));

        Mensajes.marcarCerrado(texto, 1_000_000);
        assertFalse(Mensajes.debeMostrarse(texto, 1_000_000 + Mensajes.SILENCIO_TRAS_CERRAR_MS - 1));
        assertTrue(Mensajes.debeMostrarse(texto, 1_000_000 + Mensajes.SILENCIO_TRAS_CERRAR_MS));
        assertTrue(Mensajes.debeMostrarse("Otro texto " + System.nanoTime(), 1_000_000));
    }

    @Test
    @Tag("RNF-18")
    @Tag("RNF-30")
    @DisplayName("RNF-18, RNF-30 · Oculta los detalles de errores inesperados")
    void ocultaLosDetallesDeErroresInesperados() {
        assertEquals(Mensajes.ERROR_INESPERADO, Mensajes.describir(new NullPointerException("campo x")));
    }
}
