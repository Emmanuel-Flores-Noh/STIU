package mx.uady.inventario.central.view.comun;

import java.util.Locale;
import java.util.Objects;

/**
 * Textos de la barra de estado que resume cuántas filas muestra una tabla filtrable.
 *
 * <p>Cada pantalla define sus propios textos; la regla para elegir cuál mostrar es la misma en todas:
 * tabla vacía, ninguna coincidencia con el filtro, solo una parte visible o todas visibles.</p>
 *
 * @param sinFilas         texto cuando la tabla no tiene filas
 * @param sinCoincidencias texto cuando el filtro oculta todas las filas; puede incluir {@code %s},
 *                         que se reemplaza por la búsqueda sin espacios sobrantes
 * @param parcial          formato cuando solo se ven algunas filas, con {@code %d} para las visibles
 *                         y {@code %d} para el total (p. ej. {@code "%d de %d unidades"})
 * @param una              texto cuando hay exactamente una fila y se ve
 * @param varias           formato cuando se ven todas las filas, con {@code %d} para el total
 */
public record ResumenTabla(String sinFilas, String sinCoincidencias, String parcial, String una, String varias) {

    /**
     * Valida que ningún texto sea nulo.
     *
     * @throws NullPointerException si algún texto es {@code null}
     */
    public ResumenTabla {
        Objects.requireNonNull(sinFilas, "sinFilas");
        Objects.requireNonNull(sinCoincidencias, "sinCoincidencias");
        Objects.requireNonNull(parcial, "parcial");
        Objects.requireNonNull(una, "una");
        Objects.requireNonNull(varias, "varias");
    }

    /**
     * Devuelve el texto que corresponde a las filas de la tabla.
     *
     * @param total    filas del modelo, sin filtrar
     * @param visibles filas que deja ver el filtro actual
     * @param busqueda texto buscado, usado en el mensaje sin coincidencias; puede ser {@code null}
     * @return texto para la barra de estado
     * @throws IllegalArgumentException si {@code total} es negativo o {@code visibles} está fuera de
     *                                  {@code 0..total}
     */
    public String texto(int total, int visibles, String busqueda) {
        if (total < 0 || visibles < 0 || visibles > total) {
            throw new IllegalArgumentException(
                    "Conteo de filas inválido: " + visibles + " visibles de " + total);
        }
        if (total == 0) {
            return sinFilas;
        }
        if (visibles == 0) {
            return String.format(Locale.ROOT, sinCoincidencias, busqueda == null ? "" : busqueda.strip());
        }
        if (visibles < total) {
            return String.format(Locale.ROOT, parcial, visibles, total);
        }
        return total == 1 ? una : String.format(Locale.ROOT, varias, total);
    }
}
