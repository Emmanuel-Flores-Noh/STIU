package mx.uady.inventario.cliente.model;

/**
 * Objeto de transferencia de datos (DTO) para representar a un usuario
 * con rol de proveedor en las opciones de la interfaz de ventas.
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
     * @return Los nombres del usuario.
     */
    public String getNombres() {
        return nombres;
    }
    /**
     * @return El apellido paterno.
     */
    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    /**
     *
     * @return Nombre completo
     */
    @Override
    public String toString() {
        return getNombres() + " " + getApellidoPaterno(); // ajusta según los nombres exactos de campos en UsuarioDTO
    }
}