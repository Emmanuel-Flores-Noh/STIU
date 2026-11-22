
# **Especificación de Requisitos de Software**

Basada en el formato IEEE 830 · Proyecto Final

| Nombre del proyecto | STIU \- Sistema de Trazabilidad e Inventario Uady |
| :---- | :---- |
| **Equipo** | 2 |
| **Integrantes** | De lázaro Lara Mauricio Antonio Dzib Chan Gioser Andre Canché Góngora Juan Angel Flores Noh Emmanuel Adrian May Paat Christopher |
| **Docente** | Daniel Fernando Baas Colonia |
| **Fecha de entrega** | 19/11/26 |
| **Versión** | 1.0 |

# **Control de versiones**

| Versión | Fecha | Autor | Cambios |
| :---- | :---- | :---- | :---- |
| 0.1 | 07/11/2026 | Equipo de desarrollo | Diseño de sistema inicial: requisitos, casos de uso, arquitectura, base de datos y contrato de interacciones. |
| 1.0 | 19/11/2026 | Equipo de desarrollo | Especificación completa en formato IEEE 830 alineada con la implementación y las pruebas. |

# **1\. Introducción**

## **1.1 Propósito**

Este documento describe formalmente los requisitos funcionales y no funcionales del desarrollo del sistema STIU (Sistema de Trazabilidad e Inventarios UADY), un sistema distribuido que integra un sistema centralizado de inventario con una aplicación de proveedores mediante el patrón arquitectónico Broker.

Este documento va dirigido a:

* **El equipo de desarrollo:** Como referencia para implementar, probar y mantener los tres componentes (Sistema 1, Sistema 2 y Broker)  
* **Docente Daniel Fernando Baas Colonia:** Para evaluar que el sistema cumple con el alcance de la propuesta y la calidad del software desarrollado.  
* **Evaluadores:** Para que puedan rastrear cada requisito desde su origen hasta la prueba que lo verifica.

## **1.2 Ámbito del sistema**

**Nombre del sistema:** Sistema de Trazabilidad e Inventarios UADY

**Lo que el sistema hará:**

* Registrar productos y cada unidad física (mercancía) identificada por su número de serie, y generar un código QR por unidad con el nombre del producto, su ID y su número de serie.  
* Permitir que un proveedor registre, desde una aplicación de escritorio, a qué tienda o almacén de la UADY se vendió una unidad, y consultar su código QR.  
* Intermediar toda la comunicación entre el cliente (Sistema 2\) y el proveedor (Sistema 1\) mediante un Broker independiente, que registra servicios dinámicamente y enruta las solicitudes por Sockets TCP con mensajes JSON.  
* Atender solicitudes concurrentes, manejar timeouts, desconexiones y reintentos limitados, y responder errores estructurados.  
* Mantener el historial de precios de cada producto y el historial de ventas.  
* Permitir que el administrador registre también desde el Sistema 1 la venta de una unidad (RF-15), para cuando el Sistema 2 no esté disponible. Esta función ya existe en el código base (panel VentaPanel, mencionado en la sección Equipo y roles de la propuesta) y aplica la misma validación y la misma transacción que las ventas recibidas por el Broker (RF-17), por lo que no agrega reglas de negocio nuevas.

**Lo que el sistema no hará:**

* No gestiona autenticación ni contraseñas de usuarios, el usuario proveedor se elige de un catálogo.  
* No procesa pagos, facturación y logística de envío.  
* No ofrece interfaz web ni móvil, las interfaces gráficas son aplicaciones de escritorio.  
* No replica el Broker ni persiste su registro de servicios.

**Objetivos y beneficios:**

* **Trazabilidad:** Conocer en todo momento si una unidad está en almacén o vendida, y a que tienda.  
* **Desacoplamiento:** El cliente solo conoce la dirección del Broker, el proveedor puede sustituirse sin modificar ni reiniciar el cliente.  
* **Consistencia:** Una unidad no puede venderse dos veces, aun con clientes concurrentes.  
* **Reproducibilidad:** El sistema completo se despliega con Docker Compose en cualquier equipo.

El alcance del proyecto se declaró en la propuesta del proyecto (REF-01).

## **1.3 Definiciones, acrónimos y abreviaturas**

| Término | Definición |
| :---- | :---- |
| Broker | Patrón de arquitectura que coordina la comunicación entre componentes desacoplados |
| Sistema 1 | Sistema centralizado (Proveedor de servicios): Aplica la lógica de negocio, generación de QR y persistencia de datos. |
| Sistema 2 | Aplicación cliente del proveedor, usada para registrar ventas y consultar QR. |
| Proveedor (de servicio) | Proceso que se registra en el Broker y ejecuta operaciones (En el caso de este proyecto, el sistema 1). |
| Servicio | Agrupación de operaciones con un nombre (gestorTiendas, inventario, gestorPrecios, broker) |
| Operación | Acción concreta dentro de un servicio (Por ejemplo, operacion actualizarTiendaVenta dentro del servicio gestorTiendas). |
| Mercancía | Unidad física individual de un producto identificada por su número de serie. |
| QR | Imagen PNG que identifica a una unidad. |
| Marshalling / Unmarshalling | Proceso de conversión de objetos de memoria a json y viceversa. |
| JSON | JavaScript Object Notation, formato de intercambio de mensajes. |
| TCP | Protocolo de Control de Transmisión para la comunicación fiable mediante sockets. |
| JDBC | Java Database Connectivity, API de acceso a MariaDB. |
| MVC | Patrón Modelo-Vista-Controlador |
| Timeout | Tiempo máximo de espera para establecer conexión o leer una respuesta. |
| Bitácora  | Registro en consola de cada solicitud y cada cambio del registro de servicios. |

## **1.4 Referencias**

| ID | Documento | Fuente o fecha |
| :---- | :---- | :---- |
| REF-01 | Documento de propuesta del proyecto de inventarios UADY | 17/11/26 |
| REF-02 | Diseño inicial del sistema de inventarios UADY | 12/11/26 Recuperado de: https\://github.com/Emmanuel-Flores-Noh/Inventario-Almacenes-UADY/blob/main/docs/01\_documentacion\_inicial/out/docs/DiseñoDeSistema.pdf |
| REF-03 | Documento técnico de proyecto | 14/11/26 Recuperado de: [https\://github.com/Emmanuel-Flores-Noh/Inventario-Almacenes-UADY/blob/main/docs/02\_documentacion\_final/out/docs/05\_documento\_tecnico\_final.pdf](https://github.com/Emmanuel-Flores-Noh/Inventario-Almacenes-UADY/blob/main/docs/02_documentacion_final/out/docs/05_documento_tecnico_final.pdf)  |

## **1.5 Visión general del documento**

Existen 3 secciones principales: 

* **Sección 2 (Descripción general):** contexto del producto, funciones principales, tipos de usuario, restricciones, suposiciones y requisitos futuros.  
* **Sección 3 (Requisitos específicos):** interfaces externas, requisitos funcionales (RF) con prioridad y origen, requisitos de rendimiento, restricciones de diseño y atributos de calidad (RNF) con su criterio de verificación.  
* **Sección 4 (Apéndices):** contrato JSON, catálogo de errores, modelo de datos y matriz de trazabilidad entre requisitos y pruebas.