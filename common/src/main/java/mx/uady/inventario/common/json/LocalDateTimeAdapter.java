package mx.uady.inventario.common.json;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Adaptador Gson para java.time.LocalDateTime (formato ISO-8601).
 *
 * <p>Es la única fuente del formato de fecha y hora del contrato: los campos que no pasan por
 * Gson (por ejemplo {@code fecha_venta}) usan {@link #formatear(LocalDateTime)} y
 * {@link #parsear(String)} para escribirse y leerse igual en los tres componentes.</p>
 */
public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

    /** Formato ISO-8601 sin zona horaria, p. ej. {@code 2026-11-12T09:30:00}. */
    public static final DateTimeFormatter FORMATO_ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Convierte una fecha y hora al formato ISO-8601 del contrato. Los segundos siempre se incluyen.
     *
     * @param valor fecha y hora; no puede ser {@code null}
     * @return texto ISO-8601
     */
    public static String formatear(LocalDateTime valor) {
        return valor.format(FORMATO_ISO);
    }

    /**
     * Interpreta un texto ISO-8601 del contrato.
     *
     * @param texto fecha y hora en formato ISO-8601
     * @return la fecha y hora
     * @throws java.time.format.DateTimeParseException si el texto no tiene el formato esperado
     */
    public static LocalDateTime parsear(String texto) {
        return LocalDateTime.parse(texto, FORMATO_ISO);
    }

    /**
     * Escribir un LocalDateTime al formato ISO-8601
     * @param out El json de salida
     * @param value the Java object to write. May be null.
     * @throws IOException
     */
    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(formatear(value));
        }
    }

    /**
     * Leer una fecha en formato ISO-8601 y transformarla en un LocalDateTime
     * @param in El JSON que se lee
     * @return La fecha transformada
     * @throws IOException
     */
    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return parsear(in.nextString());
    }
}
