package mx.uady.inventario.central.network;

import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mx.uady.inventario.central.repository.VentaRepositoryException;
import mx.uady.inventario.central.service.VentaService;

/**
 * Manejador de la operación {@code gestorTiendas.actualizarTiendaVenta} (RF-17).
 *
 * <p>Delega en {@link VentaService}, que valida los parámetros y registra la venta en una
 * transacción.</p>
 */
final class ActualizarTiendaVentaManejador implements ManejadorOperacion {

    private final VentaService ventas;

    /**
     * Crea el manejador.
     *
     * @param ventas servicio que registra las ventas
     */
    ActualizarTiendaVentaManejador(VentaService ventas) {
        this.ventas = Objects.requireNonNull(ventas, "ventas");
    }

    /**
     * Registra la venta.
     *
     * @param parametros objeto con {@code numero_serie}, {@code id_tienda} e {@code id_usuario}
     * @return {@code {numero_serie, id_tienda, fecha_venta}}
     * @throws IllegalArgumentException si los parámetros son inválidos o la unidad ya fue vendida
     * @throws VentaRepositoryException si falla la persistencia
     */
    @Override
    public JsonElement ejecutar(JsonObject parametros) throws VentaRepositoryException {
        return ventas.registrarVenta(parametros);
    }
}
