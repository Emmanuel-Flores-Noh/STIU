package mx.uady.inventario.cliente.network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mx.uady.inventario.cliente.model.MercanciaDTO;
import mx.uady.inventario.cliente.model.PrecioHistorialDTO;
import mx.uady.inventario.cliente.model.QrDTO;
import mx.uady.inventario.cliente.model.TiendaDTO;
import mx.uady.inventario.cliente.model.UsuarioDTO;
import mx.uady.inventario.common.contrato.Respuesta;
import mx.uady.inventario.common.contrato.Solicitud;
import mx.uady.inventario.common.json.GsonFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de pruebas unitarias para {@link VentaService}.
 * Valida la invocación de operaciones remotas del contrato y la deserialización a DTOs
 * para tiendas (RF-28), usuarios (RF-29), inventario (RF-26), carga concurrente (RF-25),
 * registro de ventas (RF-32), QR (RF-34) e historial de precios (RF-36).
 */
class VentaServiceTest {

    private final Gson gson = GsonFactory.DEFAULT;

    @Test
    @Tag("RF-28")
    @DisplayName("RF-28 · Caso Normal: listarTiendas deserializa la lista de tiendas en el orden recibido")
    void listarTiendas_CasoNormal_DeserializaLista() throws Exception {
        JsonArray tiendasArray = new JsonArray();
        JsonObject t1 = new JsonObject();
        t1.addProperty("idTienda", 1L);
        t1.addProperty("nombre", "Tienda Central UADY");
        t1.addProperty("direccion", "Calle 60");
        tiendasArray.add(t1);

        ConexionTCP conexion = new ConexionTCP("localhost", 5000, 22000) {
            @Override
            public String intercambiar(String linea) {
                Solicitud sol = gson.fromJson(linea, Solicitud.class);
                return gson.toJson(Respuesta.exito(sol.getIdSolicitud(), tiendasArray));
            }
        };

        VentaService service = new VentaService(new ClienteTCP(conexion));
        List<TiendaDTO> tiendas = service.listarTiendas();

        assertNotNull(tiendas);
        assertEquals(1, tiendas.size());
        assertEquals("Tienda Central UADY", tiendas.get(0).getNombre());
    }

    @Test
    @Tag("RF-29")
    @DisplayName("RF-29 · Caso Normal: listarUsuarios deserializa la lista de usuarios proveedores")
    void listarUsuarios_CasoNormal_DeserializaLista() throws Exception {
        JsonArray usuariosArray = new JsonArray();
        JsonObject u1 = new JsonObject();
        u1.addProperty("idUsuario", 10L);
        u1.addProperty("nombres", "Luis");
        u1.addProperty("apellidoPaterno", "González");
        usuariosArray.add(u1);

        ConexionTCP conexion = new ConexionTCP("localhost", 5000, 22000) {
            @Override
            public String intercambiar(String linea) {
                Solicitud sol = gson.fromJson(linea, Solicitud.class);
                return gson.toJson(Respuesta.exito(sol.getIdSolicitud(), usuariosArray));
            }
        };

        VentaService service = new VentaService(new ClienteTCP(conexion));
        List<UsuarioDTO> usuarios = service.listarUsuarios();

        assertNotNull(usuarios);
        assertEquals(1, usuarios.size());
        assertEquals("Luis", usuarios.get(0).getNombres());
        assertEquals("González", usuarios.get(0).getApellidoPaterno());
    }

    @Test
    @Tag("RF-26")
    @DisplayName("RF-26 · Caso Normal: listarInventarioDisponible retorna las unidades disponibles")
    void listarInventarioDisponible_CasoNormal() throws Exception {
        JsonArray invArray = new JsonArray();
        JsonObject m1 = new JsonObject();
        m1.addProperty("numeroSerie", "SN-999");
        m1.addProperty("idProducto", 5);
        m1.addProperty("nombreProducto", "Tablet");
        m1.addProperty("estado", "EN_ALMACEN");
        invArray.add(m1);

        ConexionTCP conexion = new ConexionTCP("localhost", 5000, 22000) {
            @Override
            public String intercambiar(String linea) {
                Solicitud sol = gson.fromJson(linea, Solicitud.class);
                return gson.toJson(Respuesta.exito(sol.getIdSolicitud(), invArray));
            }
        };

        VentaService service = new VentaService(new ClienteTCP(conexion));
        List<MercanciaDTO> mercancia = service.listarInventarioDisponible();

        assertNotNull(mercancia);
        assertEquals(1, mercancia.size());
        assertEquals("SN-999", mercancia.get(0).getNumeroSerie());
        assertEquals("EN_ALMACEN", mercancia.get(0).getEstado());
    }

