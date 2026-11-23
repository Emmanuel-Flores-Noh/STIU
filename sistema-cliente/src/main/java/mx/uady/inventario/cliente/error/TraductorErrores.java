package mx.uady.inventario.cliente.error;
import com.google.gson.JsonParseException;
import mx.uady.inventario.common.error.CodigoError;

import java.io.IOException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * Convierte errores técnicos (fallos de comunicación y códigos de error del contrato) en mensajes
 * que el encargado de ventas pueda entender, y decide si tiene sentido ofrecer reintentar.
 *
 * <p>Trazabilidad con Requisitos:
 * <ul>
 *   <li><b>RF-33:</b> Informar rechazos de venta ({@code PARAMETROS_INVALIDOS}, {@code RECURSO_NO_ENCONTRADO}) sin ofrecer reintentar.</li>
 *   <li><b>RF-37:</b> Presentar cada error del catálogo o de red con un mensaje descriptivo comprensible.</li>
 *   <li><b>RF-38:</b> Ofrecer reintentar operaciones que fallaron por red o servidor transitorio sin reintentar automáticamente.</li>
 *   <li><b>RF-40:</b> Traducción y explicación del error {@code TIMEOUT} (E005) recibido del Broker.</li>
 *   <li><b>RNF-05:</b> Tiempos máximos de espera alineados con el Broker.</li>
 *   <li><b>RNF-33:</b> Mensajes en español sin exposición de trazas ni JSON crudo.</li>
 * </ul>
 * </p>
 */
public final class TraductorErrores {

    private static final String TITULO_TIEMPO = "Tiempo de espera agotado";

    private TraductorErrores() {
    }

    /**
     * Describe un error: título, mensaje y si conviene reintentar.
     *
     * <p>Requisitos: RF-33, RF-37, RF-38.</p>
     * @param error excepción recibida (puede venir envuelta en {@link ExecutionException})
     * @return descripción lista para mostrar al encargado
     */
    public static DescripcionError describir(Throwable error) {
        Throwable causa = desenvolver(error);
        if (causa instanceof ErrorServicioException errorServicio) {
            return describirServidor(errorServicio.getCodigoError(), errorServicio.getMessage());
        }
        if (causa instanceof ComunicacionException comunicacion) {
            return describirRed(comunicacion.getTipo());
        }
        if (causa instanceof JsonParseException) {
            return describirRed(ComunicacionError.RESPUESTA_INVALIDA);
        }
        if (causa instanceof IOException) {
            return describirRed(ComunicacionError.CONEXION_INTERRUMPIDA);
        }
        return new DescripcionError("Error inesperado",
                "Ocurrió un problema inesperado en el sistema. Intente de nuevo.", true);
    }

    /**
     * Traduce un error a un mensaje para el encargado.
     *
     * <p>Requisitos: RF-37, RNF-33.</p>
     * @param error excepción recibida
     * @return mensaje entendible en español
     */
    public static String traducir(Throwable error) {
        return describir(error).mensaje();
    }

    /**
     * Indica si conviene ofrecer reintentar (RF-38): sí para fallas de red o del servidor, no para
     * {@code PARAMETROS_INVALIDOS} y {@code RECURSO_NO_ENCONTRADO}, cuyo resultado sería el mismo.
     *
     * <p>Requisitos: RF-33, RF-38.</p>
     * @param error excepción recibida
     * @return {@code true} si reintentar podría resolver el problema
     */
    public static boolean esReintentable(Throwable error) {
        return describir(error).reintentable();
    }

