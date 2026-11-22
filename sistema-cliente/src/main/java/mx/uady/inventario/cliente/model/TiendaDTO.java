package mx.uady.inventario.cliente.model;

/**
 * Objeto de transferencia de datos (DTO) para representar una tienda o almacén dentro de la interfaz gráfica del Sistema Proveedor.
 */
public class TiendaDTO {
    private Long idTienda;
    private String nombre;
    private String direccion;

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
        return nombre != null ? nombre : "Tienda #" + idTienda;
    }
}