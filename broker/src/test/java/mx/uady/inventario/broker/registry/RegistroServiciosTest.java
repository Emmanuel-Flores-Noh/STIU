package mx.uady.inventario.broker.registry;

import mx.uady.inventario.broker.model.Proveedor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroServiciosTest {

    @Test
    void registrarYBuscar() {
        RegistroServicios registro = new RegistroServicios();
        registro.registrar(new Proveedor("inventario", "localhost", 6000));

        Optional<Proveedor> encontrado = registro.buscar("inventario");
        assertTrue(encontrado.isPresent());
        assertEquals("localhost", encontrado.get().host());
        assertEquals(6000, encontrado.get().puerto());
    }

    @Test
    void sustituirExistente() {
        RegistroServicios registro = new RegistroServicios();
        registro.registrar(new Proveedor("inventario", "host1", 6000));

        boolean sustituido = registro.sustituir(new Proveedor("inventario", "host2", 6001));
        assertTrue(sustituido);
        assertEquals("host2", registro.buscar("inventario").orElseThrow().host());
        assertEquals(6001, registro.buscar("inventario").orElseThrow().puerto());
    }

    @Test
    void sustituirInexistenteNoRegistra() {
        RegistroServicios registro = new RegistroServicios();

        boolean sustituido = registro.sustituir(new Proveedor("inventario", "host2", 6001));
        assertFalse(sustituido);
        assertTrue(registro.buscar("inventario").isEmpty());
    }

    @Test
    void eliminar() {
        RegistroServicios registro = new RegistroServicios();
        registro.registrar(new Proveedor("inventario", "localhost", 6000));

        assertTrue(registro.eliminar("inventario"));
        assertTrue(registro.buscar("inventario").isEmpty());
        assertFalse(registro.eliminar("inventario"));
    }

    @Test
    void getServiciosEsCopia() {
        RegistroServicios registro = new RegistroServicios();
        registro.registrar(new Proveedor("inventario", "localhost", 6000));

        Map<String, Proveedor> copia = registro.getServicios();
        assertEquals(1, copia.size());
        assertThrows(UnsupportedOperationException.class, () -> copia.put("otro", new Proveedor("otro", "h", 1)));
    }

    @Test
    void buscarServicioVacio() {
        RegistroServicios registro = new RegistroServicios();
        assertTrue(registro.buscar(null).isEmpty());
        assertTrue(registro.buscar("  ").isEmpty());
    }

    @Test
    void concurrenciaBasica() throws InterruptedException {
        RegistroServicios registro = new RegistroServicios();
        int hilos = 8;
        int operaciones = 200;
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        CountDownLatch latch = new CountDownLatch(hilos);
        AtomicInteger errores = new AtomicInteger();

        for (int i = 0; i < hilos; i++) {
            final int idx = i;
            pool.submit(() -> {
                try {
                    for (int j = 0; j < operaciones; j++) {
                        String servicio = "svc-" + (idx * operaciones + j);
                        registro.registrar(new Proveedor(servicio, "localhost", 6000 + (j % 1000)));
                        registro.buscar(servicio);
                    }
                } catch (Exception e) {
                    errores.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        pool.shutdown();
        assertEquals(0, errores.get());
        assertEquals(hilos * operaciones, registro.getServicios().size());
    }

    @Test
    void marcaNoDisponibleAlAlcanzarUmbral() {
        RegistroServicios registro = new RegistroServicios(3, 30_000);
        registro.registrar(new Proveedor("inventario", "localhost", 6000));

        registro.marcarFallo("inventario");
        registro.marcarFallo("inventario");
        assertTrue(registro.estaDisponible("inventario"));
        assertEquals(2, registro.fallos("inventario"));

        registro.marcarFallo("inventario");
        assertFalse(registro.estaDisponible("inventario"));
        assertTrue(registro.buscar("inventario").isPresent());
    }

    @Test
    void exitoLimpiaLosFallos() {
        RegistroServicios registro = new RegistroServicios(2, 30_000);
        registro.registrar(new Proveedor("inventario", "localhost", 6000));

        registro.marcarFallo("inventario");
        registro.marcarExito("inventario");

        assertEquals(0, registro.fallos("inventario"));
        assertTrue(registro.estaDisponible("inventario"));
    }

    @Test
    void elEnfriamientoVenceYReactiva() {
        AtomicLong ahora = new AtomicLong(1_000);
        RegistroServicios registro = new RegistroServicios(2, 5_000, ahora::get);
        registro.registrar(new Proveedor("inventario", "localhost", 6000));

        registro.marcarFallo("inventario");
        registro.marcarFallo("inventario");
        assertFalse(registro.estaDisponible("inventario"));

        // Un segundo antes de cumplirse el enfriamiento sigue fuera.
        ahora.set(5_999);
        assertFalse(registro.estaDisponible("inventario"));

        ahora.set(6_000);
        assertTrue(registro.estaDisponible("inventario"));

        // Tras el enfriamiento el conteo arranca de cero: hace falta otro fallo nuevo.
        registro.marcarFallo("inventario");
        assertTrue(registro.estaDisponible("inventario"));
        assertEquals(1, registro.fallos("inventario"));

        registro.marcarFallo("inventario");
        assertFalse(registro.estaDisponible("inventario"));
    }

    @Test
    void registrarDeNuevoReactiva() {
        RegistroServicios registro = new RegistroServicios(1, 30_000);
        registro.registrar(new Proveedor("inventario", "localhost", 6000));
        registro.marcarFallo("inventario");
        assertFalse(registro.estaDisponible("inventario"));

        registro.registrar(new Proveedor("inventario", "localhost", 6000));

        assertTrue(registro.estaDisponible("inventario"));
        assertEquals(0, registro.fallos("inventario"));
    }

    @Test
    void sustituirReactiva() {
        RegistroServicios registro = new RegistroServicios(1, 30_000);
        registro.registrar(new Proveedor("inventario", "localhost", 6000));
        registro.marcarFallo("inventario");
        assertFalse(registro.estaDisponible("inventario"));

        assertTrue(registro.sustituir(new Proveedor("inventario", "localhost", 6001)));
        assertTrue(registro.estaDisponible("inventario"));
    }

    @Test
    void servicioNoRegistradoNoEstaDisponible() {
        RegistroServicios registro = new RegistroServicios();
        assertFalse(registro.estaDisponible("inventario"));
        assertFalse(registro.estaDisponible(null));
        assertFalse(registro.estaDisponible("  "));
    }

    @Test
    void validaciones() {
        RegistroServicios registro = new RegistroServicios();
        assertThrows(IllegalArgumentException.class, () -> new Proveedor("", "host", 1));
        assertThrows(IllegalArgumentException.class, () -> new Proveedor("svc", "", 1));
        assertThrows(IllegalArgumentException.class, () -> new Proveedor("svc", "host", 0));
        assertThrows(NullPointerException.class, () -> registro.registrar(null));
    }

    @Test
    void validacionesDeResiliencia() {
        assertThrows(IllegalArgumentException.class, () -> new RegistroServicios(0, 100));
        assertThrows(IllegalArgumentException.class, () -> new RegistroServicios(1, -1));
    }

    // ---------------------------------------------------------------------
    // Concurrencia: varios hilos del pool del broker usan el mismo registro.
    // ---------------------------------------------------------------------

    /**
     * Ejecuta la tarea en varios hilos que arrancan a la vez y espera a que terminen.
     * Falla la prueba si algun hilo lanzo una excepcion o no termino a tiempo.
     */
    private static void enParalelo(int hilos, IntConsumer tarea) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        CountDownLatch salida = new CountDownLatch(1);
        List<Throwable> errores = new CopyOnWriteArrayList<>();
        try {
            for (int i = 0; i < hilos; i++) {
                final int hilo = i;
                pool.submit(() -> {
                    try {
                        salida.await();
                        tarea.accept(hilo);
                    } catch (Throwable e) {
                        errores.add(e);
                    }
                });
            }
            salida.countDown();
        } finally {
            pool.shutdown();
        }
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS), "Los hilos no terminaron a tiempo");
        assertTrue(errores.isEmpty(), "Errores en los hilos: " + errores);
    }

    @Test
    @Tag("RF-44")
    @Tag("RF-54")
    @DisplayName("RF-44, RF-54 · Registrar a la vez el mismo servicio deja exactamente uno de los proveedores enviados")
    void registrarALaVezElMismoServicioDejaExactamenteUnoDeLosProveedoresEnviados() throws InterruptedException {
        RegistroServicios registro = new RegistroServicios();
        int hilos = 16;

        enParalelo(hilos, hilo -> registro.registrar(new Proveedor("inventario", "localhost", 7000 + hilo)));

        assertEquals(1, registro.getServicios().size());
        int puerto = registro.buscar("inventario").orElseThrow().puerto();
        assertTrue(puerto >= 7000 && puerto < 7000 + hilos, "Puerto inesperado: " + puerto);
    }

    @Test
    @Tag("RF-41")
    @Tag("RF-42")
    @Tag("RF-43")
    @Tag("RF-54")
    @DisplayName("RF-41, RF-42, RF-43, RF-54 · Registrar, sustituir y eliminar a la vez el mismo servicio deja el registro coherente")
    void registrarSustituirYEliminarALaVezElMismoServicioDejaElRegistroCoherente() throws InterruptedException {
        RegistroServicios registro = new RegistroServicios(3, 30_000);
        int hilos = 12;
        int repeticiones = 500;

        enParalelo(hilos, hilo -> {
            for (int i = 0; i < repeticiones; i++) {
                Proveedor proveedor = new Proveedor("inventario", "host-" + hilo, 7000 + i);
                switch (hilo % 4) {
                    case 0 -> registro.registrar(proveedor);
                    case 1 -> registro.sustituir(proveedor);
                    case 2 -> registro.eliminar("inventario");
                    default -> registro.marcarFallo("inventario");
                }
                registro.buscar("inventario");
                registro.estaDisponible("inventario");
            }
        });

        // Si quedo registrado, es uno de los proveedores enviados por los hilos que registran o sustituyen.
        registro.buscar("inventario").ifPresent(proveedor -> {
            int hiloOrigen = Integer.parseInt(proveedor.host().substring("host-".length()));
            assertTrue(hiloOrigen % 4 == 0 || hiloOrigen % 4 == 1, "Proveedor inesperado: " + proveedor);
        });
        // Un registro nuevo siempre deja al servicio disponible y sin fallos, sin importar lo anterior.
        registro.registrar(new Proveedor("inventario", "localhost", 9000));
        assertTrue(registro.estaDisponible("inventario"));
        assertEquals(0, registro.fallos("inventario"));
        assertEquals(1, registro.getServicios().size());
    }

    @Test
    @Tag("RF-52")
    @Tag("RF-54")
    @DisplayName("RF-52, RF-54 · Los fallos marcados a la vez se cuentan todos")
    void losFallosMarcadosALaVezSeCuentanTodos() throws InterruptedException {
        // Umbral alto para que el conteo no se reinicie por un enfriamiento.
        RegistroServicios registro = new RegistroServicios(1_000_000, 30_000, () -> 1_000L);
        registro.registrar(new Proveedor("inventario", "localhost", 6000));
        int hilos = 8;
        int fallosPorHilo = 250;

        enParalelo(hilos, hilo -> {
            for (int i = 0; i < fallosPorHilo; i++) {
                registro.marcarFallo("inventario");
            }
        });

        assertEquals(hilos * fallosPorHilo, registro.fallos("inventario"));
        assertTrue(registro.estaDisponible("inventario"));
    }

    @Test
    @Tag("RF-52")
    @Tag("RF-54")
    @DisplayName("RF-52, RF-54 · Con fallos simultáneos el proveedor queda no disponible justo al alcanzar el umbral")
    void conFallosSimultaneosElProveedorQuedaNoDisponibleJustoAlAlcanzarElUmbral() throws InterruptedException {
        int hilos = 8;
        int fallosPorHilo = 25;
        int fallosTotales = hilos * fallosPorHilo;
        RegistroServicios enElUmbral = new RegistroServicios(fallosTotales, 30_000, () -> 1_000L);
        RegistroServicios unoAntesDelUmbral = new RegistroServicios(fallosTotales + 1, 30_000, () -> 1_000L);
        enElUmbral.registrar(new Proveedor("inventario", "localhost", 6000));
        unoAntesDelUmbral.registrar(new Proveedor("inventario", "localhost", 6000));

        enParalelo(hilos, hilo -> {
            for (int i = 0; i < fallosPorHilo; i++) {
                enElUmbral.marcarFallo("inventario");
                unoAntesDelUmbral.marcarFallo("inventario");
            }
        });

        assertFalse(enElUmbral.estaDisponible("inventario"));
        assertTrue(unoAntesDelUmbral.estaDisponible("inventario"));
        assertEquals(fallosTotales, unoAntesDelUmbral.fallos("inventario"));
    }

    @Test
    @Tag("RF-44")
    @Tag("RF-54")
    @DisplayName("RF-44, RF-54 · Leer la tabla de servicios mientras otros hilos registran no falla")
    void leerLaTablaDeServiciosMientrasOtrosHilosRegistranNoFalla() throws InterruptedException {
        RegistroServicios registro = new RegistroServicios();
        int hilos = 8;
        int registrosPorHilo = 200;

        enParalelo(hilos, hilo -> {
            for (int i = 0; i < registrosPorHilo; i++) {
                if (hilo % 2 == 0) {
                    registro.registrar(new Proveedor("svc-" + hilo + "-" + i, "localhost", 6000));
                } else {
                    // La copia debe poder recorrerse aunque el registro cambie al mismo tiempo.
                    registro.getServicios().forEach((servicio, proveedor) ->
                            assertEquals(servicio, proveedor.servicio()));
                }
            }
        });

        assertEquals((hilos / 2) * registrosPorHilo, registro.getServicios().size());
    }

    @Test
    @Tag("RF-52")
    @DisplayName("RF-52 · Con enfriamiento de cero milisegundos el proveedor nunca queda fuera")
    void conEnfriamientoDeCeroMilisegundosElProveedorNuncaQuedaFuera() {
        RegistroServicios registro = new RegistroServicios(1, 0, () -> 1_000L);
        registro.registrar(new Proveedor("inventario", "localhost", 6000));

        registro.marcarFallo("inventario");

        assertTrue(registro.estaDisponible("inventario"));
        assertEquals(1, registro.fallos("inventario"));
    }

    @Test
    @Tag("RF-52")
    @DisplayName("RF-52 · Con umbral de un fallo basta un fallo para marcar al proveedor")
    void conUmbralDeUnFalloBastaUnFalloParaMarcarAlProveedor() {
        RegistroServicios registro = new RegistroServicios(1, 30_000, () -> 1_000L);
        registro.registrar(new Proveedor("inventario", "localhost", 6000));

        registro.marcarFallo("inventario");

        assertFalse(registro.estaDisponible("inventario"));
        assertTrue(registro.buscar("inventario").isPresent(), "Marcarlo no disponible no lo elimina del registro");
    }
}
