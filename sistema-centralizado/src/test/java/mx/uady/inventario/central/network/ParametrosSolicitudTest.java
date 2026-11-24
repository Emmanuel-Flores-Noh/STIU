package mx.uady.inventario.central.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * Pruebas de {@link ParametrosSolicitud}: reglas de los parámetros de las operaciones remotas.
 *
 * <ul>
 *   <li>RF-17: {@code numero_serie}, {@code id_tienda} e {@code id_usuario} son obligatorios y los
 *       identificadores son enteros mayores que 0.</li>
 *   <li>RF-22: {@code numero_serie} es obligatorio.</li>
 *   <li>RF-23: {@code id_producto} es un entero mayor que 0.</li>
 * </ul>
 */
class ParametrosSolicitudTest {

    private static JsonObject con(String nombre, String valor) {
        JsonObject parametros = new JsonObject();
        parametros.addProperty(nombre, valor);
        return parametros;
    }

    private static JsonObject con(String nombre, Number valor) {
        JsonObject parametros = new JsonObject();
        parametros.addProperty(nombre, valor);
        return parametros;
    }

    // --- textoObligatorio ---

    @Test
    @Tag("RF-17")
    @Tag("RF-22")
    @DisplayName("RF-17, RF-22: un numero_serie válido se devuelve tal cual")
    void textoValido_seDevuelveTalCual() {
        assertEquals("SN-UADY-0001",
                ParametrosSolicitud.textoObligatorio(con("numero_serie", "SN-UADY-0001"), "numero_serie"));
    }

    @Test
    @Tag("RF-17")
    @Tag("RF-22")
    @DisplayName("RF-17, RF-22: un numero_serie de solo espacios se rechaza por obligatorio")
    void textoSoloEspacios_lanzaExcepcion() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> ParametrosSolicitud.textoObligatorio(con("numero_serie", "   "), "numero_serie"));
        assertEquals("El parámetro numero_serie no puede estar vacío", error.getMessage());
    }

    @Test
    @Tag("RF-17")
    @Tag("RF-22")
    @DisplayName("RF-17, RF-22: un numero_serie ausente se rechaza con su nombre")
    void textoAusente_lanzaExcepcion() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> ParametrosSolicitud.textoObligatorio(new JsonObject(), "numero_serie"));
        assertEquals("Falta el parámetro numero_serie", error.getMessage());
    }

    @Test
    @Tag("RF-17")
    @Tag("RF-22")
    @DisplayName("RF-17, RF-22: un numero_serie con valor null se trata como ausente")
    void textoNulo_lanzaExcepcion() {
        JsonObject parametros = new JsonObject();
        parametros.add("numero_serie", JsonNull.INSTANCE);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> ParametrosSolicitud.textoObligatorio(parametros, "numero_serie"));
        assertEquals("Falta el parámetro numero_serie", error.getMessage());
    }

    // --- enteroPositivo ---

    @Test
    @Tag("RF-23")
    @DisplayName("RF-23: un id_producto positivo se devuelve tal cual")
    void enteroPositivo_seDevuelveTalCual() {
        assertEquals(7, ParametrosSolicitud.enteroPositivo(con("id_producto", 7), "id_producto"));
    }

    @Test
    @Tag("RF-17")
    @Tag("RF-23")
    @DisplayName("RF-17, RF-23: 1 es el menor identificador aceptado")
    void enteroUno_esValido() {
        assertEquals(1, ParametrosSolicitud.enteroPositivo(con("id_producto", 1), "id_producto"));
    }

    @Test
    @Tag("RF-17")
    @Tag("RF-23")
    @DisplayName("RF-17, RF-23: 0 se rechaza por no ser mayor que 0")
    void enteroCero_lanzaExcepcion() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> ParametrosSolicitud.enteroPositivo(con("id_producto", 0), "id_producto"));
        assertEquals("El parámetro id_producto debe ser positivo", error.getMessage());
    }

    @Test
    @Tag("RF-17")
    @DisplayName("RF-17: un id_tienda negativo se rechaza")
    void enteroNegativo_lanzaExcepcion() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> ParametrosSolicitud.enteroPositivo(con("id_tienda", -5), "id_tienda"));
        assertEquals("El parámetro id_tienda debe ser positivo", error.getMessage());
    }

    @Test
    @Tag("RF-23")
    @DisplayName("RF-23: un id_producto que no es número se rechaza por no ser entero")
    void enteroConTexto_lanzaExcepcion() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> ParametrosSolicitud.enteroPositivo(con("id_producto", "abc"), "id_producto"));
        assertEquals("El parámetro id_producto debe ser un entero", error.getMessage());
    }

    @Test
    @Tag("RF-17")
    @DisplayName("RF-17: un identificador ausente se rechaza con su nombre")
    void enteroAusente_lanzaExcepcion() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> ParametrosSolicitud.enteroPositivo(new JsonObject(), "id_tienda"));
        assertEquals("Falta el parámetro id_tienda", error.getMessage());
    }
}
