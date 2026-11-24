# Diagrama de clases del Sistema 2: componente view

**Paquete:** `mx.uady.inventario.cliente.view y view.components`

Interfaz Swing del encargado de ventas: ventana principal, tabla de mercancía con filtro, formulario de venta y diálogos de QR, historial de precios y mensajes.

<style>
@page horizontal { size: letter landscape; margin: 0.6in; }
.hoja-horizontal + h2, .hoja-horizontal + h2 + p { page: horizontal; }
.hoja-horizontal + h2 + p img { display: block; width: 100%; height: auto; max-height: 16cm; object-fit: contain; margin: 0 auto; }
</style>

<div class="hoja-horizontal"></div>

## Diagrama

![Diagrama de clases del Sistema 2, componente view](Clases_Sistema2_View.mmd)

<div style="page-break-after: always;"></div>

## Requisitos funcionales por clase

- `InterfazConfiguracion`: sin requisito funcional (aplica el aspecto de la interfaz)
- `RegistroVentaFrame`: RF-25, RF-26, RF-27, RF-28, RF-29, RF-30, RF-31, RF-32, RF-33, RF-34, RF-36, RF-37, RF-38
- `HistorialPrecioDialog`: RF-36
- `MercanciaFiltro`: RF-27
- `MercanciaTabla`: RF-26, RF-27, RF-34
- `Modelo`: RF-26
- `OperacionDialog`: RF-32, RF-33, RF-37, RF-38
- `QrPreviewDialog`: RF-34, RF-35
- `VentaFormulario`: RF-28, RF-29, RF-31, RF-36
