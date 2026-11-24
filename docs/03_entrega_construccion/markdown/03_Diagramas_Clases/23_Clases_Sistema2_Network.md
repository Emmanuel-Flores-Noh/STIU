# Diagrama de clases del Sistema 2: componente network

**Paquete:** `mx.uady.inventario.cliente.network`

Configuración de red leída de variables de entorno, conexión TCP con el Broker (una línea JSON por solicitud) y servicio que invoca las operaciones del Sistema 1.

<div style="page-break-after: always;"></div>

## Diagrama

![Diagrama de clases del Sistema 2, componente network](Clases_Sistema2_Network.mmd)

<div style="page-break-after: always;"></div>

## Requisitos funcionales por clase

- `ClienteTCP`: RF-37, RF-39
- `ConexionTCP`: RF-37, RF-39, RF-40
- `ConfiguracionNetwork`: RF-39, RF-40
- `VentaService`: RF-25, RF-26, RF-28, RF-29, RF-32, RF-34, RF-35, RF-36
