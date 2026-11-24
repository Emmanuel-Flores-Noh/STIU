# Diagrama de clases: componente util

**Paquete:** `mx.uady.inventario.central.util`

Configuración leída de variables de entorno, conexiones JDBC y escritura del PNG del QR.

<style>
@page horizontal { size: letter landscape; }
.hoja-horizontal + h2, .hoja-horizontal + h2 + p { page: horizontal; }
.hoja-horizontal + h2 + p img { display: block; width: 100%; height: auto; max-height: 16.5cm; object-fit: contain; margin: 0 auto; }
</style>

<div class="hoja-horizontal"></div>

## Diagrama

![Diagrama de clases del componente util](Clases_Sistema1_Util.mmd)

<div style="page-break-after: always;"></div>

## Requisitos funcionales por clase

- `ConexionBD`: RF-13
- `ConfiguracionBD`: RF-13
- `ConfiguracionServicio`: RF-16
- `QrUtil`: RF-10