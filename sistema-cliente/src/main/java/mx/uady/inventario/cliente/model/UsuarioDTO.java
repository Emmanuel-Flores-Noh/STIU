package mx.uady.inventario.cliente.model;

/**
 * Objeto de transferencia de datos (DTO) para representar a un usuario
 * con rol de proveedor en las opciones de la interfaz de ventas.
 *
 * <p>Trazabilidad con Requisitos:
 * <ul>
 *   <li><b>RF-25:</b> Carga de la lista de encargados de ventas al abrir la ventana.</li>
 *   <li><b>RF-29:</b> Despliegue de usuarios proveedores en lista desplegable mostrando nombre y apellido paterno.</li>
 *   <li><b>RF-31:</b> Validación de selección de encargado de ventas para habilitar el registro.</li>
 *   <li><b>RF-32:</b> Registro de la venta vinculado al usuario que la opera.</li>
 * </ul>
 * </p>
 */
public class UsuarioDTO {
    private Long idUsuario;
    private String nombres;
    private String apellidoPaterno;

    /**
     * Constructor
     * @param idUsuario ID
     * @param nombres Nombres
     * @param apellidoPaterno Apellido paterno
     */
    public UsuarioDTO(Long idUsuario, String nombres, String apellidoPaterno) {
        this.idUsuario = idUsuario;
        this.nombres = nombres;
        this.apellidoPaterno = apellidoPaterno;
    }

    /**
     * Obtiene el identificador único del usuario.
     * 
     * @return El ID del usuario.
     */
    public Long getIdUsuario() {
        return idUsuario;
    }

    /**
     * Obtiene los nombres del usuario.
     * 
     * @return Los nombres del usuario.
     */
    public String getNombres() {
        return nombres;
    }
    /**
     * Obtiene el apellido paterno del usuario.
     * 
     * @return El apellido paterno.
     */
    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    /**
     * Representación en cadena para mostrar el usuario amigablemente en el componente {@link javax.swing.JComboBox}.
     * 
     * @return Nombre completo formateado del proveedor.
     */
    @Override
    public String toString() {
        String nombreUser = getNombres() != null ? getNombres() : "";
        String apellidoUser = getApellidoPaterno() != null ? getApellidoPaterno() : "";
        return (nombreUser + " " + apellidoUser).trim();
    }
}