package mx.uady.inventario.cliente.network;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mx.uady.inventario.cliente.error.ComunicacionError;
import mx.uady.inventario.cliente.error.ComunicacionException;
import mx.uady.inventario.common.contrato.Respuesta;
import mx.uady.inventario.common.contrato.Solicitud;
import mx.uady.inventario.common.error.CodigoError;
import mx.uady.inventario.common.json.GsonFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de pruebas unitarias para {@link ClienteTCP}.
 * Valida la serialización y envío de solicitudes al Broker, la correlación estricta por {@code idSolicitud}
 * y el manejo de respuestas válidas, inválidas o con estado de error.
 */
class ClienteTCPTest {

    private final Gson gson = GsonFactory.DEFAULT;

    @Test
    @Tag("RF-39")
    @DisplayName("RF-39 · Caso Normal: Invoca operación y retorna resultado exitoso cuando idSolicitud coincide")
    void invocar_CasoNormal_RespuestaExitosa() throws Exception {
        ConexionTCP transporteSimulado = new ConexionTCP("localhost", 5000, 22000) {
            @Override
            public String intercambiar(String linea) {
                Solicitud sol = gson.fromJson(linea, Solicitud.class);
                assertNotNull(sol.getIdSolicitud(), "Cada solicitud debe llevar un idSolicitud único (RF-39)");
                JsonObject data = new JsonObject();
                data.addProperty("resultado", "ok");
                return gson.toJson(Respuesta.exito(sol.getIdSolicitud(), data));
            }
        };

        ClienteTCP cliente = new ClienteTCP(transporteSimulado);
        JsonElement resultado = cliente.invocar("gestorTiendas", "listarTiendas", null);

        assertNotNull(resultado);
        assertTrue(resultado.isJsonObject());
        assertEquals("ok", resultado.getAsJsonObject().get("resultado").getAsString());
    }

    @Test
    @Tag("RF-39")
    @DisplayName("RF-39 · Caso Error: Rechaza respuesta cuando idSolicitud no coincide con la solicitud enviada")
    void enviarSolicitud_CasoError_DiscrepanciaIdSolicitud() {
        ConexionTCP transporteConDiscrepancia = new ConexionTCP("localhost", 5000, 22000) {
            @Override
            public String intercambiar(String linea) {
                // Devolvemos un idSolicitud diferente al recibido
                return gson.toJson(Respuesta.exito("id-diferente-12345", new JsonObject()));
            }
        };

        ClienteTCP cliente = new ClienteTCP(transporteConDiscrepancia);
        Solicitud solicitud = Solicitud.crear("id-original-99999", "inventario", "listarInventario", null);

        ComunicacionException error = assertThrows(ComunicacionException.class,
                () -> cliente.enviarSolicitud(solicitud));

        assertEquals(ComunicacionError.RESPUESTA_INVALIDA, error.getTipo(),
                "Una respuesta con idSolicitud distinto debe rechazarse como RESPUESTA_INVALIDA (RF-39)");
    }

    @Test
    @Tag("RF-37")
    @DisplayName("RF-37 · Caso Error: Lanza excepción de servidor cuando la respuesta no es EXITO")
    void invocar_CasoError_RespuestaConEstadoError() {
        ConexionTCP transporteConErrorServidor = new ConexionTCP("localhost", 5000, 22000) {
            @Override
            public String intercambiar(String linea) {
                Solicitud sol = gson.fromJson(linea, Solicitud.class);
                return gson.toJson(Respuesta.error(
                        sol.getIdSolicitud(), CodigoError.TIMEOUT, "Timeout del Broker"));
            }
        };

        ClienteTCP cliente = new ClienteTCP(transporteConErrorServidor);

        ComunicacionException error = assertThrows(ComunicacionException.class,
                () -> cliente.invocar("inventario", "obtenerQr", null));

        assertEquals(ComunicacionError.ERROR_SERVIDOR, error.getTipo());
        assertEquals(CodigoError.TIMEOUT, error.getCodigoError());
        assertEquals("Timeout del Broker", error.getMessage());
    }

    @Test
    @Tag("RF-37")
    @DisplayName("RF-37 · Caso Límite: Rechaza respuesta vacía o JSON malformado")
    void enviarSolicitud_CasoLimite_RespuestaVaciaOMalformada() {
        ConexionTCP transporteVacio = new ConexionTCP("localhost", 5000, 22000) {
            @Override
            public String intercambiar(String linea) {
                return "{ json-invalido";
            }
        };

        ClienteTCP cliente = new ClienteTCP(transporteVacio);
        Solicitud sol = Solicitud.crear("id-1", "s", "op", null);

        ComunicacionException error = assertThrows(ComunicacionException.class,
                () -> cliente.enviarSolicitud(sol));

        assertEquals(ComunicacionError.RESPUESTA_INVALIDA, error.getTipo());
    }
}
