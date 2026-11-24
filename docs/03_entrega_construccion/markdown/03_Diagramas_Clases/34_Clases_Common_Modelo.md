# Diagrama de clases de Common: componente modelo

**Paquete:** `mx.uady.inventario.common.modelo`

Modelos del dominio que corresponden a las tablas de MariaDB.

<div style="page-break-after: always;"></div>

## Diagrama

![Diagrama de clases de Common, componente modelo](Clases_Common_Modelo.mmd)

<div style="page-break-after: always;"></div>

## Requisitos funcionales por clase

El módulo `common` no tiene requisitos propios: cada clase se asocia con los requisitos del Sistema 1, el Sistema 2 o el Broker que dependen de ella.

- `EstadoMercancia`: RF-08
- `HistorialPrecio`: RF-03, RF-05, RF-23
- `Mercancia`: RF-06, RF-08, RF-09, RF-21
- `Producto`: RF-01, RF-02, RF-04
- `Rol`: RF-20
- `TiendaAlmacen`: RF-13, RF-19
- `Usuario`: RF-13, RF-20
- `Venta`: RF-15, RF-17
