package mx.uady.inventario.broker.registry;

import com.google.gson.JsonObject;
import mx.uady.inventario.broker.model.Proveedor;
import mx.uady.inventario.broker.util.BitacoraBroker;
import mx.uady.inventario.common.contrato.EstadoRespuesta;
import mx.uady.inventario.common.contrato.Respuesta;
import mx.uady.inventario.common.contrato.Solicitud;
import mx.uady.inventario.common.error.CodigoError;
import mx.uady.inventario.common.servicio.Operaciones;
import mx.uady.inventario.common.servicio.Servicios;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de {@link AdministradorRegistro}, sin sockets.
 *
 * <p>Requisitos cubiertos:</p>
 * <ul>
 *   <li>RF-41: registrar un proveedor en tiempo de ejecución.</li>
 *   <li>RF-42: actualizar un proveedor registrado, o responder PROVEEDOR_NO_REGISTRADO.</li>
 *   <li>RF-43: dar de baja un proveedor, o responder PROVEEDOR_NO_REGISTRADO.</li>
 *   <li>RF-47: rechazar parámetros mal formados con PARAMETROS_INVALIDOS.</li>
 *   <li>RF-49: responder OPERACION_NO_SOPORTADA a una operación desconocida del broker.</li>
 *   <li>RF-56: anotar en la bitácora cada cambio en el registro de servicios.</li>
 * </ul>
 */
class AdministradorRegistroTest {

    private RegistroServicios registro;
    private List<String> bitacora;
    private AdministradorRegistro administrador;

    @BeforeEach
    void crearAdministrador() {
        registro = new RegistroServicios();
        bitacora = new ArrayList<>();
        administrador = new AdministradorRegistro(registro, new BitacoraBroker(bitacora::add));
    }

    private static Solicitud solicitud(String id, String operacion, String servicio, String host, int puerto) {
        JsonObject parametros = new JsonObject();
        parametros.addProperty("servicio", servicio);
        parametros.addProperty("host", host);
        parametros.addProperty("puerto", puerto);
        return Solicitud.crear(id, Servicios.BROKER, operacion, parametros);
    }

    private static Solicitud soloServicio(String id, String operacion, String servicio) {
        JsonObject parametros = new JsonObject();
        parametros.addProperty("servicio", servicio);
        return Solicitud.crear(id, Servicios.BROKER, operacion, parametros);
    }

    private static void assertError(CodigoError esperado, String id, Respuesta respuesta) {
        assertEquals(EstadoRespuesta.ERROR, respuesta.getEstado());
        assertEquals(esperado, respuesta.getCodigoError());
        assertEquals(id, respuesta.getIdSolicitud());
    }

    @Test
    @Tag("RF-41")
    @Tag("RF-56")
    @DisplayName("RF-41, RF-56 · Registrar un proveedor lo agrega al registro y lo anota en la bitácora")
    void registrarProveedorLoAgregaAlRegistroYLoAnotaEnLaBitacora() {
        Respuesta respuesta = administrador.atender(solicitud("reg-1", Operaciones.REGISTRAR_PROVEEDOR,
                Servicios.INVENTARIO, "localhost", 9001));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        assertEquals("reg-1", respuesta.getIdSolicitud());
        assertEquals(Optional.of(new Proveedor(Servicios.INVENTARIO, "localhost", 9001)),
                registro.buscar(Servicios.INVENTARIO));
        assertEquals(1, bitacora.size());
        assertTrue(bitacora.get(0).endsWith("registrar inventario -> localhost:9001"), bitacora.get(0));
    }

    @Test
    @Tag("RF-41")
    @DisplayName("RF-41 · Registrar de nuevo un servicio sustituye al proveedor anterior")
    void registrarDeNuevoUnServicioSustituyeAlProveedorAnterior() {
        registro.registrar(new Proveedor(Servicios.INVENTARIO, "localhost", 9001));

        Respuesta respuesta = administrador.atender(solicitud("reg-2", Operaciones.REGISTRAR_PROVEEDOR,
                Servicios.INVENTARIO, "otro-host", 9002));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        assertEquals(Optional.of(new Proveedor(Servicios.INVENTARIO, "otro-host", 9002)),
                registro.buscar(Servicios.INVENTARIO));
    }

    @Test
    @Tag("RF-42")
    @DisplayName("RF-42 · Actualizar un servicio registrado cambia su host y puerto")
    void actualizarServicioRegistradoCambiaSuHostYPuerto() {
        registro.registrar(new Proveedor(Servicios.INVENTARIO, "localhost", 9001));

        Respuesta respuesta = administrador.atender(solicitud("act-1", Operaciones.ACTUALIZAR_PROVEEDOR,
                Servicios.INVENTARIO, "otro-host", 9002));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        assertEquals("act-1", respuesta.getIdSolicitud());
        assertEquals(Optional.of(new Proveedor(Servicios.INVENTARIO, "otro-host", 9002)),
                registro.buscar(Servicios.INVENTARIO));
    }

