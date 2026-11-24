# Sistema de Trazabilidad e Inventarios UADY (STIU)

Integración distribuida de dos sistemas de inventario de los almacenes de la UADY mediante el **patrón Broker**.

- **Sistema 1 (centralizado / proveedor):** registra productos y unidades, genera un código QR por unidad, registra ventas y persiste en MariaDB.
- **Sistema 2 (cliente):** aplicación de escritorio (Swing + FlatLaf) con la que un proveedor registra ventas a tiendas y consulta los QR.
- **Broker:** registra los servicios disponibles y enruta cada solicitud al proveedor correspondiente. El cliente solo conoce la dirección del Broker.

La comunicación usa Sockets TCP con mensajes JSON (Gson), atención concurrente con hilos, registro dinámico de servicios, timeouts y reintentos.

## Documentación

Se separan por etapas de desarrollo.

### [Documentación inicial](./docs/01_documentacion_inicial/)
La documentación inicial requerida para iniciar el proyecto

contiene:
* Diagrama entidad relacion
* Diagrama de interacciones
* Definicion inicial del proyecto
* Requisitos iniciales
* Casos de uso
* Definicion de tecnologias
* Contrato de interacciones

### [Entrega arquitectura](./docs/02_entrega_arquitectura/)
La documentación para la entrega de la asignatura arquitectura de software contiene lo siguiente:

* Diagrama de componentes
* Diagramas de secuencia
* Estructura y despligue del proyecto

### [Entrega construccion de software](./docs/03_entrega_construccion/)
La documentacion formal del proyecto.


## Estructura

```text
common/                 Contrato compartido (solicitudes, respuestas, códigos de error, Gson)
broker/                 Broker: registro de servicios y enrutamiento
sistema-centralizado/   Sistema 1: lógica de negocio, QR y persistencia
sistema-cliente/        Sistema 2: interfaz de escritorio
docker/mariadb/init/    Esquema y datos iniciales de la base de datos
docs/                   Documentación (Markdown, diagramas Mermaid y PDFs)
```

## Tecnologías

Java 21 · Maven multimódulo · Gson · Swing + FlatLaf · ZXing · MariaDB (JDBC) · Docker Compose

## Ejecución

Requisitos: Docker y Docker Compose.

```bash
cp .env.example .env    # definir DB_ROOT_PASSWORD y DB_PASSWORD
docker compose up --build
```

| Componente | Puerto |
|---|---|
| MariaDB | 3306 |
| Broker | 5000 |
| Sistema 1 | 6000 |

**Zona horaria.** Todos los contenedores usan la variable `TZ` (por defecto `America/Merida`), de modo que la bitácora del Broker, `fecha_venta` y la `fechaHora` de los mensajes muestran la hora local. Para cambiarla, define `TZ` en `.env` con un identificador IANA (por ejemplo `TZ=America/Mexico_City`). Las interfaces que se ejecutan fuera de Docker (Sistema 2 y la interfaz del Sistema 1 desde el IDE) usan la zona horaria del equipo.

El **Sistema 2** se ejecuta desde el IDE o como jar; también puede levantarse en Docker con un servidor X11:

```bash
docker compose --profile client up --build
```

## Pruebas

```bash
mvn test
```
