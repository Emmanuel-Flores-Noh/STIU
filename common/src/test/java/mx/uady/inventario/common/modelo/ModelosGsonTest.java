package mx.uady.inventario.common.modelo;

import mx.uady.inventario.common.json.GsonFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelosGsonTest {

    @Test
    @Tag("RF-01")
    @DisplayName("Test de serialización y deserialización del modelo Producto (RF-01)")
    void roundTripProducto() {
        Producto original = new Producto(1, "Laptop", "Equipo", new BigDecimal("12345.67"));
        Producto copia = GsonFactory.DEFAULT.fromJson(GsonFactory.DEFAULT.toJson(original), Producto.class);
        assertEquals(original, copia);
    }

    @Test
    @Tags({@Tag("RF-06"), @Tag("RF-08"), @Tag("RF-09"), @Tag("RF-21")})
    @DisplayName("Test para la serialización y deserialización del modelo Mercancia y EstadoMercancia")
    void roundTripMercancia() {
        Mercancia original = new Mercancia("SN-001", 1, EstadoMercancia.EN_ALMACEN, "/qr/SN-001.png");
        String json = GsonFactory.DEFAULT.toJson(original);

        // El contrato viaja con el nombre del enum, igual que hacia antes con el String.
        assertTrue(json.contains("\"estado\":\"EN_ALMACEN\""), json);
        assertEquals(original, GsonFactory.DEFAULT.fromJson(json, Mercancia.class));
    }

    @Test
    @Tag("RF-08")
    @DisplayName("Test para la conversión y validación de reglas de EstadoMercancia (RF-08)")
    void convierteEstadoDesdeCadena() {
        assertEquals(EstadoMercancia.EN_ALMACEN, EstadoMercancia.desdeCadena("en_almacen"));
        assertEquals(EstadoMercancia.VENDIDO, EstadoMercancia.desdeCadena(" VENDIDO "));
        assertThrows(IllegalArgumentException.class, () -> EstadoMercancia.desdeCadena("PERDIDO"));
        assertThrows(IllegalArgumentException.class, () -> EstadoMercancia.desdeCadena(" "));
    }

    @Test
    @Tag("RF-03")
    @DisplayName("Test de serialización y deserialización del HistorialPrecio para control de cambios de precios (RF-03)")
    void roundTripHistorialPrecio() {
        HistorialPrecio original = new HistorialPrecio(1, 2, new BigDecimal("99.99"),
                LocalDateTime.of(2026, 1, 2, 3, 4, 5));
        HistorialPrecio copia = GsonFactory.DEFAULT.fromJson(GsonFactory.DEFAULT.toJson(original), HistorialPrecio.class);
        assertEquals(original, copia);
    }

    @Test
    @Tag("RF-17")
    @DisplayName("Test de serialización y deserialización del modelo Venta para registro de transacciones (RF-17)")
    void roundTripVenta() {
        Venta original = new Venta(1, "SN-001", 3, 4, LocalDateTime.of(2026, 5, 6, 7, 8, 9));
        Venta copia = GsonFactory.DEFAULT.fromJson(GsonFactory.DEFAULT.toJson(original), Venta.class);
        assertEquals(original, copia);
    }

    @Test
    @Tags({@Tag("RF-13"), @Tag("RF-20")})
    @DisplayName("Test de serialización y deserialización para modelos Rol, Usuario y TiendaAlmacen para catálogos del sistema (RF-13, RF-20)")
    void roundTripRolUsuarioTienda() {
        Rol rol = new Rol(1, "Admin", "desc");
        Usuario u = new Usuario(2, 1, "Ana", "Paz", "Lopez");
        TiendaAlmacen t = new TiendaAlmacen(3, "Tienda Centro", "Calle 1");
        assertEquals(rol, GsonFactory.DEFAULT.fromJson(GsonFactory.DEFAULT.toJson(rol), Rol.class));
        assertEquals(u, GsonFactory.DEFAULT.fromJson(GsonFactory.DEFAULT.toJson(u), Usuario.class));
        assertEquals(t, GsonFactory.DEFAULT.fromJson(GsonFactory.DEFAULT.toJson(t), TiendaAlmacen.class));
    }
}
