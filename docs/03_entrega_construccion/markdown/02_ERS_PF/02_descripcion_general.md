# **2\. Descripción general**

## **2.1 Perspectiva del producto**

El sistema se da como solución distribuida compuesta por 3 subsistemas independientes que se comunican por red: 

* Sistema 1 (sistema centralizado) que cuenta con una interfaz gráfica o modo servicio para administradores.   
* Sistema 2 (Cliente \- Proveedor) con interfaz gráfica  
* Intermediario Broker sin interfaz gráfica. 

Todos los subsistemas se organizan en contenedores aislados con Docker.

![Bloques](Bloques.mmd)

## **2.2 Funciones del producto**

* Registro y almacenamiento centralizado de productos e inventario.  
* Generación automática de códigos QR con información para la trazabilidad de los productos  
* Solicitud y actualización remota de las tiendas de destino de la mercancía mediante sockets y mensajes json  
* Broker como intermediario con soporte concurrente de hilos y enrutamiento dinámico de peticiones.

## **2.3 Características de los usuarios**

| Tipo de usuario | Experiencia técnica | Necesidades principales |
| :---- | :---- | :---- |
| Administrador de almacén (Sistema 1\) | Básica: uso de aplicaciones de escritorio con tablas y formularios. | Dar de alta productos y unidades, imprimir o consultar QR, registrar ventas y revisar el historial de ventas y de precios. |
| Proveedor (Sistema 2\) | Básica: uso de aplicaciones de escritorio. | Elegir una unidad disponible, la tienda destino y su usuario, registrar la venta, ver el QR y recibir una confirmación o un error comprensible. |
| Operador / desarrollador | Avanzada: Docker, variables de entorno, Maven y lectura de bitácoras. | Desplegar el sistema, configurar puertos y timeouts, sustituir proveedores y diagnosticar fallas mediante la bitácora. |

## **2.4 Restricciones**

* **Lenguaje:** Obligatoriamente desarrollo en Java para todos los componentes  
* **Arquitectura:** Patrón broker con cliente, intermediario y proveedor en procesos distintos.  
* **Comunicación:** Uso estricto de sockets TCP e intercambio de datos Json con Gson  
* **Persistencia:** Base de datos relacional MariaDB conectada usando JDBC  
* **QR:** generar imágenes QR por medio de la biblioteca ZXing  
* **Interfaz gráfica:** Uso de Java Swing con estilos en FlatLaf para la interfaz gráfica del cliente.  
* **Despliegue:** Uso de Docker y Docker Compose, las credenciales se definen en un .env que no se versiona.  
* **Seguridad**: el cliente nunca recibe la ubicación del proveedor ni la ruta interna de los QR.

## **2.5 Suposiciones y dependencias**

* Docker y Docker Compose instalados en el equipo de despliegue.  
* Puertos 3306 (MariaDB), 5000 (Broker) y 6000 (Sistema 1\) libres.  
* JDK 21 disponible para compilar y ejecutar el sistema 2 fuera de docker.  
* Una pantalla disponible para renderizar la interfaz gráfica del sistema 2\.  
* El Broker está en ejecución previo al Sistema 1\.   
* Los catálogos de roles, usuarios y tiendas se cargan directamente desde el archivo de inicialización de la base de datos.  
* Las tres partes comparten la misma versión del módulo common.

## **2.6 Requisitos futuros**

* Replicar el Broker y balancear la carga entre varios proveedores de un mismo servicio.  
* Autenticación de usuarios y control de acceso por rol.  
* Registro de usuarios por medio del sistema de la interfaz gráfica del administrador
