package mx.uady.inventario.cliente.network;

import java.util.Map;
import java.util.Objects;

/**
 * Configuración de red del cliente. Define el parámetro CLIENT_TIMEOUT_MS garantizando
 * que supere el tiempo de respuesta en el peor caso del Broker (~15s) (documentado en README y docker-compose.yml).
 *
 * <p>Trazabilidad con Requisitos:
 * <ul>
 *   <li><b>RF-39:</b> Comunicación exclusiva con el Broker a través de {@code BROKER_HOST} y {@code BROKER_PORT}.</li>
 *   <li><b>RF-40:</b> Tiempo límite del cliente ({@code CLIENT_TIMEOUT_MS}) de al menos 22,000 ms; rechaza valores no numéricos o &le; 0.</li>
 *   <li><b>RNF-05:</b> Tiempos máximos de espera alineados con los reintentos del Broker.</li>
 * </ul>
 * </p>
 *
 * @param brokerHost   host o dirección IP del Broker
 * @param brokerPuerto puerto del Broker
 * @param timeoutMs    tiempo límite total de una solicitud, en milisegundos
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
     * <p>Requisitos: RF-39, RF-40.</p>
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
     * <p>Requisitos: RF-39, RF-40.</p>
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
     * <p>Requisitos: RF-40, RNF-05.</p>
     * @return {@code true} si {@code timeoutMs} es menor a {@link #TIMEOUT_MINIMO_RECOMENDADO_MS}
     */
    public boolean timeoutInferiorAlRecomendado() {
        return timeoutMs < TIMEOUT_MINIMO_RECOMENDADO_MS;
    }

    /**
     * Valida que el puerto se encuentre en el rango permitido por TCP (1-65535).
     *
     * <p>Requisito: RF-39.</p>
     *
     * @param puerto   número de puerto
     * @param variable nombre de la variable de entorno
     */
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