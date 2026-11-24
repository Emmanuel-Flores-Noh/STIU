# Diagrama de clases general: Sistema 1 (Centralizado / Proveedor)

**Paquete raíz:** `mx.uady.inventario.central`

Relación entre los componentes del módulo `sistema-centralizado`. `Main` lee la configuración del entorno, crea los servicios sobre MariaDB (`AppContext`), inicia el servidor TCP y el registro en el Broker, y abre la interfaz Swing. El diagrama muestra solo la clase principal de cada componente; sus atributos y métodos están en el diagrama del componente.

Para no saturar el diagrama, se omite el acceso de `ProductoService`, `MercanciaService` y `ConsultasVentaService` a MariaDB: sus implementaciones JDBC lo hacen mediante `Transacciones` y `ProveedorConexion` (ver el diagrama del componente service).

**Notación de los diagramas:** `+` público, `-` privado, `#` protegido, `~` de paquete; los miembros subrayados son estáticos y los que están en cursiva, abstractos.
Los records tienen además un método de acceso público por cada atributo.

<div style="page-break-after: always;"></div>

## Diagrama

![Diagrama de clases general del Sistema 1](Clases_Sistema1.mmd)

<div style="page-break-after: always;"></div>

## Componentes

| Componente | Paquete | Responsabilidad |
| :---- | :---- | :---- |
| controller | `controller` | Arma la interfaz y conecta las pantallas con los servicios. |
| view | `view`, `view.comun`, `view.components` | Interfaz Swing del administrador. |
| network | `network` | Servidor TCP para las solicitudes del Broker y registro en el Broker. |
| service | `service`, `service.jdbc` | Lógica de negocio, códigos QR, validaciones y transacciones. |
| repository | `repository` | Acceso a MariaDB con JDBC. |
| model | `model` | Objetos de datos propios del Sistema 1. |
| util | `util` | Configuración, conexiones JDBC y escritura del PNG del QR. |

## Requisitos funcionales por clase

- `Main`: RF-16 (inicia el registro en el Broker) y RF-17 a RF-24 (inicia el servidor TCP)

Las demás clases del diagrama se detallan, con sus requisitos, en el documento de su componente.