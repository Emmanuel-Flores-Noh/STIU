package mx.uady.inventario.broker.routing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de {@link PoliticaReintentos}, sin red.
 *
 * <p>Requisitos cubiertos:</p>
 * <ul>
 *   <li>RF-51: reintentar un número limitado de veces una solicitud que falla por comunicación.</li>
 *   <li>RF-56: informar cada intento fallido para anotarlo en la bitácora.</li>
 *   <li>RF-58: dejar de reintentar cuando el hilo se interrumpe, para detenerse de forma ordenada.</li>
 * </ul>
 */
class PoliticaReintentosTest {

    private final AtomicInteger llamadas = new AtomicInteger();
    private final List<String> fallos = new ArrayList<>();

    @AfterEach
    void limpiarInterrupcion() {
        // Ninguna prueba debe dejar el hilo interrumpido para la siguiente.
        Thread.interrupted();
    }

    private void anotarFallo(int intento, int intentosTotales, Exception error) {
        fallos.add(intento + "/" + intentosTotales + " " + error.getMessage());
    }

    /** Intento que falla las primeras {@code fallosAntesDelExito} veces y despues devuelve "OK". */
    private PoliticaReintentos.Intento<String> fallaYLuegoResponde(int fallosAntesDelExito) {
        return () -> {
            int numero = llamadas.incrementAndGet();
            if (numero <= fallosAntesDelExito) {
                throw new IOException("fallo " + numero);
            }
            return "OK";
        };
    }

    @Test
    @Tag("RF-51")
    @DisplayName("RF-51 · Si el primer intento tiene éxito no se reintenta")
    void primerIntentoExitosoNoSeReintenta() throws IOException {
        PoliticaReintentos politica = new PoliticaReintentos(2, 0);

        String resultado = politica.ejecutar(fallaYLuegoResponde(0), this::anotarFallo);

        assertEquals("OK", resultado);
        assertEquals(1, llamadas.get());
        assertTrue(fallos.isEmpty());
    }

    @Test
    @Tag("RF-51")
    @Tag("RF-56")
    @DisplayName("RF-51, RF-56 · Un fallo seguido de un éxito devuelve el resultado e informa el intento fallido")
    void falloSeguidoDeExitoDevuelveElResultadoEInformaElIntentoFallido() throws IOException {
        PoliticaReintentos politica = new PoliticaReintentos(2, 0);

        String resultado = politica.ejecutar(fallaYLuegoResponde(1), this::anotarFallo);

        assertEquals("OK", resultado);
        assertEquals(2, llamadas.get());
        assertEquals(List.of("1/3 fallo 1"), fallos);
    }

    @Test
    @Tag("RF-51")
    @DisplayName("RF-51 · El éxito en el último intento permitido se devuelve")
    void exitoEnElUltimoIntentoPermitidoSeDevuelve() throws IOException {
        PoliticaReintentos politica = new PoliticaReintentos(2, 0);

        String resultado = politica.ejecutar(fallaYLuegoResponde(2), this::anotarFallo);

        assertEquals("OK", resultado);
        assertEquals(3, llamadas.get());
        assertEquals(List.of("1/3 fallo 1", "2/3 fallo 2"), fallos);
    }

    @Test
    @Tag("RF-51")
    @DisplayName("RF-51 · Con cero reintentos se hace un solo intento")
    void conCeroReintentosSeHaceUnSoloIntento() {
        PoliticaReintentos politica = new PoliticaReintentos(0, 0);

        IOException error = assertThrows(IOException.class,
                () -> politica.ejecutar(fallaYLuegoResponde(5), this::anotarFallo));

        assertEquals(1, politica.intentosTotales());
        assertEquals(1, llamadas.get());
        assertEquals("fallo 1", error.getMessage());
        assertEquals(List.of("1/1 fallo 1"), fallos);
    }

    @Test
    @Tag("RF-51")
    @Tag("RF-56")
    @DisplayName("RF-51, RF-56 · Si todos los intentos fallan se propaga el error del último e informa cada uno")
    void siTodosLosIntentosFallanSePropagaElErrorDelUltimoEInformaCadaUno() {
        PoliticaReintentos politica = new PoliticaReintentos(2, 0);

        IOException error = assertThrows(IOException.class,
                () -> politica.ejecutar(fallaYLuegoResponde(10), this::anotarFallo));

        assertEquals(3, llamadas.get());
        assertEquals("fallo 3", error.getMessage());
        assertEquals(List.of("1/3 fallo 1", "2/3 fallo 2", "3/3 fallo 3"), fallos);
    }

    @Test
    @Tag("RF-51")
    @DisplayName("RF-51 · Una excepción no comprobada también se reintenta y se propaga tal cual")
    void excepcionNoComprobadaTambienSeReintentaYSePropagaTalCual() {
        PoliticaReintentos politica = new PoliticaReintentos(1, 0);
        IllegalStateException esperada = new IllegalStateException("respuesta ilegible");

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> politica.ejecutar(() -> {
            llamadas.incrementAndGet();
            throw esperada;
        }, this::anotarFallo));

        assertSame(esperada, error);
        assertEquals(2, llamadas.get());
    }

    @Test
    @Tag("RF-51")
    @Tag("RF-58")
    @DisplayName("RF-51, RF-58 · Si el hilo se interrumpe durante la espera se dejan de reintentar")
    void siElHiloSeInterrumpeDuranteLaEsperaSeDejanDeReintentar() {
        PoliticaReintentos politica = new PoliticaReintentos(3, 60_000);
        // Con la bandera puesta, la espera termina de inmediato con InterruptedException.
        Thread.currentThread().interrupt();

        assertThrows(IOException.class, () -> politica.ejecutar(fallaYLuegoResponde(10), this::anotarFallo));

        assertEquals(1, llamadas.get());
        assertTrue(Thread.currentThread().isInterrupted(), "La interrupción debe conservarse para quien la pidió");
    }

    @Test
    @Tag("RF-51")
    @DisplayName("RF-51 · Con espera cero no se duerme entre intentos")
    void conEsperaCeroNoSeDuermeEntreIntentos() {
        PoliticaReintentos politica = new PoliticaReintentos(2, 0);
        // Si la politica durmiera, la bandera de interrupcion cortaria los reintentos.
        Thread.currentThread().interrupt();

        assertThrows(IOException.class, () -> politica.ejecutar(fallaYLuegoResponde(10), this::anotarFallo));

        assertEquals(3, llamadas.get());
    }

    @Test
    @Tag("RF-51")
    @DisplayName("RF-51 · Entre intentos se espera el tiempo configurado")
    void entreIntentosSeEsperaElTiempoConfigurado() {
        PoliticaReintentos politica = new PoliticaReintentos(2, 50);
        long inicio = System.nanoTime();

        assertThrows(IOException.class, () -> politica.ejecutar(fallaYLuegoResponde(10), this::anotarFallo));

        long transcurridoMillis = (System.nanoTime() - inicio) / 1_000_000;
        assertTrue(transcurridoMillis >= 100, "Dos esperas de 50 ms, transcurrido: " + transcurridoMillis + " ms");
    }

    @Test
    @Tag("RF-51")
    @DisplayName("RF-51 · Reintentos o espera negativos se rechazan y cero se acepta")
    void reintentosOEsperaNegativosSeRechazanYCeroSeAcepta() {
        assertThrows(IllegalArgumentException.class, () -> new PoliticaReintentos(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new PoliticaReintentos(0, -1));
        assertDoesNotThrow(() -> new PoliticaReintentos(0, 0));
    }
}
