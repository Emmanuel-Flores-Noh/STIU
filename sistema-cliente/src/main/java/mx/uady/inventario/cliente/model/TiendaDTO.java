package mx.uady.inventario.cliente.model;

/**
 * Objeto de transferencia de datos (DTO) para representar una tienda o almacén dentro de la interfaz gráfica del Sistema Proveedor.
 * <p>Trazabilidad con Requisitos:
 * <ul>
 *   <li><b>RF-25:</b> Carga de la lista de tiendas al abrir la ventana.</li>
 *   <li><b>RF-28:</b> Despliegue de la lista de tiendas destino ordenadas alfabéticamente por nombre.</li>
 *   <li><b>RF-31:</b> Validación de tienda seleccionada para habilitar el registro de venta.</li>
 *   <li><b>RF-32:</b> Registro de la venta asociado a la tienda elegida.</li>
 * </ul>
 * </p>
 *
 */
public class TiendaDTO {
    private Long idTienda;
    private String nombre;
    private String direccion;

    /**
     * Constructor
     * @param idTienda Id
     * @param nombre Nombre
     * @param direccion Direccion
     */
    public TiendaDTO(Long idTienda, String nombre, String direccion) {
        this.idTienda = idTienda;
        this.nombre = nombre;
        this.direccion = direccion;
    }

    /**
     * Obtiene el identificador único de la tienda.
     * 
     * @return El ID de la tienda.
     */
    public Long getIdTienda() {
        return idTienda;
    }

    /**
     * Obtiene el nombre comercial de la tienda o almacén.
     * 
     * @return El nombre de la sucursal.
     */
    public String getNombre() {
        return nombre;
    }
    /**
     * Obtiene la dirección física de la tienda.
     * 
     * @return La dirección de la sucursal.
     */
    public String getDireccion() {
        return direccion;
    }


    /**
     * Devuelve la representación en texto de la tienda.
     * 
     * @return El nombre comercial de la tienda o un texto genérico si es nulo.
     */
    @Override
    public String toString() {
        return getNombre() != null ? getNombre() : "Tienda #" + getIdTienda();
    }
}