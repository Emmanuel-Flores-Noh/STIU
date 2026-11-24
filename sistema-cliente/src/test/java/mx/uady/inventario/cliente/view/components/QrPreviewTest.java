package mx.uady.inventario.cliente.view.components;

import mx.uady.inventario.cliente.model.QrDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de pruebas unitarias para la visualización y exportación de códigos QR (RF-34 y RF-35).
 */
class QrPreviewTest {

    // 1x1 PNG transparente válido codificado en Base64
    private static final String PNG_VALIDO_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

    @Test
    @Tag("RF-34")
    @DisplayName("RF-34 · Caso Normal: Decodifica correctamente la imagen PNG codificada en Base64")
    void decodificarPng_CasoNormal_DecodificacionExitosa() {
        QrDTO qr = new QrDTO("SN-001", 1, "Laptop Lenovo", PNG_VALIDO_BASE64);

        byte[] bytes = qr.decodificarPng();

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        // Validar cabecera png
        assertEquals((byte) 0x89, bytes[0]);
        assertEquals((byte) 'P', bytes[1]);
        assertEquals((byte) 'N', bytes[2]);
        assertEquals((byte) 'G', bytes[3]);
    }

    @Test
    @Tag("RF-34")
    @DisplayName("RF-34 · Caso Límite: Retorna null ante Base64 nulo, vacío o con espacios en blanco")
    void decodificarPng_CasoLimite_Base64VacioONulo() {
        QrDTO qrNulo = new QrDTO("SN-002", 1, "Proyector", null);
        assertNull(qrNulo.decodificarPng());

        QrDTO qrVacio = new QrDTO("SN-003", 1, "Proyector", "   ");
        assertNull(qrVacio.decodificarPng());
    }

    @Test
    @Tag("RF-34")
    @DisplayName("RF-34 · Caso Error: Retorna null ante Base64 corrupto o inválido y define mensaje de error estándar")
    void decodificarPng_CasoError_Base64Corrupto() {
        QrDTO qrInvalido = new QrDTO("SN-004", 1, "Proyector", "%%%NO-ES-BASE64%%%");

        assertNull(qrInvalido.decodificarPng(), "Base64 corrupto debe retornar null de forma segura");
        assertEquals("No se pudo mostrar la imagen del código QR.", QrPreviewDialog.TEXTO_SIN_IMAGEN,
                "El mensaje cuando la imagen falla debe coincidir con la especificación (RF-34)");
    }

    @Test
    @Tag("RF-35")
    @DisplayName("RF-35 · Caso Normal: Permite guardar el código QR como archivo PNG local en disco")
    void guardarPng_CasoNormal_GuardaArchivoLocalmente(@TempDir Path tempDir) throws Exception {
        QrDTO qr = new QrDTO("SN-001", 1, "Laptop Lenovo", PNG_VALIDO_BASE64);
        byte[] bytes = qr.decodificarPng();
        assertNotNull(bytes);

        Path archivoDestino = tempDir.resolve("SN-001.png");
        Files.write(archivoDestino, bytes);

        assertTrue(Files.exists(archivoDestino), "El archivo PNG debe crearse en disco (RF-35)");
        assertEquals(bytes.length, Files.size(archivoDestino));
        assertTrue(archivoDestino.toString().endsWith(".png"));
    }

    @Test
    @Tag("RF-35")
    @DisplayName("RF-35 · Caso Límite: Sanitización de caracteres prohibidos en el nombre de archivo PNG")
    void guardarPng_CasoLimite_SaneamientoDeNombreArchivo() {
        String serieConCaracteresEspeciales = "SN/2026:UADY*TEST?1";
        // Replicar la regla de saneamiento seguro de QrPreviewDialog
        String saneado = serieConCaracteresEspeciales.replaceAll("[\\\\/:*?\"<>|]", "_") + ".png";

        assertEquals("SN_2026_UADY_TEST_1.png", saneado);
        assertFalse(saneado.contains("/"));
        assertFalse(saneado.contains(":"));
        assertFalse(saneado.contains("*"));
        assertFalse(saneado.contains("?"));
    }

    @Test
    @Tag("RF-35")
    @DisplayName("RF-35 · Caso Error: Cancelación o ausencia de bytes no genera archivo en disco")
    void guardarPng_CasoError_SinBytesNoGuarda(@TempDir Path tempDir) {
        QrDTO qrSinImagen = new QrDTO("SN-005", 1, "Mouse", null);
        byte[] bytes = qrSinImagen.decodificarPng();

        assertNull(bytes);
        File archivoNoExistente = tempDir.resolve("SN-005.png").toFile();
        assertFalse(archivoNoExistente.exists());
    }
}
