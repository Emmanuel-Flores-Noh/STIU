# Diagrama de clases de Common: componente json

**Paquete:** `mx.uady.inventario.common.json`

Instancia de Gson compartida por los tres componentes y adaptador de fechas en formato ISO-8601.

<div style="page-break-after: always;"></div>

## Diagrama

![Diagrama de clases de Common, componente json](Clases_Common_Json.mmd)

<div style="page-break-after: always;"></div>

## Requisitos funcionales por clase

El módulo `common` no tiene requisitos propios: cada clase se asocia con los requisitos del Sistema 1, el Sistema 2 o el Broker que dependen de ella.

- `GsonFactory`: RF-24, RF-39, RF-45
- `LocalDateTimeAdapter`: RF-17, RF-23, RF-32
