# Diagrama de clases: componente controller

**Paquete:** `mx.uady.inventario.central.controller`

Conecta la interfaz Swing con los servicios: arma la ventana y los controladores, y ejecuta las operaciones en segundo plano.

<style>
@page horizontal { size: letter landscape; }
.hoja-horizontal + h2, .hoja-horizontal + h2 + p { page: horizontal; }
.hoja-horizontal + h2 + p img { display: block; width: 100%; height: auto; max-height: 16.5cm; object-fit: contain; margin: 0 auto; }
</style>

<div class="hoja-horizontal"></div>

## Diagrama

![Diagrama de clases del componente controller](Clases_Sistema1_Controller.mmd)

<div style="page-break-after: always;"></div>

## Requisitos funcionales por clase

- `AppContext`: RF-01, RF-02, RF-04, RF-06, RF-09, RF-13, RF-14, RF-15
- `MercanciaController`: RF-06, RF-07, RF-09, RF-11
- `MercanciaController.Datos`: RF-06, RF-09
- `ProductoController`: RF-01, RF-02, RF-04
- `TareaAsync`: RF-01, RF-02, RF-06, RF-09, RF-14, RF-15
- `TareaAsync.Accion`: RF-01, RF-02, RF-06, RF-15
- `UiLauncher`: RF-13, RF-14, RF-15
- `VentaController`: RF-13, RF-15
- `VentaController.Datos`: RF-13, RF-15
- `VentaController.DatosMercancias`: RF-15
- `VentaController.Catalogos`: RF-13, RF-15
- `VentaController.MercanciasCargador`: RF-15
- `VentaController.CatalogosCargador`: RF-13, RF-15
- `VentaHistorialController`: RF-14
- `VentaHistorialController.VentasCargador`: RF-14
- `VentaHistorialController.QrAsegurador`: RF-11, RF-14