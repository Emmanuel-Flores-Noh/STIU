# Diagrama de clases general: Broker

**Paquete:** `mx.uady.inventario.broker`

Relación entre los componentes del módulo `broker`. `Main` lee la configuración del entorno y crea el `BrokerServer`, que reúne el registro de servicios, el enrutador y la bitácora, y atiende cada conexión con un `ManejadorConexion`. El diagrama muestra solo los nombres de las clases; sus atributos y métodos están en el diagrama de cada componente. Se omiten las clases internas `RegistroServicios.Salud`, `PoliticaReintentos.Intento` y `PoliticaReintentos.ObservadorFallo`.

Para no saturar el diagrama, la bitácora y la configuración solo se enlazan con `BrokerServer`, que las crea y las reparte: `ManejadorConexion`, `AdministradorRegistro` y `Enrutador` también escriben en la misma `BitacoraBroker`, y `RegistroServicios` y `Enrutador` reciben la `ConfiguracionBroker` al construirse.

**Notación:** `+` público, `-` privado, `#` protegido, `~` de paquete; los miembros subrayados son estáticos y los que están en cursiva, abstractos. Los records tienen además un método de acceso público por cada atributo.

<div style="page-break-after: always;"></div>

## Diagrama

![Diagrama de clases general del Broker](Clases_Broker.mmd)

<div style="page-break-after: always;"></div>

## Requisitos funcionales por clase

Requisitos del Broker en la sección 3.2 del ERS ([03_requisitos.md](../02_ERS_PF/03_requisitos.md)).

- `Main`: RF-57, RF-58

Las demás clases del diagrama se detallan, con sus requisitos, en el documento de su componente.
