package mx.uady.inventario.central.service.memoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import mx.uady.inventario.central.service.AccesoDatosException;
import mx.uady.inventario.central.service.NegocioException;
import mx.uady.inventario.central.service.QrService;
import mx.uady.inventario.central.service.SerieDuplicadaException;
import mx.uady.inventario.central.service.ValidacionException;
import mx.uady.inventario.common.modelo.EstadoMercancia;
import mx.uady.inventario.common.modelo.Mercancia;

class MercanciaServiceMemoriaTest {

    @TempDir
    Path directorioQr;

    private AlmacenMemoria almacen;
    private MercanciaServiceMemoria servicio;

    @BeforeEach
    void preparar() {
        almacen = AlmacenMemoria.conDatosDeEjemplo(Duration.ZERO);
        servicio = new MercanciaServiceMemoria(almacen, new QrService(directorioQr));
    }

    @Test
    @Tag("RF-06")
    @Tag("RF-08")
    @Tag("RF-10")
    @DisplayName("RF-06, RF-08, RF-10 · Registra la unidad en almacén con un QR legible")
    void registraLaUnidadEnAlmacenConUnQrLegible() throws Exception {
        Mercancia unidad = servicio.registrar(2, "  SN-NUEVA-01 ");

        assertEquals("SN-NUEVA-01", unidad.numeroSerie());
        assertEquals(EstadoMercancia.EN_ALMACEN, unidad.estado());
        Path qr = Path.of(unidad.rutaQr());
        assertEquals(directorioQr.resolve("SN-NUEVA-01.png"), qr);

        BufferedImage imagen = ImageIO.read(qr.toFile());
        String contenido = new QRCodeReader().decode(new BinaryBitmap(
                new HybridBinarizer(new BufferedImageLuminanceSource(imagen)))).getText();
        assertEquals("ID: 2 | Producto: Proyector Epson PowerLite | SN: SN-NUEVA-01", contenido);
    }

    @Test
    @Tag("RF-07")
    @DisplayName("RF-07 · Rechaza una serie duplicada sin importar mayúsculas")
    void rechazaUnaSerieDuplicadaSinImportarMayusculas() {
        SerieDuplicadaException e = assertThrows(SerieDuplicadaException.class,
                () -> servicio.registrar(1, "sn-uady-0001"));
        assertEquals("sn-uady-0001", e.getNumeroSerie());
        assertEquals(10, servicio.listar(null, null).size());
    }

    @Test
    @Tag("RF-06")
    @DisplayName("RF-06 · Rechaza serie vacía y producto inexistente")
    void rechazaSerieVaciaYProductoInexistente() {
        assertThrows(ValidacionException.class, () -> servicio.registrar(1, "   "));
        assertThrows(NegocioException.class, () -> servicio.registrar(99, "SN-X"));
    }

    @Test
    @Tag("RF-06")
    @Tag("RNF-16")
    @DisplayName("RF-06, RNF-16 · Si no se puede escribir el QR no registra la unidad")
    void siNoSePuedeEscribirElQrNoRegistraLaUnidad() throws Exception {
        // Un archivo en lugar de la carpeta de QR hace fallar la escritura.
        Path noEsCarpeta = Files.createFile(directorioQr.resolve("ocupado"));
        MercanciaServiceMemoria conQrRoto = new MercanciaServiceMemoria(almacen, new QrService(noEsCarpeta));

        assertThrows(AccesoDatosException.class, () -> conQrRoto.registrar(1, "SN-SIN-QR"));
        assertTrue(servicio.listar(null, null).stream().noneMatch(m -> m.numeroSerie().equals("SN-SIN-QR")));
    }

    @Test
    @Tag("RNF-26")
    @DisplayName("RNF-26 · Una serie que no sirve como nombre de archivo se rechaza como validación")
    void unaSerieQueNoSirveComoNombreDeArchivoSeRechazaComoValidacion() {
        assertThrows(ValidacionException.class, () -> servicio.registrar(1, "../fuera"));
        assertTrue(servicio.listar(null, null).stream().noneMatch(m -> m.numeroSerie().contains("fuera")));
    }

    @Test
    @Tag("RF-11")
    @DisplayName("RF-11 · Asegurar QR genera el QR de una unidad sin él y guarda la ruta")
    void asegurarQrGeneraElQrDeUnaUnidadSinElYGuardaLaRuta() {
        assertEquals(null, servicio.listar(1, null).get(0).rutaQr());

        Mercancia conQr = servicio.asegurarQr("SN-UADY-0001");

        assertEquals(directorioQr.resolve("SN-UADY-0001.png"), Path.of(conQr.rutaQr()));
        assertTrue(Files.isRegularFile(Path.of(conQr.rutaQr())));
        assertEquals(EstadoMercancia.EN_ALMACEN, conQr.estado());
        assertEquals(conQr.rutaQr(), servicio.listar(1, null).get(0).rutaQr());
    }

    @Test
    @Tag("RF-11")
    @DisplayName("RF-11 · Asegurar QR no regenera si el archivo ya existe")
    void asegurarQrNoRegeneraSiElArchivoYaExiste() throws Exception {
        Mercancia primera = servicio.asegurarQr("SN-UADY-0001");
        java.nio.file.attribute.FileTime antes = Files.getLastModifiedTime(Path.of(primera.rutaQr()));
        Thread.sleep(20);

        Mercancia segunda = servicio.asegurarQr("sn-uady-0001");

        assertEquals(primera, segunda);
        assertEquals(antes, Files.getLastModifiedTime(Path.of(segunda.rutaQr())));
    }

    @Test
    @Tag("RF-11")
    @DisplayName("RF-11 · Asegurar QR regenera si el archivo se borró")
    void asegurarQrRegeneraSiElArchivoSeBorro() throws Exception {
        Path ruta = Path.of(servicio.asegurarQr("SN-UADY-0003").rutaQr());
        Files.delete(ruta);

        servicio.asegurarQr("SN-UADY-0003");

        assertTrue(Files.isRegularFile(ruta));
    }

    @Test
    @Tag("RF-11")
    @DisplayName("RF-11 · Asegurar QR de una unidad inexistente es error de negocio")
    void asegurarQrDeUnaUnidadInexistenteEsErrorDeNegocio() {
        assertThrows(NegocioException.class, () -> servicio.asegurarQr("NO-EXISTE"));
    }

    @Test
    @Tag("RF-09")
    @DisplayName("RF-09 · Filtra por producto y por estado")
    void filtraPorProductoYPorEstado() {
        assertEquals(2, servicio.listar(1, null).size());
        assertEquals(4, servicio.listar(null, EstadoMercancia.VENDIDO).size());
        assertEquals(6, servicio.listar(null, EstadoMercancia.EN_ALMACEN).size());
        assertEquals(1, servicio.listar(1, EstadoMercancia.VENDIDO).size());
        assertEquals("SN-UADY-0002", servicio.listar(1, EstadoMercancia.VENDIDO).get(0).numeroSerie());
    }
}
