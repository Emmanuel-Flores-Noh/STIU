# Diagrama de clases de Common: componente contrato

**Paquete:** `mx.uady.inventario.common.contrato`

Mensajes inmutables que viajan en JSON entre el Sistema 2, el Broker y el Sistema 1.

<style>
@page horizontal { size: letter landscape; margin: 0.6in; }
.hoja-horizontal + h2, .hoja-horizontal + h2 + p { page: horizontal; }
.hoja-horizontal + h2 + p img { display: block; width: 100%; height: auto; max-height: 16cm; object-fit: contain; margin: 0 auto; }
</style>

<div class="hoja-horizontal"></div>

## Diagrama

![Diagrama de clases de Common, componente contrato](Clases_Common_Contrato.mmd)

<div style="page-break-after: always;"></div>

## Requisitos funcionales por clase

El módulo `common` no tiene requisitos propios: cada clase se asocia con los requisitos del Sistema 1, el Sistema 2 o el Broker que dependen de ella.

- `EstadoRespuesta`: RF-24
- `Respuesta`: RF-24, RF-45
- `Solicitud`: RF-39, RF-45, RF-47
