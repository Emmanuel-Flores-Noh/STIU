package mx.uady.inventario.broker.network;

import com.google.gson.JsonObject;
import mx.uady.inventario.broker.model.Proveedor;
import mx.uady.inventario.broker.util.BitacoraBroker;
import mx.uady.inventario.broker.util.ConfiguracionBroker;
import mx.uady.inventario.common.contrato.EstadoRespuesta;
import mx.uady.inventario.common.contrato.Respuesta;
import mx.uady.inventario.common.contrato.Solicitud;
import mx.uady.inventario.common.error.CodigoError;
import mx.uady.inventario.common.json.GsonFactory;
import mx.uady.inventario.common.servicio.Operaciones;
import mx.uady.inventario.common.servicio.Servicios;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de las operaciones de administración que el broker atiende por socket.
 *
 * <p>Requisitos cubiertos:</p>
 * <ul>
 *   <li>RF-41: registrar un proveedor en tiempo de ejecución.</li>
 *   <li>RF-42: actualizar el host y el puerto de un proveedor registrado.</li>
 *   <li>RF-43: dar de baja el proveedor de un servicio.</li>
 *   <li>RF-46: atender varias solicitudes sucesivas en una misma conexión.</li>
 *   <li>RF-47: rechazar una solicitud mal formada.</li>
 *   <li>RF-49: responder OPERACION_NO_SOPORTADA a una operación desconocida del broker.</li>
 * </ul>
 */
class ManejadorConexionTest {

    private BrokerServer broker;
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;

    @BeforeEach
    void iniciarBroker() throws IOException {
        broker = new BrokerServer(new ConfiguracionBroker(0, 2, 500, 500, 0, 0, 3, 30_000),
                new BitacoraBroker(linea -> { }));
        broker.iniciar();
        socket = new Socket("localhost", broker.getPuerto());
        salida = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    @AfterEach
    void detenerBroker() throws IOException {
        socket.close();
        broker.detener();
    }

    private Respuesta enviar(Solicitud solicitud) throws IOException {
        return enviarLinea(GsonFactory.DEFAULT.toJson(solicitud));
    }

    private Respuesta enviarLinea(String linea) throws IOException {
        salida.println(linea);
        return GsonFactory.DEFAULT.fromJson(entrada.readLine(), Respuesta.class);
    }

    private static Solicitud operacionBroker(String id, String operacion, String servicio, String host, int puerto) {
        JsonObject parametros = new JsonObject();
        parametros.addProperty("servicio", servicio);
        parametros.addProperty("host", host);
        parametros.addProperty("puerto", puerto);
        return Solicitud.crear(id, Servicios.BROKER, operacion, parametros);
    }

    @Test
    @Tag("RF-41")
    @DisplayName("RF-41 · Registrar un proveedor válido responde éxito y lo agrega al registro")
    void registrarProveedorValidoRespondeExitoYLoAgregaAlRegistro() throws IOException {
        Respuesta respuesta = enviar(operacionBroker("reg-1", Operaciones.REGISTRAR_PROVEEDOR,
                Servicios.INVENTARIO, "localhost", 9001));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        assertEquals("reg-1", respuesta.getIdSolicitud());
        assertEquals(Optional.of(new Proveedor(Servicios.INVENTARIO, "localhost", 9001)),
                broker.getRegistro().buscar(Servicios.INVENTARIO));
    }

    @Test
    @Tag("RF-42")
    @DisplayName("RF-42 · Actualizar un servicio registrado responde éxito y cambia su host y puerto")
    void actualizarServicioRegistradoRespondeExitoYCambiaSuHostYPuerto() throws IOException {
        broker.getRegistro().registrar(new Proveedor(Servicios.INVENTARIO, "localhost", 9001));

        Respuesta respuesta = enviar(operacionBroker("act-1", Operaciones.ACTUALIZAR_PROVEEDOR,
                Servicios.INVENTARIO, "otro-host", 9002));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        assertEquals("act-1", respuesta.getIdSolicitud());
        assertEquals(Optional.of(new Proveedor(Servicios.INVENTARIO, "otro-host", 9002)),
                broker.getRegistro().buscar(Servicios.INVENTARIO));
    }

    @Test
    @Tag("RF-43")
    @DisplayName("RF-43 · Remover un servicio registrado responde éxito y lo quita del registro")
    void removerServicioRegistradoRespondeExitoYLoQuitaDelRegistro() throws IOException {
        broker.getRegistro().registrar(new Proveedor(Servicios.INVENTARIO, "localhost", 9001));

        Respuesta respuesta = enviar(operacionBroker("rem-1", Operaciones.REMOVER_PROVEEDOR,
                Servicios.INVENTARIO, "localhost", 9001));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        assertEquals("rem-1", respuesta.getIdSolicitud());
        assertTrue(broker.getRegistro().buscar(Servicios.INVENTARIO).isEmpty());
    }

    @Test
    @Tag("RF-49")
    @DisplayName("RF-49 · Una operación desconocida del broker responde OPERACION_NO_SOPORTADA conservando el id")
    void operacionDesconocidaDelBrokerRespondeNoSoportadaConservandoElId() throws IOException {
        Respuesta respuesta = enviar(Solicitud.crear("op-1", Servicios.BROKER, "operacionFantasma", null));

        assertEquals(EstadoRespuesta.ERROR, respuesta.getEstado());
        assertEquals(CodigoError.OPERACION_NO_SOPORTADA, respuesta.getCodigoError());
        assertEquals("op-1", respuesta.getIdSolicitud());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            Operaciones.REGISTRAR_PROVEEDOR,
            Operaciones.ACTUALIZAR_PROVEEDOR,
            Operaciones.REMOVER_PROVEEDOR
    })
    @Tag("RF-47")
    @DisplayName("RF-47 · Una operación de administración sin parámetros responde PARAMETROS_INVALIDOS")
    void operacionDeAdministracionSinParametrosRespondeParametrosInvalidos(String operacion) throws IOException {
        Respuesta respuesta = enviar(Solicitud.crear("sin-params", Servicios.BROKER, operacion, null));

        assertEquals(EstadoRespuesta.ERROR, respuesta.getEstado());
        assertEquals(CodigoError.PARAMETROS_INVALIDOS, respuesta.getCodigoError());
        assertEquals("sin-params", respuesta.getIdSolicitud());
    }

    @Test
    @Tag("RF-46")
    @Tag("RF-47")
    @DisplayName("RF-46, RF-47 · Un JSON malformado responde PARAMETROS_INVALIDOS y la conexión sigue atendiendo")
    void jsonMalformadoRespondeParametrosInvalidosYLaConexionSigueAtendiendo() throws IOException {
        Respuesta malformada = enviarLinea("{esto no es json");
        Respuesta siguiente = enviar(operacionBroker("reg-2", Operaciones.REGISTRAR_PROVEEDOR,
                Servicios.INVENTARIO, "localhost", 9001));

        assertEquals(CodigoError.PARAMETROS_INVALIDOS, malformada.getCodigoError());
        assertEquals(EstadoRespuesta.EXITO, siguiente.getEstado());
        assertEquals("reg-2", siguiente.getIdSolicitud());
    }
}
