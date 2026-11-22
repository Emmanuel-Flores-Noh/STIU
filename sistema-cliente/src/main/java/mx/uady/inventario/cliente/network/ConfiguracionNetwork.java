package mx.uady.inventario.cliente.network;

import java.util.Map;
import java.util.Objects;

/**
 * Parámetros de red del Sistema Cliente (Sistema 2), leídos de variables de entorno.
 *
 * <p>El tiempo límite del cliente debe cubrir el peor caso del Broker: 3 intentos de 2 s de conexión
 * más 5 s de lectura, con 0.2 s entre intentos (21.4 s). Por eso el valor por defecto es de 22,000 ms
 * y se advierte cuando se configura uno menor ({@link #timeoutInferiorAlRecomendado()}). Un valor no
 * numérico o menor o igual a 0 impide el arranque.</p>
 *
 * @param brokerHost   host o dirección IP del Broker ({@code BROKER_HOST})
 * @param brokerPuerto puerto del Broker ({@code BROKER_PORT})
 * @param timeoutMs    tiempo límite total de una solicitud, en milisegundos ({@code CLIENT_TIMEOUT_MS})
 */
public record ConfiguracionNetwork(
        String brokerHost,
        int brokerPuerto,
        int timeoutMs) {

    /** Nombre de la variable de entorno con el host del Broker. */
    public static final String VAR_BROKER_HOST = "BROKER_HOST";
    /** Nombre de la variable de entorno con el puerto del Broker. */
    public static final String VAR_BROKER_PUERTO = "BROKER_PORT";
    /** Nombre de la variable de entorno con el tiempo límite del cliente. */
    public static final String VAR_CLIENT_TIMEOUT = "CLIENT_TIMEOUT_MS";

    /** Tiempo mínimo (ms) para recibir siempre la respuesta del Broker con su configuración por defecto. */
    public static final int TIMEOUT_MINIMO_RECOMENDADO_MS = 22_000;

    private static final String HOST_POR_DEFECTO = "localhost";
    private static final String PUERTO_BROKER_POR_DEFECTO = "5000";
    private static final String TIMEOUT_POR_DEFECTO = String.valueOf(TIMEOUT_MINIMO_RECOMENDADO_MS);

    /**
     * Valida los valores recibidos.
     *
     * @throws IllegalArgumentException si el host está vacío, el puerto está fuera de 1-65535
     *                                  o el tiempo límite no es positivo
     */
    public ConfiguracionNetwork {
        brokerHost = requerirTexto(brokerHost, "host del broker");
        validarPuerto(brokerPuerto, VAR_BROKER_PUERTO);
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException(VAR_CLIENT_TIMEOUT + " debe ser positivo: " + timeoutMs);
        }
    }

    /**
     * Lee la configuración de las variables de entorno del proceso.
     *
     * @return configuración lista para el cliente TCP
     * @throws IllegalArgumentException si algún valor no es válido
     */
    public static ConfiguracionNetwork desdeEntorno() {
        return desdeMapa(System.getenv());
    }

    /**
     * Construye la configuración a partir de un mapa de variables (útil en pruebas).
     *
     * @param variables mapa de variables indexado por nombre
     * @return configuración resultante
     * @throws IllegalArgumentException si algún valor no es válido
     */
    public static ConfiguracionNetwork desdeMapa(Map<String, String> variables) {
        Objects.requireNonNull(variables, "El mapa de variables no puede ser nulo");
        return new ConfiguracionNetwork(
                valorODefecto(variables.get(VAR_BROKER_HOST), HOST_POR_DEFECTO),
                parseEntero(valorODefecto(variables.get(VAR_BROKER_PUERTO), PUERTO_BROKER_POR_DEFECTO), VAR_BROKER_PUERTO),
                parseEntero(valorODefecto(variables.get(VAR_CLIENT_TIMEOUT), TIMEOUT_POR_DEFECTO), VAR_CLIENT_TIMEOUT));
    }

    /**
     * Indica si el tiempo límite configurado es menor al necesario para esperar al Broker en su peor caso.
     *
     * @return {@code true} si {@code timeoutMs} es menor a {@link #TIMEOUT_MINIMO_RECOMENDADO_MS}
     */
    public boolean timeoutInferiorAlRecomendado() {
        return timeoutMs < TIMEOUT_MINIMO_RECOMENDADO_MS;
    }

    private static void validarPuerto(int puerto, String variable) {
        if (puerto <= 0 || puerto > 65535) {
            throw new IllegalArgumentException(variable + " fuera de rango válido (1-65535): " + puerto);
        }
    }

    private static String valorODefecto(String valor, String defecto) {
        return valor == null || valor.isBlank() ? defecto : valor.trim();
    }

    private static String requerirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " no puede ser nulo o vacío");
        }
        return valor.trim();
    }

    private static int parseEntero(String valor, String variable) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(variable + " no es un número válido: " + valor, e);
        }
    }
}