package mx.uady.inventario.broker.routing;

import mx.uady.inventario.broker.model.Proveedor;
import mx.uady.inventario.broker.registry.RegistroServicios;
import mx.uady.inventario.common.contrato.Respuesta;
import mx.uady.inventario.common.contrato.Solicitud;
import mx.uady.inventario.common.json.GsonFactory;
import mx.uady.inventario.common.servicio.Servicios;
import mx.uady.inventario.broker.util.BitacoraBroker;
import mx.uady.inventario.common.contrato.EstadoRespuesta;
import mx.uady.inventario.common.error.CodigoError;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnrutadorTest {

    @Test
    @Tag("RF-45")
    @DisplayName("RF-45 · Enruta la solicitud al proveedor y devuelve su respuesta")
    void enrutaAlProveedorYDevuelveRespuesta() throws Exception {
        ServerSocket provider = new ServerSocket(0);
        int port = provider.getLocalPort();
        CountDownLatch listo = new CountDownLatch(1);

        Thread hilo = new Thread(() -> {
            try {
                Socket socket = provider.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                String linea = in.readLine();
                Solicitud recibida = GsonFactory.DEFAULT.fromJson(linea, Solicitud.class);
                Respuesta respuesta = Respuesta.exito(recibida.getIdSolicitud(),
                        new com.google.gson.JsonPrimitive("OK"));
                out.println(GsonFactory.DEFAULT.toJson(respuesta));
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                listo.countDown();
            }
        });
        hilo.start();

        RegistroServicios registro = new RegistroServicios();
        registro.registrar(new Proveedor(Servicios.INVENTARIO, "localhost", port));

        Enrutador enrutador = new Enrutador(2000);
        Solicitud solicitud = Solicitud.crear("req-1", Servicios.INVENTARIO, "listarInventario",
                new com.google.gson.JsonObject());
        Respuesta respuesta = enrutador.enrutar(solicitud, registro);

        assertEquals("req-1", respuesta.getIdSolicitud());
        assertEquals(mx.uady.inventario.common.contrato.EstadoRespuesta.EXITO, respuesta.getEstado());
        assertEquals("OK", respuesta.getResultado().getAsString());

        listo.await();
        provider.close();
    }

    @Test
    @Tag("RF-48")
    @DisplayName("RF-48 · Un servicio sin proveedor responde SERVICIO_DESCONOCIDO")
    void servicioDesconocido() {
        RegistroServicios registro = new RegistroServicios();
        Enrutador enrutador = new Enrutador(2000);

        Solicitud solicitud = Solicitud.crear("req-2", Servicios.INVENTARIO, "listarInventario",
                new com.google.gson.JsonObject());
        Respuesta respuesta = enrutador.enrutar(solicitud, registro);

        assertEquals(mx.uady.inventario.common.error.CodigoError.SERVICIO_DESCONOCIDO, respuesta.getCodigoError());
    }

    @Test
    @Tag("RF-47")
    @DisplayName("RF-47 · Una solicitud nula se rechaza")
    void solicitudInvalidaLanza() {
        RegistroServicios registro = new RegistroServicios();
        Enrutador enrutador = new Enrutador(2000);

        assertThrows(IllegalArgumentException.class, () -> enrutador.enrutar(null, registro));
    }

    @Test
    @Tag("RF-53")
    @DisplayName("RF-53 · Un proveedor que cierra sin responder produce un error del broker")
    void proveedorSinRespuesta() throws Exception {
        ServerSocket provider = new ServerSocket(0);
        int port = provider.getLocalPort();

        Thread hilo = new Thread(() -> {
            try {
                Socket socket = provider.accept();
                socket.close();
            } catch (Exception ignored) {
            }
        });
        hilo.start();

        RegistroServicios registro = new RegistroServicios();
        registro.registrar(new Proveedor(Servicios.INVENTARIO, "localhost", port));

        Enrutador enrutador = new Enrutador(500);
        Solicitud solicitud = Solicitud.crear("req-4", Servicios.INVENTARIO, "listarInventario",
                new com.google.gson.JsonObject());
        Respuesta respuesta = enrutador.enrutar(solicitud, registro);

        assertTrue(respuesta.getEstado() == mx.uady.inventario.common.contrato.EstadoRespuesta.ERROR);

        provider.close();
        hilo.join();
    }

    @Test
    @Tag("RF-51")
    @DisplayName("RF-51 · Reintenta hasta agotar los intentos configurados")
    void reintentaHastaAgotarLosIntentos() throws Exception {
        ServerSocket provider = new ServerSocket(0);
        int port = provider.getLocalPort();
        int intentosEsperados = 3;
        AtomicInteger conexiones = new AtomicInteger();
        CountDownLatch aceptadas = new CountDownLatch(intentosEsperados);

        Thread hilo = new Thread(() -> {
            try {
                while (conexiones.get() < intentosEsperados) {
                    Socket socket = provider.accept();
                    conexiones.incrementAndGet();
                    // Se cierra sin responder: simula un proveedor que se cae.
                    socket.close();
                    aceptadas.countDown();
                }
            } catch (Exception ignored) {
            }
        });
        hilo.start();

        RegistroServicios registro = new RegistroServicios();
        registro.registrar(new Proveedor(Servicios.INVENTARIO, "localhost", port));
        // reintentos = 2 -> 3 intentos en total
        Enrutador enrutador = new Enrutador(500, 500, 2, 10);

        Solicitud solicitud = Solicitud.crear("req-retry", Servicios.INVENTARIO, "listarInventario",
                new com.google.gson.JsonObject());
        Respuesta respuesta = enrutador.enrutar(solicitud, registro);

        assertTrue(aceptadas.await(5, java.util.concurrent.TimeUnit.SECONDS));
        assertEquals(intentosEsperados, conexiones.get());
        assertEquals(mx.uady.inventario.common.contrato.EstadoRespuesta.ERROR, respuesta.getEstado());
        assertEquals("req-retry", respuesta.getIdSolicitud());

        provider.close();
        hilo.join();
    }

    @Test
    @Tag("RF-45")
    @Tag("RF-51")
    @DisplayName("RF-45, RF-51 · Una respuesta de error del proveedor no se reintenta")
    void unaRespuestaDeErrorNoSeReintenta() throws Exception {
        ServerSocket provider = new ServerSocket(0);
        int port = provider.getLocalPort();
        AtomicInteger conexiones = new AtomicInteger();

        Thread hilo = new Thread(() -> {
            try {
                Socket socket = provider.accept();
                conexiones.incrementAndGet();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                Solicitud recibida = GsonFactory.DEFAULT.fromJson(in.readLine(), Solicitud.class);
                // El proveedor responde con un error de negocio valido.
                Respuesta respuesta = Respuesta.error(recibida.getIdSolicitud(),
                        mx.uady.inventario.common.error.CodigoError.OPERACION_NO_SOPORTADA, "no soportada");
                out.println(GsonFactory.DEFAULT.toJson(respuesta));
                socket.close();
            } catch (Exception ignored) {
            }
        });
        hilo.start();

        RegistroServicios registro = new RegistroServicios(3, 60_000);
        registro.registrar(new Proveedor(Servicios.INVENTARIO, "localhost", port));
        Enrutador enrutador = new Enrutador(2000, 2000, 2, 10);

        Solicitud solicitud = Solicitud.crear("req-error", Servicios.INVENTARIO, "listarInventario",
                new com.google.gson.JsonObject());
        Respuesta respuesta = enrutador.enrutar(solicitud, registro);

        assertEquals(mx.uady.inventario.common.error.CodigoError.OPERACION_NO_SOPORTADA, respuesta.getCodigoError());
        assertEquals(1, conexiones.get());
        // La comunicacion fue exitosa: no debe acumular fallos.
        assertEquals(0, registro.fallos(Servicios.INVENTARIO));

        provider.close();
        hilo.join();
    }

    @Test
    @Tag("RF-50")
    @DisplayName("RF-50 · Un proveedor lento agota el tiempo límite de lectura y responde TIMEOUT")
    void usaElTimeoutDeLecturaConfigurado() throws Exception {
        ServerSocket provider = new ServerSocket(0);
        int port = provider.getLocalPort();

        Thread hilo = new Thread(() -> {
            try {
                Socket socket = provider.accept();
                Thread.sleep(1500);
                socket.close();
            } catch (Exception ignored) {
            }
        });
        hilo.start();

        RegistroServicios registro = new RegistroServicios();
        registro.registrar(new Proveedor(Servicios.INVENTARIO, "localhost", port));
        // Conexion holgada y lectura corta: si se usara el timeout de conexion, tardaria mucho mas.
        Enrutador enrutador = new Enrutador(5000, 300, 0, 0);

        Solicitud solicitud = Solicitud.crear("req-timeout", Servicios.INVENTARIO, "listarInventario",
                new com.google.gson.JsonObject());
        long inicio = System.nanoTime();
        Respuesta respuesta = enrutador.enrutar(solicitud, registro);
        long transcurridoMs = (System.nanoTime() - inicio) / 1_000_000;

        assertEquals(mx.uady.inventario.common.error.CodigoError.TIMEOUT, respuesta.getCodigoError());
        assertTrue(transcurridoMs < 1500, "debio cortar por timeout de lectura, tardo " + transcurridoMs + " ms");

        provider.close();
        hilo.join();
    }

    @Test
    @Tag("RF-52")
    @DisplayName("RF-52 · Un proveedor no disponible responde PROVEEDOR_NO_REGISTRADO sin contactarlo")
    void proveedorNoDisponibleNoSeContacta() throws Exception {
        ServerSocket proveedor = new ServerSocket(0);
        int port = proveedor.getLocalPort();
        RegistroServicios registro = new RegistroServicios(1, 60_000);
        registro.registrar(new Proveedor(Servicios.INVENTARIO, "localhost", port));
        registro.marcarFallo(Servicios.INVENTARIO);

        Enrutador enrutador = new Enrutador(500, 500, 2, 10);
        Solicitud solicitud = Solicitud.crear("req-nd", Servicios.INVENTARIO, "listarInventario",
                new com.google.gson.JsonObject());
        Respuesta respuesta = enrutador.enrutar(solicitud, registro);

        assertEquals(mx.uady.inventario.common.error.CodigoError.PROVEEDOR_NO_REGISTRADO, respuesta.getCodigoError());
        assertTrue(respuesta.getMensajeError().contains("no disponible"));
        // El proveedor sigue escuchando: nadie se conecto.
        proveedor.setSoTimeout(300);
        assertThrows(java.net.SocketTimeoutException.class, proveedor::accept);
        proveedor.close();
    }

    @Test
    @Tag("RF-52")
    @DisplayName("RF-52 · Fallos repetidos marcan al proveedor como no disponible")
    void fallosRepetidosMarcanElProveedorNoDisponible() throws Exception {
        // Puerto cerrado: las conexiones se rechazan de inmediato.
        ServerSocket temporal = new ServerSocket(0);
        int port = temporal.getLocalPort();
        temporal.close();

        RegistroServicios registro = new RegistroServicios(2, 60_000);
        registro.registrar(new Proveedor(Servicios.INVENTARIO, "localhost", port));
        Enrutador enrutador = new Enrutador(500, 500, 0, 0);

        Respuesta primera = enrutador.enrutar(
                Solicitud.crear("req-f1", Servicios.INVENTARIO, "listarInventario", new com.google.gson.JsonObject()),
                registro);
        assertEquals(mx.uady.inventario.common.contrato.EstadoRespuesta.ERROR, primera.getEstado());
        assertTrue(registro.estaDisponible(Servicios.INVENTARIO));

        Respuesta segunda = enrutador.enrutar(
                Solicitud.crear("req-f2", Servicios.INVENTARIO, "listarInventario", new com.google.gson.JsonObject()),
                registro);
        assertEquals(mx.uady.inventario.common.contrato.EstadoRespuesta.ERROR, segunda.getEstado());
        assertFalse(registro.estaDisponible(Servicios.INVENTARIO));
        assertEquals(2, registro.fallos(Servicios.INVENTARIO));

        Respuesta tercera = enrutador.enrutar(
                Solicitud.crear("req-f3", Servicios.INVENTARIO, "listarInventario", new com.google.gson.JsonObject()),
                registro);
        assertEquals(mx.uady.inventario.common.error.CodigoError.PROVEEDOR_NO_REGISTRADO, tercera.getCodigoError());
    }

    /**
     * La bitacora anota cada intento fallido hacia el proveedor y, al alcanzar el umbral,
     * que el servicio quedo marcado como no disponible (una sola vez).
     */
    @Test
    @Tag("RF-52")
    @Tag("RF-56")
    @DisplayName("RF-52, RF-56 · La bitácora anota cada intento fallido y la marca de no disponible")
    void laBitacoraAnotaLosIntentosFallidosYLaMarcaDeNoDisponible() throws Exception {
        ServerSocket temporal = new ServerSocket(0);
        int port = temporal.getLocalPort();
        temporal.close();

        java.util.List<String> lineas = new java.util.ArrayList<>();
        RegistroServicios registro = new RegistroServicios(1, 60_000);
        registro.registrar(new Proveedor(Servicios.INVENTARIO, "localhost", port));
        // reintentos = 1 -> 2 intentos en total
        Enrutador enrutador = new Enrutador(500, 500, 1, 0,
                new mx.uady.inventario.broker.util.BitacoraBroker(lineas::add));

        enrutador.enrutar(Solicitud.crear("req-b1", Servicios.INVENTARIO, "listarInventario",
                new com.google.gson.JsonObject()), registro);
        enrutador.enrutar(Solicitud.crear("req-b2", Servicios.INVENTARIO, "listarInventario",
                new com.google.gson.JsonObject()), registro);

        String todas = String.join("\n", lineas);
        assertEquals(3, lineas.size(), todas);
        assertTrue(lineas.get(0).contains("id=req-b1 inventario.listarInventario -> localhost:" + port
                + " | intento 1/2 fallo: ERROR_INTERNO"), todas);
        assertTrue(lineas.get(1).contains("| intento 2/2 fallo: ERROR_INTERNO"), todas);
        assertTrue(lineas.get(2).startsWith("[REGISTRO]")
                && lineas.get(2).endsWith("inventario marcado como NO DISPONIBLE tras 1 fallos"), todas);
        // La segunda solicitud ya no contacta al proveedor: no agrega intentos.
    }

    // ---------------------------------------------------------------------
    // Pruebas con un ConectorProveedor falso: sin sockets ni esperas reales.
    // ---------------------------------------------------------------------

    /**
     * Conector que devuelve o lanza, en orden, los resultados programados.
     * El ultimo resultado se repite si se agotan.
     */
    private static final class ConectorFalso extends ConectorProveedor {
        private final Deque<Object> resultados = new ArrayDeque<>();
        private int llamadas;

        ConectorFalso(Object... resultados) {
            super(1, 1);
            this.resultados.addAll(List.of(resultados));
        }

        @Override
        public Respuesta enviar(Solicitud solicitud, Proveedor proveedor) throws IOException {
            llamadas++;
            Object resultado = resultados.size() > 1 ? resultados.poll() : resultados.peek();
            if (resultado instanceof IOException e) {
                throw e;
            }
            if (resultado instanceof RuntimeException e) {
                throw e;
            }
            return (Respuesta) resultado;
        }
    }

    private final List<String> bitacora = new ArrayList<>();

    private Enrutador enrutadorCon(ConectorFalso conector, int reintentos) {
        return new Enrutador(conector, new PoliticaReintentos(reintentos, 0), new BitacoraBroker(bitacora::add));
    }

    private static RegistroServicios registroConInventario(int fallosParaIndisponible) {
        RegistroServicios registro = new RegistroServicios(fallosParaIndisponible, 60_000);
        registro.registrar(new Proveedor(Servicios.INVENTARIO, "localhost", 9001));
        return registro;
    }

    private static Solicitud solicitudInventario(String id) {
        return Solicitud.crear(id, Servicios.INVENTARIO, "listarInventario", null);
    }

    private static Respuesta exito(String id) {
        return Respuesta.exito(id, new com.google.gson.JsonPrimitive("OK"));
    }

    @Test
    @Tag("RF-45")
    @DisplayName("RF-45 · Una respuesta exitosa al primer intento se devuelve y limpia los fallos previos")
    void respuestaExitosaAlPrimerIntentoSeDevuelveYLimpiaLosFallosPrevios() {
        RegistroServicios registro = registroConInventario(3);
        registro.marcarFallo(Servicios.INVENTARIO);
        Respuesta esperada = exito("ok-1");
        ConectorFalso conector = new ConectorFalso(esperada);

        Respuesta respuesta = enrutadorCon(conector, 2).enrutar(solicitudInventario("ok-1"), registro);

        assertSame(esperada, respuesta);
        assertEquals(1, conector.llamadas);
        assertEquals(0, registro.fallos(Servicios.INVENTARIO));
        assertTrue(bitacora.isEmpty());
    }

    @Test
    @Tag("RF-45")
    @Tag("RF-51")
    @DisplayName("RF-45, RF-51 · Una respuesta de error del proveedor se devuelve sin reintentar")
    void respuestaDeErrorDelProveedorSeDevuelveSinReintentar() {
        RegistroServicios registro = registroConInventario(3);
        Respuesta rechazo = Respuesta.error("err-1", CodigoError.RECURSO_NO_ENCONTRADO, "No existe");
        ConectorFalso conector = new ConectorFalso(rechazo);

        Respuesta respuesta = enrutadorCon(conector, 2).enrutar(solicitudInventario("err-1"), registro);

        assertSame(rechazo, respuesta);
        assertEquals(1, conector.llamadas);
        assertEquals(0, registro.fallos(Servicios.INVENTARIO));
    }

    @Test
    @Tag("RF-51")
    @Tag("RF-56")
    @DisplayName("RF-51, RF-56 · Un fallo de comunicación seguido de un éxito devuelve la respuesta y anota el intento")
    void falloDeComunicacionSeguidoDeExitoDevuelveLaRespuestaYAnotaElIntento() {
        RegistroServicios registro = registroConInventario(3);
        ConectorFalso conector = new ConectorFalso(new IOException("Connection reset"), exito("rt-1"));

        Respuesta respuesta = enrutadorCon(conector, 2).enrutar(solicitudInventario("rt-1"), registro);

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        assertEquals(2, conector.llamadas);
        assertEquals(0, registro.fallos(Servicios.INVENTARIO));
        assertEquals(1, bitacora.size(), String.join("\n", bitacora));
        assertTrue(bitacora.get(0).contains("id=rt-1") && bitacora.get(0).contains("intento 1/3 fallo: ERROR_INTERNO"),
                bitacora.get(0));
    }

    @Test
    @Tag("RF-50")
    @Tag("RF-51")
    @DisplayName("RF-50, RF-51 · Si todos los intentos agotan el tiempo responde TIMEOUT con el id y suma un fallo")
    void siTodosLosIntentosAgotanElTiempoRespondeTimeoutConElIdYSumaUnFallo() {
        RegistroServicios registro = registroConInventario(3);
        ConectorFalso conector = new ConectorFalso(new SocketTimeoutException("Read timed out"));

        Respuesta respuesta = enrutadorCon(conector, 2).enrutar(solicitudInventario("to-1"), registro);

        assertEquals(CodigoError.TIMEOUT, respuesta.getCodigoError());
        assertEquals("to-1", respuesta.getIdSolicitud());
        assertEquals("Tiempo de espera agotado al contactar al proveedor", respuesta.getMensajeError());
        assertEquals(3, conector.llamadas);
        assertEquals(1, registro.fallos(Servicios.INVENTARIO));
        assertEquals(3, bitacora.size(), String.join("\n", bitacora));
    }

    @Test
    @Tag("RF-51")
    @Tag("RF-53")
    @DisplayName("RF-51, RF-53 · Si todos los intentos fallan por comunicación responde ERROR_INTERNO con la causa")
    void siTodosLosIntentosFallanPorComunicacionRespondeErrorInternoConLaCausa() {
        RegistroServicios registro = registroConInventario(3);
        ConectorFalso conector = new ConectorFalso(new IOException("Connection refused"));

        Respuesta respuesta = enrutadorCon(conector, 1).enrutar(solicitudInventario("ei-1"), registro);

        assertEquals(CodigoError.ERROR_INTERNO, respuesta.getCodigoError());
        assertEquals("ei-1", respuesta.getIdSolicitud());
        assertTrue(respuesta.getMensajeError().contains("Connection refused"), respuesta.getMensajeError());
        assertEquals(2, conector.llamadas);
    }

    @Test
    @Tag("RF-53")
    @DisplayName("RF-53 · Una excepción no prevista del envío responde ERROR_INTERNO sin propagarse")
    void excepcionNoPrevistaDelEnvioRespondeErrorInternoSinPropagarse() {
        RegistroServicios registro = registroConInventario(3);
        ConectorFalso conector = new ConectorFalso(new IllegalStateException("JSON ilegible"));

        Respuesta respuesta = enrutadorCon(conector, 0).enrutar(solicitudInventario("ei-2"), registro);

        assertEquals(CodigoError.ERROR_INTERNO, respuesta.getCodigoError());
        assertEquals("ei-2", respuesta.getIdSolicitud());
        assertEquals(1, conector.llamadas);
    }

    @Test
    @Tag("RF-48")
    @DisplayName("RF-48 · Un servicio sin proveedor responde SERVICIO_DESCONOCIDO sin contactar a nadie")
    void servicioSinProveedorRespondeServicioDesconocidoSinContactarANadie() {
        ConectorFalso conector = new ConectorFalso(exito("sd-1"));

        Respuesta respuesta = enrutadorCon(conector, 2).enrutar(solicitudInventario("sd-1"), new RegistroServicios());

        assertEquals(CodigoError.SERVICIO_DESCONOCIDO, respuesta.getCodigoError());
        assertEquals("sd-1", respuesta.getIdSolicitud());
        assertEquals(0, conector.llamadas);
    }

    @Test
    @Tag("RF-52")
    @Tag("RF-56")
    @DisplayName("RF-52, RF-56 · Al alcanzar el umbral el proveedor queda no disponible y ya no se contacta")
    void alAlcanzarElUmbralElProveedorQuedaNoDisponibleYYaNoSeContacta() {
        RegistroServicios registro = registroConInventario(1);
        ConectorFalso conector = new ConectorFalso(new IOException("Connection refused"));
        Enrutador enrutador = enrutadorCon(conector, 0);

        Respuesta primera = enrutador.enrutar(solicitudInventario("nd-1"), registro);
        Respuesta segunda = enrutador.enrutar(solicitudInventario("nd-2"), registro);

        assertEquals(CodigoError.ERROR_INTERNO, primera.getCodigoError());
        assertEquals(CodigoError.PROVEEDOR_NO_REGISTRADO, segunda.getCodigoError());
        assertEquals("nd-2", segunda.getIdSolicitud());
        assertEquals(1, conector.llamadas);
        assertFalse(registro.estaDisponible(Servicios.INVENTARIO));
        assertTrue(bitacora.get(bitacora.size() - 1).endsWith("inventario marcado como NO DISPONIBLE tras 1 fallos"),
                String.join("\n", bitacora));
    }

    @Test
    @Tag("RF-51")
    @DisplayName("RF-51 · El enrutador exige un conector, una política y una bitácora")
    void enrutadorExigeUnConectorUnaPoliticaYUnaBitacora() {
        ConectorFalso conector = new ConectorFalso(exito("x"));
        PoliticaReintentos politica = new PoliticaReintentos(0, 0);
        BitacoraBroker vacia = new BitacoraBroker(linea -> { });

        assertThrows(NullPointerException.class, () -> new Enrutador(null, politica, vacia));
        assertThrows(NullPointerException.class, () -> new Enrutador(conector, null, vacia));
        assertThrows(NullPointerException.class, () -> new Enrutador(conector, politica, null));
    }
}
