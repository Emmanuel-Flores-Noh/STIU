# Diagrama de clases del Sistema 2: componente controller

**Paquete:** `mx.uady.inventario.cliente.controller`

Conecta la ventana con el servicio de ventas: carga las listas, registra la venta, consulta el QR y el historial de precios en segundo plano, y presenta los errores.

<div style="page-break-after: always;"></div>

## Diagrama

![Diagrama de clases del Sistema 2, componente controller](Clases_Sistema2_Controller.mmd)

<div style="page-break-after: always;"></div>

## Requisitos funcionales por clase

- `EjecutorAsincrono`: RF-25
- `PresentadorErrores`: RF-33, RF-37, RF-38
- `ProductoController`: RF-34, RF-36
- `RegistroVentaController`: RF-25, RF-30, RF-31, RF-32, RF-33, RF-34, RF-36
- `VentaOperaciones`: RF-25, RF-30, RF-31, RF-32, RF-34, RF-36
- `VistaController`: RF-25, RF-26, RF-28, RF-29, RF-30, RF-32, RF-33, RF-34, RF-36, RF-37, RF-38
