package mx.uady.inventario.cliente.controller;

import javax.swing.SwingWorker;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Ejecuta un trabajo bloqueante en segundo plano y entrega su resultado, o su error, en el hilo de
 * la interfaz. Permite probar los controladores con una implementación síncrona.
 */
public interface EjecutorAsincrono {

    /**
     * Ejecuta el trabajo fuera del hilo de la interfaz.
     *
     * @param trabajo    operación bloqueante a ejecutar
     * @param alTerminar recibe el resultado en el hilo de la interfaz
     * @param alFallar   recibe el error original (sin envolver) en el hilo de la interfaz
     * @param <T>        tipo del resultado
     */
    <T> void ejecutar(Callable<T> trabajo, Consumer<T> alTerminar, Consumer<Throwable> alFallar);

    /**
     * Crea el ejecutor de producción, basado en {@link SwingWorker}.
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
