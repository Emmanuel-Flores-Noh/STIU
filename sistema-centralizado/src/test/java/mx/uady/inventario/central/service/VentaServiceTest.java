package mx.uady.inventario.central.service;

import com.google.gson.JsonObject;
import mx.uady.inventario.central.repository.VentaRepositoryException;
import mx.uady.inventario.central.repository.VentaResultado;
import mx.uady.inventario.central.service.memoria.AlmacenMemoria;
import mx.uady.inventario.central.service.memoria.VentasMemoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VentaServiceTest {

    @Test
    @Tag("RF-17")
    @DisplayName("RF-17 · Valida los parámetros y devuelve la confirmación")
    void validaParametrosYDevuelveConfirmacion() throws Exception {
        AtomicReference<String> serieRecibida = new AtomicReference<>();
        VentaService servicio = new VentaService((serie, tienda, usuario) -> {
            serieRecibida.set(serie + ":" + tienda + ":" + usuario);
            return new VentaResultado(serie, tienda, LocalDateTime.parse("2026-11-12T00:00:00"));
        });
        JsonObject parametros = new JsonObject();
        parametros.addProperty("numero_serie", "SN-001");
        parametros.addProperty("id_tienda", 3);
        parametros.addProperty("id_usuario", 12);

        JsonObject resultado = servicio.registrarVenta(parametros);

        assertEquals("SN-001:3:12", serieRecibida.get());
        assertEquals("SN-001", resultado.get("numero_serie").getAsString());
        assertEquals(3, resultado.get("id_tienda").getAsInt());
        assertEquals("2026-11-12T00:00", resultado.get("fecha_venta").getAsString());
    }

    @Test
    @Tag("RF-17")
    @DisplayName("RF-17 · Rechaza parámetros faltantes o negativos")
    void rechazaParametrosFaltantesONegativos() {
        VentaService servicio = new VentaService((serie, tienda, usuario) ->
                new VentaResultado(serie, tienda, LocalDateTime.now()));
        JsonObject parametros = new JsonObject();
        parametros.addProperty("numero_serie", "SN-001");
        parametros.addProperty("id_tienda", 0);
        parametros.addProperty("id_usuario", 12);

        assertThrows(IllegalArgumentException.class, () -> servicio.registrarVenta(parametros));
    }

    @Test
    @Tag("RF-17")
    @Tag("RNF-16")
    @DisplayName("RF-17, RNF-16 · Propaga el fallo del repositorio")
    void propagaFalloDelRepositorio() {
        VentaService servicio = new VentaService((serie, tienda, usuario) -> {
            throw new VentaRepositoryException("fallo de prueba");
        });
        JsonObject parametros = new JsonObject();
        parametros.addProperty("numero_serie", "SN-001");
        parametros.addProperty("id_tienda", 3);
        parametros.addProperty("id_usuario", 12);

        assertThrows(VentaRepositoryException.class, () -> servicio.registrarVenta(parametros));
    }

    // ---- Con el registrador en memoria, que aplica las mismas reglas que MariaDB ----

    private static final String DISPONIBLE = "SN-UADY-0001";
    private static final String YA_VENDIDA = "La mercancía ya fue vendida";

    @Test
    @Tag("RF-17")
    @DisplayName("RF-17 · Vende una unidad En almacén y la confirma")
    void vendeUnaUnidadEnAlmacenYLaConfirma() throws Exception {
        VentaService servicio = conVentasEnMemoria(Duration.ZERO);

        JsonObject confirmacion = servicio.registrarVenta(parametros(DISPONIBLE, 1, 2));

        assertEquals(DISPONIBLE, confirmacion.get("numero_serie").getAsString());
        assertEquals(1, confirmacion.get("id_tienda").getAsInt());
    }

    @Test
    @Tag("RF-18")
    @DisplayName("RF-18 · Rechaza la segunda venta de la misma unidad")
    void rechazaLaSegundaVentaDeLaMismaUnidad() throws Exception {
        AlmacenMemoria almacen = AlmacenMemoria.conDatosDeEjemplo(Duration.ZERO);
        VentasMemoria ventas = new VentasMemoria(almacen);
        VentaService servicio = new VentaService(ventas);
        servicio.registrarVenta(parametros(DISPONIBLE, 1, 2));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarVenta(parametros(DISPONIBLE, 2, 4)));

        assertEquals(YA_VENDIDA, error.getMessage());
        assertEquals(1, ventas.listarHistorial().stream()
                .filter(v -> v.numeroSerie().equals(DISPONIBLE)).count());
    }

    @Test
    @Tag("RF-18")
    @Tag("RNF-15")
    @DisplayName("RF-18, RNF-15 · De diez ventas simultáneas de la misma unidad solo una se registra")
    void deDiezVentasSimultaneasDeLaMismaUnidadSoloUnaSeRegistra() throws Exception {
        AlmacenMemoria almacen = AlmacenMemoria.conDatosDeEjemplo(Duration.ofMillis(20));
        VentasMemoria ventas = new VentasMemoria(almacen);
        VentaService servicio = new VentaService(ventas);
        int hilos = 10;
        ExecutorService ejecutor = Executors.newFixedThreadPool(hilos);
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            List<Future<String>> resultados = new ArrayList<>();
            for (int i = 0; i < hilos; i++) {
                Callable<String> venta = () -> {
                    inicio.await();
                    try {
                        servicio.registrarVenta(parametros(DISPONIBLE, 1, 2));
                        return "EXITO";
                    } catch (IllegalArgumentException rechazo) {
                        return rechazo.getMessage();
                    }
                };
                resultados.add(ejecutor.submit(venta));
            }
            inicio.countDown();

            List<String> obtenidos = new ArrayList<>();
            for (Future<String> resultado : resultados) {
                obtenidos.add(resultado.get(10, TimeUnit.SECONDS));
            }
            assertEquals(1, obtenidos.stream().filter("EXITO"::equals).count(), obtenidos.toString());
            assertEquals(hilos - 1, obtenidos.stream().filter(YA_VENDIDA::equals).count(), obtenidos.toString());
            assertEquals(1, ventas.listarHistorial().stream()
                    .filter(v -> v.numeroSerie().equals(DISPONIBLE)).count());
        } finally {
            ejecutor.shutdownNow();
        }
    }

    @Test
    @Tag("RF-17")
    @DisplayName("RF-17 · El identificador mínimo 1 se acepta y 0 se rechaza")
    void elIdentificadorMinimoUnoSeAceptaYCeroSeRechaza() throws Exception {
        VentaService servicio = conVentasEnMemoria(Duration.ZERO);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarVenta(parametros(DISPONIBLE, 1, 0)));
        assertEquals("El parámetro id_usuario debe ser positivo", error.getMessage());

        assertEquals(DISPONIBLE, servicio.registrarVenta(parametros(DISPONIBLE, 1, 1))
                .get("numero_serie").getAsString());
    }

    @Test
    @Tag("RF-17")
    @DisplayName("RF-17 · Rechaza parámetros nulos, serie faltante o vacía e identificadores no enteros")
    void rechazaParametrosNulosSerieFaltanteOVaciaEIdentificadoresNoEnteros() {
        VentaService servicio = conVentasEnMemoria(Duration.ZERO);

        assertEquals("Los parámetros son obligatorios",
                assertThrows(IllegalArgumentException.class, () -> servicio.registrarVenta(null)).getMessage());

        JsonObject sinSerie = parametros(DISPONIBLE, 1, 2);
        sinSerie.remove("numero_serie");
        assertEquals("Falta el parámetro numero_serie",
                assertThrows(IllegalArgumentException.class, () -> servicio.registrarVenta(sinSerie)).getMessage());

        assertEquals("El parámetro numero_serie no puede estar vacío", assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarVenta(parametros("  ", 1, 2))).getMessage());

        JsonObject usuarioTexto = parametros(DISPONIBLE, 1, 2);
        usuarioTexto.addProperty("id_usuario", "abc");
        assertEquals("El parámetro id_usuario debe ser un entero",
                assertThrows(IllegalArgumentException.class, () -> servicio.registrarVenta(usuarioTexto)).getMessage());
    }

    @Test
    @Tag("RF-17")
    @DisplayName("RF-17 · Rechaza vender una unidad, tienda o usuario inexistentes")
    void rechazaVenderUnaUnidadTiendaOUsuarioInexistentes() {
        VentaService servicio = conVentasEnMemoria(Duration.ZERO);

        assertEquals("La mercancía no existe", assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarVenta(parametros("SN-NO-EXISTE", 1, 2))).getMessage());
        assertEquals("La tienda no existe", assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarVenta(parametros(DISPONIBLE, 99, 2))).getMessage());
        assertEquals("El usuario no existe", assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarVenta(parametros(DISPONIBLE, 1, 99))).getMessage());
    }

    private static VentaService conVentasEnMemoria(Duration latencia) {
        return new VentaService(new VentasMemoria(AlmacenMemoria.conDatosDeEjemplo(latencia)));
    }

    private static JsonObject parametros(String numeroSerie, int idTienda, int idUsuario) {
        JsonObject parametros = new JsonObject();
        parametros.addProperty("numero_serie", numeroSerie);
        parametros.addProperty("id_tienda", idTienda);
        parametros.addProperty("id_usuario", idUsuario);
        return parametros;
    }
}
