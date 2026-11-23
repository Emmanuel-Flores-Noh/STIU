package mx.uady.inventario.central.view;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import mx.uady.inventario.central.model.VentaHistorial;

/**
 * Criterios con los que se filtra el historial de ventas (RF-14), sin depender de Swing.
 *
 * <p>La serie se busca sin distinguir mayúsculas ni acentos; la tienda y el producto deben
 * coincidir exactamente ({@code null} = todas); el rango de fechas es inclusivo en ambos
 * extremos. Una fecha mal escrita se ignora y se indica con {@link #fechaInvalida()}.</p>
 *
 * @param serie         texto buscado en el número de serie (vacío = todas)
 * @param tienda        tienda elegida, o {@code null} para todas
 * @param producto      producto elegido, o {@code null} para todos
 * @param desde         primer día incluido, o {@code null} sin límite
 * @param hasta         último día incluido, o {@code null} sin límite
 * @param fechaInvalida {@code true} si alguna fecha escrita no tiene el formato aaaa-mm-dd
 */
record CriteriosHistorialVentas(String serie, String tienda, String producto,
        LocalDate desde, LocalDate hasta, boolean fechaInvalida) {

    /** Arma los criterios a partir de lo escrito en la pantalla; recorta espacios. */
    static CriteriosHistorialVentas de(String serie, String tienda, String producto, String desde, String hasta) {
        LocalDate desdeFecha = fecha(desde);
        LocalDate hastaFecha = fecha(hasta);
        boolean invalida = !vacio(desde) && desdeFecha == null || !vacio(hasta) && hastaFecha == null;
        return new CriteriosHistorialVentas(serie == null ? "" : serie.strip(), tienda, producto,
                desdeFecha, hastaFecha, invalida);
    }

    /** @return {@code true} si la venta cumple todos los criterios */
    boolean cumple(VentaHistorial venta) {
        return FiltroTexto.contiene(venta.numeroSerie(), serie)
                && (tienda == null || tienda.equals(venta.tienda()))
                && (producto == null || producto.equals(venta.producto()))
                && (desde == null || !venta.fechaVenta().toLocalDate().isBefore(desde))
                && (hasta == null || !venta.fechaVenta().toLocalDate().isAfter(hasta));
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private static LocalDate fecha(String texto) {
        if (vacio(texto)) {
            return null;
        }
        try {
            return LocalDate.parse(texto.strip());
        } catch (DateTimeParseException error) {
            return null;
        }
    }
}
