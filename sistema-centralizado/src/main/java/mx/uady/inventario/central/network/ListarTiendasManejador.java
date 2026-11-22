package mx.uady.inventario.central.network;

import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mx.uady.inventario.central.service.ConsultasVentaService;
import mx.uady.inventario.common.json.GsonFactory;

/**
 * Manejador de la operación {@code gestorTiendas.listarTiendas} (RF-19).
 *
 * <p>Devuelve el arreglo de tiendas en el orden que entrega el servicio de catálogos.</p>
 */
final class ListarTiendasManejador implements ManejadorOperacion {

    private final ConsultasVentaService consultas;

    /**
     * Crea el manejador.
     *
     * @param consultas servicio de catálogos
     */
    ListarTiendasManejador(ConsultasVentaService consultas) {
        this.consultas = Objects.requireNonNull(consultas, "consultas");
    }

    /**
     * Lista las tiendas. No usa parámetros.
     *
     * @param parametros parámetros de la solicitud (se ignoran)
     * @return arreglo JSON de tiendas
     */
    @Override
    public JsonElement ejecutar(JsonObject parametros) {
        return GsonFactory.DEFAULT.toJsonTree(consultas.listarTiendas());
    }
}
