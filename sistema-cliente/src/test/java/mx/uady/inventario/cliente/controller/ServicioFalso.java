package mx.uady.inventario.cliente.controller;

import mx.uady.inventario.cliente.error.ComunicacionError;
import mx.uady.inventario.cliente.error.ComunicacionException;
import mx.uady.inventario.cliente.model.*;
import mx.uady.inventario.cliente.network.VentaService;
import mx.uady.inventario.common.error.CodigoError;

import javax.naming.CommunicationException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ServicioFalso extends VentaService {
    final AtomicInteger ventas = new AtomicInteger();
    CommunicationException falloVenta;
    CommunicationException falloInventario;

    ServicioFalso() { super(null); }

    @Override public List<TiendaDTO> listarTiendas() { return List.of(); }
    @Override public List<UsuarioDTO> listarUsuarios() { return List.of(); }
    @Override public List<MercanciaDTO> listarInventarioDisponible() throws ComunicacionException {
        if (falloInventario != null) throw new ComunicacionException();
        return List.of(new MercanciaDTO("A1", 1, "Laptop", "EN_ALMACEN"));
    }
    @Override public String registrarVenta(String s, Long t, Long u) throws ComunicacionException {
        ventas.incrementAndGet();
        if (falloVenta != null) throw new ComunicacionException();
        return "2026-11-21T10:30:00";
    }
    @Override public QrDTO obtenerQr(String s) { return new QrDTO(s, 1, "Laptop", "%%%"); }
    @Override public PrecioHistorialDTO obtenerHistorialPrecios(Integer id) { return new PrecioHistorialDTO(); }
}