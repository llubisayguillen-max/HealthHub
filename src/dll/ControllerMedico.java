package dll;

import bll.Consulta;
import bll.Medico;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ControllerMedico {

	private final Medico medico;

	public ControllerMedico(Medico medico) {
		this.medico = medico;
	}

	// crea la franja de disponibilidad
	public void registrarDisponibilidad(Time horaInicio, Time horaFin) {
		String sql = "INSERT INTO medico_disponibilidad(id_medico, hora_inicio, hora_fin) "
				+ "VALUES ( (SELECT m.id FROM medicos m JOIN usuarios u ON u.id_usuario=m.id_usuario WHERE u.usuario_login=?), ?, ?)";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, medico.getUsuario());
			ps.setTime(2, horaInicio);
			ps.setTime(3, horaFin);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error registrando disponibilidad", e);
		}
	}

	// modifica la franja de disponibilidad
	public void modificarDisponibilidad(int idDisponibilidad, Time horaInicio, Time horaFin) {
		String sql = "UPDATE medico_disponibilidad SET hora_inicio=?, hora_fin=? WHERE id=?";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setTime(1, horaInicio);
			ps.setTime(2, horaFin);
			ps.setInt(3, idDisponibilidad);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error modificando disponibilidad", e);
		}
	}

	// listar la disponibilidad
	public List<String> listarDisponibilidad() {
		String sql = "SELECT md.id, md.hora_inicio, md.hora_fin " + "FROM medico_disponibilidad md "
				+ "JOIN medicos m ON m.id = md.id_medico " + "JOIN usuarios u ON u.id_usuario = m.id_usuario "
				+ "WHERE u.usuario_login=? ORDER BY md.hora_inicio";
		List<String> list = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, medico.getUsuario());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(rs.getInt("id") + " | " + rs.getTime("hora_inicio") + " - " + rs.getTime("hora_fin"));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error listando disponibilidad", e);
		}
		return list;
	}

	// elimina la disponibilidad de la franja
	public void eliminarDisponibilidad(int idDisponibilidad) {
		String sql = "DELETE FROM medico_disponibilidad WHERE id=?";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setInt(1, idDisponibilidad);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error eliminando disponibilidad", e);
		}
	}

	// devuelve los proximos turnos
	public List<String> proximosTurnos(int limit) {
		String sql = "SELECT t.id, t.fecha, t.hora, t.estado, up.usuario_login AS paciente " + "FROM turnos t "
				+ "JOIN medicos m ON m.id = t.id_medico " + "JOIN usuarios um ON um.id_usuario = m.id_usuario "
				+ "JOIN pacientes p ON p.id = t.id_paciente " + "JOIN usuarios up ON up.id_usuario = p.id_usuario "
				+ "WHERE um.usuario_login=? AND CONCAT(t.fecha,' ',t.hora) >= NOW() "
				+ "ORDER BY t.fecha, t.hora LIMIT ?";
		List<String> list = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, medico.getUsuario());
			ps.setInt(2, Math.max(1, limit));
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add("#" + rs.getLong("id") + " | " + rs.getDate("fecha") + " " + rs.getTime("hora") + " | "
							+ rs.getString("estado") + " | Paciente: " + rs.getString("paciente"));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error listando próximos turnos", e);
		}
		return list;
	}

	// agenda del medico
	public List<Date> visualizarAgenda(java.sql.Date desde, java.sql.Date hasta) {
		String sql = "SELECT t.fecha, t.hora " + "FROM turnos t " + "JOIN medicos m ON m.id = t.id_medico "
				+ "JOIN usuarios u ON u.id_usuario = m.id_usuario "
				+ "WHERE u.usuario_login=? AND t.fecha BETWEEN ? AND ? " + "ORDER BY t.fecha, t.hora";
		List<Date> agenda = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, medico.getUsuario());
			ps.setDate(2, desde);
			ps.setDate(3, hasta);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					agenda.add(new Date(rs.getDate("fecha").getTime() + rs.getTime("hora").getTime()));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error visualizando agenda", e);
		}
		return agenda;	}

	public void confirmarAsistencia(long idTurno) {
		String sql = "UPDATE turnos SET estado='Confirmado' WHERE id=?";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, idTurno);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error confirmando asistencia", e);
		}
	}

	// cancelacion de turo
	public void cancelarTurno(long idTurno) {
		String sql = "UPDATE turnos SET estado='Cancelado' WHERE id=?";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, idTurno);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error cancelando turno", e);
		}
	}

	// reprograma el turno si se cancela
	public void reprogramarTurno(long idTurno, Date nuevaFechaHora) {
		java.sql.Date f = new java.sql.Date(nuevaFechaHora.getTime());
		java.sql.Time h = new java.sql.Time(nuevaFechaHora.getTime());

		long idMedico = -1;
		String qMed = "SELECT t.id_medico FROM turnos t WHERE t.id=?";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(qMed)) {
			ps.setLong(1, idTurno);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					idMedico = rs.getLong(1);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error obteniendo turno", e);
		}
		if (idMedico <= 0)
			throw new IllegalStateException("Turno inexistente");

		String qSolape = "SELECT 1 FROM turnos t WHERE t.id_medico=? AND t.fecha=? AND t.hora=? "
				+ "AND t.estado IN ('Reservado','Confirmado') AND t.id<>?";
		try (Connection c = Conexion.getInstance().getConnection();
				PreparedStatement ps = c.prepareStatement(qSolape)) {
			ps.setLong(1, idMedico);
			ps.setDate(2, f);
			ps.setTime(3, h);
			ps.setLong(4, idTurno);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					throw new IllegalStateException("Hay otro turno en ese horario.");
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error verificando solape", e);
		}

		String upd = "UPDATE turnos SET fecha=?, hora=? WHERE id=?";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(upd)) {
			ps.setDate(1, f);
			ps.setTime(2, h);
			ps.setLong(3, idTurno);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error reprogramando turno", e);
		}
	}

	// registrar consulta en la tabla consultas
	public long registrarConsulta(String usernamePaciente, Consulta consulta) {
		String sql = "INSERT INTO consultas(motivo, diagnostico, tratamiento, fecha, id_medico, id_paciente, recomendaciones) "
				+ "VALUES (?, ?, ?, ?, "
				+ " (SELECT m.id FROM medicos m JOIN usuarios u ON u.id_usuario=m.id_usuario WHERE u.usuario_login=?), "
				+ " (SELECT p.id FROM pacientes p JOIN usuarios u ON u.id_usuario=p.id_usuario WHERE u.usuario_login=?), "
				+ " ?)";
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
