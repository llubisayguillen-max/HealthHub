package dll;

import bll.Consulta;
import bll.Medico;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ControllerMedico {

    private final Medico medico;

    public ControllerMedico(Medico medico) { this.medico = medico; }

    public void registrarDisponibilidad(Time horaInicio, Time horaFin) {
        String sql = "INSERT INTO medico_disponibilidad(id_medico, hora_inicio, hora_fin) " +
                     "VALUES ( (SELECT m.id FROM medicos m JOIN usuarios u ON u.id_usuario=m.id_usuario WHERE u.usuario_login=?), ?, ?)";
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, medico.getUsuario());
            ps.setTime(2, horaInicio);
            ps.setTime(3, horaFin);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error registrando disponibilidad", e);
        }
    }

    public void modificarDisponibilidad(int idDisponibilidad, Time horaInicio, Time horaFin) {
        String sql = "UPDATE medico_disponibilidad SET hora_inicio=?, hora_fin=? WHERE id=?";
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTime(1, horaInicio);
            ps.setTime(2, horaFin);
            ps.setInt(3, idDisponibilidad);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Error modificando disponibilidad", e); }
    }

  // agenda del medico
    public List<Date> visualizarAgenda(java.sql.Date desde, java.sql.Date hasta) {
        String sql = "SELECT t.fecha, t.hora " +
                     "FROM turnos t " +
                     "JOIN medicos m ON m.id = t.id_medico " +
                     "JOIN usuarios u ON u.id_usuario = m.id_usuario " +
                     "WHERE u.usuario_login=? AND t.fecha BETWEEN ? AND ? " +
                     "ORDER BY t.fecha, t.hora";
        List<Date> agenda = new ArrayList<>();
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, medico.getUsuario());
            ps.setDate(2, desde);
            ps.setDate(3, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    agenda.add(new Date(rs.getDate("fecha").getTime() + rs.getTime("hora").getTime()));
                }
            }
        } catch (SQLException e) { throw new RuntimeException("Error visualizando agenda", e); }
        return agenda;
    }

    public void confirmarAsistencia(long idTurno) {
        String sql = "UPDATE turnos SET estado='Confirmado' WHERE id=?";
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idTurno);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Error confirmando asistencia", e); }
    }

   // registrar consulta en la tabla consultas
    public long registrarConsulta(String usernamePaciente, Consulta consulta) {
        String sql = "INSERT INTO consultas(motivo, diagnostico, tratamiento, fecha, id_medico, id_paciente, recomendaciones) " +
                     "VALUES (?, ?, ?, ?, " +
                     " (SELECT m.id FROM medicos m JOIN usuarios u ON u.id_usuario=m.id_usuario WHERE u.usuario_login=?), " +
                     " (SELECT p.id FROM pacientes p JOIN usuarios u ON u.id_usuario=p.id_usuario WHERE u.usuario_login=?), " +
                     " ?)";
        Timestamp ts = new Timestamp(consulta.getFecha().getTime());
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, consulta.getMotivo());
            ps.setString(2, consulta.getDiagnostico());
            ps.setString(3, consulta.getTratamiento());
            ps.setTimestamp(4, ts);
            ps.setString(5, medico.getUsuario());
            ps.setString(6, usernamePaciente);
            ps.setString(7, consulta.getSeguimiento());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error registrando consulta", e);
        }
    }
}

