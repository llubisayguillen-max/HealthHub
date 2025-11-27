package dll;

import bll.Consulta;
import bll.Medico;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ControllerMedico {

	private final Medico medico;

	public ControllerMedico(Medico medico) {
		if (medico == null || medico.getUsuario() == null || medico.getUsuario().trim().isEmpty()) {
			throw new IllegalArgumentException("Datos inválidos");
		}
		this.medico = medico;
	}

	// registra la disponibilidad semanal
	public void registrarDisponibilidad(java.sql.Date fecha, Time horaInicio, Time horaFin) {
		if (fecha == null || horaInicio == null || horaFin == null)
			throw new IllegalArgumentException("Ingrese fecha y horas válidas");
		if (!horaFin.after(horaInicio))
			throw new IllegalArgumentException("La hora fin debe ser mayor que la hora inicio");

		final String qMed = """
				    SELECT m.id
				    FROM medicos m
				    JOIN usuarios u ON u.id_usuario = m.id_usuario
				    WHERE u.usuario_login = ?
				""";

		final String qSolape = """
				    SELECT 1
				    FROM medico_disponibilidad d
				    WHERE d.id_medico = ?
				      AND d.fecha     = ?
				      AND d.hora_inicio < ?
				      AND d.hora_fin    > ?
				""";

		final String ins = """
				    INSERT INTO medico_disponibilidad(id_medico, fecha, hora_inicio, hora_fin)
				    VALUES (?, ?, ?, ?)
				""";

		try (Connection c = Conexion.getInstance().getConnection()) {
			Long idMedico = null;
			try (PreparedStatement ps = c.prepareStatement(qMed)) {
				ps.setString(1, medico.getUsuario());
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						idMedico = rs.getLong(1);
				}
			}
			if (idMedico == null)
				throw new IllegalStateException("No se encontró el médico");

			try (PreparedStatement ps = c.prepareStatement(qSolape)) {
				ps.setLong(1, idMedico);
				ps.setDate(2, fecha);
				ps.setTime(3, horaFin);
				ps.setTime(4, horaInicio);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						throw new IllegalArgumentException("La franja se solapa con otra existente");
				}
			}

			try (PreparedStatement ps = c.prepareStatement(ins)) {
				ps.setLong(1, idMedico);
				ps.setDate(2, fecha);
				ps.setTime(3, horaInicio);
				ps.setTime(4, horaFin);
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error registrando disponibilidad: " + e.getMessage(), e);
		}
	}

	// Valida que exista el id de la disponibilidad
	public void validarId(int idDisponibilidad) {
		if (idDisponibilidad <= 0)
			throw new IllegalArgumentException("Id inválido");
		final String qCheck = """
				    SELECT 1
				    FROM medico_disponibilidad md
				    JOIN medicos m  ON m.id = md.id_medico
				    JOIN usuarios u ON u.id_usuario = m.id_usuario
				    WHERE md.id = ? AND u.usuario_login = ?
				""";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(qCheck)) {
			ps.setInt(1, idDisponibilidad);
			ps.setString(2, medico.getUsuario());
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					throw new IllegalStateException("Id inexistente");
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error verificando disponibilidad", e);
		}
	}

	public void modificarDisponibilidad(int idDisponibilidad, Time horaInicio, Time horaFin) {
		if (horaInicio == null || horaFin == null)
			throw new IllegalArgumentException("Las horas no pueden ser nulas");
		if (!horaFin.after(horaInicio))
			throw new IllegalArgumentException("La hora fin debe ser mayor que la hora inicio");

		final String qMed = """
				    SELECT m.id
				    FROM medicos m
				    JOIN usuarios u ON u.id_usuario = m.id_usuario
				    WHERE u.usuario_login = ?
				""";

		final String qSolape = """
				    SELECT 1
				    FROM medico_disponibilidad d
				    WHERE d.id_medico = ?
				      AND d.fecha = (SELECT fecha FROM medico_disponibilidad WHERE id = ?)
				      AND d.id <> ?
				      AND d.hora_inicio < ?
				      AND d.hora_fin    > ?
				""";

		final String upd = """
				    UPDATE medico_disponibilidad d
				    JOIN medicos m  ON m.id = d.id_medico
				    JOIN usuarios u ON u.id_usuario = m.id_usuario
				    SET d.hora_inicio = ?, d.hora_fin = ?
				    WHERE d.id = ? AND u.usuario_login = ?
				""";

		try (Connection c = Conexion.getInstance().getConnection()) {
			Long idMedico = null;
			try (PreparedStatement ps = c.prepareStatement(qMed)) {
				ps.setString(1, medico.getUsuario());
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						idMedico = rs.getLong(1);
				}
			}
			if (idMedico == null)
				throw new IllegalStateException("No se encontró el médico");

			try (PreparedStatement ps = c.prepareStatement(qSolape)) {
				ps.setLong(1, idMedico);
				ps.setInt(2, idDisponibilidad);
				ps.setInt(3, idDisponibilidad);
				ps.setTime(4, horaFin);
				ps.setTime(5, horaInicio);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						throw new IllegalArgumentException("Horario no disponible");
				}
			}

			try (PreparedStatement ps = c.prepareStatement(upd)) {
				ps.setTime(1, horaInicio);
				ps.setTime(2, horaFin);
				ps.setInt(3, idDisponibilidad);
				ps.setString(4, medico.getUsuario());
				if (ps.executeUpdate() == 0)
					throw new IllegalStateException("Id inexistente");
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error modificando disponibilidad", e);
		}
	}

	public void eliminarDisponibilidad(int idDisponibilidad) {
		if (idDisponibilidad <= 0)
			throw new IllegalArgumentException("Id inválido");
		final String del = """
				    DELETE md
				    FROM medico_disponibilidad md
				    JOIN medicos m  ON m.id = md.id_medico
				    JOIN usuarios u ON u.id_usuario = m.id_usuario
				    WHERE md.id = ? AND u.usuario_login = ?
				""";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(del)) {
			ps.setInt(1, idDisponibilidad);
			ps.setString(2, medico.getUsuario());
			if (ps.executeUpdate() == 0)
				throw new IllegalStateException("Id inexistente");
		} catch (SQLException e) {
			throw new RuntimeException("Error eliminando disponibilidad", e);
		}
	}

	// visualiza la agenda fecha, hora y datos del paciente

	public List<String> visualizarAgenda(java.sql.Date desde, java.sql.Date hasta, boolean incluirCancelados) {
		if (desde == null || hasta == null)
			throw new IllegalArgumentException("Ingrese fecha");
		if (hasta.before(desde))
			throw new IllegalArgumentException("Rango de fechas inválido");

		final String sql = """
				SELECT  t.id,
				        t.fecha,
				        t.hora,
				        t.estado,
				        p.id                AS id_paciente,
				        uPac.usuario_login  AS usuario_paciente,
				        uPac.nombre         AS nombre_paciente,
				        uPac.apellido       AS apellido_paciente
				FROM turnos t
				JOIN medicos m    ON m.id = t.id_medico
				JOIN usuarios uM  ON uM.id_usuario = m.id_usuario
				JOIN pacientes p  ON p.id = t.id_paciente
				JOIN usuarios uPac ON uPac.id_usuario = p.id_usuario
				WHERE uM.usuario_login = ?
				  AND t.fecha BETWEEN ? AND ?
				  AND ( ? OR t.estado <> 'Cancelado' )
				ORDER BY t.fecha, t.hora
				""";

		List<String> agenda = new ArrayList<>();

		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setString(1, medico.getUsuario());
			ps.setDate(2, desde);
			ps.setDate(3, hasta);
			ps.setBoolean(4, incluirCancelados);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					var f = rs.getDate("fecha").toLocalDate();
					var h = rs.getTime("hora").toLocalTime();
					var estado = rs.getString("estado");

					long idTurno = rs.getLong("id");
					long idPaciente = rs.getLong("id_paciente");
					String userPac = rs.getString("usuario_paciente");
					String nombrePac = rs.getString("nombre_paciente");
					String apellidoPac = rs.getString("apellido_paciente");

					String linea = String.format(
							"%s %s | Paciente: %s, %s (%s) | idTurno: %d | idPaciente: %d | estado: %s", f, h,
							apellidoPac != null ? apellidoPac : "", nombrePac != null ? nombrePac : "",
							userPac != null ? userPac : "", idTurno, idPaciente, estado);

					agenda.add(linea);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error visualizando agenda", e);
		}

		return agenda;
	}

	public List<Object[]> visualizarAgendaUI(java.sql.Date desde, java.sql.Date hasta, boolean incluirCancelados) {
		if (desde == null || hasta == null)
			throw new IllegalArgumentException("Ingrese fecha");
		if (hasta.before(desde))
			throw new IllegalArgumentException("Rango de fechas inválido");

		final String sql = """
				SELECT t.id,
				t.fecha,
				t.hora,
				t.estado,
				up.nombre        AS nombrePaciente,
				up.apellido      AS apellidoPaciente,
				up.usuario_login AS usuarioPaciente
				FROM turnos t
				JOIN medicos  m  ON m.id = t.id_medico
				JOIN usuarios u  ON u.id_usuario = m.id_usuario      -- médico
				JOIN pacientes p ON p.id = t.id_paciente
				JOIN usuarios up ON up.id_usuario = p.id_usuario     -- paciente
				WHERE u.usuario_login = ?
				AND t.fecha BETWEEN ? AND ?
				AND ( ? OR t.estado <> 'Cancelado' )
				ORDER BY t.fecha, t.hora
				""";

		List<Object[]> lista = new ArrayList<>();

		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setString(1, medico.getUsuario());
			ps.setDate(2, desde);
			ps.setDate(3, hasta);
			ps.setBoolean(4, incluirCancelados);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					long idTurno = rs.getLong("id");
					LocalDate f = rs.getDate("fecha").toLocalDate();
					LocalTime h = rs.getTime("hora").toLocalTime();
					String estado = rs.getString("estado");
					String nombre = rs.getString("nombrePaciente");
					String apellido = rs.getString("apellidoPaciente");
					String usuario = rs.getString("usuarioPaciente");

					String pac = apellido + ", " + nombre;

					lista.add(new Object[] { idTurno, f, h, pac, estado });
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error visualizando agenda (UI)", e);
		}

		return lista;
	}

	public static record AgendaItem(long idTurno, long idPaciente, LocalDate fecha, LocalTime hora,
			String pacienteNombre, String usuarioPaciente, String estado) {
	}

	public List<AgendaItem> visualizarAgendaDetallada(java.sql.Date desde, java.sql.Date hasta,
			boolean incluirCancelados) {
		if (desde == null || hasta == null)
			throw new IllegalArgumentException("Ingrese fecha");
		if (hasta.before(desde))
			throw new IllegalArgumentException("Rango de fechas inválido");

		final String sql = """
				SELECT  t.id,
				        t.fecha,
				        t.hora,
				        t.estado,
				        p.id              AS id_paciente,
				        uPac.apellido     AS ape_pac,
				        uPac.nombre       AS nom_pac,
				        uPac.usuario_login AS user_pac
				FROM turnos t
				JOIN medicos m   ON m.id = t.id_medico
				JOIN usuarios uM ON uM.id_usuario = m.id_usuario
				JOIN pacientes p ON p.id = t.id_paciente
				JOIN usuarios uPac ON uPac.id_usuario = p.id_usuario
				WHERE uM.usuario_login = ?
				  AND t.fecha BETWEEN ? AND ?
				  AND ( ? OR t.estado <> 'Cancelado' )
				ORDER BY t.fecha, t.hora
				""";

		List<AgendaItem> agenda = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setString(1, medico.getUsuario());
			ps.setDate(2, desde);
			ps.setDate(3, hasta);
			ps.setBoolean(4, incluirCancelados);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					long idTurno = rs.getLong("id");
					long idPaciente = rs.getLong("id_paciente");
					LocalDate fecha = rs.getDate("fecha").toLocalDate();
					LocalTime hora = rs.getTime("hora").toLocalTime();
					String ape = rs.getString("ape_pac");
					String nom = rs.getString("nom_pac");
					String userPac = rs.getString("user_pac");
					String estado = rs.getString("estado");

					String nombrePac = (ape != null ? ape : "") + (nom != null ? ", " + nom : "");

					agenda.add(new AgendaItem(idTurno, idPaciente, fecha, hora, nombrePac.trim(), userPac, estado));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error visualizando agenda detallada", e);
		}
		return agenda;
	}

	public void confirmarAsistencia(long idTurno) {
		if (idTurno <= 0)
			throw new IllegalArgumentException("Id inválido");
		final String sql = """
				    UPDATE turnos t
				    JOIN medicos m  ON m.id = t.id_medico
				    JOIN usuarios u ON u.id_usuario = m.id_usuario
				    SET t.estado='Confirmado'
				    WHERE t.id=? AND u.usuario_login=?
				""";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, idTurno);
			ps.setString(2, medico.getUsuario());
			if (ps.executeUpdate() == 0)
				throw new IllegalStateException("Turno inexistente");
		} catch (SQLException e) {
			throw new RuntimeException("Error confirmando asistencia", e);
		}
	}

	public void cancelarTurno(long idTurno) {
		if (idTurno <= 0)
			throw new IllegalArgumentException("Id de turno inválido");
		final String sql = """
				    UPDATE turnos t
				    JOIN medicos m  ON m.id = t.id_medico
				    JOIN usuarios u ON u.id_usuario = m.id_usuario
				    SET t.estado='Cancelado'
				    WHERE t.id=? AND u.usuario_login=?
				""";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, idTurno);
			ps.setString(2, medico.getUsuario());
			if (ps.executeUpdate() == 0)
				throw new IllegalStateException("Turno inexistente");
		} catch (SQLException e) {
			throw new RuntimeException("Error cancelando turno", e);
		}
	}

	public boolean validarIdTurno(long idTurno) {
		if (idTurno <= 0)
			return false;
		final String q = """
				    SELECT 1
				    FROM turnos t
				    JOIN medicos m  ON m.id = t.id_medico
				    JOIN usuarios u ON u.id_usuario = m.id_usuario
				    WHERE t.id=? AND u.usuario_login=?
				""";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(q)) {
			ps.setLong(1, idTurno);
			ps.setString(2, medico.getUsuario());
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error verificando turno", e);
		}
	}

	public void reprogramarTurno(long idTurno, Date nuevaFechaHora) {
		if (idTurno <= 0)
			throw new IllegalArgumentException("Id de turno inválido");
		if (nuevaFechaHora == null)
			throw new IllegalArgumentException("Ingresar fecha y hora");

		java.sql.Date f = new java.sql.Date(nuevaFechaHora.getTime());
		java.sql.Time h = new java.sql.Time(nuevaFechaHora.getTime());

		long idMedico = -1;
		String qMedEst = """
				    SELECT t.id_medico, t.estado
				    FROM turnos t
				    JOIN medicos m ON m.id = t.id_medico
				    JOIN usuarios u ON u.id_usuario = m.id_usuario
				    WHERE t.id = ? AND u.usuario_login = ?
				""";

		try (Connection c = Conexion.getInstance().getConnection();
				PreparedStatement ps = c.prepareStatement(qMedEst)) {
			ps.setLong(1, idTurno);
			ps.setString(2, medico.getUsuario());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					idMedico = rs.getLong(1);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error obteniendo turno", e);
		}

		if (idMedico <= 0)
			throw new IllegalStateException("Turno inexistente o no te pertenece");

		// validación de solape con otros turnos
		String qSolape = """
				    SELECT 1
				    FROM turnos t
				    WHERE t.id_medico = ? AND t.fecha = ? AND t.hora = ?
				      AND t.estado IN ('Reservado','Confirmado')
				      AND t.id <> ?
				""";
		try (Connection c = Conexion.getInstance().getConnection();
				PreparedStatement ps = c.prepareStatement(qSolape)) {
			ps.setLong(1, idMedico);
			ps.setDate(2, f);
			ps.setTime(3, h);
			ps.setLong(4, idTurno);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					throw new IllegalStateException("Hay otro turno en ese horario");
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error verificando solape", e);
		}

		// actualizar fecha hora
		String upd = """
				    UPDATE turnos t
				    JOIN medicos m ON m.id = t.id_medico
				    JOIN usuarios u ON u.id_usuario = m.id_usuario
				    SET t.fecha = ?, t.hora = ?,
				        t.estado = CASE WHEN t.estado = 'Cancelado' THEN 'Reservado' ELSE t.estado END
				    WHERE t.id = ? AND u.usuario_login = ?
				""";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(upd)) {
			ps.setDate(1, f);
			ps.setTime(2, h);
			ps.setLong(3, idTurno);
			ps.setString(4, medico.getUsuario());
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error reprogramando turno", e);
		}
	}

	// validar que el usuario exista
	public boolean validarPaciente(String usernamePaciente) {
		if (usernamePaciente == null || usernamePaciente.isBlank())
			return false;

		final String q = """
				    SELECT 1
				    FROM usuarios u
				    JOIN pacientes p ON p.id_usuario = u.id_usuario
				    WHERE u.usuario_login = ?
				""";

		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(q)) {
			ps.setString(1, usernamePaciente.trim());
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error verificando paciente", e);
		}
	}

	// registra la consulta, primero valida que exista id de turno
	public long registrarConsulta(String usernamePaciente, Consulta consulta) {
		if (usernamePaciente == null || usernamePaciente.isBlank())
			throw new IllegalArgumentException("Usuario del paciente requerido");
		if (consulta == null)
			throw new IllegalArgumentException("Consulta requerida");
		if (consulta.getMotivo() == null || consulta.getMotivo().isBlank())
			throw new IllegalArgumentException("Motivo requerido");

		final String qCheckPaciente = """
				    SELECT 1
				    FROM usuarios u
				    JOIN pacientes p ON p.id_usuario = u.id_usuario
				    WHERE u.usuario_login = ?
				""";
		try (Connection c = Conexion.getInstance().getConnection();
				PreparedStatement ps = c.prepareStatement(qCheckPaciente)) {
			ps.setString(1, usernamePaciente);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					throw new IllegalStateException("Paciente inexistente");
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error verificando la existencia del paciente", e);
		}

		final String ins = """
				    INSERT INTO consultas(motivo, diagnostico, tratamiento, fecha, id_medico, id_paciente, seguimiento)
				    VALUES (?, ?, ?, ?,
				            (SELECT m.id FROM medicos m JOIN usuarios u ON u.id_usuario=m.id_usuario WHERE u.usuario_login=?),
				            (SELECT p.id FROM pacientes p JOIN usuarios u ON u.id_usuario=p.id_usuario WHERE u.usuario_login=?),
				            ?)
				""";
		Timestamp ts = new Timestamp(consulta.getFecha().getTime());
		try (Connection c = Conexion.getInstance().getConnection();
				PreparedStatement ps = c.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
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
