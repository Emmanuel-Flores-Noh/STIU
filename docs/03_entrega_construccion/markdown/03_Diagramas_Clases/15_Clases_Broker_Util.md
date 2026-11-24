# Diagrama de clases del Broker: componente util

**Paquete:** `mx.uady.inventario.broker.util`

Configuración leída de variables de entorno, bitácora en consola y conversión JSON de los mensajes.

<style>
@page horizontal { size: letter landscape; margin: 0.6in; }
.hoja-horizontal + h2, .hoja-horizontal + h2 + p { page: horizontal; }
.hoja-horizontal + h2 + p img { display: block; width: 100%; height: auto; max-height: 16cm; object-fit: contain; margin: 0 auto; }
</style>

<div class="hoja-horizontal"></div>

## Diagrama

![Diagrama de clases del Broker, componente util](Clases_Broker_Util.mmd)

<div style="page-break-after: always;"></div>

## Requisitos funcionales por clase

Requisitos del Broker en la sección 3.2 del ERS ([03_requisitos.md](../02_ERS_PF/03_requisitos.md)).

- `BitacoraBroker`: RF-55, RF-56
- `ConfiguracionBroker`: RF-50, RF-51, RF-52, RF-54, RF-57
- `JsonBroker`: RF-45, RF-47
