package mx.uady.inventario.central.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import mx.uady.inventario.common.modelo.Usuario;

/** Pruebas de {@link ListarUsuariosManejador}: operación {@code listarUsuarios} (RF-20). */
@Tag("RF-20")
class ListarUsuariosManejadorTest {

    @Test
    @DisplayName("RF-20: devuelve cada usuario con identificador, rol, nombres y apellidos")
    void usuariosProveedores_devuelveCadaUsuarioConSusCampos() {
        ConsultasVentaFalsa consultas = ConsultasVentaFalsa.con(List.of(),
                List.of(new Usuario(2, 2, "Carlos", "Mendez", "Ruiz")));

        JsonArray resultado = new ListarUsuariosManejador(consultas).ejecutar(new JsonObject()).getAsJsonArray();

        assertEquals(1, resultado.size());
        JsonObject usuario = resultado.get(0).getAsJsonObject();
        assertEquals(2, usuario.get("idUsuario").getAsInt());
        assertEquals(2, usuario.get("idRol").getAsInt());
        assertEquals("Carlos", usuario.get("nombres").getAsString());
        assertEquals("Mendez", usuario.get("apellidoPaterno").getAsString());
        assertEquals("Ruiz", usuario.get("apellidoMaterno").getAsString());
    }

    @Test
    @DisplayName("RF-20: solo incluye usuarios con rol Proveedor, no al administrador")
    void listaDeProveedores_excluyeAlAdministrador() {
        Usuario proveedor = new Usuario(4, 2, "Emmanuel", "Flores", "Noh");
        ConsultasVentaFalsa consultas = ConsultasVentaFalsa.con(List.of(), List.of(proveedor));

        JsonArray resultado = new ListarUsuariosManejador(consultas).ejecutar(new JsonObject()).getAsJsonArray();

        assertEquals(1, resultado.size(), "el administrador no debe aparecer");
        assertEquals(4, resultado.get(0).getAsJsonObject().get("idUsuario").getAsInt());
    }

    @Test
    @DisplayName("RF-20: si ningún usuario tiene rol Proveedor devuelve un arreglo vacío")
    void ningunProveedor_devuelveArregloVacio() {
        // Solo existe el administrador (lo agrega ConsultasVentaFalsa a la lista completa de usuarios).
        ConsultasVentaFalsa consultas = ConsultasVentaFalsa.con(List.of(), List.of());

        JsonArray resultado = new ListarUsuariosManejador(consultas).ejecutar(new JsonObject()).getAsJsonArray();

        assertTrue(resultado.isEmpty());
    }

    @Test
    @Tag("RF-24")
    @DisplayName("RF-24: si la consulta falla, la excepción llega al despachador (ERROR_INTERNO)")
    void servicioFalla_propagaLaExcepcion() {
        IllegalStateException fallo = new IllegalStateException("No se pudo consultar los usuarios");
        ListarUsuariosManejador manejador = new ListarUsuariosManejador(ConsultasVentaFalsa.queFalla(fallo));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> manejador.ejecutar(new JsonObject()));
        assertSame(fallo, error);
    }
}
