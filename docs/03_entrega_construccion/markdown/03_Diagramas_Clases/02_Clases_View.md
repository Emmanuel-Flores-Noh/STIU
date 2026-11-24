# Diagrama de clases: componente view

**Paquete:** `mx.uady.inventario.central.view, view.comun y view.components`

Interfaz Swing del administrador: paneles, diálogos, modelos de tabla y componentes reutilizables.

<style>
@page horizontal { size: letter landscape; margin: 0.6in; }
.hoja-horizontal + h2, .hoja-horizontal + h2 + p { page: horizontal; }
.hoja-horizontal + h2 + p img { display: block; width: 100%; height: auto; max-height: 16cm; object-fit: contain; margin: 0 auto; }
</style>

<div class="hoja-horizontal"></div>

## Diagrama

![Diagrama de clases del componente view](Clases_Sistema1_View.mmd)

<div style="page-break-after: always;"></div>

## Requisitos funcionales por clase

- `CriteriosHistorialVentas`: RF-14
- `FiltroTexto`: RF-04, RF-09, RF-14
- `FiltrosMercanciaBar`: RF-09
- `Mensajes`: RF-02, RF-06, RF-07, RF-15
- `MercanciaPanel`: RF-06, RF-09, RF-11
- `MercanciaTableModel`: RF-08, RF-09
- `ProductoDialog`: RF-01, RF-02
- `ProductosPanel`: RF-01, RF-02, RF-04
- `ProductosTableModel`: RF-04
- `RegistroMercanciaForm`: RF-06, RF-07
- `VentaHistorialPanel`: RF-14
- `VentaHistorialPanel.Componentes`: RF-14
- `VentaHistorialTableModel`: RF-14
- `VentaPanel`: RF-13, RF-15
- `VentaTableModel`: RF-15
- `VentanaPrincipal`: RF-01, RF-06, RF-14, RF-15
- `QrExportador`: RF-12
- `QrExportador.Resultado`: RF-12
- `QrPreviewDialog`: RF-11, RF-12
- `QrVista`: RF-11, RF-14
- `Combos`: RF-09, RF-13, RF-14
- `Combos.RenderTexto`: RF-09, RF-13, RF-14
- `Documentos`: RF-06, RF-09, RF-14
- `ResumenTabla`: RF-09, RF-14
- `TablaFiltrable`: RF-09, RF-14, RF-15
