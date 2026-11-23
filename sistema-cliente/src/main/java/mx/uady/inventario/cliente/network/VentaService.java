package mx.uady.inventario.cliente.network;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import mx.uady.inventario.cliente.error.ComunicacionException;
import mx.uady.inventario.cliente.error.ComunicacionError;
import mx.uady.inventario.cliente.model.*;
import mx.uady.inventario.common.json.GsonFactory;
import mx.uady.inventario.common.servicio.Operaciones;
import mx.uady.inventario.common.servicio.Servicios;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Operaciones del Sistema 1 que necesita el Sistema 2, expuestas con objetos de datos. Es la única
 * clase del cliente que conoce servicios, operaciones y nombres de parámetros del contrato; habla
 * exclusivamente con el Broker a través de {@link ClienteTCP}.
 *
 * <p>Trazabilidad con Requisitos:
 * <ul>
 *   <li><b>RF-25:</b> Carga asíncrona y paralela de tiendas, usuarios proveedores e inventario disponible.</li>
 *   <li><b>RF-26:</b> Consulta del inventario disponible en estado «En almacén» ({@code listarInventario}).</li>
 *   <li><b>RF-28:</b> Consulta de tiendas destino ({@code listarTiendas}).</li>
 *   <li><b>RF-29:</b> Consulta de usuarios con rol de proveedor ({@code listarUsuarios}).</li>
 *   <li><b>RF-32:</b> Registro de venta de la unidad seleccionada ({@code actualizarTiendaVenta}).</li>
 *   <li><b>RF-34:</b> Consulta de imagen PNG de código QR en Base64 ({@code obtenerQr}).</li>
 *   <li><b>RF-36:</b> Consulta del historial cronológico de precios de un producto ({@code listarPreciosProducto}).</li>
 *   <li><b>RF-39:</b> Comunicación exclusiva a través del Broker.</li>
 *   <li><b>RNF-06:</b> Rendimiento de carga concurrente de catálogos e inventario.</li>
 * </ul>
 * </p>
 *
 * <p>Todas las operaciones son bloqueantes: deben llamarse fuera del hilo de la interfaz.</p>
 */
public class VentaService {

    /**
     * Operación del Sistema 1 que devuelve el historial de precios de un producto.
     * Verificar contra {@code Operaciones} de {@code common} y reemplazar por su constante.
     */
    static final String OPERACION_HISTORIAL_PRECIOS = "obtenerHistorialPrecios";

    private final ClienteTCP cliente;
    private final ExecutorService hilos;
    private final Gson gson = GsonFactory.DEFAULT;

    /**
     * Crea el servicio con un grupo propio de tres hilos de segundo plano para la carga paralela.
     *
     * <p>Requisitos: RF-25, RNF-06.</p>
     * @param cliente cliente TCP del Broker
     */
    public VentaService(ClienteTCP cliente) {
        this(cliente, Executors.newFixedThreadPool(3, tarea -> {
            Thread hilo = new Thread(tarea, "venta-service");
            hilo.setDaemon(true);
            return hilo;
        }));
    }

    /**
     * Crea el servicio con el ejecutor indicado.
     *
     * <p>Requisitos: RF-25, RNF-06.</p>
     * @param cliente cliente TCP del Broker
     * @param hilos   ejecutor donde corren las consultas paralelas
     */
    public VentaService(ClienteTCP cliente, ExecutorService hilos) {
        this.cliente = cliente;
        this.hilos = hilos;
    }

    /**
     * Consulta las tiendas destino, en el orden en que las envía el Sistema 1.
     *
     * <p>Requisito: RF-28 (lista de tiendas ordenadas por nombre).</p>
     * @return tiendas; lista vacía si no hay
     * @throws ComunicacionException si falla la comunicación o el servidor responde con error
     */
    public List<TiendaDTO> listarTiendas() throws ComunicacionException {
        return consultarLista(Servicios.GESTOR_TIENDAS, Operaciones.LISTAR_TIENDAS,
                new TypeToken<List<TiendaDTO>>() { }.getType());
    }

    /**
     * Consulta los usuarios con rol de proveedor.
     *
     * <p>Requisito: RF-29 (usuarios con rol proveedor).</p>
     * @return usuarios; lista vacía si no hay
     * @throws ComunicacionException si falla la comunicación o el servidor responde con error
     */
    public List<UsuarioDTO> listarUsuarios() throws ComunicacionException {
        return consultarLista(Servicios.GESTOR_TIENDAS, Operaciones.LISTAR_USUARIOS,
                new TypeToken<List<UsuarioDTO>>() { }.getType());
    }

    /**
     * Consulta las unidades disponibles (solo las que están «En almacén»).
     *
     * <p>Requisito: RF-26 (inventario de mercancía disponible).</p>
     * @return unidades disponibles; lista vacía si no hay
     * @throws ComunicacionException si falla la comunicación o el servidor responde con error
     */
    public List<MercanciaDTO> listarInventarioDisponible() throws ComunicacionException {
        List<MercanciaDTO> unidades = consultarLista(Servicios.INVENTARIO, Operaciones.LISTAR_INVENTARIO,
                new TypeToken<List<MercanciaDTO>>() { }.getType());
        return unidades.stream().toList();
    }

