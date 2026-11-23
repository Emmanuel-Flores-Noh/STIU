package mx.uady.inventario.cliente.controller;

import javax.swing.SwingWorker;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Ejecuta un trabajo bloqueante en segundo plano y entrega su resultado, o su error, en el hilo de
 * la interfaz. Permite probar los controladores con una implementación síncrona.
 *
 * <p>Trazabilidad con Requisitos:
 * <ul>
 *   <li><b>RF-25:</b> Ejecución de consultas de tiendas, usuarios e inventario en segundo plano.</li>
 *   <li><b>RF-30:</b> Actualización de catálogos e inventario sin bloquear la interfaz.</li>
 *   <li><b>RNF-06:</b> No congelamiento de la ventana durante operaciones de entrada y salida de red.</li>
 * </ul>
 * </p>
 */
public interface EjecutorAsincrono {

    /**
     * Ejecuta el trabajo fuera del hilo de la interfaz.
     *
     * <p>Requisito: RF-25.</p>
     *
     * @param trabajo    operación bloqueante a ejecutar en segundo plano
     * @param alTerminar consumidor que recibe el resultado exitoso en el hilo de la interfaz
     * @param alFallar   consumidor que recibe la excepción raíz en el hilo de la interfaz
     * @param <T>        tipo del resultado retornado
     */
    <T> void ejecutar(Callable<T> trabajo, Consumer<T> alTerminar, Consumer<Throwable> alFallar);

    /**
     * Crea el ejecutor de producción, basado en {@link SwingWorker}.
     *
     * <p>Requisitos: RF-25, RNF-06.</p>
     *
     * @return ejecutor que corre en segundo plano y responde en el hilo de despacho de eventos
     */
    static EjecutorAsincrono swing() {
        return new EjecutorAsincrono() {
            @Override
            public <T> void ejecutar(Callable<T> trabajo, Consumer<T> alTerminar, Consumer<Throwable> alFallar) {
                new SwingWorker<T, Void>() {
                    @Override
                    protected T doInBackground() throws Exception {
                        return trabajo.call();
                    }

                    @Override
                    protected void done() {
                        T resultado;
                        try {
                            resultado = get();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            alFallar.accept(e);
                            return;
                        } catch (ExecutionException e) {
                            alFallar.accept(e.getCause() != null ? e.getCause() : e);
                            return;
                        } catch (CancellationException e) {
                            alFallar.accept(e);
                            return;
                        }
                        // Fuera del try: un fallo al mostrar el resultado no debe tratarse como fallo de la operación.
                        alTerminar.accept(resultado);
                    }
                }.execute();
            }
        };
    }
}
