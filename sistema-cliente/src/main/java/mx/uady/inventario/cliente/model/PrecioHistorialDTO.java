package mx.uady.inventario.cliente.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado de {@code listarPreciosProducto}: nombre, precio actual y precios anteriores del más
 * antiguo al más reciente, tal como los envía el Sistema 1 ({@code {idProducto, nombre, precioActual, historial}}).
 */

public class PrecioHistorialDTO {
    private Integer idProducto;
    private String nombre;
    private BigDecimal precioActual;

    @SerializedName("historial")
    private List<PrecioDTO> historialPrecios;

    public PrecioHistorialDTO(){};

    /**
     * Crea el DTO con todos sus datos.
     *
     * @param idProducto       identificador del producto
     * @param nombre           nombre del producto
     * @param precioActual     precio vigente
     * @param historialPrecios precios anteriores, del más antiguo al más reciente
     */

    public PrecioHistorialDTO(Integer idProducto, String nombre, BigDecimal precioActual, List<PrecioDTO> historialPrecios) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.precioActual = precioActual;
        this.historialPrecios = historialPrecios;
    }

    /**
     * Obtiene el identificador del producto.
     *
     * @return id del producto; puede ser nulo
     */

    public Integer getIdProducto() {
        return idProducto;
    }

    /**
     * Obtiene el nombre del producto.
     *
     * @return nombre; puede ser nulo
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene el precio vigente.
     *
     * @return precio actual; puede ser nulo
     */
    public BigDecimal getPrecioActual() {
        return precioActual;
    }

    /**
     * Obtiene los precios anteriores (RF-36: lista vacía si el producto nunca cambió de precio).
     *
     * @return lista de precios anteriores; nunca nula
     */
    public List<PrecioDTO> getHistorialPrecios() {
        return historialPrecios;
    }
}