    /**
     * Consulta en paralelo tiendas, usuarios e inventario, de modo que la carga tarde lo que la más
     * lenta y no la suma de las tres (RNF-06 y RNF-08). Si una falla, se propaga su error original.
     *
     * <p>Requisitos: RF-25 (carga al abrir ventana), RNF-06 (rendimiento de carga concurrente).</p>
     * @return las tres listas
     * @throws ComunicacionException error de la primera consulta que falle
     */
    public Catalogos cargarCatalogos() throws ComunicacionException {
        Callable<List<TiendaDTO>> consultaTiendas = this::listarTiendas;
        Callable<List<UsuarioDTO>> consultaUsuarios = this::listarUsuarios;
        Callable<List<MercanciaDTO>> consultaInventario = this::listarInventarioDisponible;
        Future<List<TiendaDTO>> tiendas = hilos.submit(consultaTiendas);
        Future<List<UsuarioDTO>> usuarios = hilos.submit(consultaUsuarios);
        Future<List<MercanciaDTO>> inventario = hilos.submit(consultaInventario);
        try {
            return new Catalogos(esperar(tiendas), esperar(usuarios), esperar(inventario));
        } finally {
            tiendas.cancel(true);
            usuarios.cancel(true);
            inventario.cancel(true);
        }
    }

    /**
     * Registra la venta de una unidad a una tienda, vinculada al usuario que la registra. Se envía una
     * sola vez: nunca se reintenta de forma automática.
     *
     * <p>Requisitos: RF-32 (registro de venta exitosa), RF-38 (no reintento automático de ventas).</p>
     * @param numeroSerie número de serie de la unidad
     * @param idTienda    identificador de la tienda destino
     * @param idUsuario   identificador del usuario que registra la venta
     * @return fecha de la venta en texto ISO, o {@code null} si el servidor no la envió (la venta ya
     *         quedó registrada: una confirmación incompleta no se reporta como error)
     * @throws ComunicacionException si falla la comunicación o el servidor rechaza la venta
     */
    public String registrarVenta(String numeroSerie, Long idTienda, Long idUsuario) throws ComunicacionException {
        JsonObject params = new JsonObject();
        params.addProperty("numero_serie", numeroSerie);
        params.addProperty("id_tienda", idTienda);
        params.addProperty("id_usuario", idUsuario);
        JsonElement resultado = cliente.invocar(Servicios.GESTOR_TIENDAS, Operaciones.ACTUALIZAR_TIENDA_VENTA, params);
        return extraerFechaVenta(resultado);
    }

    /**
     * Obtiene el código QR de una unidad (imagen PNG codificada en Base64).
     *
     * <p>Requisitos: RF-34 (visualización de QR en base64), RF-35 (guardado como PNG local).</p>
     * @param numeroSerie número de serie de la unidad
     * @return QR de la unidad
     * @throws ComunicacionException si falla la comunicación o el servidor no devuelve el QR
     */
    public QrDTO obtenerQr(String numeroSerie) throws ComunicacionException {
        JsonObject params = new JsonObject();
        params.addProperty("numero_serie", numeroSerie);
        JsonElement resultado = cliente.invocar(Servicios.INVENTARIO, Operaciones.OBTENER_QR, params);
        return convertirObjeto(resultado, QrDTO.class, "El servidor no devolvió el código QR.");
    }

    /**
     * Obtiene el historial de precios de un producto.
     *
     * <p>Requisito: RF-36 (consulta de historial de precios del producto).</p>
     * @param idProducto identificador del producto
     * @return nombre, precio actual y precios anteriores
     * @throws ComunicacionException si falla la comunicación o el servidor no devuelve el historial
     */
    public PrecioHistorialDTO obtenerHistorialPrecios(Integer idProducto) throws ComunicacionException {
        JsonObject params = new JsonObject();
        params.addProperty("id_producto", idProducto);
        JsonElement resultado = cliente.invocar(Servicios.GESTOR_PRECIOS, Operaciones.LISTAR_PRECIOS_PRODUCTO, params);
        System.out.println("JSON historial precios (" + idProducto + "): " + gson.toJson(resultado));
        return convertirObjeto(resultado, PrecioHistorialDTO.class, "El servidor no devolvió el historial de precios.");
    }

    /** Consulta una operación que devuelve un arreglo; un resultado vacío se interpreta como lista vacía. */
    private <T> List<T> consultarLista(String servicio, String operacion, Type tipo) throws ComunicacionException {
        JsonElement resultado = cliente.invocar(servicio, operacion, null);
        if (resultado == null || resultado.isJsonNull()) {
            return List.of();
        }
        try {
            List<T> lista = gson.fromJson(resultado, tipo);
            return lista != null ? lista : List.of();
        } catch (JsonParseException e) {
            throw new ComunicacionException(ComunicacionError.RESPUESTA_INVALIDA, "Lista con formato inesperado.", e);
        }
    }

    private <T> T convertirObjeto(JsonElement resultado, Class<T> clase, String mensajeSiFalta)
            throws ComunicacionException {
        if (resultado == null || !resultado.isJsonObject()) {
            throw ComunicacionException.servidor(null, mensajeSiFalta);
        }
        try {
            return gson.fromJson(resultado, clase);
        } catch (JsonParseException e) {
            throw ComunicacionException.servidor(null, mensajeSiFalta);
        }
    }

    private static String extraerFechaVenta(JsonElement resultado) {
        if (resultado == null || !resultado.isJsonObject()) {
            return null;
        }
        JsonObject objeto = resultado.getAsJsonObject();
        for (String clave : new String[]{"fecha_venta", "fechaVenta"}) {
            if (objeto.has(clave) && objeto.get(clave).isJsonPrimitive()) {
                return objeto.get(clave).getAsString();
            }
        }
        return null;
    }

    private static <T> T esperar(Future<T> consulta) throws ComunicacionException {
        try {
            return consulta.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ComunicacionException(ComunicacionError.CONEXION_INTERRUMPIDA, "Consulta interrumpida.", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ComunicacionException original) {
                throw original;
            }
            if (e.getCause() instanceof RuntimeException original) {
                throw original;
            }
            throw new ComunicacionException(ComunicacionError.CONEXION_INTERRUMPIDA, "La consulta falló.", e.getCause());
        }
    }
}
