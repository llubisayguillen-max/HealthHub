package dll;

import bll.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ControllerAdministrador {

    private final Usuario admin;

    public ControllerAdministrador(Usuario admin) {
        if (admin == null || !"Administrador".equalsIgnoreCase(admin.getRol())) {
            throw new SecurityException("Acceso denegado: solo el usuario Administrador puede gestionar roles y permisos.");
        }
        this.admin = admin;
    }

    //validaciones auxiliares-------------------------

    private boolean existeUsuario(String usuario) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE usuario_login=?";
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error verificando existencia de usuario", e);
        }
    }

    private boolean existeMatricula(String matricula) {
        String sql = "SELECT COUNT(*) FROM medicos WHERE matricula=?";
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error verificando existencia de matrícula", e);
        }
    }

    private void validarNoVacio(String campo, String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede estar vacío.");
        }
    }

    //REGISTRAR PACIENTE -------------------------

    public void registrarPaciente(String usuario, String nombre, String apellido, String contrasenia,
                                  int nroContrato, String obraSocial) {

        // Validaciones de campos
        validarNoVacio("Usuario", usuario);
        validarNoVacio("Nombre", nombre);
        validarNoVacio("Apellido", apellido);
        validarNoVacio("Contraseña", contrasenia);
        validarNoVacio("Obra social", obraSocial);

        if (nroContrato <= 0)
            throw new IllegalArgumentException("El número de contrato debe ser mayor que cero.");

        if (existeUsuario(usuario))
            throw new IllegalArgumentException("El usuario '" + usuario + "' ya existe.");

        String sqlUsuario = "INSERT INTO usuarios(usuario_login, contrasenia, nombre, apellido, rol) " +
                "VALUES (?, ?, ?, ?, 'Paciente')";
        String sqlPaciente = "INSERT INTO pacientes(id_usuario, nro_contrato, obra_social) VALUES (?, ?, ?)";

        try (Connection c = Conexion.getInstance().getConnection()) {
            c.setAutoCommit(false);

            String passEncriptada = Encriptador.encriptar(contrasenia);
            long idUsuario;

            try (PreparedStatement psUser = c.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, usuario);
                psUser.setString(2, passEncriptada);
                psUser.setString(3, nombre);
                psUser.setString(4, apellido);
                psUser.executeUpdate();

                try (ResultSet keys = psUser.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No se pudo obtener id_usuario");
                    idUsuario = keys.getLong(1);
                }
            }

            try (PreparedStatement psPac = c.prepareStatement(sqlPaciente)) {
                psPac.setLong(1, idUsuario);
                psPac.setInt(2, nroContrato);
                psPac.setString(3, obraSocial);
                psPac.executeUpdate();
            }

            c.commit();
            c.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Error registrando paciente: " + e.getMessage(), e);
        }
    }

    //MODIFICAR PACIENTE -------------------------

    public void modificarPaciente(String usuario, String nombre, String apellido, String contrasenia,
                                  int nroContrato, String obraSocial) {

        if (!existeUsuario(usuario))
            throw new IllegalArgumentException("El usuario '" + usuario + "' no existe.");

        validarNoVacio("Nombre", nombre);
        validarNoVacio("Apellido", apellido);
        validarNoVacio("Contraseña", contrasenia);
        validarNoVacio("Obra social", obraSocial);

        if (nroContrato <= 0)
            throw new IllegalArgumentException("El número de contrato debe ser mayor que cero.");

        String sqlUsuario = "UPDATE usuarios SET nombre=?, apellido=?, contrasenia=? WHERE usuario_login=?";
        String sqlPaciente = "UPDATE pacientes SET nro_contrato=?, obra_social=? " +
                "WHERE id_usuario=(SELECT id_usuario FROM usuarios WHERE usuario_login=?)";

        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement psUser = c.prepareStatement(sqlUsuario);
             PreparedStatement psPac = c.prepareStatement(sqlPaciente)) {

            String passEncriptada = Encriptador.encriptar(contrasenia);

            psUser.setString(1, nombre);
            psUser.setString(2, apellido);
            psUser.setString(3, passEncriptada);
            psUser.setString(4, usuario);
            psUser.executeUpdate();

            psPac.setInt(1, nroContrato);
            psPac.setString(2, obraSocial);
            psPac.setString(3, usuario);
            psPac.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error modificando paciente", e);
        }
    }

    //DAR DE BAJA PACIENTE -------------------------

    public void eliminarPaciente(String usuario) {
        if (!existeUsuario(usuario))
            throw new IllegalArgumentException("El paciente '" + usuario + "' no existe.");

        eliminarUsuario(usuario);
    }

    //REGISTRAR MÉDICO -------------------------

    public void registrarMedico(String usuario, String nombre, String apellido, String contrasenia,
                                String matricula, String especialidad) {

        validarNoVacio("Usuario", usuario);
        validarNoVacio("Nombre", nombre);
        validarNoVacio("Apellido", apellido);
        validarNoVacio("Contraseña", contrasenia);
        validarNoVacio("Especialidad", especialidad);
        validarNoVacio("Matrícula", matricula);

        if (!matricula.matches("\\d+"))
            throw new IllegalArgumentException("La matrícula debe contener solo números.");

        if (existeUsuario(usuario))
            throw new IllegalArgumentException("El usuario '" + usuario + "' ya existe.");

        if (existeMatricula(matricula))
            throw new IllegalArgumentException("La matrícula '" + matricula + "' ya está registrada.");

        String sqlUsuario = "INSERT INTO usuarios(usuario_login, contrasenia, nombre, apellido, rol) VALUES (?, ?, ?, ?, 'Medico')";
        String sqlMedico = "INSERT INTO medicos(id_usuario, matricula, especialidad) VALUES (?, ?, ?)";

        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement psUser = c.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {

            String passEncriptada = Encriptador.encriptar(contrasenia);

            psUser.setString(1, usuario);
            psUser.setString(2, passEncriptada);
            psUser.setString(3, nombre);
            psUser.setString(4, apellido);
            psUser.executeUpdate();

            long idUsuario;
            try (ResultSet keys = psUser.getGeneratedKeys()) {
                if (keys.next()) idUsuario = keys.getLong(1);
                else throw new SQLException("No se pudo obtener id_usuario");
            }

            try (PreparedStatement psMed = c.prepareStatement(sqlMedico)) {
                psMed.setLong(1, idUsuario);
                psMed.setString(2, matricula);
                psMed.setString(3, especialidad);
                psMed.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error registrando médico", e);
        }
    }

    //MODIFICAR MÉDICO -------------------------

    public void modificarMedico(String usuario, String nombre, String apellido, String contrasenia,
                                String matricula, String especialidad) {

        if (!existeUsuario(usuario))
            throw new IllegalArgumentException("El médico '" + usuario + "' no existe.");

        validarNoVacio("Nombre", nombre);
        validarNoVacio("Apellido", apellido);
        validarNoVacio("Contraseña", contrasenia);
        validarNoVacio("Matrícula", matricula);
        validarNoVacio("Especialidad", especialidad);

        if (!matricula.matches("\\d+"))
            throw new IllegalArgumentException("La matrícula debe contener solo números.");

        if (existeMatricula(matricula))
            throw new IllegalArgumentException("La matrícula '" + matricula + "' ya está registrada.");

        String sqlUsuario = "UPDATE usuarios SET nombre=?, apellido=?, contrasenia=? WHERE usuario_login=?";
        String sqlMedico = "UPDATE medicos SET matricula=?, especialidad=? " +
                "WHERE id_usuario=(SELECT id_usuario FROM usuarios WHERE usuario_login=?)";

        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement psUser = c.prepareStatement(sqlUsuario);
             PreparedStatement psMed = c.prepareStatement(sqlMedico)) {

            String passEncriptada = Encriptador.encriptar(contrasenia);

            psUser.setString(1, nombre);
            psUser.setString(2, apellido);
            psUser.setString(3, passEncriptada);
            psUser.setString(4, usuario);
            psUser.executeUpdate();

            psMed.setString(1, matricula);
            psMed.setString(2, especialidad);
            psMed.setString(3, usuario);
            psMed.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error modificando médico", e);
        }
    }

    //DAR DE BAJA MÉDICO -------------------------

    public void eliminarMedico(String usuario) {
        if (!existeUsuario(usuario))
            throw new IllegalArgumentException("El médico '" + usuario + "' no existe.");

        eliminarUsuario(usuario);
    }

  //ELIMINAR USUARIO (GENERAL) -------------------------

    public void eliminarUsuario(String usuario) {
        String sql = "DELETE FROM usuarios WHERE usuario_login=?";
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, usuario);
            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas == 0) {
                throw new IllegalArgumentException("No se encontró ningún usuario con el nombre '" + usuario + "'.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando usuario", e);
        }
    }


    //LISTAR USUARIOS POR ROL -------------------------

    public List<String> listarUsuariosPorRol(String rol) {
        String sql = "SELECT usuario_login, nombre, apellido FROM usuarios WHERE rol=?";
        List<String> list = new ArrayList<>();
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, rol);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String login = rs.getString("usuario_login");
                    String nom = rs.getString("nombre");
                    String ape = rs.getString("apellido");
                    if (login != null)
                        list.add(login + " | " + nom + " " + ape);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listando usuarios", e);
        }
        return list;
    }
    
  //BLOQUEAR USUARIO -------------------------
    public void bloquearUsuario(String usuario) {

        validarNoVacio("Usuario", usuario);

        if (!existeUsuario(usuario)) {
            throw new IllegalArgumentException("El usuario '" + usuario + "' no existe.");
        }

        String sql = "UPDATE usuarios SET bloqueado = 1 WHERE usuario_login=? AND bloqueado = 0";

        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, usuario);
            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new IllegalStateException("El usuario ya está bloqueado o no se pudo bloquear.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al bloquear usuario", e);
        }
    }

    //DESBLOQUEAR USUARIO -------------------------
    public void desbloquearUsuario(String usuario) {

        validarNoVacio("Usuario", usuario);

        if (!existeUsuario(usuario)) {
            throw new IllegalArgumentException("El usuario '" + usuario + "' no existe.");
        }

        String sql = "UPDATE usuarios SET bloqueado = 0 WHERE usuario_login=? AND bloqueado = 1";

        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, usuario);
            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new IllegalStateException("El usuario no está bloqueado o no se pudo desbloquear.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al desbloquear usuario", e);
        }
    }

}
