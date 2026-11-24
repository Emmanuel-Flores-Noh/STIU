package mx.uady.inventario.central.service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QrServiceTest {

    @Test
    @Tag("RF-10")
    @DisplayName("RF-10 · Genera el PNG con su payload y permite leer sus bytes")
    void generaPngConPayloadYPermiteLeerSusBytes() throws Exception {
        Path directorio = Files.createTempDirectory("qr-test-");
        QrService servicio = new QrService(directorio);

        String ruta = servicio.generarCodigoQR("SN-001", 7, "Producto ágil");
        Path archivo = Path.of(ruta);

        assertEquals(directorio.resolve("SN-001.png"), archivo);
        assertEquals("ID: 7 | Producto: Producto ágil | SN: SN-001", leerPayload(archivo));
        assertArrayEquals(Files.readAllBytes(archivo), servicio.obtenerImagenQRBytes(ruta));
    }

    @Test
    @Tag("RNF-26")
    @DisplayName("RNF-26 · Rechaza un número de serie que intenta salir del directorio")
    void rechazaNumeroDeSerieQueIntentaSalirDelDirectorio() throws Exception {
        QrService servicio = new QrService(Files.createTempDirectory("qr-test-"));

        assertThrows(QrGenerationException.class,
                () -> servicio.generarCodigoQR("../fuera", 7, "Producto"));
    }

    @Test
    @Tag("RF-10")
    @DisplayName("RF-10 · La imagen del QR mide 300 × 300 píxeles")
    void laImagenDelQrMide300x300Pixeles(@TempDir Path directorio) throws Exception {
        String ruta = new QrService(directorio).generarCodigoQR("SN-002", 7, "Producto");

        BufferedImage imagen = ImageIO.read(Path.of(ruta).toFile());
        assertEquals(300, imagen.getWidth());
        assertEquals(300, imagen.getHeight());
    }

    @Test
    @Tag("RF-10")
    @DisplayName("RF-10 · Cada unidad tiene su propio archivo y su propio contenido")
    void cadaUnidadTieneSuPropioArchivoYSuPropioContenido(@TempDir Path directorio) throws Exception {
        QrService servicio = new QrService(directorio);

        Path primero = Path.of(servicio.generarCodigoQR("SN-A", 7, "Producto"));
        Path segundo = Path.of(servicio.generarCodigoQR("SN-B", 7, "Producto"));

        assertNotEquals(primero, segundo);
        assertEquals("ID: 7 | Producto: Producto | SN: SN-A", leerPayload(primero));
        assertEquals("ID: 7 | Producto: Producto | SN: SN-B", leerPayload(segundo));
    }

    @Test
    @Tag("RF-10")
    @DisplayName("RF-10 · Acepta el identificador de producto mínimo (1)")
    void aceptaElIdentificadorDeProductoMinimo(@TempDir Path directorio) throws Exception {
        String ruta = new QrService(directorio).generarCodigoQR("SN-003", 1, "Producto");

        assertEquals("ID: 1 | Producto: Producto | SN: SN-003", leerPayload(Path.of(ruta)));
    }

    @Test
    @Tag("RF-10")
    @DisplayName("RF-10 · Rechaza serie vacía, identificador no positivo y nombre vacío sin crear archivos")
    void rechazaDatosInvalidosSinCrearArchivos(@TempDir Path directorio) throws Exception {
        QrService servicio = new QrService(directorio);

        assertEquals("El número de serie es obligatorio", assertThrows(QrGenerationException.class,
                () -> servicio.generarCodigoQR("  ", 7, "Producto")).getMessage());
        assertEquals("El id del producto debe ser positivo", assertThrows(QrGenerationException.class,
                () -> servicio.generarCodigoQR("SN-004", 0, "Producto")).getMessage());
        assertThrows(QrGenerationException.class, () -> servicio.generarCodigoQR("SN-004", -1, "Producto"));
        assertEquals("El nombre del producto es obligatorio", assertThrows(QrGenerationException.class,
                () -> servicio.generarCodigoQR("SN-004", 7, null)).getMessage());
        assertThrows(QrGenerationException.class, () -> servicio.generarCodigoQR(null, 7, "Producto"));

        try (Stream<Path> archivos = Files.list(directorio)) {
            assertEquals(0, archivos.count());
        }
    }

    @Test
    @Tag("RF-11")
    @Tag("RF-22")
    @DisplayName("RF-11, RF-22 · Leer el QR exige una ruta y falla si el archivo no existe")
    void leerElQrExigeUnaRutaYFallaSiElArchivoNoExiste(@TempDir Path directorio) {
        QrService servicio = new QrService(directorio);

        assertThrows(IllegalArgumentException.class, () -> servicio.obtenerImagenQRBytes(null));
        assertThrows(IllegalArgumentException.class, () -> servicio.obtenerImagenQRBytes(" "));
        assertThrows(NoSuchFileException.class,
                () -> servicio.obtenerImagenQRBytes(directorio.resolve("no-existe.png").toString()));
    }

    @Test
    @Tag("RF-10")
    @DisplayName("RF-10 · El servicio exige un directorio de almacenamiento")
    void elServicioExigeUnDirectorioDeAlmacenamiento() {
        assertThrows(IllegalArgumentException.class, () -> new QrService(null));
    }

    private String leerPayload(Path archivo) throws Exception {
        BufferedImage imagen = ImageIO.read(archivo.toFile());
        BinaryBitmap bitmap = new BinaryBitmap(
                new HybridBinarizer(new BufferedImageLuminanceSource(imagen))
        );
        Result resultado = new MultiFormatReader().decode(bitmap);
        return resultado.getText();
    }
}
