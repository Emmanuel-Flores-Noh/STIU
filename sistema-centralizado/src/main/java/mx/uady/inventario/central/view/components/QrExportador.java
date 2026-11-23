package mx.uady.inventario.central.view.components;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;

/**
 * Guarda una copia del PNG de un código QR en la ruta que elige el Administrador (RF-12).
 * No usa Swing: devuelve el mensaje que la pantalla debe mostrar.
 */
public final class QrExportador {

    /** Mensaje cuando la copia se guarda. */
    public static final String GUARDADO = "El código QR se guardó correctamente.";
    /** Inicio del mensaje cuando la copia falla; le sigue la causa. */
    public static final String NO_GUARDADO = "No se pudo guardar el código QR: ";

    /**
     * Resultado de guardar la copia.
     *
     * @param guardado {@code true} si el archivo quedó guardado
     * @param mensaje  texto para el Administrador
     */
    public record Resultado(boolean guardado, String mensaje) {
    }

    private QrExportador() {
    }

    /**
     * Copia el PNG al destino, sobrescribiéndolo si ya existe.
     *
     * @param origen  PNG generado por el sistema
     * @param destino archivo elegido, o {@code null} si el Administrador canceló
     * @return el resultado, o vacío si no había destino (no se crea ningún archivo)
     * @throws NullPointerException si {@code origen} es {@code null}
     */
    public static Optional<Resultado> guardar(Path origen, Path destino) {
        Objects.requireNonNull(origen, "origen");
        if (destino == null) {
            return Optional.empty();
        }
        try {
            Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);
            return Optional.of(new Resultado(true, GUARDADO));
        } catch (IOException error) {
            return Optional.of(new Resultado(false, NO_GUARDADO + error.getMessage()));
        }
    }
}
