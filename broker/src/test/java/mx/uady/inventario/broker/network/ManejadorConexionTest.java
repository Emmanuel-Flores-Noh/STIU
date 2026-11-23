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
 *   <li>RF-42: actualizar el host y el puerto de un proveedor registrado, o responder
 *       PROVEEDOR_NO_REGISTRADO si no lo está.</li>
 *   <li>RF-43: dar de baja el proveedor de un servicio, o responder PROVEEDOR_NO_REGISTRADO
 *       si no lo está.</li>
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
    @Tag("RF-42")
    @DisplayName("RF-42 · Actualizar un servicio no registrado responde PROVEEDOR_NO_REGISTRADO y no lo agrega")
    void actualizarServicioNoRegistradoRespondeProveedorNoRegistradoYNoLoAgrega() throws IOException {
        Respuesta respuesta = enviar(operacionBroker("act-2", Operaciones.ACTUALIZAR_PROVEEDOR,
                Servicios.INVENTARIO, "localhost", 9001));

        assertEquals(EstadoRespuesta.ERROR, respuesta.getEstado());
        assertEquals(CodigoError.PROVEEDOR_NO_REGISTRADO, respuesta.getCodigoError());
        assertEquals("act-2", respuesta.getIdSolicitud());
        assertTrue(broker.getRegistro().buscar(Servicios.INVENTARIO).isEmpty());
    }

    @Test
    @Tag("RF-43")
    @DisplayName("RF-43 · Remover un servicio no registrado responde PROVEEDOR_NO_REGISTRADO")
    void removerServicioNoRegistradoRespondeProveedorNoRegistrado() throws IOException {
        Respuesta respuesta = enviar(operacionBroker("rem-2", Operaciones.REMOVER_PROVEEDOR,
                Servicios.INVENTARIO, "localhost", 9001));

        assertEquals(EstadoRespuesta.ERROR, respuesta.getEstado());
        assertEquals(CodigoError.PROVEEDOR_NO_REGISTRADO, respuesta.getCodigoError());
        assertEquals("rem-2", respuesta.getIdSolicitud());
    }

    @Test
    @Tag("RF-43")
    @DisplayName("RF-43 · Remover indicando solo el servicio lo quita del registro")
    void removerIndicandoSoloElServicioLoQuitaDelRegistro() throws IOException {
        broker.getRegistro().registrar(new Proveedor(Servicios.INVENTARIO, "localhost", 9001));
        JsonObject parametros = new JsonObject();
        parametros.addProperty("servicio", Servicios.INVENTARIO);

        Respuesta respuesta = enviar(Solicitud.crear("rem-3", Servicios.BROKER,
                Operaciones.REMOVER_PROVEEDOR, parametros));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        assertTrue(broker.getRegistro().buscar(Servicios.INVENTARIO).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            Operaciones.REGISTRAR_PROVEEDOR,
            Operaciones.ACTUALIZAR_PROVEEDOR,
            Operaciones.REMOVER_PROVEEDOR
    })
    @Tag("RF-47")
    @DisplayName("RF-47 · Un servicio ausente, vacío o en blanco responde PARAMETROS_INVALIDOS conservando el id")
    void servicioAusenteVacioOEnBlancoRespondeParametrosInvalidosConservandoElId(String operacion)
            throws IOException {
        JsonObject sinServicio = new JsonObject();
        sinServicio.addProperty("host", "localhost");
        sinServicio.addProperty("puerto", 9001);

        Respuesta ausente = enviar(Solicitud.crear("srv-1", Servicios.BROKER, operacion, sinServicio));
        Respuesta vacio = enviar(operacionBroker("srv-2", operacion, "", "localhost", 9001));
        Respuesta enBlanco = enviar(operacionBroker("srv-3", operacion, "   ", "localhost", 9001));

        assertEquals(CodigoError.PARAMETROS_INVALIDOS, ausente.getCodigoError());
        assertEquals("srv-1", ausente.getIdSolicitud());
        assertEquals(CodigoError.PARAMETROS_INVALIDOS, vacio.getCodigoError());
        assertEquals("srv-2", vacio.getIdSolicitud());
        assertEquals(CodigoError.PARAMETROS_INVALIDOS, enBlanco.getCodigoError());
        assertEquals("srv-3", enBlanco.getIdSolicitud());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 65535})
    @Tag("RF-41")
    @DisplayName("RF-41 · Registrar con el puerto mínimo o máximo permitido responde éxito")
    void registrarConPuertoMinimoOMaximoRespondeExito(int puerto) throws IOException {
        Respuesta respuesta = enviar(operacionBroker("pto-1", Operaciones.REGISTRAR_PROVEEDOR,
                Servicios.INVENTARIO, "localhost", puerto));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        assertEquals(puerto, broker.getRegistro().buscar(Servicios.INVENTARIO).orElseThrow().puerto());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 65536})
    @Tag("RF-41")
    @Tag("RF-47")
    @DisplayName("RF-41, RF-47 · Registrar con el puerto fuera de rango responde PARAMETROS_INVALIDOS sin registrarlo")
    void registrarConPuertoFueraDeRangoRespondeParametrosInvalidosSinRegistrarlo(int puerto) throws IOException {
        Respuesta respuesta = enviar(operacionBroker("pto-2", Operaciones.REGISTRAR_PROVEEDOR,
                Servicios.INVENTARIO, "localhost", puerto));

        assertEquals(CodigoError.PARAMETROS_INVALIDOS, respuesta.getCodigoError());
        assertEquals("pto-2", respuesta.getIdSolicitud());
        assertTrue(broker.getRegistro().buscar(Servicios.INVENTARIO).isEmpty());
    }

    @Test
    @Tag("RF-47")
    @DisplayName("RF-47 · Un puerto que no es número responde PARAMETROS_INVALIDOS conservando el id")
    void puertoQueNoEsNumeroRespondeParametrosInvalidosConservandoElId() throws IOException {
        JsonObject parametros = new JsonObject();
        parametros.addProperty("servicio", Servicios.INVENTARIO);
        parametros.addProperty("host", "localhost");
        parametros.addProperty("puerto", "abc");

        Respuesta respuesta = enviar(Solicitud.crear("pto-3", Servicios.BROKER,
                Operaciones.REGISTRAR_PROVEEDOR, parametros));

        assertEquals(CodigoError.PARAMETROS_INVALIDOS, respuesta.getCodigoError());
        assertEquals("pto-3", respuesta.getIdSolicitud());
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
