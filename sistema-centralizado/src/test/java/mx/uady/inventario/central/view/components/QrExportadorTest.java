package mx.uady.inventario.central.view.components;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import mx.uady.inventario.central.view.components.QrExportador.Resultado;

/** Guardado de una copia del PNG del código QR en la ruta elegida (RF-12). */
class QrExportadorTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};

    @TempDir
    Path carpeta;

    private Path origen;

    @BeforeEach
    void preparar() throws IOException {
        origen = Files.write(carpeta.resolve("SN-UADY-0001.png"), PNG);
    }

    @Test
    @Tag("RF-12")
    @DisplayName("RF-12 · Guarda una copia idéntica en la ruta elegida y lo confirma")
    void guardaUnaCopiaIdentica() throws IOException {
        Path destino = carpeta.resolve("copia.png");

        Resultado resultado = QrExportador.guardar(origen, destino).orElseThrow();

        assertTrue(resultado.guardado());
        assertEquals("El código QR se guardó correctamente.", resultado.mensaje());
        assertArrayEquals(PNG, Files.readAllBytes(destino));
        assertArrayEquals(PNG, Files.readAllBytes(origen), "el original no cambia");
    }

    @Test
    @Tag("RF-12")
    @DisplayName("RF-12 · Sobrescribe un archivo existente y acepta guardar sobre el mismo PNG")
    void sobrescribeYAceptaElMismoArchivo() throws IOException {
        Path destino = Files.writeString(carpeta.resolve("viejo.png"), "contenido anterior");

        assertTrue(QrExportador.guardar(origen, destino).orElseThrow().guardado());
        assertArrayEquals(PNG, Files.readAllBytes(destino));

        assertTrue(QrExportador.guardar(origen, origen).orElseThrow().guardado());
        assertArrayEquals(PNG, Files.readAllBytes(origen));
    }

    @Test
    @Tag("RF-12")
    @DisplayName("RF-12 · Si el Administrador cancela no se crea ningún archivo")
    void siSeCancelaNoSeCreaNada() throws IOException {
        long antes;
        try (var archivos = Files.list(carpeta)) {
            antes = archivos.count();
        }

        assertEquals(Optional.empty(), QrExportador.guardar(origen, null));

        try (var archivos = Files.list(carpeta)) {
            assertEquals(antes, archivos.count());
        }
    }

    @Test
    @Tag("RF-12")
    @DisplayName("RF-12 · Un PNG inexistente o una carpeta de destino inexistente se informan sin lanzar")
    void losErroresDeArchivoSeInformanSinLanzar() {
        Resultado sinOrigen = QrExportador.guardar(carpeta.resolve("no-existe.png"), carpeta.resolve("a.png"))
                .orElseThrow();
        assertFalse(sinOrigen.guardado());
        assertTrue(sinOrigen.mensaje().startsWith("No se pudo guardar el código QR: "), sinOrigen.mensaje());
        assertFalse(Files.exists(carpeta.resolve("a.png")));

        Path sinCarpeta = carpeta.resolve("no-existe").resolve("copia.png");
        Resultado resultado = QrExportador.guardar(origen, sinCarpeta).orElseThrow();
        assertFalse(resultado.guardado());
        assertTrue(resultado.mensaje().startsWith(QrExportador.NO_GUARDADO), resultado.mensaje());
        assertFalse(Files.exists(sinCarpeta));
    }

    @Test
    @Tag("RF-12")
    @DisplayName("RF-12 · Rechaza un origen nulo")
    void rechazaUnOrigenNulo() {
        assertThrows(NullPointerException.class, () -> QrExportador.guardar(null, carpeta.resolve("a.png")));
    }
}
