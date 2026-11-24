# Diagrama de clases: componente service

**Paquete:** `mx.uady.inventario.central.service y service.jdbc`

Lógica de negocio: servicios y su implementación sobre MariaDB, generación de QR, validaciones y excepciones.

<style>
@page horizontal { size: letter landscape; margin: 0.6in; }
.hoja-horizontal + h2, .hoja-horizontal + h2 + p { page: horizontal; }
.hoja-horizontal + h2 + p img { display: block; width: 100%; height: auto; max-height: 16cm; object-fit: contain; margin: 0 auto; }
</style>

<div class="hoja-horizontal"></div>

## Diagrama

![Diagrama de clases del componente service](Clases_Sistema1_Service.mmd)

<div style="page-break-after: always;"></div>

## Requisitos funcionales por clase

- `AccesoDatosException`: RF-01, RF-02, RF-06
- `ConsultasVentaService`: RF-13, RF-14, RF-19, RF-20
- `MercanciaService`: RF-06, RF-08, RF-09, RF-11, RF-21, RF-22
- `MercanciaValidator`: RF-06
- `NegocioException`: RF-02, RF-06
- `ProductoService`: RF-01, RF-02, RF-03, RF-04, RF-05, RF-23
- `ProductoValidator`: RF-01, RF-02
- `ProductoValidator.Campo`: RF-01, RF-02
- `QrGenerationException`: RF-10
- `QrService`: RF-10, RF-22
- `QrUnidades`: RF-06, RF-10, RF-11
- `SerieDuplicadaException`: RF-07
- `ValidacionException`: RF-01, RF-02, RF-06
- `VentaService`: RF-15, RF-17
- `VentaService.VentaRegistrador`: RF-15, RF-17
- `ConsultasVentaJdbc`: RF-13, RF-14, RF-19, RF-20
- `MercanciaServiceJdbc`: RF-06, RF-07, RF-08, RF-10, RF-11, RF-22
- `ProductoServiceJdbc`: RF-01, RF-02, RF-03, RF-04, RF-05, RF-23
- `Transacciones`: RF-03, RF-06
- `Transacciones.Trabajo`: RF-03, RF-06