    /**
     * Genera la descripción para fallos originados en la capa de red o transporte TCP.
     *
     * <p>Requisitos: RF-37, RF-39, RF-40.</p>
     *
     * @param tipo categoría del fallo de comunicación
     * @return descripción amigable con título y reintentabilidad
     */
    private static DescripcionError describirRed(ComunicacionError tipo) {
        return switch (tipo) {
            case TIEMPO_AGOTADO -> new DescripcionError(TITULO_TIEMPO,
                    "El servidor tardó demasiado en responder. Intente de nuevo en unos momentos.", true);
            case SIN_CONEXION -> new DescripcionError("Sin conexión con el Broker",
                    "No hay conexión con el servidor central. Verifique que el Broker esté encendido.", true);
            case CONEXION_INTERRUMPIDA -> new DescripcionError("Conexión interrumpida",
                    "Se perdió la conexión con el Broker antes de recibir la respuesta.", true);
            case RESPUESTA_INVALIDA, ERROR_SERVIDOR -> new DescripcionError("Respuesta no válida",
                    "El Broker envió una respuesta que no se pudo interpretar.", true);
        };
    }

    /**
     * Genera la descripción amigable para códigos de error devueltos por el servidor remoto según el catálogo.
     *
     * <p>Requisitos: RF-33, RF-37, RF-38, RF-40.</p>
     *
     * @param codigo  código de error del contrato
     * @param mensaje mensaje textual provisto por el servidor
     * @return descripción con mensaje comprensible y reintentabilidad
     */
    private static DescripcionError describirServidor(CodigoError codigo, String mensaje) {
        if (codigo == null) {
            return new DescripcionError("Error del servidor",
                    mensaje != null ? mensaje : "El servidor rechazó la operación.", true);
        }
        return switch (codigo) {
            case PARAMETROS_INVALIDOS, RECURSO_NO_ENCONTRADO -> new DescripcionError("Operación rechazada",
                    mensaje != null ? mensaje : "Los datos enviados no son válidos.", false);
            case TIMEOUT -> new DescripcionError(TITULO_TIEMPO, describirTimeoutDelBroker(codigo, mensaje), true);
            case PROVEEDOR_NO_REGISTRADO -> new DescripcionError("Sistema central no disponible",
                    "El sistema central no está disponible en este momento. Verifique que esté encendido.", true);
            case SERVICIO_DESCONOCIDO, OPERACION_NO_SOPORTADA -> new DescripcionError("Operación no reconocida",
                    "El sistema central no reconoce esta operación. Contacte al administrador." + detalle(mensaje), true);
            case ERROR_INTERNO -> new DescripcionError("Error del sistema central",
                    "Ocurrió un error en el sistema central." + detalle(mensaje), true);
        };
    }

    /**
     * Conserva el mensaje descriptivo que el Broker envió con E005 y lo acompaña de explicación comprensible.
     *
     * <p>Requisitos: RF-40, RNF-05, RNF-33.</p>
     *
     * @param codigo        código de error (TIMEOUT / E005)
     * @param mensajeBroker mensaje retornado por el Broker
     * @return texto claro explicando el timeout
     */
    private static String describirTimeoutDelBroker(CodigoError codigo, String mensajeBroker) {
        boolean existeMensajeBroker = mensajeBroker != null && !mensajeBroker.isBlank();
        String detalleBroker = existeMensajeBroker ? mensajeBroker : codigo.getMensajePorDefecto();
        return "El sistema central no respondió a tiempo (error " + codigo.getCodigo() + "): " + detalleBroker + ". Intente de nuevo en unos momentos.";
    }

    /** Formatea el detalle textual si está presente. */
    private static String detalle(String mensaje) {
        boolean esMensajeVacio = mensaje != null && !mensaje.isBlank();
        return esMensajeVacio ? "\nDetalle: " + mensaje : "";
    }

    /**
     * Desenuelve la causa raíz de un error encapsulado en {@link ExecutionException} o {@link CompletionException}.
     *
     * @param error excepción posiblemente envuelta
     * @return excepción causante original
     */
    private static Throwable desenvolver(Throwable error) {
        Throwable actual = error;
        while ((actual instanceof ExecutionException || actual instanceof CompletionException)
                && actual.getCause() != null) {
            actual = actual.getCause();
        }
        return actual;
    }
}
