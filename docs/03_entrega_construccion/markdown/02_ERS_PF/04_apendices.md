# **4\. Apéndices***.*

1. **Contrato de mensajes JSON**

| {"idSolicitud": "ev-04", "nombreServicio": "gestorTiendas", "nombreOperacion": "actualizarTiendaVenta", "parametros": {"numero\_serie": "SN-UADY-0003", "id\_tienda": 1, "id\_usuario": 4}, "fechaHora": "..."} {"idSolicitud": "ev-04", "estado": "EXITO", "resultado": {"numero\_serie": "SN-UADY-0003", "id\_tienda": 1, "fecha\_venta": "..."}, "fechaHora": "..."} |
| :---- |

2. **Lista de operaciones por servicio**

***Broker:*** registrarProveedor, actualizarProveedor, removerProveedor.  
***gestorTiendas:*** listarTiendas, listarUsuarios, actualizarTiendaVenta.  
***inventario:*** listarInventario, obtenerQr  
***gestorPrecios:*** listarPreciosProducto

3. **Catálogo de errores**

| Código | Nombre | Descripción |
| :---- | :---- | :---- |
| E001 | SERVICIO\_DESCONOCIDO | El servicio solicitado no está registrado en el Broker. |
| E002 | OPERACION\_NO\_SOPORTADA | El servicio no ofrece la operación solicitada. |
| E003 | PARAMETROS\_INVALIDOS | Faltan parámetros, tienen un formato invalido o violan una regla de negocio. |
| E004 | PROVEEDOR\_NO\_REGISTRADO | No hay un proveedor disponible para el servicio. |
| E005 | TIMEOUT | Se agotó el tiempo de espera del proveedor. |
| E006 | RECURSO\_NO\_ENCONTRADO | El recurso solicitado no existe. |
| E999 | ERROR\_INTERNO | Error inesperado en el proveedor. |

4. **Diagrama entidad relación**

![Base de datos](BaseDeDatos.mmd)

5. **Casos de uso**

**Caso de Uso 1: Registrar Producto y Generar QR**  
**Actor**: Administrador del Almacén (Sistema 1 \- Centralizado)  
**Descripción**: Permite registrar la información detallada de los productos y generar un código QR que permita dar seguimiento a su trazabilidad.  
**Flujo principal**:

* Se ingresa la información del producto.  
* El Sistema 1 registra y almacena la información detallada.  
* Se genera un código QR vinculado al producto.

**Caso de Uso 2: Actualizar Tienda de Venta**  
**Actor**: Proveedor (Sistema 2 \- Cliente)  
**Descripción**: Permite registrar a qué tienda específica se le vendió un producto  
mediante el envío de una solicitud.  
**Flujo principal**:

* El Sistema 2 construye una solicitud estructurada en JSON.  
* El Sistema 2 envía la solicitud al Broker a través de Sockets TCP.  
* El Sistema 2 recibe la respuesta enviada por el Broker y la presenta al usuario.

**Caso de Uso 3: Procesamiento y Vinculación de Venta**  
**Actor**: Sistema Centralizado (Sistema 1 \- Proveedor)  
**Descripción**: El Sistema 1 procesa la actualización de la tienda y la vincula con el código QR correspondiente.  
**Flujo principal**:

* El Sistema 1 recibe la solicitud a través de su interfaz común de servicio.  
* Procesa la actualización y vincula la venta al código QR.  
* Genera una respuesta incluyendo estado, resultado, fecha y hora.  
* Retorna la respuesta al Broker.

**Caso de Uso 4: Intermediación de Solicitudes y Enrutamiento**  
**Actor**: Broker  
**Descripción**: El Broker recibe peticiones del Sistema 2, identifica la operación y la enruta al Sistema 1 sin que los sistemas se conozcan directamente.  
**Flujo principal**:

* El Broker recibe la solicitud del Sistema 2\.  
* Identifica la operación y busca al proveedor adecuado en el registro de servicios.  
* Reenvía la solicitud al Sistema 1\.  
* Recibe el resultado y se lo devuelve al Sistema 2 original.  
* Gestiona posibles errores como servicios desconocidos o parámetros inválidos.

**Caso de Uso 5: Gestión de Registro de Servicios**  
**Actor**: Broker y Proveedores  
**Descripción**: Mantenimiento del registro dinámico de proveedores para poder sustituirlos sin afectar al cliente.  
**Flujo principal:**

* Los proveedores (como el Sistema 1\) se registran asociando un nombre de servicio con su instancia.  
* El Broker mantiene esta lista actualizada y permite localizar al proveedor correcto durante una solicitud