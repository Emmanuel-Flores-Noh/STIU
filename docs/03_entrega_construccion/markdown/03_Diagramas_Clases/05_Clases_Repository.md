# Diagrama de clases: componente repository

**Paquete:** `mx.uady.inventario.central.repository`

Acceso a MariaDB con JDBC.

<style>
@page horizontal { size: letter landscape; }
.hoja-horizontal + h2, .hoja-horizontal + h2 + p { page: horizontal; }
.hoja-horizontal + h2 + p img { display: block; width: 100%; height: auto; max-height: 16.5cm; object-fit: contain; margin: 0 auto; }
</style>

<div class="hoja-horizontal"></div>

## Diagrama

![Diagrama de clases del componente repository](Clases_Sistema1_Repository.mmd)

<div style="page-break-after: always;"></div>

## Requisitos funcionales por clase

- `CatalogoRepository`: RF-13, RF-19, RF-20
- `HistorialPrecioRepository`: RF-03, RF-05, RF-23
- `MercanciaRepository`: RF-06, RF-07, RF-08, RF-09, RF-11, RF-21, RF-22
- `ProductoRepository`: RF-01, RF-02, RF-04, RF-06, RF-23
- `ProveedorConexion`: RF-01, RF-06, RF-13, RF-17
- `VentaRepository`: RF-08, RF-14, RF-15, RF-17, RF-18
- `VentaRepositoryException`: RF-17, RF-18
- `VentaResultado`: RF-17