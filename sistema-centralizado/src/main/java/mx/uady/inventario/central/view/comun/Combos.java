package mx.uady.inventario.central.view.comun;

import java.awt.Component;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/** Utilidades para llenar los combos de las pantallas y mostrar sus elementos con texto legible. */
public final class Combos {

    private Combos() {
    }

    /**
     * Reemplaza los elementos del combo y conserva la selección si el elemento elegido sigue en la lista.
     *
     * <p>Los elementos se comparan por la clave indicada (por ejemplo, el identificador), de modo que
     * una instancia nueva del mismo producto, tienda o usuario se reconoce como el mismo elemento. Si el
     * elegido ya no está, queda seleccionado el primero; si la lista está vacía, el combo queda vacío.</p>
     *
     * @param <T>       tipo de los elementos
     * @param combo     combo a llenar
     * @param elementos nuevos elementos, en el orden en que se mostrarán
     * @param clave     función que identifica a cada elemento
     * @throws NullPointerException si algún argumento es {@code null}
     */
    public static <T> void rellenarConservando(JComboBox<T> combo, List<? extends T> elementos,
            Function<? super T, ?> clave) {
        Objects.requireNonNull(combo, "combo");
        Objects.requireNonNull(elementos, "elementos");
        Objects.requireNonNull(clave, "clave");
        int indice = combo.getSelectedIndex();
        Object claveAnterior = indice < 0 ? null : clave.apply(combo.getItemAt(indice));

        combo.removeAllItems();
        elementos.forEach(combo::addItem);
        seleccionarOPrimero(combo, e -> claveAnterior != null && claveAnterior.equals(clave.apply(e)));
    }

    /**
     * Selecciona el primer elemento que cumple el criterio o, si ninguno lo cumple, el primero del combo.
     * Un combo vacío se deja sin cambios.
     *
     * @param <T>      tipo de los elementos
     * @param combo    combo en el que se selecciona
     * @param criterio condición del elemento buscado
     * @throws NullPointerException si algún argumento es {@code null}
     */
    public static <T> void seleccionarOPrimero(JComboBox<T> combo, Predicate<? super T> criterio) {
        Objects.requireNonNull(combo, "combo");
        Objects.requireNonNull(criterio, "criterio");
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (criterio.test(combo.getItemAt(i))) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    /**
     * Crea un renderer que muestra cada elemento con el texto que devuelve la función.
     *
     * @param texto función que convierte un elemento (que puede ser {@code null}) en lo que se muestra
     * @return renderer para {@link JComboBox#setRenderer}
     * @throws NullPointerException si {@code texto} es {@code null}
     */
    public static ListCellRenderer<Object> renderTexto(Function<Object, Object> texto) {
        Objects.requireNonNull(texto, "texto");
        return new RenderTexto(texto);
    }

    /** Renderer de la apariencia estándar que solo cambia el valor mostrado. */
    private static final class RenderTexto extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        private final transient Function<Object, Object> texto;

        RenderTexto(Function<Object, Object> texto) {
            this.texto = texto;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                boolean seleccionado, boolean foco) {
            return super.getListCellRendererComponent(lista, texto.apply(valor), indice, seleccionado, foco);
        }
    }
}
