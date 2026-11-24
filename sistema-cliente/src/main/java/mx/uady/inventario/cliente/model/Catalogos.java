package mx.uady.inventario.cliente.model;


import java.util.List;

/**
 * Listas que se cargan al abrir la ventana o al actualizar.
 *
 * <p>Requisito: RF-25, RNF-06.</p>
 * @param tiendas   tiendas destino, en el orden del Sistema 1
 * @param usuarios  usuarios con rol de proveedor
 * @param mercancia unidades disponibles «En almacén»
 */
public record Catalogos(List<TiendaDTO> tiendas, List<UsuarioDTO> usuarios, List<MercanciaDTO> mercancia) {
}