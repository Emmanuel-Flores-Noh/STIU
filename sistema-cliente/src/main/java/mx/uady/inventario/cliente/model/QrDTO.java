package mx.uady.inventario.cliente.model;

import java.util.Base64;

/**
 * Código QR de una unidad tal como lo devuelve {@code obtenerQr}: {@code {numeroSerie, idProducto,
 * nombreProducto, pngBase64}}. La imagen PNG llega codificada en Base64.
 *
 * <p>Requisitos: RF-22 (contrato de {@code obtenerQr}), RF-34 (mostrar el QR; la imagen se pide en
 * Base64) y RF-35 (guardar el PNG).</p>
 */
public class QrDTO {
    private String numeroSerie;
    private Integer idProducto;
    private String nombreProducto;
    private String pngBase64;

    public QrDTO(){}

    /**
     * Crea el DTO con todos sus datos.
     *
     * @param numeroSerie    número de serie de la unidad
     * @param idProducto     identificador del producto; puede ser nulo
     * @param nombreProducto nombre del producto; puede ser nulo
     * @param pngBase64      imagen PNG codificada en Base64; puede ser nula
     */
    public QrDTO(String numeroSerie, Integer idProducto, String nombreProducto, String pngBase64) {
        this.numeroSerie = numeroSerie;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.pngBase64 = pngBase64;
    }

    /**
     * Obtiene el número de serie de la unidad.
     *
     * @return número de serie
     */
    public String getNumeroSerie() {
        return numeroSerie;
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
     * @return nombre del producto; puede ser nulo
     */
    public String getNombreProducto() {
        return nombreProducto;
    }

    /**
     * Obtiene la imagen png en base 64
     *
     * @return Imagen png de qr en base 64
     */
    private String getPngBase64() {
        return pngBase64;
    }

    /**
     * Decodifica la imagen PNG recibida en Base64 (RF-34).
     *
     * @return bytes del PNG, o {@code null} si no vino imagen o la codificación es inválida
     */
    public byte[] decodificarPng() throws IllegalArgumentException{
        if (getPngBase64() == null || getPngBase64().isBlank()) return null;
        try{
            return Base64.getDecoder().decode(getPngBase64().replaceAll("\\s",""));
        } catch (IllegalArgumentException e){
            return null;
        }
    }
}
