package mx.uady.inventario.broker.util;

import com.google.gson.JsonObject;
import mx.uady.inventario.common.contrato.Respuesta;
import mx.uady.inventario.common.contrato.Solicitud;
import mx.uady.inventario.common.json.GsonFactory;
import mx.uady.inventario.common.servicio.Servicios;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonBrokerTest {

    @Test
    @Tag("RNF-11")
    @DisplayName("RNF-11 · Una solicitud se serializa y se lee igual con el contrato común")
    void roundTripSolicitud() {
        JsonObject params = new JsonObject();
        params.addProperty("numero_serie", "SN-001");
        Solicitud original = Solicitud.crear("req-1", Servicios.INVENTARIO, "listarInventario", params);

        String json = GsonFactory.DEFAULT.toJson(original);
        Solicitud copia = JsonBroker.deserializarSolicitud(json);

        assertEquals(original, copia);
    }

    @Test
    @Tag("RNF-11")
    @DisplayName("RNF-11 · Una respuesta se serializa y conserva su id y estado")
    void roundTripRespuesta() {
        Respuesta original = Respuesta.exito("req-2", new com.google.gson.JsonPrimitive("OK"));
        String json = JsonBroker.serializarRespuesta(original);
        Respuesta copia = GsonFactory.DEFAULT.fromJson(json, Respuesta.class);

        assertEquals(original.getIdSolicitud(), copia.getIdSolicitud());
        assertEquals(original.getEstado(), copia.getEstado());
    }

    @Test
    @Tag("RF-47")
    @DisplayName("RF-47 · Un JSON nulo o en blanco se rechaza")
    void deserializarJsonVacioLanza() {
        assertThrows(IllegalArgumentException.class, () -> JsonBroker.deserializarSolicitud(null));
        assertThrows(IllegalArgumentException.class, () -> JsonBroker.deserializarSolicitud("  "));
    }

    @Test
    @Tag("RNF-11")
    @DisplayName("RNF-11 · La respuesta serializada contiene el id de la solicitud")
    void serializarRespuestaContieneId() {
        Respuesta original = Respuesta.error("req-3", mx.uady.inventario.common.error.CodigoError.PARAMETROS_INVALIDOS, null);
        String json = JsonBroker.serializarRespuesta(original);
        assertTrue(json.contains("req-3"));
    }
}
