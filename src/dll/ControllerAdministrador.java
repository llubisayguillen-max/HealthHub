package dll;

import bll.Administrador;
import bll.Paciente;
import bll.Medico;
import bll.HistorialMedico;

import java.sql.*;

public class ControllerAdministrador {

    private final Administrador admin;

    public ControllerAdministrador(Administrador admin) {
        this.admin = admin;
    }

    // Alta de paciente
    public long altaPaciente(Paciente p) {
        String sqlUsuario = "INSERT INTO usuarios(nombre, apellido, usuario_login, contrasenia, rol) VALUES (?, ?, ?, ?, 'Paciente')";
        String sqlPaciente = "INSERT INTO pacientes(dni, obra_social, id_usuario) VALUES (?, ?, ?)";
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement psUser = c.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement psPac = c.prepareStatement(sqlPaciente)) {

            psUser.setString(1, p.getNombre());
            psUser.setString(2, p.getApellido());
            psUser.setString(3, p.getUsuario());
            psUser.setString(4, p.getContrasenia());
            psUser.executeUpdate();

            ResultSet rs = psUser.getGeneratedKeys();
            if (rs.next()) {
                long idUsuario = rs.getLong(1);
                psPac.setInt(1, p.getDni());
                psPac.setString(2, p.getObraSocial());
                psPac.setLong(3, idUsuario);
                psPac.executeUpdate();
                return idUsuario;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al dar de alta paciente", e);
        }
        return 0;
    }

    // Alta de médico
    public long altaMedico(Medico m) {
        String sqlUsuario = "INSERT INTO usuarios(nombre, apellido, usuario_login, contrasenia, rol) VALUES (?, ?, ?, ?, 'Medico')";
        String sqlMedico = "INSERT INTO medicos(id_medico, especialidad, id_usuario) VALUES (?, ?, ?)";
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement psUser = c.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement psMed = c.prepareStatement(sqlMedico)) {

            psUser.setString(1, m.getNombre());
            psUser.setString(2, m.getApellido());
            psUser.setString(3, m.getUsuario());
            psUser.setString(4, m.getContrasenia());
            psUser.executeUpdate();

            ResultSet rs = psUser.getGeneratedKeys();
            if (rs.next()) {
                long idUsuario = rs.getLong(1);
                psMed.setInt(1, m.getId());
                psMed.setString(2, m.getEspecialidad());
                psMed.setLong(3, idUsuario);
                psMed.executeUpdate();
                return idUsuario;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al dar de alta médico", e);
        }
        return 0;
    }

    // Baja de paciente
    public void bajaPaciente(String username) {
        String sql = "DELETE FROM pacientes WHERE id_usuario=(SELECT id_usuario FROM usuarios WHERE usuario_login=?)";
        String sqlUser = "DELETE FROM usuarios WHERE usuario_login=?";
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             PreparedStatement psUser = c.prepareStatement(sqlUser)) {

            ps.setString(1, username);
            ps.executeUpdate();

            psUser.setString(1, username);
            psUser.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar paciente", e);
        }
    }

    // Baja de médico
    public void bajaMedico(String username) {
        String sql = "DELETE FROM medicos WHERE id_usuario=(SELECT id_usuario FROM usuarios WHERE usuario_login=?)";
        String sqlUser = "DELETE FROM usuarios WHERE usuario_login=?";
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             PreparedStatement psUser = c.prepareStatement(sqlUser)) {

            ps.setString(1, username);
            ps.executeUpdate();

            psUser.setString(1, username);
            psUser.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar médico", e);
        }
    }

    // Crear historia médica
    public void crearHistoriaMedica(Paciente p, HistorialMedico h) {
        String sql = "INSERT INTO historial_medico(id_paciente, fecha, diagnostico, observaciones) VALUES (?, ?, ?, ?)";
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, p.getId());
            ps.setDate(2, new java.sql.Date(h.getFecha().getTime()));
            ps.setString(3, h.getDiagnostico());
            ps.setString(4, String.join(",", h.getObservaciones())); // ejemplo de arreglo a string
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error creando historia médica", e);
        }
    }

    // Resetear contraseña
    public void resetContrasenia(String username, String nuevaContrasenia) {
        String sql = "UPDATE usuarios SET contrasenia=? WHERE usuario_login=?";
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, nuevaContrasenia);
            ps.setString(2, username);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error reseteando contraseña", e);
        }
    }

    // Bloquear usuario
    public void bloquearUsr(String username) {
        String sql = "UPDATE usuarios SET estado='Bloqueado' WHERE usuario_login=?";
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error bloqueando usuario", e);
        }
    }

    // Desbloquear usuario
    public void desbloquearUsr(String username) {
        String sql = "UPDATE usuarios SET estado='Activo' WHERE usuario_login=?";
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error desbloqueando usuario", e);
        }
    }
}
