# Diagrama de clases del Broker: componente routing

**Paquete:** `mx.uady.inventario.broker.routing`

Enrutamiento de solicitudes al proveedor registrado, con tiempos límite, reintentos y marca de no disponible.

<style>
@page horizontal { size: letter landscape; margin: 0.6in; }
.hoja-horizontal + h2, .hoja-horizontal + h2 + p { page: horizontal; }
.hoja-horizontal + h2 + p img { display: block; width: 100%; height: auto; max-height: 16cm; object-fit: contain; margin: 0 auto; }
</style>

<div class="hoja-horizontal"></div>

## Diagrama

![Diagrama de clases del Broker, componente routing](Clases_Broker_Routing.mmd)

<div style="page-break-after: always;"></div>

## Requisitos funcionales por clase

Requisitos del Broker en la sección 3.2 del ERS ([03_requisitos.md](../02_ERS_PF/03_requisitos.md)).

- `ConectorProveedor`: RF-45, RF-50, RF-51
- `Enrutador`: RF-45, RF-47, RF-48, RF-50, RF-51, RF-52, RF-56
- `PoliticaReintentos`: RF-51
- `PoliticaReintentos.Intento`: RF-51
- `PoliticaReintentos.ObservadorFallo`: RF-51, RF-56
