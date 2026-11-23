package mx.uady.inventario.cliente.model;

/**
 * Objeto de transferencia de datos (DTO) de una unidad de mercancía disponible, tal como la
 * devuelve la operación {@code listarInventario} del Sistema 1: incluye el nombre del producto
 * para mostrarlo y buscarlo en la tabla del proveedor.
 * <p>Trazabilidad con Requisitos:
 * <ul>
 *   <li><b>RF-25:</b> Carga del inventario disponible en segundo plano al iniciar o actualizar la vista.</li>
 *   <li><b>RF-26:</b> Presentación en la tabla de inventario (columnas Número de serie, Producto y Estado «En almacén»).</li>
 *   <li><b>RF-27:</b> Filtrado de inventario por número de serie o nombre de producto.</li>
 *   <li><b>RF-31:</b> Selección de una unidad requerida para habilitar el registro de venta.</li>
 *   <li><b>RF-32:</b> Venta de la unidad seleccionada y posterior remoción de la vista.</li>
 * </ul>
 * </p>
 */
public class MercanciaDTO {
    private String numeroSerie;
    private Integer idProducto;
    private String nombreProducto;
    private String estado;

    /**
     * Constructor vacío requerido por Gson para el unmarshalling.
     */
    public MercanciaDTO() {
    }

    /**
     * Crea el DTO con todos sus datos.
     *
     * <p>Requisito: RF-26.</p>
     * @param numeroSerie    número de serie de la unidad
     * @param idProducto     identificador del producto
     * @param nombreProducto nombre del producto
     * @param estado         estado de la unidad (p. ej. {@code EN_ALMACEN})
     */
    public MercanciaDTO(String numeroSerie, Integer idProducto, String nombreProducto, String estado) {
        this.numeroSerie = numeroSerie;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.estado = estado;
    }

    /**
     * Obtiene el número de serie de la unidad.
     *
     *
     * @return número de serie
     */
    public String getNumeroSerie() {
        return numeroSerie;
    }

    /**
     * Obtiene el identificador del producto.
     *
     * @return id del producto
     */
    public Integer getIdProducto() {
        return idProducto;
    }

    /**
     * Obtiene el nombre del producto.
     *
     * @return nombre del producto
     */
    public String getNombreProducto() {
        return nombreProducto;
    }

    /**
     * Obtiene el estado de la unidad.
     *
     * @return estado como texto
     */
    public String getEstado() {
        return estado;
    }
}
