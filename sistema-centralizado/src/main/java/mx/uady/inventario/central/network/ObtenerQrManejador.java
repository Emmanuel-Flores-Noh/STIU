package mx.uady.inventario.central.network;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mx.uady.inventario.central.service.MercanciaService;
import mx.uady.inventario.central.service.NegocioException;
import mx.uady.inventario.central.service.ProductoService;
import mx.uady.inventario.central.service.QrService;
import mx.uady.inventario.common.modelo.Mercancia;

/**
 * Manejador de la operación {@code inventario.obtenerQr} (RF-22).
 *
 * <p>Genera el PNG si la unidad aún no lo tiene y lo devuelve codificado en Base64.</p>
 */
final class ObtenerQrManejador implements ManejadorOperacion {

    private final MercanciaService mercancia;
    private final ProductoService productos;
    private final QrService qr;

    /**
     * Crea el manejador.
     *
     * @param mercancia servicio de unidades físicas
     * @param productos servicio de productos, para el nombre del producto
     * @param qr        servicio que lee el PNG del almacenamiento
     */
    ObtenerQrManejador(MercanciaService mercancia, ProductoService productos, QrService qr) {
        this.mercancia = Objects.requireNonNull(mercancia, "mercancia");
        this.productos = Objects.requireNonNull(productos, "productos");
        this.qr = Objects.requireNonNull(qr, "qr");
    }

    /**
     * Obtiene el QR de una unidad.
     *
     * @param parametros objeto con {@code numero_serie}
     * @return {@code {numeroSerie, idProducto, nombreProducto, pngBase64}}
     * @throws IllegalArgumentException si falta el número de serie o la unidad no existe
     * @throws IllegalStateException    si no se puede leer el PNG
     */
    @Override
    public JsonElement ejecutar(JsonObject parametros) {
        String numeroSerie = ParametrosSolicitud.textoObligatorio(parametros, "numero_serie");
        Mercancia unidad;
        try {
            unidad = mercancia.asegurarQr(numeroSerie);
        } catch (NegocioException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        byte[] png;
        try {
            png = qr.obtenerImagenQRBytes(unidad.rutaQr());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el QR de la unidad " + numeroSerie, e);
        }
        JsonObject resultado = new JsonObject();
        resultado.addProperty("numeroSerie", unidad.numeroSerie());
        resultado.addProperty("idProducto", unidad.idProducto());
        resultado.addProperty("nombreProducto",
                NombresProducto.porId(productos).getOrDefault(unidad.idProducto(), NombresProducto.DESCONOCIDO));
        resultado.addProperty("pngBase64", Base64.getEncoder().encodeToString(png));
        return resultado;
    }
}
