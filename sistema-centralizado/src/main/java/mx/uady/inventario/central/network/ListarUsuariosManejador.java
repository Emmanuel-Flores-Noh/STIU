package mx.uady.inventario.central.network;

import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mx.uady.inventario.central.service.ConsultasVentaService;
import mx.uady.inventario.common.json.GsonFactory;

/**
 * Manejador de la operación {@code gestorTiendas.listarUsuarios} (RF-20).
 *
 * <p>Devuelve solo los usuarios con rol Proveedor, que son los encargados de ventas del Sistema 2.</p>
 */
final class ListarUsuariosManejador implements ManejadorOperacion {

    private final ConsultasVentaService consultas;

    /**
     * Crea el manejador.
     *
     * @param consultas servicio de catálogos
     */
    ListarUsuariosManejador(ConsultasVentaService consultas) {
        this.consultas = Objects.requireNonNull(consultas, "consultas");
    }

    /**
     * Lista los usuarios proveedores. No usa parámetros.
     *
     * @param parametros parámetros de la solicitud (se ignoran)
     * @return arreglo JSON de usuarios proveedores
     */
    @Override
    public JsonElement ejecutar(JsonObject parametros) {
        return GsonFactory.DEFAULT.toJsonTree(consultas.listarUsuariosProveedores());
    }
}