    @Test
    @Tag("RF-42")
    @Tag("RF-56")
    @DisplayName("RF-42, RF-56 · Actualizar un servicio no registrado responde PROVEEDOR_NO_REGISTRADO y la bitácora lo anota sin efecto")
    void actualizarServicioNoRegistradoRespondeProveedorNoRegistradoYLaBitacoraLoAnotaSinEfecto() {
        Respuesta respuesta = administrador.atender(solicitud("act-2", Operaciones.ACTUALIZAR_PROVEEDOR,
                Servicios.INVENTARIO, "localhost", 9001));

        assertError(CodigoError.PROVEEDOR_NO_REGISTRADO, "act-2", respuesta);
        assertTrue(respuesta.getMensajeError().contains(Servicios.INVENTARIO), respuesta.getMensajeError());
        assertTrue(registro.buscar(Servicios.INVENTARIO).isEmpty());
        assertEquals(1, bitacora.size());
        assertTrue(bitacora.get(0).contains("sustituir inventario") && bitacora.get(0).contains("sin efecto"),
                bitacora.get(0));
    }

    @Test
    @Tag("RF-43")
    @DisplayName("RF-43 · Remover un servicio registrado indicando solo su nombre lo quita del registro")
    void removerServicioRegistradoIndicandoSoloSuNombreLoQuitaDelRegistro() {
        registro.registrar(new Proveedor(Servicios.INVENTARIO, "localhost", 9001));

        Respuesta respuesta = administrador.atender(soloServicio("rem-1", Operaciones.REMOVER_PROVEEDOR,
                Servicios.INVENTARIO));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        assertEquals("rem-1", respuesta.getIdSolicitud());
        assertTrue(registro.buscar(Servicios.INVENTARIO).isEmpty());
    }

    @Test
    @Tag("RF-43")
    @Tag("RF-56")
    @DisplayName("RF-43, RF-56 · Remover un servicio no registrado responde PROVEEDOR_NO_REGISTRADO y la bitácora lo anota sin efecto")
    void removerServicioNoRegistradoRespondeProveedorNoRegistradoYLaBitacoraLoAnotaSinEfecto() {
        Respuesta respuesta = administrador.atender(soloServicio("rem-2", Operaciones.REMOVER_PROVEEDOR,
                Servicios.INVENTARIO));

        assertError(CodigoError.PROVEEDOR_NO_REGISTRADO, "rem-2", respuesta);
        assertEquals(1, bitacora.size());
        assertTrue(bitacora.get(0).contains("eliminar inventario") && bitacora.get(0).contains("sin efecto"),
                bitacora.get(0));
    }

