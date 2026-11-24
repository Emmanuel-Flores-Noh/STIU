package mx.uady.inventario.central.network;

import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Lectura y validación de los parámetros JSON de una solicitud.
 *
 * <p>Los errores se señalan con {@link IllegalArgumentException}, que
 * {@link DespachadorSolicitudes} responde como {@code PARAMETROS_INVALIDOS} con el mismo mensaje.</p>
 */
final class ParametrosSolicitud {

    private ParametrosSolicitud() {
    }

    /**
     * Lee un parámetro de texto obligatorio.
     *
     * @param parametros objeto JSON de la solicitud
     * @param nombre     nombre del parámetro
     * @return valor sin espacios al inicio ni al final
     * @throws IllegalArgumentException si falta o está vacío
     */
    static String textoObligatorio(JsonObject parametros, String nombre) {
        return leer(parametros, nombre, valor -> {
            String texto = valor.getAsString().strip();
            if (texto.isEmpty()) {
                throw new IllegalArgumentException("El parámetro " + nombre + " no puede estar vacío");
            }
            return texto;
        });
    }

    /**
     * Lee un parámetro entero positivo obligatorio.
     *
     * @param parametros objeto JSON de la solicitud
     * @param nombre     nombre del parámetro
     * @return valor mayor que cero
     * @throws IllegalArgumentException si falta, no es entero o no es positivo
     */
    static int enteroPositivo(JsonObject parametros, String nombre) {
        return leer(parametros, nombre, valor -> {
            int numero;
            try {
                numero = valor.getAsInt();
            } catch (NumberFormatException | UnsupportedOperationException e) {
                throw new IllegalArgumentException("El parámetro " + nombre + " debe ser un entero", e);
            }
            if (numero <= 0) {
                throw new IllegalArgumentException("El parámetro " + nombre + " debe ser positivo");
            }
            return numero;
        });
    }

    /**
     * Verifica que el parámetro exista y aplica la conversión indicada.
     *
     * @param parametros objeto JSON de la solicitud
     * @param nombre     nombre del parámetro
     * @param conversion función que convierte y valida el valor
     * @param <T>        tipo del valor convertido
     * @return valor convertido
     * @throws IllegalArgumentException si el parámetro falta o es nulo
     */
    private static <T> T leer(JsonObject parametros, String nombre, Function<JsonElement, T> conversion) {
        if (parametros == null || !parametros.has(nombre) || parametros.get(nombre).isJsonNull()) {
            throw new IllegalArgumentException("Falta el parámetro " + nombre);
        }
        return conversion.apply(parametros.get(nombre));
    }
}
