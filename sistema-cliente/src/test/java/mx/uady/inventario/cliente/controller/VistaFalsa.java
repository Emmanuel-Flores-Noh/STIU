package mx.uady.inventario.cliente.controller;

import mx.uady.inventario.cliente.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación simulada de {@link VistaController} para pruebas unitarias de controladores.
 */public class VistaFalsa implements VistaController {
    public final List<String> eventos = new ArrayList<>();
    public final List<String> mensajes = new ArrayList<>();
    public List<TiendaDTO> ultimasTiendas = new ArrayList<>();
    public List<UsuarioDTO> ultimosUsuarios = new ArrayList<>();
    public List<MercanciaDTO> ultimaMercancia = new ArrayList<>();
    public Runnable reintentoOfrecido;
    public boolean cargando;
    public QrDTO qrMostrado;
    public PrecioHistorialDTO historialMostrado;

    @Override
    public void mostrarIndicadorCarga(String m, boolean c) {
        eventos.add("estado:" + m);
        this.cargando = c;
    }

    @Override
    public void actualizarListasCombos(List<TiendaDTO> t, List<UsuarioDTO> u) {
        eventos.add("combos");
        this.ultimasTiendas = t;
        this.ultimosUsuarios = u;
    }

    @Override
    public void actualizarTablaMercancia(List<MercanciaDTO> u) {
        eventos.add("tabla:" + (u != null ? u.size() : 0));
        this.ultimaMercancia = u;
    }

    @Override
    public void limpiarFormulario() {
        eventos.add("limpiar");
    }

    @Override
    public void mostrarMensajeExito(String m) {
        eventos.add("exito");
        mensajes.add(m);
    }

    @Override
    public void mostrarMensajeError(String m) {
        eventos.add("error");
        mensajes.add(m);
    }

    @Override
    public void mostrarDialogoReintentar(String tipo, String m, Runnable r) {
        eventos.add("reintentar:" + tipo);
        mensajes.add(m);
        this.reintentoOfrecido = r;
    }

    @Override
    public void mostrarQr(QrDTO qr) {
        eventos.add("qr");
        this.qrMostrado = qr;
    }

    @Override
    public void mostrarHistorialPrecios(PrecioHistorialDTO h) {
        eventos.add("historial");
        this.historialMostrado = h;
    }
}