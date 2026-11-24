package mx.uady.inventario.broker.routing;

import com.google.gson.JsonPrimitive;
import mx.uady.inventario.broker.model.Proveedor;
import mx.uady.inventario.common.contrato.EstadoRespuesta;
import mx.uady.inventario.common.contrato.Respuesta;
import mx.uady.inventario.common.contrato.Solicitud;
import mx.uady.inventario.common.json.GsonFactory;
import mx.uady.inventario.common.servicio.Servicios;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de {@link ConectorProveedor} contra un proveedor simulado en un puerto efímero.
 *
 * <p>Requisitos cubiertos:</p>
 * <ul>
 *   <li>RF-45: reenviar la solicitud al proveedor y devolver su respuesta.</li>
 *   <li>RF-50: aplicar tiempos límite de conexión y de lectura.</li>
 *   <li>RF-53: tratar como fallo cualquier respuesta no utilizable.</li>
 *   <li>RNF-28: no entregar una respuesta que corresponde a otra solicitud.</li>
 * </ul>
 */
class ConectorProveedorTest {

    private final CountDownLatch liberar = new CountDownLatch(1);
    private ServerSocket servidor;
    private Thread hilo;

    @AfterEach
    void detenerProveedor() throws Exception {
        liberar.countDown();
        if (servidor != null) {
            servidor.close();
        }
        if (hilo != null) {
            hilo.join(2_000);
        }
    }

    /**
     * Levanta un proveedor que atiende una conexion y responde la linea que produce la funcion.
     * Si la funcion devuelve {@code null} cierra la conexion sin responder.
     */
    private Proveedor proveedorQueResponde(Function<Solicitud, String> respuesta) throws IOException {
        servidor = new ServerSocket(0);
        hilo = new Thread(() -> {
            try (Socket s = servidor.accept()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(s.getOutputStream(), true, StandardCharsets.UTF_8);
                String linea = respuesta.apply(GsonFactory.DEFAULT.fromJson(in.readLine(), Solicitud.class));
                if (linea != null) {
                    out.println(linea);
                }
            } catch (IOException ignored) {
            }
        });
        hilo.start();
        return new Proveedor(Servicios.INVENTARIO, "localhost", servidor.getLocalPort());
    }

    /** Levanta un proveedor que recibe la solicitud y no responde hasta que termina la prueba. */
    private Proveedor proveedorQueNoResponde() throws IOException {
        return proveedorQueResponde(solicitud -> {
            try {
                liberar.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        });
    }

    private static String exito(String id) {
        return GsonFactory.DEFAULT.toJson(Respuesta.exito(id, new JsonPrimitive("OK")));
    }

    private static Solicitud solicitud(String id) {
        return Solicitud.crear(id, Servicios.INVENTARIO, "listarInventario", null);
    }

    @Test
    @Tag("RF-45")
    @DisplayName("RF-45 · Devuelve la respuesta del proveedor cuando corresponde a la solicitud")
    void devuelveLaRespuestaDelProveedorCuandoCorrespondeALaSolicitud() throws IOException {
        Proveedor proveedor = proveedorQueResponde(s -> exito(s.getIdSolicitud()));

        Respuesta respuesta = new ConectorProveedor(1_000, 1_000).enviar(solicitud("req-1"), proveedor);

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        assertEquals("req-1", respuesta.getIdSolicitud());
    }

    @Test
    @Tag("RNF-28")
    @DisplayName("RNF-28 · Una respuesta con otro id se rechaza")
    void respuestaConOtroIdSeRechaza() throws IOException {
        Proveedor proveedor = proveedorQueResponde(s -> exito("otra-solicitud"));

        IOException error = assertThrows(IOException.class,
                () -> new ConectorProveedor(1_000, 1_000).enviar(solicitud("req-2"), proveedor));

        assertTrue(error.getMessage().contains("no coincide"), error.getMessage());
    }

    @Test
    @Tag("RF-53")
    @DisplayName("RF-53 · Un proveedor que cierra sin responder se trata como fallo")
    void proveedorQueCierraSinResponderSeTrataComoFallo() throws IOException {
        Proveedor proveedor = proveedorQueResponde(s -> null);

        IOException error = assertThrows(IOException.class,
                () -> new ConectorProveedor(1_000, 1_000).enviar(solicitud("req-3"), proveedor));

        assertTrue(error.getMessage().contains("no devolvio respuesta"), error.getMessage());
    }

    @Test
    @Tag("RF-50")
    @DisplayName("RF-50 · Un proveedor que no responde agota el tiempo límite de lectura")
    void proveedorQueNoRespondeAgotaElTiempoLimiteDeLectura() throws IOException {
        Proveedor proveedor = proveedorQueNoResponde();
        long inicio = System.nanoTime();

        assertThrows(SocketTimeoutException.class,
                () -> new ConectorProveedor(1_000, 200).enviar(solicitud("req-4"), proveedor));

        long transcurridoMillis = (System.nanoTime() - inicio) / 1_000_000;
        assertTrue(transcurridoMillis >= 200, "Debe esperar el timeout de lectura: " + transcurridoMillis + " ms");
    }

    @Test
    @Tag("RF-53")
    @DisplayName("RF-53 · Una conexión rechazada se trata como fallo de comunicación")
    void conexionRechazadaSeTrataComoFalloDeComunicacion() throws IOException {
        int puertoCerrado;
        try (ServerSocket temporal = new ServerSocket(0)) {
            puertoCerrado = temporal.getLocalPort();
        }
        Proveedor proveedor = new Proveedor(Servicios.INVENTARIO, "localhost", puertoCerrado);

        assertThrows(IOException.class,
                () -> new ConectorProveedor(1_000, 1_000).enviar(solicitud("req-5"), proveedor));
    }

    @Test
    @Tag("RF-50")
    @DisplayName("RF-50 · Los tiempos límite deben ser positivos y 1 ms es el mínimo aceptado")
    void tiemposLimiteDebenSerPositivosYUnMilisegundoEsElMinimoAceptado() {
        assertThrows(IllegalArgumentException.class, () -> new ConectorProveedor(0, 1_000));
        assertThrows(IllegalArgumentException.class, () -> new ConectorProveedor(1_000, 0));
        assertThrows(IllegalArgumentException.class, () -> new ConectorProveedor(-1, 1_000));
        assertDoesNotThrow(() -> new ConectorProveedor(1, 1));
    }
}
