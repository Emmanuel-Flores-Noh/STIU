package mx.uady.inventario.central.network;

import java.util.ArrayList;
import java.util.List;

import mx.uady.inventario.central.model.VentaHistorial;
import mx.uady.inventario.central.service.ConsultasVentaService;
import mx.uady.inventario.common.modelo.TiendaAlmacen;
import mx.uady.inventario.common.modelo.Usuario;

/**
 * Doble de prueba de {@link ConsultasVentaService} con catálogos fijos, o que falla en cada
 * consulta para simular una base de datos no disponible.
 */
final class ConsultasVentaFalsa implements ConsultasVentaService {

    /** Usuario con rol de administrador, que nunca debe aparecer entre los proveedores. */
    static final Usuario ADMINISTRADOR = new Usuario(1, 1, "Ana", "Pineda", "Solis");

    private final List<TiendaAlmacen> tiendas;
    private final List<Usuario> proveedores;
    private final RuntimeException fallo;

    private ConsultasVentaFalsa(List<TiendaAlmacen> tiendas, List<Usuario> proveedores, RuntimeException fallo) {
        this.tiendas = tiendas;
        this.proveedores = proveedores;
        this.fallo = fallo;
    }

    /**
     * Crea un doble que devuelve los catálogos indicados.
     *
     * @param tiendas     tiendas que devuelve {@code listarTiendas}
     * @param proveedores usuarios que devuelve {@code listarUsuariosProveedores}
     * @return el doble de prueba
     */
    static ConsultasVentaFalsa con(List<TiendaAlmacen> tiendas, List<Usuario> proveedores) {
        return new ConsultasVentaFalsa(List.copyOf(tiendas), List.copyOf(proveedores), null);
    }

    /**
     * Crea un doble que lanza la excepción indicada en cada consulta.
     *
     * @param fallo excepción que se lanza
     * @return el doble de prueba
     */
    static ConsultasVentaFalsa queFalla(RuntimeException fallo) {
        return new ConsultasVentaFalsa(List.of(), List.of(), fallo);
    }

    @Override
    public List<VentaHistorial> listarHistorial() {
        fallarSiCorresponde();
        return List.of();
    }

    @Override
    public List<TiendaAlmacen> listarTiendas() {
        fallarSiCorresponde();
        return tiendas;
    }

    /**
     * Devuelve los proveedores más un administrador, para detectar si un manejador usa esta
     * consulta en lugar de {@link #listarUsuariosProveedores()}.
     */
    @Override
    public List<Usuario> listarUsuarios() {
        fallarSiCorresponde();
        List<Usuario> todos = new ArrayList<>(proveedores);
        todos.add(ADMINISTRADOR);
        return todos;
    }

    @Override
    public List<Usuario> listarUsuariosProveedores() {
        fallarSiCorresponde();
        return proveedores;
    }

    private void fallarSiCorresponde() {
        if (fallo != null) {
            throw fallo;
        }
    }
}
