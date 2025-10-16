package dll;

import bll.Consulta;
import bll.Medico;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ControllerMedico {

	private static final int MIN_BLOCK_MINUTES = 15;
	private final Medico medico;

	public ControllerMedico(Medico medico) {
		this.medico = medico;
	}

	public void registrarDisponibilidad(Time horaInicio, Time horaFin) {
		if (horaInicio == null || horaFin == null)
			throw new IllegalArgumentException("Las horas no pueden ser nulas.");
		if (!horaFin.after(horaInicio))
			throw new IllegalArgumentException("La hora fin debe ser mayor que la hora inicio.");
		long mins = java.time.Duration.between(horaInicio.toLocalTime(), horaFin.toLocalTime()).toMinutes();
		if (mins < MIN_BLOCK_MINUTES)
			throw new IllegalArgumentException("La franja debe durar al menos " + MIN_BLOCK_MINUTES + " minutos.");

		final String qMed = "SELECT m.id FROM medicos m JOIN usuarios u ON u.id_usuario = m.id_usuario "
				+ "WHERE u.usuario_login = ?";
		final String qSolape = "SELECT COUNT(*) FROM medico_disponibilidad d "
				+ "WHERE d.id_medico = ? AND NOT (d.hora_fin <= ? OR d.hora_inicio >= ?)";
		final String ins = "INSERT INTO medico_disponibilidad(id_medico, hora_inicio, hora_fin) VALUES (?, ?, ?)";

		try (Connection c = Conexion.getInstance().getConnection()) {
			c.setAutoCommit(false);

			Long idMedico = null;
			try (PreparedStatement ps = c.prepareStatement(qMed)) {
				ps.setString(1, medico.getUsuario());
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						idMedico = rs.getLong(1);
				}
			}
			if (idMedico == null)
				throw new IllegalStateException("No se encontró el médico del usuario.");

			// validar solape
			try (PreparedStatement ps = c.prepareStatement(qSolape)) {
				ps.setLong(1, idMedico);
				ps.setTime(2, horaInicio);
				ps.setTime(3, horaFin);
				try (ResultSet rs = ps.executeQuery()) {
					rs.next();
					if (rs.getInt(1) > 0)
						throw new IllegalArgumentException("La franja se solapa con otra existente.");
				}
			}

			// insertar
			try (PreparedStatement ps = c.prepareStatement(ins)) {
				ps.setLong(1, idMedico);
				ps.setTime(2, horaInicio);
				ps.setTime(3, horaFin);
				ps.executeUpdate();
			}

			c.commit();
		} catch (SQLException e) {
			throw new RuntimeException("Error registrando disponibilidad: " + e.getMessage(), e);
		}
	}

	public void modificarDisponibilidad(int idDisponibilidad, Time horaInicio, Time horaFin) {
		if (horaInicio == null || horaFin == null)
			throw new IllegalArgumentException("Las horas no pueden ser nulas.");
		if (!horaFin.after(horaInicio))
			throw new IllegalArgumentException("La hora fin debe ser mayor que la hora inicio.");
		long mins = java.time.Duration.between(horaInicio.toLocalTime(), horaFin.toLocalTime()).toMinutes();
		if (mins < MIN_BLOCK_MINUTES)
			throw new IllegalArgumentException("La franja debe durar al menos " + MIN_BLOCK_MINUTES + " minutos.");

		final String qMed = "SELECT m.id FROM medicos m JOIN usuarios u ON u.id_usuario = m.id_usuario "
				+ "WHERE u.usuario_login = ?";
		final String qSolape = "SELECT COUNT(*) FROM medico_disponibilidad d "
				+ "WHERE d.id_medico = ? AND d.id <> ? AND NOT (d.hora_fin <= ? OR d.hora_inicio >= ?)";
		final String upd = "UPDATE medico_disponibilidad SET hora_inicio=?, hora_fin=? WHERE id=?";

		try (Connection c = Conexion.getInstance().getConnection()) {
			c.setAutoCommit(false);

			Long idMedico = null;
			try (PreparedStatement ps = c.prepareStatement(qMed)) {
				ps.setString(1, medico.getUsuario());
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						idMedico = rs.getLong(1);
				}
			}
			if (idMedico == null)
				throw new IllegalStateException("No se encontró el médico del usuario.");

			// validar solape
			try (PreparedStatement ps = c.prepareStatement(qSolape)) {
				ps.setLong(1, idMedico);
				ps.setInt(2, idDisponibilidad);
				ps.setTime(3, horaInicio);
				ps.setTime(4, horaFin);
				try (ResultSet rs = ps.executeQuery()) {
					rs.next();
					if (rs.getInt(1) > 0)
						throw new IllegalArgumentException("La franja se solapa con otra existente.");
				}
			}

			// actualizar
			try (PreparedStatement ps = c.prepareStatement(upd)) {
				ps.setTime(1, horaInicio);
				ps.setTime(2, horaFin);
				ps.setInt(3, idDisponibilidad);
				ps.executeUpdate();
			}

			c.commit();
		} catch (SQLException e) {
			throw new RuntimeException("Error modificando disponibilidad: " + e.getMessage(), e);
		}
	}

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
			throw new RuntimeException("Error listando disponibilidad: " + e.getMessage(), e);
		}
		return list;
	}

	public void eliminarDisponibilidad(int idDisponibilidad) {
		String sql = "DELETE FROM medico_disponibilidad WHERE id=?";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setInt(1, idDisponibilidad);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error eliminando disponibilidad: " + e.getMessage(), e);
		}
	}

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
			throw new RuntimeException("Error listando próximos turnos: " + e.getMessage(), e);
		}
		return list;
	}

	public List<String> turnosDelDia(java.sql.Date dia) {
		String sql = "SELECT t.id, t.fecha, t.hora, t.estado, up.usuario_login AS paciente " + "FROM turnos t "
				+ "JOIN medicos m ON m.id = t.id_medico " + "JOIN usuarios um ON um.id_usuario = m.id_usuario "
				+ "JOIN pacientes p ON p.id = t.id_paciente " + "JOIN usuarios up ON up.id_usuario = p.id_usuario "
				+ "WHERE um.usuario_login=? AND t.fecha=? " + "ORDER BY t.hora";
		List<String> list = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, medico.getUsuario());
			ps.setDate(2, dia);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add("#" + rs.getLong("id") + " | " + rs.getDate("fecha") + " " + rs.getTime("hora") + " | "
							+ rs.getString("estado") + " | Paciente: " + rs.getString("paciente"));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error listando turnos del día: " + e.getMessage(), e);
		}
		return list;
	}

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
			throw new RuntimeException("Error visualizando agenda: " + e.getMessage(), e);
		}
		return agenda;
	}

	public void confirmarAsistencia(long idTurno) {
		String sql = "UPDATE turnos SET estado='Confirmado' WHERE id=?";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, idTurno);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error confirmando asistencia: " + e.getMessage(), e);
		}
	}

	public void cancelarTurno(long idTurno) {
		String sql = "UPDATE turnos SET estado='Cancelado' WHERE id=?";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, idTurno);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error cancelando turno: " + e.getMessage(), e);
		}
	}

	public void reprogramarTurno(long idTurno, Date nuevaFechaHora) {
		final String qMed = "SELECT t.id_medico FROM turnos t WHERE t.id=?";
		final String qSolape = "SELECT 1 FROM turnos t " + "WHERE t.id_medico=? AND t.fecha=? AND t.hora=? "
				+ "AND t.estado IN ('Reservado','Confirmado') AND t.id<>?";
		final String upd = "UPDATE turnos SET fecha=?, hora=? WHERE id=?";

		java.sql.Date f = new java.sql.Date(nuevaFechaHora.getTime());
		java.sql.Time h = new java.sql.Time(nuevaFechaHora.getTime());

		try (Connection c = Conexion.getInstance().getConnection()) {
			c.setAutoCommit(false);

			long idMedico = -1;
			try (PreparedStatement ps = c.prepareStatement(qMed)) {
				ps.setLong(1, idTurno);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						idMedico = rs.getLong(1);
				}
			}
			if (idMedico <= 0)
				throw new IllegalStateException("Turno inexistente.");

			try (PreparedStatement ps = c.prepareStatement(qSolape)) {
				ps.setLong(1, idMedico);
				ps.setDate(2, f);
				ps.setTime(3, h);
				ps.setLong(4, idTurno);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						throw new IllegalStateException("Hay otro turno en ese horario.");
				}
			}

			try (PreparedStatement ps = c.prepareStatement(upd)) {
				ps.setDate(1, f);
				ps.setTime(2, h);
				ps.setLong(3, idTurno);
				ps.executeUpdate();
			}

			c.commit();
		} catch (SQLException e) {
			throw new RuntimeException("Error reprogramando turno: " + e.getMessage(), e);
		}
	}

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
			throw new RuntimeException("Error registrando consulta: " + e.getMessage(), e);
		}
	}
}
