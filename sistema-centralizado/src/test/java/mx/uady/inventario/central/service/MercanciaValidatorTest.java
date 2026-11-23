package mx.uady.inventario.central.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Reglas del número de serie al registrar una unidad (RF-06). */
class MercanciaValidatorTest {

    private static final String OBLIGATORIO = "El número de serie es obligatorio.";

    @Test
    @Tag("RF-06")
    @DisplayName("RF-06 · Acepta una serie válida sin cambiarla")
    void aceptaUnaSerieValidaSinCambiarla() {
        assertEquals("SN-001", MercanciaValidator.normalizarSerie("SN-001"));
    }

    @Test
    @Tag("RF-06")
    @DisplayName("RF-06 · Quita los espacios al inicio y al final de la serie")
    void quitaLosEspaciosAlInicioYAlFinalDeLaSerie() {
        assertEquals("SN-001", MercanciaValidator.normalizarSerie("  SN-001\t "));
    }

    @Test
    @Tag("RF-06")
    @DisplayName("RF-06 · Acepta una serie de exactamente 100 caracteres, aun con espacios alrededor")
    void aceptaUnaSerieDeExactamente100Caracteres() {
        String justa = "S".repeat(MercanciaValidator.LONGITUD_MAXIMA_SERIE);

        assertEquals(justa, MercanciaValidator.normalizarSerie(justa));
        assertEquals(justa, MercanciaValidator.normalizarSerie("   " + justa + "   "));
    }

    @Test
    @Tag("RF-06")
    @Tag("RNF-30")
    @DisplayName("RF-06, RNF-30 · Rechaza una serie de 101 caracteres indicando el máximo")
    void rechazaUnaSerieDe101CaracteresIndicandoElMaximo() {
        String larga = "S".repeat(MercanciaValidator.LONGITUD_MAXIMA_SERIE + 1);

        ValidacionException error = assertThrows(ValidacionException.class,
                () -> MercanciaValidator.normalizarSerie(larga));
        assertEquals("El número de serie admite como máximo 100 caracteres.", error.getMessage());
    }

    @Test
    @Tag("RF-06")
    @Tag("RNF-30")
    @DisplayName("RF-06, RNF-30 · Rechaza una serie nula, vacía o solo con espacios")
    void rechazaUnaSerieNulaVaciaOSoloConEspacios() {
        for (String serie : new String[] {null, "", "   ", "\t\n"}) {
            ValidacionException error = assertThrows(ValidacionException.class,
                    () -> MercanciaValidator.normalizarSerie(serie), "Serie: [" + serie + "]");
            assertEquals(OBLIGATORIO, error.getMessage());
        }
    }
}