    @Test
    @Tag("RF-43")
    @DisplayName("RF-43 · Remover un servicio no deja de atender a los demás servicios registrados")
    void removerUnServicioNoAfectaALosDemasServiciosRegistrados() {
        registro.registrar(new Proveedor(Servicios.INVENTARIO, "localhost", 9001));
        registro.registrar(new Proveedor(Servicios.GESTOR_TIENDAS, "localhost", 9002));

        administrador.atender(soloServicio("rem-3", Operaciones.REMOVER_PROVEEDOR, Servicios.INVENTARIO));

        assertTrue(registro.buscar(Servicios.INVENTARIO).isEmpty());
        assertTrue(registro.buscar(Servicios.GESTOR_TIENDAS).isPresent());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 65535})
    @Tag("RF-41")
    @DisplayName("RF-41 · El puerto mínimo y el máximo permitidos se aceptan al registrar")
    void puertoMinimoYMaximoSeAceptanAlRegistrar(int puerto) {
        Respuesta respuesta = administrador.atender(solicitud("pto-1", Operaciones.REGISTRAR_PROVEEDOR,
                Servicios.INVENTARIO, "localhost", puerto));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        assertEquals(puerto, registro.buscar(Servicios.INVENTARIO).orElseThrow().puerto());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 65536, -1})
    @Tag("RF-47")
    @DisplayName("RF-47 · Un puerto fuera de rango responde PARAMETROS_INVALIDOS y no cambia el registro")
    void puertoFueraDeRangoRespondeParametrosInvalidosYNoCambiaElRegistro(int puerto) {
        registro.registrar(new Proveedor(Servicios.INVENTARIO, "localhost", 9001));

        Respuesta registrar = administrador.atender(solicitud("pto-2", Operaciones.REGISTRAR_PROVEEDOR,
                Servicios.GESTOR_TIENDAS, "localhost", puerto));
        Respuesta actualizar = administrador.atender(solicitud("pto-3", Operaciones.ACTUALIZAR_PROVEEDOR,
                Servicios.INVENTARIO, "localhost", puerto));

        assertError(CodigoError.PARAMETROS_INVALIDOS, "pto-2", registrar);
        assertTrue(registrar.getMensajeError().contains("puerto fuera de rango"), registrar.getMensajeError());
        assertError(CodigoError.PARAMETROS_INVALIDOS, "pto-3", actualizar);
        assertTrue(registro.buscar(Servicios.GESTOR_TIENDAS).isEmpty());
        assertEquals(9001, registro.buscar(Servicios.INVENTARIO).orElseThrow().puerto());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    @Tag("RF-47")
    @DisplayName("RF-47 · Un servicio vacío o en blanco responde PARAMETROS_INVALIDOS en las tres operaciones")
    void servicioVacioOEnBlancoRespondeParametrosInvalidosEnLasTresOperaciones(String servicio) {
        Respuesta registrar = administrador.atender(solicitud("srv-1", Operaciones.REGISTRAR_PROVEEDOR,
                servicio, "localhost", 9001));
        Respuesta actualizar = administrador.atender(solicitud("srv-2", Operaciones.ACTUALIZAR_PROVEEDOR,
                servicio, "localhost", 9001));
        Respuesta remover = administrador.atender(soloServicio("srv-3", Operaciones.REMOVER_PROVEEDOR, servicio));

        assertError(CodigoError.PARAMETROS_INVALIDOS, "srv-1", registrar);
        assertError(CodigoError.PARAMETROS_INVALIDOS, "srv-2", actualizar);
        assertError(CodigoError.PARAMETROS_INVALIDOS, "srv-3", remover);
        assertTrue(registro.getServicios().isEmpty());
        assertTrue(bitacora.isEmpty(), "Una solicitud rechazada no debe anotar cambios en el registro");
    }

    @Test
    @Tag("RF-47")
    @DisplayName("RF-47 · Un servicio que no es texto ni número responde PARAMETROS_INVALIDOS")
    void servicioQueNoEsTextoRespondeParametrosInvalidos() {
        JsonObject parametros = new JsonObject();
        parametros.add("servicio", new JsonObject());
        parametros.addProperty("host", "localhost");
        parametros.addProperty("puerto", 9001);

        Respuesta respuesta = administrador.atender(Solicitud.crear("srv-4", Servicios.BROKER,
                Operaciones.REGISTRAR_PROVEEDOR, parametros));

        assertError(CodigoError.PARAMETROS_INVALIDOS, "srv-4", respuesta);
    }

    @Test
    @Tag("RF-47")
    @DisplayName("RF-47 · Registrar sin host responde PARAMETROS_INVALIDOS")
    void registrarSinHostRespondeParametrosInvalidos() {
        Respuesta respuesta = administrador.atender(soloServicio("hst-1", Operaciones.REGISTRAR_PROVEEDOR,
                Servicios.INVENTARIO));

        assertError(CodigoError.PARAMETROS_INVALIDOS, "hst-1", respuesta);
        assertTrue(registro.buscar(Servicios.INVENTARIO).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            Operaciones.REGISTRAR_PROVEEDOR,
            Operaciones.ACTUALIZAR_PROVEEDOR,
            Operaciones.REMOVER_PROVEEDOR
    })
    @Tag("RF-47")
    @DisplayName("RF-47 · Una operación de administración sin parámetros responde PARAMETROS_INVALIDOS")
    void operacionDeAdministracionSinParametrosRespondeParametrosInvalidos(String operacion) {
        Respuesta respuesta = administrador.atender(Solicitud.crear("sin-params", Servicios.BROKER,
                operacion, null));

        assertError(CodigoError.PARAMETROS_INVALIDOS, "sin-params", respuesta);
    }

    @Test
    @Tag("RF-49")
    @DisplayName("RF-49 · Una operación desconocida responde OPERACION_NO_SOPORTADA sin cambiar el registro")
    void operacionDesconocidaRespondeOperacionNoSoportadaSinCambiarElRegistro() {
        Respuesta respuesta = administrador.atender(solicitud("op-1", "operacionFantasma",
                Servicios.INVENTARIO, "localhost", 9001));

        assertError(CodigoError.OPERACION_NO_SOPORTADA, "op-1", respuesta);
        assertTrue(registro.getServicios().isEmpty());
    }

    @Test
    @Tag("RF-41")
    @Tag("RF-42")
    @Tag("RF-43")
    @DisplayName("RF-41, RF-42, RF-43 · El administrador exige un registro y una bitácora")
    void constructorConRegistroOBitacoraNulosLanzaExcepcion() {
        BitacoraBroker bitacoraVacia = new BitacoraBroker(linea -> { });

        assertThrows(NullPointerException.class, () -> new AdministradorRegistro(null, bitacoraVacia));
        assertThrows(NullPointerException.class, () -> new AdministradorRegistro(registro, null));
    }
}
