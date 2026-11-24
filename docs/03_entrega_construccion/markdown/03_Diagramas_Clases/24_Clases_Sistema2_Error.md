# Diagrama de clases del Sistema 2: componente error

**Paquete:** `mx.uady.inventario.cliente.error`

Clasificación de los fallos al comunicarse con el Broker y su traducción a un título, un mensaje y la decisión de ofrecer reintentar.

<div style="page-break-after: always;"></div>

## Diagrama

![Diagrama de clases del Sistema 2, componente error](Clases_Sistema2_Error.mmd)

<div style="page-break-after: always;"></div>

## Requisitos funcionales por clase

- `ComunicacionError`: RF-37, RF-38, RF-40
- `ComunicacionException`: RF-37, RF-38
- `DescripcionError`: RF-37, RF-38
- `ErrorServicioException`: RF-33, RF-37
- `TraductorErrores`: RF-33, RF-37, RF-38, RF-39, RF-40
