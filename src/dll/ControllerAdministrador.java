package dll;

import bll.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ControllerAdministrador {

    private final Usuario admin;

    public ControllerAdministrador(Usuario admin) {
        this.admin = admin;
    }

    //REGISTRAR PACIENTE
    public void registrarPaciente(String usuario, String nombre, String apellido, String contrasenia,
                                  int nroContrato, String obraSocial) {
        String sqlUsuario  = "INSERT INTO usuarios(usuario_login, contrasenia, nombre, apellido, rol) " +
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
        } catch (SQLIntegrityConstraintViolationException dup) {
            throw new RuntimeException("Usuario ya existente: " + usuario, dup);
        } catch (SQLException e) {
            throw new RuntimeException("Error registrando paciente: " + e.getMessage(), e);
        }
    }

    // MODIFICAR PACIENTE
    public void modificarPaciente(String usuario, String nombre, String apellido, String contrasenia,
                                  int nroContrato, String obraSocial) {
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

    //REGISTRAR MÉDICO
    public void registrarMedico(String usuario, String nombre, String apellido, String contrasenia,
                                String matricula, String especialidad) {
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
                if (keys.next()) {
                    idUsuario = keys.getLong(1);
                } else {
                    throw new SQLException("No se pudo obtener id_usuario");
                }
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

    //MODIFICAR MÉDICO
    public void modificarMedico(String usuario, String nombre, String apellido, String contrasenia,
                                String matricula, String especialidad) {
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

    //ELIMINAR USUARIO
    public void eliminarUsuario(String usuario) {
        String sql = "DELETE FROM usuarios WHERE usuario_login=?";
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando usuario", e);
        }
    }

    //LISTAR USUARIOS
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
                    if (login != null) list.add(login + " | " + nom + " " + ape);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error listando usuarios", e);
        }
        return list;
    }
}
