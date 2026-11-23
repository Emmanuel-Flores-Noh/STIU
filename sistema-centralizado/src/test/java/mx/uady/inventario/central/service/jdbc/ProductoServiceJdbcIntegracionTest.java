package mx.uady.inventario.central.service.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import mx.uady.inventario.central.repository.HistorialPrecioRepository;
import mx.uady.inventario.central.repository.ProductoRepository;
import mx.uady.inventario.central.service.AccesoDatosException;
import mx.uady.inventario.central.service.NegocioException;
import mx.uady.inventario.common.modelo.Producto;

/** Requiere MariaDB con el esquema y las variables {@code DB_*}; no modifica los datos del seed. */
@Tag("mariadb")
@EnabledIfEnvironmentVariable(named = "DB_PASSWORD", matches = ".+",
        disabledReason = "Requiere MariaDB real y las variables DB_* definidas")
class ProductoServiceJdbcIntegracionTest {

    private static final String HISTORIAL = "SELECT COUNT(*) FROM historial_precio WHERE id_producto = ?";

    private final BaseDatosDePrueba base = new BaseDatosDePrueba();
    private final ProductoServiceJdbc servicio = new ProductoServiceJdbc(base.conexiones);

    @AfterEach
    void limpiar() throws SQLException {
        base.close();
    }

    @Test
    @Tag("RF-01")
    @DisplayName("RF-01 · Crea un producto normalizado que aparece con cero unidades")
    void creaUnProductoNormalizadoQueApareceConCeroUnidades() {
        Producto nuevo = servicio.guardar(new Producto(0, "  " + base.prefijo + " Monitor  ", "  ",
                new BigDecimal("3200.50")));
        base.recordar(nuevo.idProducto());

        assertTrue(nuevo.idProducto() > 0);
        assertEquals(base.prefijo + " Monitor", nuevo.nombre());
        assertNull(nuevo.descripcion());
        assertTrue(servicio.listarConUnidades().stream()
                .anyMatch(f -> f.producto().idProducto() == nuevo.idProducto() && f.unidades() == 0));
    }

    @Test
    @Tag("RF-03")
    @DisplayName("RF-03 · Cambiar el precio guarda el anterior en el historial")
    void cambiarElPrecioGuardaElAnteriorEnElHistorial() throws SQLException {
        int id = base.crearProducto("Silla", "3499.00");

        servicio.guardar(new Producto(id, base.prefijo + " Silla", null, new BigDecimal("3999.00")));

        assertEquals(1, base.contar(HISTORIAL, id));
        assertEquals(0, new BigDecimal("3999.00").compareTo(base.precioEnBase(id)));
        try (Connection c = base.conexiones.abrir()) {
            assertEquals(0, new BigDecimal("3499.00")
                    .compareTo(new HistorialPrecioRepository().listar(c, id).get(0).precioHistorico()));
        }
    }

    @Test
    @Tag("RF-03")
    @DisplayName("RF-03 · Sin cambio de precio no agrega historial")
    void sinCambioDePrecioNoAgregaHistorial() throws SQLException {
        int id = base.crearProducto("Mesa", "100.00");

        servicio.guardar(new Producto(id, base.prefijo + " Mesa renombrada", null, new BigDecimal("100")));

        assertEquals(0, base.contar(HISTORIAL, id));
    }

    @Test
    @Tag("RF-03")
    @Tag("RNF-16")
    @DisplayName("RF-03, RNF-16 · Si falla la actualización tampoco queda el historial")
    void siFallaLaActualizacionTampocoQuedaElHistorial() throws SQLException {
        int id = base.crearProducto("Lampara", "50.00");
        ProductoRepository queFalla = new ProductoRepository() {
            @Override
            public boolean actualizar(Connection conexion, Producto producto) throws SQLException {
                throw new SQLException("fallo simulado después de insertar el historial");
            }
        };
        ProductoServiceJdbc conFallo = new ProductoServiceJdbc(base.conexiones, queFalla,
                new HistorialPrecioRepository());

        assertThrows(AccesoDatosException.class,
                () -> conFallo.guardar(new Producto(id, base.prefijo + " Lampara", null, new BigDecimal("75"))));

        assertEquals(0, base.contar(HISTORIAL, id), "El historial debió deshacerse con el rollback");
        assertEquals(0, new BigDecimal("50.00").compareTo(base.precioEnBase(id)));
    }

    @Test
    @Tag("RF-02")
    @DisplayName("RF-02 · Editar un producto inexistente es error de negocio")
    void editarUnProductoInexistenteEsErrorDeNegocio() {
        assertThrows(NegocioException.class,
                () -> servicio.guardar(new Producto(Integer.MAX_VALUE, "Fantasma", null, BigDecimal.ONE)));
    }
}
