package mx.uady.inventario.cliente.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

/**
 * Precio anterior de un producto con la fecha en que cambió. Corresponde a un elemento del arreglo
 * {@code historial} que devuelve {@code listarPreciosProducto}, serializado desde el modelo
 * {@code HistorialPrecio} de {@code common} ({@code precioHistorico}, {@code fechaCambio}).
 *
 * <p>Requisitos: RF-23 (contrato de {@code listarPreciosProducto}) y RF-36 (historial de precios).</p>
 */

public class PrecioDTO {

    @SerializedName("precioHistorico")
    private BigDecimal precio;

    @SerializedName("fechaCambio")
    private String fecha;

    /**
     * Crea el DTO con todos sus datos.
     *
     * @param precio precio anterior
     * @param fecha  fecha del cambio, en texto ISO 8601
     */

    public PrecioDTO(BigDecimal precio, String fecha){
        this.precio = precio;
        this.fecha = fecha;
    }
    /**
     * Obtiene el precio anterior.
     *
     * @return precio; puede ser nulo
     */

    public BigDecimal getPrecio() {
        return precio;
    }

    /**
     * Obtiene la fecha del cambio de precio.
     *
     * @return fecha (puede ser nula)
     */

    public String getFecha() {
        return fecha;
    }
}
