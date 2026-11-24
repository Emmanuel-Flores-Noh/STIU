package mx.uady.inventario.central.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mx.uady.inventario.common.contrato.EstadoRespuesta;
import mx.uady.inventario.common.contrato.Respuesta;
import mx.uady.inventario.common.contrato.Solicitud;
import mx.uady.inventario.common.error.CodigoError;
import mx.uady.inventario.common.servicio.Operaciones;
import mx.uady.inventario.common.servicio.Servicios;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DespachadorSolicitudesTest {

    private static Solicitud solicitud(String id, String servicio, String operacion, JsonObject parametros) {
        return Solicitud.crear(id, servicio, operacion, parametros);
    }

    @Test
    @Tag("RF-16")
    @Tag("RF-24")
    @DisplayName("RF-16, RF-24 · El catálogo estándar reconoce los servicios y operaciones del contrato")
    void catalogoEstandarReconoceLosServiciosYOperacionesDelContrato() {
        DespachadorSolicitudes despachador = DespachadorSolicitudes.estandar();

        assertTrue(despachador.conoceServicio(Servicios.GESTOR_TIENDAS));
        assertTrue(despachador.conoceServicio(Servicios.INVENTARIO));
        assertTrue(despachador.conoceServicio(Servicios.GESTOR_PRECIOS));
        assertTrue(despachador.conoceOperacion(Servicios.GESTOR_TIENDAS, Operaciones.LISTAR_TIENDAS));
        assertTrue(despachador.conoceOperacion(Servicios.GESTOR_TIENDAS, Operaciones.ACTUALIZAR_TIENDA_VENTA));
        assertTrue(despachador.conoceOperacion(Servicios.INVENTARIO, Operaciones.LISTAR_INVENTARIO));
        assertTrue(despachador.conoceOperacion(Servicios.GESTOR_PRECIOS, Operaciones.LISTAR_PRECIOS_PRODUCTO));
        assertFalse(despachador.conoceServicio(Servicios.BROKER));
    }

    @Test
    @Tag("RF-24")
    @DisplayName("RF-24 · Una operación sin manejador responde OPERACION_NO_SOPORTADA conservando el id")
    void operacionSinManejadorRespondeNoSoportadaConservandoElId() {
        Respuesta respuesta = DespachadorSolicitudes.estandar()
                .despachar(solicitud("req-1", Servicios.INVENTARIO, Operaciones.LISTAR_INVENTARIO, null));

        assertEquals("req-1", respuesta.getIdSolicitud());
        assertEquals(EstadoRespuesta.ERROR, respuesta.getEstado());
        assertEquals(CodigoError.OPERACION_NO_SOPORTADA, respuesta.getCodigoError());
        assertTrue(respuesta.getMensajeError().contains(Operaciones.LISTAR_INVENTARIO));
    }

    @Test
    @Tag("RF-24")
    @DisplayName("RF-24 · Un servicio desconocido devuelve el error del catálogo")
    void servicioDesconocidoDevuelveErrorDelCatalogo() {
        Respuesta respuesta = DespachadorSolicitudes.estandar()
                .despachar(solicitud("req-2", "servicioFantasma", Operaciones.LISTAR_INVENTARIO, null));

        assertEquals("req-2", respuesta.getIdSolicitud());
        assertEquals(CodigoError.SERVICIO_DESCONOCIDO, respuesta.getCodigoError());
    }

    @Test
    @Tag("RF-24")
    @DisplayName("RF-24 · Una operación desconocida devuelve el error del catálogo")
    void operacionDesconocidaDevuelveErrorDelCatalogo() {
        Respuesta respuesta = DespachadorSolicitudes.estandar()
                .despachar(solicitud("req-3", Servicios.INVENTARIO, "operacionFantasma", null));

        assertEquals("req-3", respuesta.getIdSolicitud());
        assertEquals(CodigoError.OPERACION_NO_SOPORTADA, respuesta.getCodigoError());
    }

    @Test
    @Tag("RF-24")
    @DisplayName("RF-24 · Ejecuta el manejador registrado y devuelve éxito")
    void ejecutaElManejadorRegistradoYDevuelveExito() {
        DespachadorSolicitudes despachador = new DespachadorSolicitudes();
        despachador.registrarOperacion(Servicios.INVENTARIO, Operaciones.LISTAR_INVENTARIO,
                parametros -> new JsonPrimitive("resultado-" + parametros.get("filtro").getAsString()));

        JsonObject parametros = new JsonObject();
        parametros.addProperty("filtro", "uady");
        Respuesta respuesta = despachador.despachar(
                solicitud("req-4", Servicios.INVENTARIO, Operaciones.LISTAR_INVENTARIO, parametros));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        assertEquals("resultado-uady", respuesta.getResultado().getAsString());
        assertEquals("req-4", respuesta.getIdSolicitud());
    }

    @Test
    @Tag("RF-24")
    @DisplayName("RF-24 · Los parámetros nulos llegan al manejador como objeto vacío")
    void parametrosNulosLleganAlManejadorComoObjetoVacio() {
        AtomicReference<JsonObject> recibidos = new AtomicReference<>();
        DespachadorSolicitudes despachador = new DespachadorSolicitudes();
        despachador.registrarOperacion(Servicios.INVENTARIO, Operaciones.LISTAR_INVENTARIO, parametros -> {
            recibidos.set(parametros);
            return new JsonPrimitive("ok");
        });

        Respuesta respuesta = despachador.despachar(
                solicitud("req-5", Servicios.INVENTARIO, Operaciones.LISTAR_INVENTARIO, null));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        assertNotNull(recibidos.get());
        assertTrue(recibidos.get().isEmpty());
    }

    @Test
    @Tag("RF-24")
    @DisplayName("RF-24 · Una excepción del manejador se convierte en ERROR_INTERNO")
    void excepcionDelManejadorSeConvierteEnErrorInterno() {
        DespachadorSolicitudes despachador = new DespachadorSolicitudes();
        despachador.registrarOperacion(Servicios.INVENTARIO, Operaciones.LISTAR_INVENTARIO,
                parametros -> {
                    throw new Exception("fallo de base de datos");
                });

        Respuesta respuesta = despachador.despachar(
                solicitud("req-7", Servicios.INVENTARIO, Operaciones.LISTAR_INVENTARIO, null));

        assertEquals("req-7", respuesta.getIdSolicitud());
        assertEquals(CodigoError.ERROR_INTERNO, respuesta.getCodigoError());
        assertTrue(respuesta.getMensajeError().contains("fallo de base de datos"));
    }

    @Test
    @Tag("RF-24")
    @DisplayName("RF-24 · IllegalArgumentException se convierte en PARAMETROS_INVALIDOS")
    void illegalArgumentSeConvierteEnParametrosInvalidos() {
        DespachadorSolicitudes despachador = new DespachadorSolicitudes();
        despachador.registrarOperacion(Servicios.GESTOR_TIENDAS, Operaciones.ACTUALIZAR_TIENDA_VENTA,
                parametros -> {
                    throw new IllegalArgumentException("numero_serie es obligatorio");
                });

        Respuesta respuesta = despachador.despachar(
                solicitud("req-8", Servicios.GESTOR_TIENDAS, Operaciones.ACTUALIZAR_TIENDA_VENTA, null));

        assertEquals("req-8", respuesta.getIdSolicitud());
        assertEquals(CodigoError.PARAMETROS_INVALIDOS, respuesta.getCodigoError());
        assertTrue(respuesta.getMensajeError().contains("numero_serie"));
    }

    @Test
    @Tag("RF-24")
    @DisplayName("RF-24 · Una solicitud nula devuelve error")
    void solicitudNulaDevuelveError() {
        Respuesta respuesta = DespachadorSolicitudes.estandar().despachar(null);

        assertEquals(CodigoError.PARAMETROS_INVALIDOS, respuesta.getCodigoError());
    }
}
