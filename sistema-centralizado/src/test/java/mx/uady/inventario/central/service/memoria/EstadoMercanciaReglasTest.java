package mx.uady.inventario.central.service.memoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import mx.uady.inventario.central.service.QrService;
import mx.uady.inventario.central.service.SerieDuplicadaException;
import mx.uady.inventario.central.view.MercanciaTableModel;
import mx.uady.inventario.common.modelo.EstadoMercancia;
import mx.uady.inventario.common.modelo.Mercancia;

/** Ciclo de vida de una unidad: "En almacén" al registrarse y "Vendido" sin retorno al venderse (RF-08). */
class EstadoMercanciaReglasTest {

    private static final int ID_PRODUCTO = 1;
    private static final int ID_TIENDA = 1;
    private static final int ID_USUARIO = 2;

    @TempDir
    Path directorioQr;

    private MercanciaServiceMemoria mercancia;
    private VentasMemoria ventas;

    @BeforeEach
    void preparar() {
        AlmacenMemoria almacen = AlmacenMemoria.conDatosDeEjemplo(Duration.ZERO);
        mercancia = new MercanciaServiceMemoria(almacen, new QrService(directorioQr));
        ventas = new VentasMemoria(almacen);
    }

    @Test
    @Tag("RF-08")
    @DisplayName("RF-08 · Una unidad recién registrada queda En almacén")
    void unaUnidadRecienRegistradaQuedaEnAlmacen() {
        Mercancia unidad = mercancia.registrar(ID_PRODUCTO, "SN-ESTADO-01");

        assertEquals(EstadoMercancia.EN_ALMACEN, unidad.estado());
        assertEquals(EstadoMercancia.EN_ALMACEN, estadoDe("SN-ESTADO-01"));
    }

    @Test
    @Tag("RF-08")
    @Tag("RF-15")
    @DisplayName("RF-08, RF-15 · Al venderse la unidad pasa a Vendido")
    void alVenderseLaUnidadPasaAVendido() {
        mercancia.registrar(ID_PRODUCTO, "SN-ESTADO-02");

        ventas.registrar("SN-ESTADO-02", ID_TIENDA, ID_USUARIO);

        assertEquals(EstadoMercancia.VENDIDO, estadoDe("SN-ESTADO-02"));
    }

    @Test
    @Tag("RF-08")
    @Tag("RF-18")
    @DisplayName("RF-08, RF-18 · Vender de nuevo una unidad vendida se rechaza y sigue Vendido")
    void venderDeNuevoUnaUnidadVendidaSeRechazaYSigueVendido() {
        mercancia.registrar(ID_PRODUCTO, "SN-ESTADO-03");
        ventas.registrar("SN-ESTADO-03", ID_TIENDA, ID_USUARIO);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> ventas.registrar("SN-ESTADO-03", ID_TIENDA, ID_USUARIO));

        assertEquals("La mercancía ya fue vendida", error.getMessage());
        assertEquals(EstadoMercancia.VENDIDO, estadoDe("SN-ESTADO-03"));
    }

    @Test
    @Tag("RF-07")
    @Tag("RF-08")
    @DisplayName("RF-07, RF-08 · Una unidad vendida no puede volver a registrarse como En almacén")
    void unaUnidadVendidaNoPuedeVolverARegistrarseComoEnAlmacen() {
        mercancia.registrar(ID_PRODUCTO, "SN-ESTADO-04");
        ventas.registrar("SN-ESTADO-04", ID_TIENDA, ID_USUARIO);

        assertThrows(SerieDuplicadaException.class, () -> mercancia.registrar(ID_PRODUCTO, "sn-estado-04"));

        assertEquals(EstadoMercancia.VENDIDO, estadoDe("SN-ESTADO-04"));
    }

    @Test
    @Tag("RF-08")
    @Tag("RF-09")
    @DisplayName("RF-08, RF-09 · Cada estado se muestra con su texto: En almacén y Vendido")
    void cadaEstadoSeMuestraConSuTexto() {
        assertEquals("En almacén", MercanciaTableModel.texto(EstadoMercancia.EN_ALMACEN));
        assertEquals("Vendido", MercanciaTableModel.texto(EstadoMercancia.VENDIDO));
    }

    @Test
    @Tag("RF-08")
    @DisplayName("RF-08 · El estado leído de la base de datos tolera mayúsculas y espacios")
    void elEstadoLeidoToleraMayusculasYEspacios() {
        assertEquals(EstadoMercancia.EN_ALMACEN, EstadoMercancia.desdeCadena(" en_almacen "));
        assertEquals(EstadoMercancia.VENDIDO, EstadoMercancia.desdeCadena("Vendido"));
    }

    @Test
    @Tag("RF-08")
    @DisplayName("RF-08 · Solo existen los estados En almacén y Vendido")
    void soloExistenLosEstadosEnAlmacenYVendido() {
        assertEquals(2, EstadoMercancia.values().length);
        for (String invalido : new String[] {null, "", "   ", "devuelto", "EN ALMACEN"}) {
            assertThrows(IllegalArgumentException.class, () -> EstadoMercancia.desdeCadena(invalido),
                    "Estado: [" + invalido + "]");
        }
    }

    private EstadoMercancia estadoDe(String numeroSerie) {
        return mercancia.listar(null, null).stream()
                .filter(u -> u.numeroSerie().equalsIgnoreCase(numeroSerie))
                .findFirst()
                .orElseThrow()
                .estado();
    }
}
