package mx.uady.inventario.cliente.controller;

import mx.uady.inventario.cliente.error.ComunicacionError;
import mx.uady.inventario.cliente.error.ComunicacionException;
import mx.uady.inventario.cliente.model.*;
import mx.uady.inventario.cliente.network.VentaService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementación simulada de {@link VentaService} para pruebas unitarias de controladores.
 */
public class ServicioFalso extends VentaService {
    public final AtomicInteger ventas = new AtomicInteger();
    public Throwable falloVenta;
    public Throwable falloInventario;
    public Throwable falloCatalogos;
    public Throwable falloQr;
    public Throwable falloHistorial;

    public List<TiendaDTO> tiendas = new ArrayList<>(List.of(new TiendaDTO(1L, "Tienda Central UADY", "Calle 60")));
    public List<UsuarioDTO> usuarios = new ArrayList<>(List.of(new UsuarioDTO(1L, "Juan", "Pérez")));
    public List<MercanciaDTO> inventario = new ArrayList<>(List.of(new MercanciaDTO("SN-001", 101, "Laptop Lenovo", "EN_ALMACEN")));
    public QrDTO qrRespuesta = new QrDTO("SN-001", 101, "Laptop Lenovo", "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");
    public PrecioHistorialDTO historialRespuesta = new PrecioHistorialDTO(
            101,
            "Laptop Lenovo",
            new BigDecimal("15000.00"),
            List.of(new PrecioDTO(new BigDecimal("14000.00"), "2026-01-10T10:00:00"))
    );

    public ServicioFalso() {
        super(null);
    }

    private void lanzarSiAplica(Throwable fallo, ComunicacionError tipoPorDefecto, String mensaje)
            throws ComunicacionException {
        switch (fallo) {
            case null -> {
                return;
            }
            case ComunicacionException ce -> throw ce;
            case RuntimeException re -> throw re;
            default -> {
            }
        }
        throw new ComunicacionException(tipoPorDefecto, mensaje, fallo);
    }

    @Override
    public List<TiendaDTO> listarTiendas() throws ComunicacionException {
        return tiendas;
    }

    @Override
    public List<UsuarioDTO> listarUsuarios() throws ComunicacionException {
        return usuarios;
    }

    @Override
    public List<MercanciaDTO> listarInventarioDisponible() throws ComunicacionException {
        lanzarSiAplica(falloInventario, ComunicacionError.TIEMPO_AGOTADO, "Error simulado");
        return inventario;
    }

    @Override
    public Catalogos cargarCatalogos() throws ComunicacionException {
        lanzarSiAplica(falloCatalogos, ComunicacionError.TIEMPO_AGOTADO, "Error simulado");
        return new Catalogos(listarTiendas(), listarUsuarios(), listarInventarioDisponible());
    }

    @Override
    public String registrarVenta(String s, Long t, Long u) throws ComunicacionException {
        ventas.incrementAndGet();
        lanzarSiAplica(falloVenta, ComunicacionError.ERROR_SERVIDOR, "Fallo venta simulado");
        return "2026-11-21T10:30:00";
    }

    @Override
    public QrDTO obtenerQr(String s) throws ComunicacionException {
        lanzarSiAplica(falloQr, ComunicacionError.ERROR_SERVIDOR, "Fallo QR simulado");
        return qrRespuesta;
    }

    @Override
    public PrecioHistorialDTO obtenerHistorialPrecios(Integer id) throws ComunicacionException {
        lanzarSiAplica(falloHistorial, ComunicacionError.ERROR_SERVIDOR, "Fallo historial simulado");
        return historialRespuesta;
    }
}