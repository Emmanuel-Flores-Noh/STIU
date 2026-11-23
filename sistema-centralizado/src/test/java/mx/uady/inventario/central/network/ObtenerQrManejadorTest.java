package mx.uady.inventario.central.network;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.JsonObject;

import mx.uady.inventario.central.service.QrService;
import mx.uady.inventario.central.service.memoria.AlmacenMemoria;
import mx.uady.inventario.central.service.memoria.MercanciaServiceMemoria;
import mx.uady.inventario.central.service.memoria.ProductoServiceMemoria;

/**
 * Pruebas de {@link ObtenerQrManejador}: operación {@code obtenerQr} (RF-22).
 *
 * <p>Cada prueba usa su propio directorio temporal para los PNG y su propio almacén en memoria.</p>
 */
@Tag("RF-22")
class ObtenerQrManejadorTest {

    /** Firma de los primeros 8 bytes de todo archivo PNG. */
    private static final byte[] FIRMA_PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

    @TempDir
    Path directorioQr;

    private ObtenerQrManejador manejador;

    @BeforeEach
    void crearManejador() {
        AlmacenMemoria almacen = AlmacenMemoria.conDatosDeEjemplo(Duration.ZERO);
        QrService qr = new QrService(directorioQr);
        manejador = new ObtenerQrManejador(
                new MercanciaServiceMemoria(almacen, qr), new ProductoServiceMemoria(almacen), qr);
    }

    private static JsonObject serie(String numeroSerie) {
        JsonObject parametros = new JsonObject();
        parametros.addProperty("numero_serie", numeroSerie);
        return parametros;
    }

    private boolean hayArchivos() throws IOException {
        try (Stream<Path> archivos = Files.list(directorioQr)) {
            return archivos.findAny().isPresent();
        }
    }

    @Test
    @DisplayName("RF-22: devuelve numeroSerie, idProducto, nombreProducto y la imagen PNG en Base64")
    void unidadExistente_devuelveQrEnBase64() {
        JsonObject resultado = manejador.ejecutar(serie("SN-UADY-0001")).getAsJsonObject();

        assertEquals("SN-UADY-0001", resultado.get("numeroSerie").getAsString());
        assertEquals(1, resultado.get("idProducto").getAsInt());
        assertEquals("Laptop Lenovo ThinkPad E14", resultado.get("nombreProducto").getAsString());
        byte[] png = Base64.getDecoder().decode(resultado.get("pngBase64").getAsString());
        assertArrayEquals(FIRMA_PNG, Arrays.copyOf(png, FIRMA_PNG.length));
    }

    @Test
    @DisplayName("RF-22: si el PNG no existe, se genera antes de responder")
    void unidadSinPng_generaElArchivo() throws IOException {
        assertFalse(hayArchivos(), "el directorio inicia vacío");

        manejador.ejecutar(serie("SN-UADY-0003"));

        assertTrue(hayArchivos());
    }

    @Test
    @DisplayName("RF-22: una unidad que no existe se rechaza como parámetro inválido")
    void serieInexistente_lanzaExcepcion() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> manejador.ejecutar(serie("SN-NO-EXISTE")));
        assertTrue(error.getMessage().contains("SN-NO-EXISTE"));
    }

    @Test
    @DisplayName("RF-22: sin numero_serie se rechaza la solicitud")
    void sinNumeroSerie_lanzaExcepcion() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> manejador.ejecutar(new JsonObject()));
        assertEquals("Falta el parámetro numero_serie", error.getMessage());
    }

    @Test
    @DisplayName("RF-22: un numero_serie vacío se rechaza")
    void numeroSerieVacio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> manejador.ejecutar(serie("   ")));
    }
}
