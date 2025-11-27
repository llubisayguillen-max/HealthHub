package dll;

import bll.Medico;
import bll.Paciente;
import bll.Turno;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ControllerPaciente {

	private final Paciente paciente;

	public static record TurnoDisponible(long idDisponibilidad, String medicoNombre, String medicoEspecialidad,
			LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
	}

	public ControllerPaciente(Paciente paciente) {
		this.paciente = paciente;
	}

	public List<TurnoDisponible> buscarTurnos(String especialidad, LocalDate fechaDesde, LocalDate fechaHasta) {
		if (especialidad == null || especialidad.trim().isEmpty())
			throw new IllegalArgumentException("Especialidad requerida.");
		if (fechaDesde == null || fechaHasta == null)
			throw new IllegalArgumentException("Debe seleccionar ambas fechas.");
		if (fechaDesde.isBefore(LocalDate.now()))
			throw new IllegalArgumentException("La fecha 'Desde' no puede ser en el pasado.");
		if (fechaHasta.isBefore(fechaDesde))
			throw new IllegalArgumentException("La fecha 'Hasta' debe ser posterior a 'Desde'.");

		java.sql.Date sqlDesde = java.sql.Date.valueOf(fechaDesde);
		java.sql.Date sqlHasta = java.sql.Date.valueOf(fechaHasta);

		final String sql = """
				SELECT
				    md.id AS id_disponibilidad,
				    u.nombre, u.apellido, m.especialidad,
				    md.fecha, md.hora_inicio, md.hora_fin
				FROM medico_disponibilidad md
				JOIN medicos m ON m.id = md.id_medico
				JOIN usuarios u ON u.id_usuario = m.id_usuario
				WHERE
				    m.especialidad LIKE ?
				    AND md.fecha BETWEEN ? AND ?
				    AND NOT EXISTS (
				        SELECT 1 FROM turnos t
				        WHERE t.id_medico = m.id
				          AND t.fecha = md.fecha
				          AND t.hora = md.hora_inicio
				          AND t.estado IN ('Reservado', 'Confirmado')
				    )
				ORDER BY md.fecha, md.hora_inicio, u.apellido
				""";

		List<TurnoDisponible> res = new ArrayList<>();

		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, "%" + especialidad + "%");
			ps.setDate(2, sqlDesde);
			ps.setDate(3, sqlHasta);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");

					res.add(new TurnoDisponible(rs.getLong("id_disponibilidad"), nombreCompleto.trim(),
							rs.getString("especialidad"), rs.getDate("fecha").toLocalDate(),
							rs.getTime("hora_inicio").toLocalTime(), rs.getTime("hora_fin").toLocalTime()));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error buscando turnos: " + e.getMessage(), e);
		}

		if (res.isEmpty())
			throw new IllegalStateException("No se encontraron turnos disponibles en ese rango.");
		return res;
	}

	public long solicitarTurno(long idDisponibilidad) {
		if (idDisponibilidad <= 0)
			throw new IllegalArgumentException("Horario inválido.");

		final String qDisp = "SELECT id_medico, fecha, hora_inicio FROM medico_disponibilidad WHERE id = ?";

		final String qCheckTurno = "SELECT id, estado FROM turnos WHERE id_medico = ? AND fecha = ? AND hora = ?";

		final String qPac = "SELECT p.id FROM pacientes p JOIN usuarios u ON u.id_usuario = p.id_usuario WHERE u.usuario_login = ?";

		final String ins = "INSERT INTO turnos (fecha, hora, estado, id_paciente, id_medico) VALUES (?, ?, 'Reservado', ?, ?)";

		final String upd = "UPDATE turnos SET estado='Reservado', id_paciente=? WHERE id=?";

		Connection c = null;
		try {
			c = Conexion.getInstance().getConnection();
			c.setAutoCommit(false);

			// Obtener datos del horario
			Long idMedico = null;
			java.sql.Date f = null;
			java.sql.Time h = null;

			try (PreparedStatement ps = c.prepareStatement(qDisp)) {
				ps.setLong(1, idDisponibilidad);
				try (ResultSet rs = ps.executeQuery()) {
					if (!rs.next())
						throw new IllegalStateException("Horario no disponible.");
					idMedico = rs.getLong("id_medico");
					f = rs.getDate("fecha");
					h = rs.getTime("hora_inicio");
				}
			}

			// Obtener ID del paciente
			Long idPaciente = null;
			try (PreparedStatement ps = c.prepareStatement(qPac)) {
				ps.setString(1, paciente.getUsuario());
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						idPaciente = rs.getLong(1);
				}
			}
			if (idPaciente == null)
				throw new IllegalStateException("Paciente no encontrado en DB.");

			// Verificar si el turno ya existe en la tabla
			Long idTurnoExistente = null;
			String estadoExistente = null;

			try (PreparedStatement ps = c.prepareStatement(qCheckTurno)) {
				ps.setLong(1, idMedico);
				ps.setDate(2, f);
				ps.setTime(3, h);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						idTurnoExistente = rs.getLong("id");
						estadoExistente = rs.getString("estado");
					}
				}
			}

			long idTurnoFinal = 0;

			if (idTurnoExistente != null) {

				if ("Reservado".equalsIgnoreCase(estadoExistente) || "Confirmado".equalsIgnoreCase(estadoExistente)) {
					throw new IllegalStateException("Este turno ya fue reservado por otro paciente.");
				} else {

					try (PreparedStatement ps = c.prepareStatement(upd)) {
						ps.setLong(1, idPaciente);
						ps.setLong(2, idTurnoExistente);
						ps.executeUpdate();
						idTurnoFinal = idTurnoExistente;
					}
				}
			} else {

				try (PreparedStatement ps = c.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
					ps.setDate(1, f);
					ps.setTime(2, h);
					ps.setLong(3, idPaciente);
					ps.setLong(4, idMedico);
					ps.executeUpdate();
					try (ResultSet rs = ps.getGeneratedKeys()) {
						if (rs.next())
							idTurnoFinal = rs.getLong(1);
					}
				}
			}

			c.commit();
			return idTurnoFinal;

		} catch (SQLException e) {
			if (c != null)
				try {
					c.rollback();
				} catch (SQLException ex) {
				}
			throw new RuntimeException("Error reservando: " + e.getMessage(), e);
		} finally {
			if (c != null)
				try {
					c.close();
				} catch (SQLException ex) {
				}
		}
	}

	public List<Turno> turnosActivos() {

		final String sql = """
				SELECT t.id, t.fecha, t.hora, t.estado,
				       m.id AS id_medico, u.nombre, u.apellido, m.especialidad
				FROM turnos t
				JOIN pacientes p ON p.id = t.id_paciente
				JOIN usuarios up ON up.id_usuario = p.id_usuario
				JOIN medicos m ON m.id = t.id_medico
				JOIN usuarios u ON u.id_usuario = m.id_usuario
				WHERE up.usuario_login = ? AND t.estado IN ('Reservado','Confirmado')
				ORDER BY t.fecha, t.hora
				""";

		List<Turno> list = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, paciente.getUsuario());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Date fechaHora = convertirResultSetADate(rs, "fecha", "hora");

					Medico medico = new Medico(rs.getString("nombre"), rs.getString("apellido"), "", "", "",
							rs.getString("especialidad"));

					Turno t = new Turno(fechaHora, paciente, medico);
					t.setIdTurno(rs.getLong("id"));

					String est = rs.getString("estado");
					if ("Confirmado".equalsIgnoreCase(est))
						t.confirmarAsistencia();
					else if ("Cancelado".equalsIgnoreCase(est))
						t.cancelar();
					else
						t.reservar();

					list.add(t);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error listando turnos: " + e.getMessage(), e);
		}

		if (list.isEmpty())
			throw new IllegalStateException("No tienes turnos activos.");

		return list;
	}

	public void cancelarTurno(long idTurno) {
		if (idTurno <= 0)
			throw new IllegalArgumentException("ID inválido.");

		final String upd = "UPDATE turnos SET estado='Cancelado' WHERE id = ?";
		final String check = "SELECT estado FROM turnos WHERE id = ?";

		Connection c = null;
		try {
			c = Conexion.getInstance().getConnection();
			c.setAutoCommit(false);

			String estado = null;
			try (PreparedStatement ps = c.prepareStatement(check)) {
				ps.setLong(1, idTurno);
				try (ResultSet rs = ps.executeQuery()) {
					if (!rs.next())
						throw new IllegalStateException("Turno no existe");
					estado = rs.getString("estado");
				}
			}

			if ("Cancelado".equalsIgnoreCase(estado))
				throw new IllegalStateException("Ya estaba cancelado.");

			try (PreparedStatement ps = c.prepareStatement(upd)) {
				ps.setLong(1, idTurno);
				ps.executeUpdate();
			}

			c.commit();
		} catch (SQLException e) {
			if (c != null)
				try {
					c.rollback();
				} catch (SQLException ex) {
				}
			throw new RuntimeException("Error cancelando: " + e.getMessage(), e);
		} finally {
			if (c != null)
				try {
					c.close();
				} catch (SQLException ex) {
				}
		}
	}

	public void confirmarAsistencia(long idTurno) {
		if (idTurno <= 0)
			throw new IllegalArgumentException("ID inválido.");

		final String upd = "UPDATE turnos SET estado='Confirmado' WHERE id = ?";

		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(upd)) {
			ps.setLong(1, idTurno);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error confirmando: " + e.getMessage(), e);
		}
	}

	public String[] obtenerEspecialidades() {
		String sql = "SELECT DISTINCT especialidad FROM medicos ORDER BY especialidad";
		List<String> l = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection();
				Statement st = c.createStatement();
				ResultSet rs = st.executeQuery(sql)) {
			while (rs.next())
				l.add(rs.getString(1));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return l.toArray(new String[0]);
	}

	public List<Medico> obtenerTodosMedicos() {

		String sql = """
				SELECT m.id, u.nombre, u.apellido, u.usuario_login, m.matricula, m.especialidad
				FROM medicos m
				JOIN usuarios u ON m.id_usuario = u.id_usuario
				ORDER BY u.apellido
				""";
		List<Medico> l = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection();
				Statement st = c.createStatement();
				ResultSet rs = st.executeQuery(sql)) {
			while (rs.next()) {
				l.add(new Medico(rs.getString("nombre"), rs.getString("apellido"), rs.getString("usuario_login"),
						"pass", rs.getString("matricula"), rs.getString("especialidad")));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return l;
	}

	// Favoritos y Recomendaciones
	public void agregarAFavoritos(String usernameMedico) {

		String sql = """
				INSERT INTO pacientes_favoritos (id_paciente, id_medico)
				SELECT
				    (SELECT p.id FROM pacientes p JOIN usuarios u ON u.id_usuario=p.id_usuario WHERE u.usuario_login=?),
				    (SELECT m.id FROM medicos m JOIN usuarios u ON u.id_usuario=m.id_usuario WHERE u.usuario_login=?)
				FROM dual
				WHERE NOT EXISTS (
				    SELECT 1 FROM pacientes_favoritos
				    WHERE id_paciente=(SELECT p.id FROM pacientes p JOIN usuarios u ON u.id_usuario=p.id_usuario WHERE u.usuario_login=?)
				      AND id_medico=(SELECT m.id FROM medicos m JOIN usuarios u ON u.id_usuario=m.id_usuario WHERE u.usuario_login=?)
				)
				""";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, paciente.getUsuario());
			ps.setString(2, usernameMedico);
			ps.setString(3, paciente.getUsuario());
			ps.setString(4, usernameMedico);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error favoritos: " + e.getMessage(), e);
		}
	}

	public List<Medico> verFavoritos() {
		String sql = """
				SELECT u.nombre, u.apellido, u.usuario_login, m.matricula, m.especialidad
				FROM pacientes_favoritos pf
				JOIN medicos m ON m.id = pf.id_medico
				JOIN usuarios u ON u.id_usuario = m.id_usuario
				JOIN pacientes p ON p.id = pf.id_paciente
				JOIN usuarios up ON up.id_usuario = p.id_usuario
				WHERE up.usuario_login = ?
				""";
		List<Medico> l = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, paciente.getUsuario());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					l.add(new Medico(rs.getString("nombre"), rs.getString("apellido"), rs.getString("usuario_login"),
							"pass", rs.getString("matricula"), rs.getString("especialidad")));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		if (l.isEmpty())
			throw new IllegalStateException("Sin favoritos.");
		return l;
	}

	public void eliminarFavorito(String usernameMedico) {
		if (usernameMedico == null || usernameMedico.isBlank())
			throw new IllegalArgumentException("Médico inválido.");

		String sql = """
				DELETE FROM pacientes_favoritos
				WHERE id_paciente = (
				    SELECT p.id FROM pacientes p
				    JOIN usuarios u ON u.id_usuario = p.id_usuario
				    WHERE u.usuario_login = ?
				)
				AND id_medico = (
				    SELECT m.id FROM medicos m
				    JOIN usuarios u ON u.id_usuario = m.id_usuario
				    WHERE u.usuario_login = ?
				)
				""";

		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, paciente.getUsuario());
			ps.setString(2, usernameMedico);

			int filasAfectadas = ps.executeUpdate();

			if (filasAfectadas == 0) {
				System.out.println("Aviso: No se encontró el favorito para eliminar.");
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error eliminando favorito: " + e.getMessage(), e);
		}
	}

	public List<String> mostrarRecomendaciones() {
		String sql = """
				SELECT p.obra_social
				FROM pacientes p
				JOIN usuarios u ON u.id_usuario = p.id_usuario
				WHERE u.usuario_login = ?
				""";

		String obraSocial = "Particular"; // Valor por defecto

		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, paciente.getUsuario());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					String osDB = rs.getString("obra_social");
					if (osDB != null && !osDB.isBlank()) {
						obraSocial = osDB;
					}
				}
			}
		} catch (SQLException e) {
			// Si falla, seguimos con "Particular"
			System.err.println("Error buscando obra social: " + e.getMessage());
		}

		// recomendaciones según la Obra Social
		List<String> recomendaciones = new ArrayList<>();
		String osLower = obraSocial.toLowerCase();

		if (osLower.contains("osde")) {
			recomendaciones.add("- Tenés cobertura al 100% en consultas clínicas.");
			recomendaciones.add("- Emergencias OSDE: 0810-888-7788.");
			recomendaciones.add("- Recordá presentar tu credencial digital.");

		} else if (osLower.contains("swiss") || osLower.contains("medical")) {
			recomendaciones.add("- Acceso directo a especialistas sin derivación.");
			recomendaciones.add("- Urgencias Swiss Medical: 0810-333-8800.");
			recomendaciones.add("- Descuento del 50% en farmacias adheridas.");

		} else if (osLower.contains("galeno")) {
			recomendaciones.add("- Autorizaciones online inmediatas.");
			recomendaciones.add("- Llamadas de urgencia: 0810-999-8765.");

		} else if (osLower.contains("no tiene") || osLower.contains("particular")) {
			recomendaciones.add("- Aceptamos tarjetas de crédito y débito.");
			recomendaciones.add("- Pedí tu factura para reintegros.");
			recomendaciones.add("- Consultá por nuestros planes de financiación.");

		} else {
			recomendaciones.add("- Cobertura sujeta a tu plan (" + obraSocial + ").");
			recomendaciones.add("- Consultá en recepción por copagos.");
			recomendaciones.add("- Traé tu carnet físico y DNI.");
		}

		return recomendaciones;
	}

	private Date convertirResultSetADate(ResultSet rs, String colFecha, String colHora) throws SQLException {
		LocalDate fecha = rs.getDate(colFecha).toLocalDate();
		LocalTime hora = rs.getTime(colHora).toLocalTime();
		LocalDateTime ldt = LocalDateTime.of(fecha, hora);
		return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
	}

	// Historial medico

	public static record ItemHistorial(LocalDate fecha, String nombreMedico, String especialidad, String motivo,
			String diagnostico, String tratamiento, String seguimiento) {
	}

	public List<ItemHistorial> obtenerHistorialMedico() {

		final String sql = """
				SELECT
				    c.fecha, c.motivo, c.diagnostico, c.tratamiento, c.seguimiento,
				    u.nombre, u.apellido, m.especialidad
				FROM consultas c
				JOIN pacientes p ON p.id = c.id_paciente
				JOIN usuarios up ON up.id_usuario = p.id_usuario
				JOIN medicos m ON m.id = c.id_medico
				JOIN usuarios u ON u.id_usuario = m.id_usuario
				WHERE up.usuario_login = ?
				ORDER BY c.fecha DESC
				""";

		List<ItemHistorial> lista = new ArrayList<>();

		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, paciente.getUsuario());

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					LocalDate fecha = rs.getTimestamp("fecha").toLocalDateTime().toLocalDate();
					String nombreDoc = rs.getString("nombre") + " " + rs.getString("apellido");

					lista.add(new ItemHistorial(fecha, nombreDoc, rs.getString("especialidad"), rs.getString("motivo"),
							rs.getString("diagnostico"), rs.getString("tratamiento"), rs.getString("seguimiento")));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error obteniendo historial: " + e.getMessage(), e);
		}

		return lista;
	}

	public List<Medico> buscarMedicosPorEspecialidad(String especialidad) {
		String sql = """
				SELECT m.id, u.nombre, u.apellido, u.usuario_login, m.matricula, m.especialidad
				FROM medicos m
				JOIN usuarios u ON m.id_usuario = u.id_usuario
				WHERE m.especialidad LIKE ?
				ORDER BY u.apellido, u.nombre
				""";

		List<Medico> lista = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setString(1, "%" + especialidad + "%");

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					lista.add(new Medico(rs.getString("nombre"), rs.getString("apellido"),
							rs.getString("usuario_login"), "", // Pass dummy
							rs.getString("matricula"), rs.getString("especialidad")));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error buscando médicos", e);
		}
		return lista;
	}

	public List<String> obtenerProximaDisponibilidad(String usernameMedico) {
		if (usernameMedico == null)
			return new ArrayList<>();

		LocalDate hoy = LocalDate.now();
		LocalDate limite = hoy.plusDays(15);

		java.sql.Date sqlHoy = java.sql.Date.valueOf(hoy);
		java.sql.Date sqlLimite = java.sql.Date.valueOf(limite);

		final String sql = """
				SELECT md.fecha, md.hora_inicio
				FROM medico_disponibilidad md
				JOIN medicos m ON m.id = md.id_medico
				JOIN usuarios u ON u.id_usuario = m.id_usuario
				WHERE u.usuario_login = ?
				  AND md.fecha BETWEEN ? AND ?
				  AND NOT EXISTS (
				      SELECT 1 FROM turnos t
				      WHERE t.id_medico = md.id_medico
				        AND t.fecha = md.fecha
				        AND t.hora = md.hora_inicio
				        AND t.estado IN ('Reservado', 'Confirmado')
				  )
				ORDER BY md.fecha, md.hora_inicio
				LIMIT 10
				""";

		List<String> horarios = new ArrayList<>();

		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, usernameMedico);
			ps.setDate(2, sqlHoy);
			ps.setDate(3, sqlLimite);

			try (ResultSet rs = ps.executeQuery()) {
				java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM - HH:mm");
				while (rs.next()) {
					LocalDate f = rs.getDate("fecha").toLocalDate();
					LocalTime h = rs.getTime("hora_inicio").toLocalTime();
					LocalDateTime ldt = LocalDateTime.of(f, h);

					horarios.add(ldt.format(fmt));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error obteniendo disponibilidad: " + e.getMessage(), e);
		}

		return horarios;
	}
}