package mx.uady.inventario.cliente.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Normalización y presentación de texto para el encargado: comparación sin acentos ni mayúsculas,
 * estado de las unidades, fechas {@code dd/MM/yyyy HH:mm} y precios en pesos.
 */
public final class FormateadorTexto {

    private static final Pattern MARCAS_DIACRITICAS = Pattern.compile("\\p{M}+");
    private static final String EN_ALMACEN = "en almacen";
    private static final String SIN_DATO = "N/D";
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter SOLO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale LOCALE_MX = Locale.forLanguageTag("es-MX");

    private FormateadorTexto() {
    }

    /**
     * Convierte el texto a minúsculas y le quita los acentos
     *
     * <p>Requisitos: RF-27.</p>
     * @param texto texto original; puede ser nulo
     * @return texto normalizado; cadena vacía si el original es nulo
     */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String descompuesto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return MARCAS_DIACRITICAS.matcher(descompuesto).replaceAll("").toLowerCase(Locale.ROOT);
    }

    /**
     * Indica si el estado equivale a «En almacén», sin importar mayúsculas, acentos ni guiones bajos
     *
     * <p>Requisitos: RF-26.</p>
     *
     * @param estado estado recibido; puede ser nulo
     * @return {@code true} si la unidad está en almacén
     */
    public static boolean esEnAlmacen(String estado) {
        return EN_ALMACEN.equals(normalizar(estado).replace('_', ' ').trim());
    }

    /**
     * Da al estado un texto legible para la tabla
     *
     * @param estado estado recibido; puede ser nulo
     * @return texto para mostrar; cadena vacía si no hay estado
     */
    public static String estadoATexto(String estado) {
        if (estado == null || estado.isBlank()) {
            return "";
        }
        if (esEnAlmacen(estado)) {
            return "En almacén";
        }
        String texto = estado.trim().replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    /**
     * Da formato a una fecha ISO 8601 usando la zona horaria del equipo.
     *
     * @param texto fecha en texto; puede ser nulo
     * @return fecha en {@code dd/MM/yyyy HH:mm}, el texto original si no se puede interpretar, o "N/D" si no hay fecha
     */
    public static String fecha(String texto) {
        return fecha(texto, ZoneId.systemDefault());
    }

    /**
     * Da formato a una fecha ISO 8601. Si trae zona horaria
     *
     * <p>Requisitos: RF-32.</p>
     * @param texto fecha en texto; puede ser nulo
     * @param zona  zona a la que se convierten las fechas con zona horaria
     * @return fecha en {@code dd/MM/yyyy HH:mm}, el texto original si no se puede interpretar, o "N/D" si no hay fecha
     */
    public static String fecha(String texto, ZoneId zona) {
        if (texto == null || texto.isBlank()) {
            return SIN_DATO;
        }
        String limpio = texto.trim().replaceFirst("^(\\d{4}-\\d{2}-\\d{2}) (\\d)", "$1T$2");
        try {
            TemporalAccessor fecha = DateTimeFormatter.ISO_DATE_TIME.parseBest(limpio, OffsetDateTime::from, LocalDateTime::from);
            if (fecha instanceof OffsetDateTime conZona) {
                return conZona.atZoneSameInstant(zona).format(FECHA_HORA);
            }
            return ((LocalDateTime) fecha).format(FECHA_HORA);
        } catch (DateTimeParseException e) {
            return formatearSoloFecha(limpio, texto);
        }
    }

    /**
     * Formatea un precio como moneda (pesos mexicanos).
     *
     * @param precio precio a mostrar; puede ser nulo
     * @return precio con símbolo de moneda y dos decimales, o "N/D" si no hay precio
     */
    public static String precio(BigDecimal precio) {
        return precio == null ? SIN_DATO : NumberFormat.getCurrencyInstance(LOCALE_MX).format(precio);
    }

    private static String formatearSoloFecha(String limpio, String original) {
        try {
            return LocalDate.parse(limpio).format(SOLO_FECHA);
        } catch (DateTimeParseException e) {
            return original;
        }
    }
}