    @Test
    @Tag("RF-25")
    @DisplayName("RF-25 · Caso Normal: cargarCatalogos consulta concurrentemente tiendas, usuarios e inventario")
    void cargarCatalogos_CasoNormal_CargaConcurrente() throws Exception {
        ConexionTCP conexion = new ConexionTCP("localhost", 5000, 22000) {
            @Override
            public String intercambiar(String linea) {
                Solicitud sol = gson.fromJson(linea, Solicitud.class);
                return gson.toJson(Respuesta.exito(sol.getIdSolicitud(), new JsonArray()));
            }
        };

        VentaService service = new VentaService(new ClienteTCP(conexion));
        VentaService.Catalogos catalogos = service.cargarCatalogos();

        assertNotNull(catalogos);
        assertNotNull(catalogos.tiendas());
        assertNotNull(catalogos.usuarios());
        assertNotNull(catalogos.mercancia());
    }

    @Test
    @Tag("RF-32")
    @DisplayName("RF-32 · Caso Normal: registrarVenta envía los parámetros requeridos y extrae la fecha de venta")
    void registrarVenta_CasoNormal_RetornaFecha() throws Exception {
        ConexionTCP conexion = new ConexionTCP("localhost", 5000, 22000) {
            @Override
            public String intercambiar(String linea) {
                Solicitud sol = gson.fromJson(linea, Solicitud.class);
                assertEquals("SN-001", sol.getParametros().get("numero_serie").getAsString());
                assertEquals(1L, sol.getParametros().get("id_tienda").getAsLong());
                assertEquals(2L, sol.getParametros().get("id_usuario").getAsLong());

                JsonObject res = new JsonObject();
                res.addProperty("numero_serie", "SN-001");
                res.addProperty("id_tienda", 1L);
                res.addProperty("fecha_venta", "2026-11-22T19:00:00");
                return gson.toJson(Respuesta.exito(sol.getIdSolicitud(), res));
            }
        };

        VentaService service = new VentaService(new ClienteTCP(conexion));
        String fechaVenta = service.registrarVenta("SN-001", 1L, 2L);

        assertEquals("2026-11-22T19:00:00", fechaVenta);
    }

    @Test
    @Tag("RF-34")
    @DisplayName("RF-34 · Caso Normal: obtenerQr retorna el DTO del código QR con imagen en Base64")
    void obtenerQr_CasoNormal_RetornaQrDTO() throws Exception {
        JsonObject qrObj = new JsonObject();
        qrObj.addProperty("numeroSerie", "SN-001");
        qrObj.addProperty("idProducto", 1);
        qrObj.addProperty("nombreProducto", "Laptop");
        qrObj.addProperty("pngBase64", "iVBORw0KGgoAAAANSUhEUg==");

        ConexionTCP conexion = new ConexionTCP("localhost", 5000, 22000) {
            @Override
            public String intercambiar(String linea) {
                Solicitud sol = gson.fromJson(linea, Solicitud.class);
                return gson.toJson(Respuesta.exito(sol.getIdSolicitud(), qrObj));
            }
        };

        VentaService service = new VentaService(new ClienteTCP(conexion));
        QrDTO qr = service.obtenerQr("SN-001");

        assertNotNull(qr);
        assertEquals("SN-001", qr.getNumeroSerie());
        assertEquals("iVBORw0KGgoAAAANSUhEUg==", qr.getPngBase64());
    }

    @Test
    @Tag("RF-36")
    @DisplayName("RF-36 · Caso Normal: obtenerHistorialPrecios retorna nombre, precio actual e historial")
    void obtenerHistorialPrecios_CasoNormal_RetornaHistorialDTO() throws Exception {
        JsonObject histObj = new JsonObject();
        histObj.addProperty("idProducto", 5);
        histObj.addProperty("nombre", "Impresora");
        histObj.addProperty("precioActual", 4500.00);
        histObj.add("historial", new JsonArray());

        ConexionTCP conexion = new ConexionTCP("localhost", 5000, 22000) {
            @Override
            public String intercambiar(String linea) {
                Solicitud sol = gson.fromJson(linea, Solicitud.class);
                return gson.toJson(Respuesta.exito(sol.getIdSolicitud(), histObj));
            }
        };

        VentaService service = new VentaService(new ClienteTCP(conexion));
        PrecioHistorialDTO historial = service.obtenerHistorialPrecios(5);

        assertNotNull(historial);
        assertEquals("Impresora", historial.getNombre());
        assertTrue(historial.getHistorialPrecios().isEmpty());
    }
}
