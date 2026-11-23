package mx.uady.inventario.broker.routing;

import java.io.IOException;

/**
 * Politica de reintentos acotados ante fallos de comunicacion.
 *
 * <p>Ejecuta un intento y, si falla, lo repite hasta {@code reintentos} veces
 * mas, esperando {@code esperaReintentoMillis} entre intentos. Si el hilo se
 * interrumpe durante la espera se dejan de reintentar para no demorar el apagado.</p>
 */
public class PoliticaReintentos {
    private final int reintentos;
    private final long esperaReintentoMillis;

    /**
     * Una operacion que puede fallar por un error de comunicacion.
     *
     * @param <T> el tipo del resultado
     */
    @FunctionalInterface
    public interface Intento<T> {
        /**
         * Ejecuta el intento.
         *
         * @return el resultado del intento
         * @throws IOException si falla la comunicacion
         */
        T ejecutar() throws IOException;
    }

    /**
     * Recibe cada intento fallido.
     */
    @FunctionalInterface
    public interface ObservadorFallo {
        /**
         * Se invoca despues de cada intento fallido.
         *
         * @param intento numero del intento que fallo, desde 1
         * @param intentosTotales intentos totales permitidos
         * @param error el error del intento
         */
        void alFallar(int intento, int intentosTotales, Exception error);
    }

    /**
     * Crea una politica de reintentos.
     *
     * @param reintentos reintentos adicionales tras el primer intento
     * @param esperaReintentoMillis espera entre intentos
     * @throws IllegalArgumentException si algun valor es negativo
     */
    public PoliticaReintentos(int reintentos, long esperaReintentoMillis) {
        if (reintentos < 0) {
            throw new IllegalArgumentException("reintentos no puede ser negativo");
        }
        if (esperaReintentoMillis < 0) {
            throw new IllegalArgumentException("esperaReintentoMillis no puede ser negativo");
        }
        this.reintentos = reintentos;
        this.esperaReintentoMillis = esperaReintentoMillis;
    }

    /**
     * Intentos totales que permite la politica: el primero mas los reintentos.
     *
     * @return {@code reintentos + 1}
     */
    public int intentosTotales() {
        return reintentos + 1;
    }

    /**
     * Ejecuta el intento con reintentos acotados.
     *
     * @param intento la operacion a ejecutar
     * @param observador recibe cada intento fallido
     * @param <T> el tipo del resultado
     * @return el resultado del primer intento exitoso
     * @throws IOException el error del ultimo intento, si todos fallan por comunicacion
     * @throws RuntimeException el error del ultimo intento, si fue una excepcion no comprobada
     */
    public <T> T ejecutar(Intento<T> intento, ObservadorFallo observador) throws IOException {
        int intentosTotales = intentosTotales();
        Exception ultimoError = null;

        for (int numero = 1; numero <= intentosTotales; numero++) {
            try {
                return intento.ejecutar();
            } catch (IOException | RuntimeException e) {
                ultimoError = e;
            }
            observador.alFallar(numero, intentosTotales, ultimoError);

            if (numero < intentosTotales && !esperarAntesDelSiguienteIntento()) {
                break;
            }
        }

        if (ultimoError instanceof IOException e) {
            throw e;
        }
        throw (RuntimeException) ultimoError;
    }

    /**
     * Espera entre intentos. Devuelve {@code false} si el hilo fue interrumpido,
     * en cuyo caso se dejan de reintentar para no demorar el apagado.
     */
    private boolean esperarAntesDelSiguienteIntento() {
        if (esperaReintentoMillis <= 0) {
            return true;
        }
        try {
            Thread.sleep(esperaReintentoMillis);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
