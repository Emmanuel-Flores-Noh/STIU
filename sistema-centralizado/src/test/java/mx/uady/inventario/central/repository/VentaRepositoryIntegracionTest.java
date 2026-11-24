package mx.uady.inventario.central.repository;

import mx.uady.inventario.central.util.ConfiguracionBD;
import mx.uady.inventario.central.util.ConexionBD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifica la transacción contra una MariaDB real cuando el entorno está disponible.
 */
@Tag("mariadb")
@EnabledIfEnvironmentVariable(named = "DB_PASSWORD", matches = ".+",
        disabledReason = "Requiere MariaDB real y las variables DB_* definidas")
class VentaRepositoryIntegracionTest {

    private static final String NUMERO_SERIE = "SN-UADY-0001";
    private static final int ID_TIENDA = 1;
    private static final int ID_USUARIO = 2;
    private static final int HILOS = 10;
    private static final String EXITO = "EXITO";
    private static final String YA_VENDIDA = "La mercancía ya fue vendida";

    @AfterEach
    void restaurarMercancia() throws Exception {
        try (Connection conexion = ConexionBD.abrir(ConfiguracionBD.desdeEntorno());
             PreparedStatement eliminar = conexion.prepareStatement(
                     "DELETE FROM ventas WHERE numero_serie = ?");
             PreparedStatement restaurar = conexion.prepareStatement(
                     "UPDATE mercancia SET estado = 'EN_ALMACEN' WHERE numero_serie = ?")) {
            eliminar.setString(1, NUMERO_SERIE);
            eliminar.executeUpdate();
            restaurar.setString(1, NUMERO_SERIE);
            restaurar.executeUpdate();
        }
    }

    /**
     * RNF-15 y RF-18: diez solicitudes simultáneas para la misma unidad. Solo una se registra; las
     * otras nueve se rechazan con "La mercancía ya fue vendida" (el despachador lo traduce a
     * PARAMETROS_INVALIDOS) y en la base de datos queda exactamente una venta.
     */
    @Test
    @Tag("RF-18")
    @Tag("RNF-15")
    @DisplayName("RF-18, RNF-15 · Solo una de diez ventas concurrentes se registra")
    void soloUnaDeDiezVentasConcurrentesSeRegistra() throws Exception {
        VentaRepository repositorio = new VentaRepository();
        ExecutorService ejecutor = Executors.newFixedThreadPool(HILOS);
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            List<Future<String>> resultados = new ArrayList<>();
            for (int i = 0; i < HILOS; i++) {
                resultados.add(ejecutor.submit(() -> {
                    inicio.await();
                    try {
                        repositorio.registrarVenta(NUMERO_SERIE, ID_TIENDA, ID_USUARIO);
                        return EXITO;
                    } catch (IllegalArgumentException rechazo) {
                        return rechazo.getMessage();
                    }
                }));
            }
            inicio.countDown();

            List<String> obtenidos = new ArrayList<>();
            for (Future<String> resultado : resultados) {
                try {
                    obtenidos.add(resultado.get());
                } catch (ExecutionException error) {
                    throw new AssertionError("Falló una transacción concurrente", error.getCause());
                }
            }
            assertEquals(1, obtenidos.stream().filter(EXITO::equals).count(),
                    "Ventas registradas: " + obtenidos);
            assertEquals(HILOS - 1, obtenidos.stream().filter(YA_VENDIDA::equals).count(),
                    "Rechazos esperados: " + obtenidos);
            assertEquals(1, ventasRegistradas());
        } finally {
            ejecutor.shutdownNow();
        }
    }

    private static int ventasRegistradas() throws Exception {
        try (Connection conexion = ConexionBD.abrir(ConfiguracionBD.desdeEntorno());
             PreparedStatement contar = conexion.prepareStatement(
                     "SELECT COUNT(*) FROM ventas WHERE numero_serie = ?")) {
            contar.setString(1, NUMERO_SERIE);
            try (ResultSet resultado = contar.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
    }
}
