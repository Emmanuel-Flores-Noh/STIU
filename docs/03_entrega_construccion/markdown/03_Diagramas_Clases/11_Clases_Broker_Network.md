# Diagrama de clases del Broker: componente network

**Paquete:** `mx.uady.inventario.broker.network`

Servidor TCP del Broker: acepta conexiones con un pool de hilos y atiende cada solicitud JSON de la conexión.

<div style="page-break-after: always;"></div>

## Diagrama

![Diagrama de clases del Broker, componente network](Clases_Broker_Network.mmd)

<div style="page-break-after: always;"></div>

## Requisitos funcionales por clase

Requisitos del Broker en la sección 3.2 del ERS ([03_requisitos.md](../02_ERS_PF/03_requisitos.md)).

- `BrokerServer`: RF-44, RF-54, RF-58
- `ManejadorConexion`: RF-41, RF-42, RF-43, RF-45, RF-46, RF-47, RF-53, RF-55
