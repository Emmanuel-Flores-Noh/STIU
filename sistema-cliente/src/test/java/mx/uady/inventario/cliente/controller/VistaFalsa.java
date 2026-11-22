package mx.uady.inventario.cliente.controller;

import mx.uady.inventario.cliente.model.*;

import java.util.ArrayList;
import java.util.List;

public class VistaFalsa implements VistaController {
    final List<String> eventos = new ArrayList<>();
    final List<String> mensajes = new ArrayList<>();
    Runnable reintentoOfrecido;
    boolean cargando;
    QrDTO qrMostrado;

    public void mostrarIndicadorCarga(String m, boolean c) { eventos.add("estado:" + m); cargando = c; }
    public void actualizarListasCombos(List<TiendaDTO> t, List<UsuarioDTO> u) { eventos.add("combos"); }
    public void actualizarTablaMercancia(List<MercanciaDTO> u) { eventos.add("tabla:" + u.size()); }
    public void limpiarFormulario() { eventos.add("limpiar"); }
    public void mostrarMensajeExito(String m) { eventos.add("exito"); mensajes.add(m); }
    public void mostrarMensajeError(String m) { eventos.add("error"); mensajes.add(m); }
    public void mostrarDialogoReintentar(String tipo, String m, Runnable r) {
        eventos.add("reintentar:" + tipo); mensajes.add(m); reintentoOfrecido = r;
    }
    public void mostrarQr(QrDTO qr) { eventos.add("qr"); qrMostrado = qr; }
    public void mostrarHistorialPrecios(PrecioHistorialDTO h) { eventos.add("historial"); }
}