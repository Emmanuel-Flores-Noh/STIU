# Diagrama de clases general: Sistema 2 (Cliente del Proveedor)

**Paquete raíz:** `mx.uady.inventario.cliente`

Relación entre los componentes del módulo `sistema-cliente`. `Main` lee la configuración de red del entorno, aplica el aspecto de la interfaz, crea el cliente TCP hacia el Broker, el servicio de ventas, la ventana y su controlador, y abre la ventana. El diagrama muestra solo la clase principal de cada componente; sus atributos y métodos están en el diagrama del componente.

Las clases del contrato (`Solicitud`, `Respuesta`, `CodigoError`) y la configuración de Gson vienen del módulo `common` y se detallan en sus propios diagramas. Se omiten las superclases de Swing (`JFrame`, `JDialog`, `JPanel`, `JScrollPane`, `AbstractTableModel`) y la de `ComunicacionException` (`Exception`).

**Notación de los diagramas:** `+` público, `-` privado, `#` protegido, `~` de paquete; los miembros subrayados son estáticos y los que están en cursiva, abstractos.
Los records tienen además un método de acceso público por cada atributo.

<div style="page-break-after: always;"></div>

## Diagrama

![Diagrama de clases general del Sistema 2](Clases_Sistema2.mmd)

<div style="page-break-after: always;"></div>

## Componentes

| Componente | Paquete | Responsabilidad |
| :---- | :---- | :---- |
| controller | `controller` | Coordina la ventana con el servicio y ejecuta las consultas en segundo plano. |
| view | `view`, `view.components` | Interfaz Swing del encargado de ventas. |
| network | `network` | Configuración de red, cliente TCP del Broker y servicio de ventas. |
| error | `error` | Clasificación de los fallos de comunicación y su traducción a mensajes. |
| model | `model` | Objetos de datos recibidos del Sistema 1 a través del Broker. |
| util | `util` | Formato de textos, fechas y precios, y mensajes de estado de la pantalla. |

## Requisitos funcionales por clase

- `Main`: RF-25, RF-39, RF-40

Las demás clases del diagrama se detallan, con sus requisitos, en el documento de su componente.
