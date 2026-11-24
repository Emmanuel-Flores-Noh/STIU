package mx.uady.inventario.common.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link LocalDateTimeAdapter}: formato ISO-8601 único para las fechas del contrato.
 *
 * <ul>
 *   <li>RF-17: la venta devuelve {@code fecha_venta}.</li>
 *   <li>RF-32: el Sistema 2 muestra esa fecha al confirmar la venta.</li>
 * </ul>
 */
@Tag("RF-17")
@Tag("RF-32")
class LocalDateTimeAdapterTest {

    @Test
    @DisplayName("RF-17: una fecha con segundos se escribe en ISO-8601 y se lee igual")
    void fechaConSegundos_seEscribeYSeLeeIgual() {
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 12, 9, 30, 15);

        String texto = LocalDateTimeAdapter.formatear(fecha);

        assertEquals("2026-11-12T09:30:15", texto);
        assertEquals(fecha, LocalDateTimeAdapter.parsear(texto));
    }

    @Test
    @DisplayName("RF-17: a medianoche los segundos :00 siempre se incluyen")
    void medianoche_incluyeLosSegundos() {
        assertEquals("2026-11-12T00:00:00",
                LocalDateTimeAdapter.formatear(LocalDateTime.of(2026, 11, 12, 0, 0, 0)));
    }

    @Test
    @DisplayName("RF-17: las fracciones de segundo se conservan al escribir y leer")
    void fraccionesDeSegundo_seConservan() {
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 12, 9, 30, 15, 123_000_000);

        String texto = LocalDateTimeAdapter.formatear(fecha);

        assertEquals("2026-11-12T09:30:15.123", texto);
        assertEquals(fecha, LocalDateTimeAdapter.parsear(texto));
    }

    @Test
    @DisplayName("RF-32: se lee una fecha escrita sin segundos, como las generadas antes de unificar el formato")
    void fechaSinSegundos_seLee() {
        assertEquals(LocalDateTime.of(2026, 11, 12, 0, 0), LocalDateTimeAdapter.parsear("2026-11-12T00:00"));
    }

    @Test
    @DisplayName("RF-32: un texto que no es fecha ISO-8601 se rechaza")
    void textoInvalido_lanzaExcepcion() {
        assertThrows(DateTimeParseException.class, () -> LocalDateTimeAdapter.parsear("12/11/2026 09:30"));
    }

    @Test
    @DisplayName("RF-17, RF-32: Gson escribe las fechas con el mismo formato que formatear")
    void gson_usaElMismoFormatoQueFormatear() {
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 12, 0, 0, 0);

        String json = GsonFactory.DEFAULT.toJson(fecha);

        assertEquals("\"" + LocalDateTimeAdapter.formatear(fecha) + "\"", json);
        assertEquals(fecha, GsonFactory.DEFAULT.fromJson(json, LocalDateTime.class));
    }
}
