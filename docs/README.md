# Los documentos van en esta carpeta

Se separan por etapas de desarrollo.

## [Documentación inicial](./01_documentacion_inicial/)
La documentación inicial requerida para iniciar el proyecto

contiene:
* Diagrama entidad relacion
* Diagrama de interacciones
* Definicion inicial del proyecto
* Requisitos iniciales
* Casos de uso
* Definicion de tecnologias
* Contrato de interacciones

## [Entrega arquitectura](./02_entrega_arquitectura/)
La documentación para la entrega de la asignatura arquitectura de software contiene lo siguiente:

* Diagrama de componentes
* Diagramas de secuencia
* Estructura y despligue del proyecto

## [Entrega construccion de software](./03_entrega_construccion/)
La documentacion formal del proyecto.

# Estructura de cada subcarpeta
Aqui se lista la estructura que sigue cada subcarpeta de este directorio y como funciona.

## diagramas
Todos los diagramas en formato mermaid van en esta carpeta.

## markdown
Todos los documentos en formato markdown van en esta carpeta.

Cada subdirectorio es un documento. 

El nombre de la carpeta define el nombre del documento de salida pdf, dentro de esa carpeta tambien se incluiran todos los elementos necesarios para el markdown excepto los diagramas

Para incrustar un diagrama se puede hacer con la sintaxis de imagen de markdown

```markdown
![nombre por si no se encuentra](nombreDiagrama.mmd)
```

La herramienta se encarga de compilar el markdown a formato pdf y de incrustar los diagramas como imagenes.

Si se desea, se pueden utilizar varios documentos markdown para simular saltos de pagina.

### Fórmulas matemáticas en LaTeX
Puedes incluir fórmulas matemáticas directamente en el Markdown:
* **En línea (inline):** usando `$ ... $` o `\( ... \)`. Ejemplo: `$E = mc^2$`
* **En bloque (display):** usando `$$ ... $$`, `\[ ... \]`, o bloques de código ```math. Ejemplo:
  ```latex
  $$
  \int_{-\infty}^{\infty} e^{-x^2} dx = \sqrt{\pi}
  $$
  ```
Las fórmulas se compilan a gráficos vectoriales SVG incrustados con resolución nítida.

### Convencion de documentos
* Si solo se tiene un archivo markdown dentro de un subdirectorio se debe de llamar README.md
* Si se poseen varios archivos markdown se debe de llamar $numeroDeDocumento$_$nombreDescriptivo$.md siendo numeroDeDocumento el numero que define en que orden se insertan, preferente nombrarlo 01, 02, etc.

### Documento de demostración
En `markdown/Demo_Funcionalidades/` hay un documento de ejemplo que reúne las
funcionalidades del proyecto: Markdown, tablas, código, imágenes locales,
fórmulas LaTeX y diagramas Mermaid. También incluye una lista de comprobación
para usarlo como prueba después de modificar el compilador.

## out
Los documentos y diagramas convertidos en formatos de facil lectura.

### diagramas
En esta se guardaran los diagramas en dos carpetas segun su formato

* png
* pdf

## extras
En este directorio pueden ir otros documentos externos al proyecto, por ejemplo:
* documentaciones
* imagenes / diagramas de referencia


