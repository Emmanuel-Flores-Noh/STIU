# Diagrama de clases: componente network

**Paquete:** `mx.uady.inventario.central.network`

Servidor TCP que atiende las solicitudes JSON que llegan desde el Broker y registro del sistema en el Broker.

<style>
@page horizontal { size: letter landscape; }
.hoja-horizontal + h2, .hoja-horizontal + h2 + p { page: horizontal; }
.hoja-horizontal + h2 + p img { display: block; width: 100%; height: auto; max-height: 16.5cm; object-fit: contain; margin: 0 auto; }
</style>

<div class="hoja-horizontal"></div>

## Diagrama

![Diagrama de clases del componente network](Clases_Sistema1_Network.mmd)

<div style="page-break-after: always;"></div>

## Requisitos funcionales por clase

Requisitos de la sección 3.2 del ERS ([03_requisitos.md](../02_ERS_PF/03_requisitos.md)).

- `ActualizarTiendaVentaManejador`: RF-17, RF-18
- `CatalogoOperaciones`: RF-17, RF-19, RF-20, RF-21, RF-22, RF-23
- `DespachadorSolicitudes`: RF-17, RF-19, RF-20, RF-21, RF-22, RF-23, RF-24
- `JsonServicio`: RF-24
- `ListarInventarioManejador`: RF-21
- `ListarPreciosProductoManejador`: RF-23
- `ListarTiendasManejador`: RF-19
- `ListarUsuariosManejador`: RF-20
- `ManejadorConexionServicio`: RF-24
- `ManejadorOperacion`: RF-17, RF-19, RF-20, RF-21, RF-22, RF-23
- `NombresProducto`: RF-21, RF-22
- `ObtenerQrManejador`: RF-22
- `ParametrosSolicitud`: RF-22, RF-23, RF-24
- `RegistradorBroker`: RF-16
- `ServidorServicio`: RF-17, RF-19, RF-20, RF-21, RF-22, RF-23, RF-24