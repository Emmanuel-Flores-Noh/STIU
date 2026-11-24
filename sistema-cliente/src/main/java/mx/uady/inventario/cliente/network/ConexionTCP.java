package mx.uady.inventario.cliente.network;

import mx.uady.inventario.cliente.error.ComunicacionException;
import mx.uady.inventario.cliente.error.ComunicacionError;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Transporte TCP de una línea de texto: abre un socket, envía una línea UTF-8 y lee otra de vuelta.
 *
 * <p>El tiempo límite es <b>total</b> (conexión + espera de respuesta): lo que se gasta conectando
 * se descuenta del tiempo de lectura. No reintenta; cada fallo se clasifica en una
 * {@link ComunicacionException}.</p>
 */
public class ConexionTCP {

    private final String host;
    private final int puerto;
    private final int timeoutMs;

    /**
     * Crea el transporte.
     *
     * <p>Requisitos: RF-39, RF-40.</p>
     * @param host      host o IP del Broker
     * @param puerto    puerto del Broker
     * @param timeoutMs tiempo límite total, en milisegundos
     */
    public ConexionTCP(String host, int puerto, int timeoutMs) {
        this.host = host;
        this.puerto = puerto;
        this.timeoutMs = timeoutMs;
    }

    /**
     * Envía una línea y espera la línea de respuesta.
     *
     * <p>Requisitos: RF-39, RF-40, RF-37.</p>
     * @param linea texto a enviar (sin salto de línea)
     * @return línea recibida, sin salto de línea
     * @throws ComunicacionException si no hay conexión, se agota el tiempo o se cierra la conexión
     */
    public String intercambiar(String linea) throws ComunicacionException {
        long limite = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
        try (Socket socket = new Socket()) {
            conectar(socket);
            socket.setSoTimeout(milisRestantes(limite));
            enviar(socket, linea);
            return recibir(socket);
        } catch (ComunicacionException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw new ComunicacionException(ComunicacionError.TIEMPO_AGOTADO,
                    "Se agotó el tiempo de espera (" + timeoutMs + " ms) del Broker " + host + ":" + puerto, e);
        } catch (IOException e) {
            throw new ComunicacionException(ComunicacionError.CONEXION_INTERRUMPIDA,
                    "Se interrumpió la comunicación con el Broker " + host + ":" + puerto, e);
        }
    }

    /**
     * Establece la conexión del socket TCP con el Broker aplicando el timeout correspondiente.
     *
     * <p>Requisitos: RF-39, RF-40, RF-37.</p>
     *
     * @param socket instancia de socket a conectar
     * @throws ComunicacionException si se agota el tiempo o no hay conexión con el Broker
     */
    private void conectar(Socket socket) throws ComunicacionException {
        try {
            socket.connect(new InetSocketAddress(host, puerto), timeoutMs);
        } catch (SocketTimeoutException e) {
            throw new ComunicacionException(ComunicacionError.TIEMPO_AGOTADO,
                    "Se agotó el tiempo al conectar con el Broker " + host + ":" + puerto, e);
        } catch (IOException e) {
            throw new ComunicacionException(ComunicacionError.SIN_CONEXION,
                    "No se pudo conectar con el Broker " + host + ":" + puerto, e);
        }
    }

    /**
     * Escribe la trama de texto en el canal de salida UTF-8 terminada con salto de línea y vacía el búfer.
     *
     * @param socket socket conectado
     * @param linea  cadena a transmitir
     * @throws IOException si falla la escritura en el socket
     */
    private static void enviar(Socket socket, String linea) throws IOException {
        BufferedWriter salida = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        salida.write(linea);
        salida.write('\n');
        salida.flush();
    }

    /**
     * Lee una línea de respuesta desde el canal de entrada del socket.
     *
     * <p>Requisito: RF-37.</p>
     *
     * @param socket socket conectado
     * @return línea recibida desde el Broker
     * @throws IOException           si ocurre un error de lectura de bajo nivel
     * @throws ComunicacionException si la conexión se cierra inesperadamente sin recibir datos
     */
    private static String recibir(Socket socket) throws IOException, ComunicacionException {
        BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        String respuesta = entrada.readLine();
        if (respuesta == null || respuesta.isBlank()) {
            throw new ComunicacionException(ComunicacionError.CONEXION_INTERRUMPIDA,"El Broker cerró la conexión sin responder.", null);
        }
        return respuesta;
    }

    /**
     * Calcula los milisegundos restantes para el timeout de lectura descontando el tiempo de conexión.
     *
     * <p>Requisito: RF-40.</p>
     *
     * @param limiteNanos tiempo límite absoluto en nanosegundos
     * @return milisegundos restantes para la operación de socket
     * @throws ComunicacionException si el tiempo límite ya expiró
     */
    private int milisRestantes(long limiteNanos) throws ComunicacionException {
        long restante = TimeUnit.NANOSECONDS.toMillis(limiteNanos - System.nanoTime());
        if (restante <= 0) {
            throw new ComunicacionException(ComunicacionError.TIEMPO_AGOTADO,
                                            "Se agotó el tiempo (" + timeoutMs + " ms) al conectar con el Broker.", null);
        }
        return (int) Math.min(restante, Integer.MAX_VALUE);
    }
}
