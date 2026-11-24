# Diagrama de clases general: Common

**Paquete:** `mx.uady.inventario.common`

Relación entre los componentes del módulo `common`, la biblioteca que comparten el Sistema 1, el Sistema 2 y el Broker: el contrato de mensajes (`Solicitud` y `Respuesta`), el catálogo de códigos de error, la configuración de Gson, los nombres de servicios y operaciones, y los modelos del dominio. El diagrama muestra solo los nombres de las clases; sus atributos y métodos están en el diagrama de cada componente.

`Servicios` y `Operaciones` solo contienen constantes: su relación con `Solicitud` indica los valores que toman `nombreServicio` y `nombreOperacion`. Las relaciones entre los modelos son por identificador (por ejemplo, `Mercancia.idProducto`), no por referencia a objeto, por eso se dibujan como dependencias.

**Notación:** `+` público, `-` privado, `#` protegido, `~` de paquete; los miembros subrayados son estáticos y los que están en cursiva, abstractos. Los records tienen además un método de acceso público por cada atributo.

<div style="page-break-after: always;"></div>

## Diagrama

![Diagrama de clases general de Common](Clases_Common.mmd)

<div style="page-break-after: always;"></div>

## Componentes

| Componente | Paquete | Responsabilidad |
| :---- | :---- | :---- |
| contrato | `contrato` | Mensajes de solicitud y respuesta entre cliente, Broker y proveedor. |
| error | `error` | Catálogo de códigos de error del contrato. |
| json | `json` | Instancia de Gson compartida y formato ISO-8601 de las fechas. |
| servicio | `servicio` | Nombres de los servicios y de las operaciones. |
| modelo | `modelo` | Modelos del dominio que corresponden a las tablas de MariaDB. |

## Requisitos funcionales por clase

Las clases del diagrama se detallan, con sus requisitos, en el documento de su componente.
