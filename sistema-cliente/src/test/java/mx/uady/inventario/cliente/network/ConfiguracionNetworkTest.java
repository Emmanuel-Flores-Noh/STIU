package mx.uady.inventario.cliente.network;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de pruebas unitarias para la clase {@link ConfiguracionNetwork}.
 * Valida la correcta lectura de los parámetros de red TCP y la aplicación de restricciones
 * ante valores de configuración inválidos o tiempos de espera inferiores al recomendado.
 */
class ConfiguracionNetworkTest {

    @Test
    @Tag("RF-39")
    @Tag("RF-40")
    @DisplayName("RF-39 / RF-40 · Caso Normal: Usa valores por defecto requeridos cuando no hay variables de entorno")
    void usaValoresPorDefectoCuandoNoHayVariables() {
        ConfiguracionNetwork configuracion = ConfiguracionNetwork.desdeMapa(Map.of());

        assertEquals("localhost", configuracion.brokerHost());
        assertEquals(5000, configuracion.brokerPuerto());
        assertEquals(22000, configuracion.timeoutMs(), "El timeout por defecto debe ser 22,000 ms (RF-40)");
        assertFalse(configuracion.timeoutInferiorAlRecomendado(), "22,000 ms no es inferior al recomendado");
    }

    @Test
    @Tag("RF-39")
    @Tag("RF-40")
    @DisplayName("RF-39 · Caso Normal: Lee todas las variables de red desde mapa de configuración")
    void leeTodasLasVariablesDeRed() {
        ConfiguracionNetwork configuracion = ConfiguracionNetwork.desdeMapa(Map.of(
                "BROKER_HOST", "broker-cluster",
                "BROKER_PORT", "5150",
                "CLIENT_TIMEOUT_MS", "25000"));

        assertEquals("broker-cluster", configuracion.brokerHost());
        assertEquals(5150, configuracion.brokerPuerto());
        assertEquals(25000, configuracion.timeoutMs());
        assertFalse(configuracion.timeoutInferiorAlRecomendado());
    }

    @Test
    @Tag("RF-40")
    @DisplayName("RF-40 · Caso Límite: Verificación de límite inferior recomendado (22,000 ms vs 21,999 ms)")
    void verificaLimiteInferiorRecomendadoTimeout() {
        ConfiguracionNetwork exacta = ConfiguracionNetwork.desdeMapa(Map.of("CLIENT_TIMEOUT_MS", "22000"));
        assertFalse(exacta.timeoutInferiorAlRecomendado(), "22,000 ms cumple con el timeout mínimo del Broker");

        ConfiguracionNetwork inferior = ConfiguracionNetwork.desdeMapa(Map.of("CLIENT_TIMEOUT_MS", "21999"));
        assertTrue(inferior.timeoutInferiorAlRecomendado(), "Menor a 22,000 ms activa la advertencia de timeout inferior");
    }

    @Test
    @Tag("RF-39")
    @DisplayName("RF-39 · Caso Error: Rechaza puertos fuera de rango (1-65535) y valores no numéricos")
    void rechazaPuertosInvalidos() {
        assertThrows(IllegalArgumentException.class,
                () -> ConfiguracionNetwork.desdeMapa(Map.of("BROKER_PORT", "70000")));
        assertThrows(IllegalArgumentException.class,
                () -> ConfiguracionNetwork.desdeMapa(Map.of("BROKER_PORT", "0")));
        assertThrows(IllegalArgumentException.class,
                () -> ConfiguracionNetwork.desdeMapa(Map.of("BROKER_PORT", "no-es-puerto")));
    }

    @Test
    @Tag("RF-40")
    @DisplayName("RF-40 · Caso Error: Rechaza CLIENT_TIMEOUT_MS menor o igual a cero o no numérico")
    void rechazaTimeoutMenorOIgualACeroONoNumerico() {
        IllegalArgumentException errNegativo = assertThrows(IllegalArgumentException.class,
                () -> ConfiguracionNetwork.desdeMapa(Map.of("CLIENT_TIMEOUT_MS", "-500")));
        assertTrue(errNegativo.getMessage().contains("CLIENT_TIMEOUT_MS"));

        IllegalArgumentException errCero = assertThrows(IllegalArgumentException.class,
                () -> ConfiguracionNetwork.desdeMapa(Map.of("CLIENT_TIMEOUT_MS", "0")));
        assertTrue(errCero.getMessage().contains("CLIENT_TIMEOUT_MS"));

        IllegalArgumentException errTexto = assertThrows(IllegalArgumentException.class,
                () -> ConfiguracionNetwork.desdeMapa(Map.of("CLIENT_TIMEOUT_MS", "diez-segundos")));
        assertTrue(errTexto.getMessage().contains("CLIENT_TIMEOUT_MS"));
    }

    @Test
    @Tag("RF-39")
    @DisplayName("RF-39 · Caso Error: Rechaza host de Broker nulo o vacío")
    void rechazaHostVacio() {
        assertThrows(IllegalArgumentException.class,
                () -> new ConfiguracionNetwork("", 5000, 22000));
        assertThrows(IllegalArgumentException.class,
                () -> new ConfiguracionNetwork("   ", 5000, 22000));
    }
}
