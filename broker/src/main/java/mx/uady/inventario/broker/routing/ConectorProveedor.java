package mx.uady.inventario.broker.routing;

import mx.uady.inventario.broker.model.Proveedor;
import mx.uady.inventario.common.contrato.Respuesta;
import mx.uady.inventario.common.contrato.Solicitud;
import mx.uady.inventario.common.json.GsonFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Envia una solicitud a un proveedor por TCP y lee su respuesta.
 *
 * <p>Aplica por separado el timeout de conexion y el de lectura, y descarta
 * cualquier respuesta vacia, invalida o con un id distinto al de la solicitud.</p>
 */
public class ConectorProveedor {
    private final int timeoutConexionMillis;
    private final int timeoutLecturaMillis;

    /**
     * Crea un conector con los timeouts de red indicados.
     *
     * @param timeoutConexionMillis timeout de conexion al proveedor
     * @param timeoutLecturaMillis timeout de lectura de la respuesta
     * @throws IllegalArgumentException si algun timeout no es positivo
     */
    public ConectorProveedor(int timeoutConexionMillis, int timeoutLecturaMillis) {
        if (timeoutConexionMillis <= 0) {
            throw new IllegalArgumentException("timeoutConexionMillis debe ser positivo");
        }
        if (timeoutLecturaMillis <= 0) {
            throw new IllegalArgumentException("timeoutLecturaMillis debe ser positivo");
        }
        this.timeoutConexionMillis = timeoutConexionMillis;
        this.timeoutLecturaMillis = timeoutLecturaMillis;
    }

    /**
     * Envia la solicitud al proveedor y espera su respuesta, sea exitosa o no.
     *
     * @param solicitud La solicitud a redireccionar
     * @param proveedor El proveedor al cual se conecta el socket
     * @return La respuesta del servicio, sea exitosa o no
     * @throws java.net.SocketTimeoutException si se agota el timeout de conexion o de lectura
     * @throws IOException si no se pudo conectar, leer o la respuesta no es utilizable
     */
    public Respuesta enviar(Solicitud solicitud, Proveedor proveedor) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(proveedor.host(), proveedor.puerto()), timeoutConexionMillis);
            socket.setSoTimeout(timeoutLecturaMillis);

            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            BufferedReader entrada = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            salida.println(GsonFactory.DEFAULT.toJson(solicitud));
            salida.flush();

            String respuestaJson = entrada.readLine();
            if (respuestaJson == null || respuestaJson.isBlank()) {
                throw new IOException("El proveedor no devolvio respuesta");
            }

            Respuesta respuesta = GsonFactory.DEFAULT.fromJson(respuestaJson, Respuesta.class);
            if (respuesta == null) {
                throw new IOException("Respuesta invalida del proveedor");
            }

            if (!Objects.equals(respuesta.getIdSolicitud(), solicitud.getIdSolicitud())) {
                throw new IOException("El id de la respuesta no coincide con la solicitud");
            }

            return respuesta;
        }
    }
}
