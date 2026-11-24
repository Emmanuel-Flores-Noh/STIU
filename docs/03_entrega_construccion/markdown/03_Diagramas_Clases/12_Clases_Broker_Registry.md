# Diagrama de clases del Broker: componente registry

**Paquete:** `mx.uady.inventario.broker.registry`

Registro en memoria de los proveedores de cada servicio, su estado de salud y las operaciones internas broker.registrarProveedor, broker.actualizarProveedor y broker.removerProveedor.

<div style="page-break-after: always;"></div>

## Diagrama

![Diagrama de clases del Broker, componente registry](Clases_Broker_Registry.mmd)

<div style="page-break-after: always;"></div>

## Requisitos funcionales por clase

Requisitos del Broker en la sección 3.2 del ERS ([03_requisitos.md](../02_ERS_PF/03_requisitos.md)).

- `AdministradorRegistro`: RF-41, RF-42, RF-43, RF-49, RF-56
- `RegistroServicios`: RF-41, RF-42, RF-43, RF-44, RF-45, RF-48, RF-52
- `RegistroServicios.Salud`: RF-52
