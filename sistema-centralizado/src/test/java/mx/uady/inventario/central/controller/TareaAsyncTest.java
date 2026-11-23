package mx.uady.inventario.central.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class TareaAsyncTest {

    @Test
    @Tag("RNF-08")
    @Tag("RNF-12")
    @DisplayName("RNF-08, RNF-12 · Ejecuta fuera del hilo de Swing y entrega el resultado en él")
    void ejecutaFueraDelHiloDeSwingYEntregaElResultadoEnEl() throws Exception {
        AtomicBoolean operacionEnEdt = new AtomicBoolean(true);
        AtomicBoolean callbackEnEdt = new AtomicBoolean(false);
        AtomicReference<String> resultado = new AtomicReference<>();
        CountDownLatch fin = new CountDownLatch(1);

        SwingUtilities.invokeAndWait(() -> TareaAsync.de(() -> {
                    operacionEnEdt.set(SwingUtilities.isEventDispatchThread());
                    return "ok";
                })
                .alTerminar(r -> {
                    callbackEnEdt.set(SwingUtilities.isEventDispatchThread());
                    resultado.set(r);
                    fin.countDown();
                })
                .ejecutar(null));

        assertTrue(fin.await(5, TimeUnit.SECONDS));
        assertFalse(operacionEnEdt.get());
        assertTrue(callbackEnEdt.get());
        assertEquals("ok", resultado.get());
    }

    @Test
    @Tag("RNF-08")
    @Tag("RNF-12")
    @DisplayName("RNF-08, RNF-12 · Bloquea los controles mientras corre y restaura su estado")
    void bloqueaLosControlesMientrasCorreYRestauraSuEstado() throws Exception {
        JButton habilitado = new JButton();
        JButton yaDeshabilitado = new JButton();
        yaDeshabilitado.setEnabled(false);
        CountDownLatch enCurso = new CountDownLatch(1);
        CountDownLatch liberar = new CountDownLatch(1);
        CountDownLatch fin = new CountDownLatch(1);

        SwingUtilities.invokeAndWait(() -> TareaAsync.deAccion(() -> {
                    enCurso.countDown();
                    liberar.await(5, TimeUnit.SECONDS);
                })
                .bloqueando(habilitado, yaDeshabilitado)
                .alTerminar(r -> fin.countDown())
                .ejecutar(null));

        assertTrue(enCurso.await(5, TimeUnit.SECONDS));
        assertFalse(habilitado.isEnabled());
        liberar.countDown();
        assertTrue(fin.await(5, TimeUnit.SECONDS));

        assertTrue(habilitado.isEnabled());
        assertFalse(yaDeshabilitado.isEnabled());
    }

    @Test
    @Tag("RNF-08")
    @Tag("RNF-12")
    @DisplayName("RNF-08, RNF-12 · Entrega la causa original al callback de error")
    void entregaLaCausaOriginalAlCallbackDeError() throws Exception {
        IllegalStateException original = new IllegalStateException("falla");
        AtomicReference<Throwable> recibido = new AtomicReference<>();
        AtomicBoolean exitoLlamado = new AtomicBoolean(false);
        CountDownLatch fin = new CountDownLatch(1);

        SwingUtilities.invokeAndWait(() -> TareaAsync.<String>de(() -> {
                    throw original;
                })
                .alTerminar(r -> exitoLlamado.set(true))
                .alFallar(e -> {
                    recibido.set(e);
                    fin.countDown();
                })
                .ejecutar(null));

        assertTrue(fin.await(5, TimeUnit.SECONDS));
        assertEquals(original, recibido.get());
        assertFalse(exitoLlamado.get());
    }

    @Test
    @Tag("RNF-08")
    @Tag("RNF-12")
    @DisplayName("RNF-08, RNF-12 · Rechaza ejecutarse fuera del hilo de Swing")
    void rechazaEjecutarseFueraDelHiloDeSwing() {
        assertFalse(SwingUtilities.isEventDispatchThread());
        assertThrows(IllegalStateException.class, () -> TareaAsync.de(() -> 1).ejecutar(null));
    }
}
