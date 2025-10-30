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

	public ControllerPaciente(Paciente paciente) {
		this.paciente = paciente;
	}

	// Filtra por especialidad
	public List<Medico> filtrarPorEspecialidad(String especialidad) {
		if (especialidad == null)
			especialidad = "";
		String sql = """
				SELECT um.nombre, um.apellido, um.usuario_login, um.contrasenia, m.matricula, m.especialidad
				FROM medicos m
				JOIN usuarios um ON um.id_usuario = m.id_usuario
				WHERE m.especialidad LIKE ?
				ORDER BY um.apellido, um.nombre
				""";

		List<Medico> res = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, "%" + especialidad + "%");
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					res.add(crearMedicoDesdeResultSet(rs));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error filtrando por especialidad", e);
		}
		return res;
	}

	public String[] obtenerEspecialidades() {
		String sql = "SELECT DISTINCT especialidad FROM medicos ORDER BY especialidad";
		List<String> lista = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection();
				PreparedStatement ps = c.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next())
				lista.add(rs.getString("especialidad"));
		} catch (SQLException e) {
			throw new RuntimeException("Error obteniendo especialidades", e);
		}
		return lista.toArray(new String[0]);
	}

	// Mostrar fecha + horarios disponibles
	public List<String> obtenerHorariosDisponibles(String usernameMedico) {
		if (usernameMedico == null || usernameMedico.trim().isEmpty())
			throw new IllegalArgumentException("Debe seleccionar un médico válido.");

		String sql = """
				SELECT md.fecha, md.hora_inicio, md.hora_fin
				FROM medico_disponibilidad md
				JOIN medicos m ON m.id = md.id_medico
				JOIN usuarios u ON u.id_usuario = m.id_usuario
				WHERE u.usuario_login = ?
				ORDER BY md.fecha, md.hora_inicio
				""";

		List<String> franjas = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, usernameMedico);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Date fecha = rs.getDate("fecha");
					Time inicio = rs.getTime("hora_inicio");
					Time fin = rs.getTime("hora_fin");
					franjas.add(String.format("%s - %s a %s", fecha, inicio, fin));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error obteniendo disponibilidad", e);
		}

		if (franjas.isEmpty())
			throw new IllegalStateException("No hay horarios disponibles para este médico.");

		return franjas;
	}

	// Solicitar turno
	public long solicitarTurno(Medico medico, Date fechaHora) {
		if (medico == null)
			throw new IllegalArgumentException("Debe seleccionar un médico válido.");
		if (fechaHora == null)
			throw new IllegalArgumentException("Debe seleccionar una fecha y hora válidas.");
		if (fechaHora.before(new Date()))
			throw new IllegalArgumentException("No se puede reservar un turno en el pasado.");

		String usernameMedico = medico.getUsuario();
		java.sql.Date f = new java.sql.Date(fechaHora.getTime());
		java.sql.Time h = new java.sql.Time(fechaHora.getTime());

		try (Connection c = Conexion.getInstance().getConnection()) {
			// Verificar solapamiento médico
			verificarSolapamientoMedico(c, usernameMedico, f, h);

			// Verificar solapamiento paciente
			for (Turno t : paciente.getTurnos()) {
				if (t.getFechaHora().equals(fechaHora))
					throw new IllegalStateException("Ya tienes un turno reservado en esa fecha y hora.");
			}

			// Insertar el turno
			String sqlInsert = """
					INSERT INTO turnos (fecha, hora, estado, id_paciente, id_medico)
					VALUES (?, ?, 'Reservado',
					       (SELECT p.id FROM pacientes p JOIN usuarios up ON up.id_usuario=p.id_usuario WHERE up.usuario_login=?),
					       (SELECT m.id FROM medicos m JOIN usuarios um ON um.id_usuario=m.id_usuario WHERE um.usuario_login=?))
					""";

			try (PreparedStatement ps = c.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
				ps.setDate(1, f);
				ps.setTime(2, h);
				ps.setString(3, paciente.getUsuario());
				ps.setString(4, usernameMedico);
				ps.executeUpdate();

				try (ResultSet rs = ps.getGeneratedKeys()) {
					long id = rs.next() ? rs.getLong(1) : 0L;
					paciente.agregarTurno(new Turno(fechaHora, paciente, null));
					return id;
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("Error al reservar turno", e);
		}
	}

	public long solicitarTurno(String usernameMedico, Date fechaHora) {
		if (usernameMedico == null || usernameMedico.trim().isEmpty())
			throw new IllegalArgumentException("Debe ingresar un nombre de usuario de médico válido.");

		List<Medico> medicos = filtrarPorEspecialidad("");
		Medico medicoSeleccionado = medicos.stream().filter(m -> m.getUsuario().equalsIgnoreCase(usernameMedico.trim()))
				.findFirst().orElseThrow(
						() -> new IllegalStateException("No se encontró el médico con username: " + usernameMedico));

		return solicitarTurno(medicoSeleccionado, fechaHora);
	}

	private void verificarSolapamientoMedico(Connection c, String usernameMedico, java.sql.Date f, java.sql.Time h)
			throws SQLException {
		String sqlSolape = """
				SELECT 1 FROM turnos t
				JOIN medicos m ON m.id = t.id_medico
				JOIN usuarios u ON u.id_usuario = m.id_usuario
				WHERE u.usuario_login = ? AND t.fecha = ? AND t.hora = ? AND t.estado IN ('Reservado','Confirmado')
				""";
		try (PreparedStatement ps = c.prepareStatement(sqlSolape)) {
			ps.setString(1, usernameMedico);
			ps.setDate(2, f);
			ps.setTime(3, h);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					throw new IllegalStateException("El médico ya tiene un turno en ese horario.");
			}
		}
	}

	// Turnos activos
	public List<Turno> turnosActivos() {
		String sql = """
				SELECT t.id, t.fecha, t.hora, t.estado
				FROM turnos t
				JOIN pacientes p ON p.id = t.id_paciente
				JOIN usuarios u ON u.id_usuario = p.id_usuario
				WHERE u.usuario_login = ? AND t.estado IN ('Reservado','Confirmado')
				ORDER BY t.fecha, t.hora
				""";

		List<Turno> list = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, paciente.getUsuario());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Date fechaHora = convertirResultSetADate(rs, "fecha", "hora");
					Turno t = new Turno(fechaHora, paciente, null);
					t.setIdTurno(rs.getLong("id"));

					String estadoDB = rs.getString("estado");
					switch (estadoDB) {
					case "Reservado" -> t.reservar();
					case "Cancelado" -> t.cancelar();
					case "Confirmado" -> t.confirmarAsistencia();
					}

					list.add(t);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error listando turnos", e);
		}

		if (list.isEmpty())
			throw new IllegalStateException("No tienes turnos activos.");

		return list;
	}

	private Date convertirResultSetADate(ResultSet rs, String colFecha, String colHora) throws SQLException {
		LocalDate fecha = rs.getDate(colFecha).toLocalDate();
		LocalTime hora = rs.getTime(colHora).toLocalTime();
		LocalDateTime ldt = LocalDateTime.of(fecha, hora);
		ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
		return Date.from(zdt.toInstant());
	}

	// cancelar / confirmar turno
	public void cancelarTurno(long idTurno) {
		cambiarEstadoTurno(idTurno, "Cancelado", "El turno ya estaba cancelado.");
	}

	public void confirmarAsistencia(long idTurno) {
		cambiarEstadoTurno(idTurno, "Confirmado", "El turno ya fue confirmado.", true);
	}

	private void cambiarEstadoTurno(long idTurno, String nuevoEstado, String mensajeSiYa, boolean esConfirmacion) {
		if (idTurno <= 0)
			throw new IllegalArgumentException("ID de turno inválido.");

		String sqlCheck = "SELECT estado FROM turnos WHERE id = ?";
		try (Connection c = Conexion.getInstance().getConnection();
				PreparedStatement ps = c.prepareStatement(sqlCheck)) {

			ps.setLong(1, idTurno);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					throw new IllegalStateException("El turno no existe.");
				String estado = rs.getString("estado");
				if (nuevoEstado.equalsIgnoreCase(estado))
					throw new IllegalStateException(mensajeSiYa);
				if (esConfirmacion && "Cancelado".equalsIgnoreCase(estado))
					throw new IllegalStateException("No se puede confirmar un turno cancelado.");
			}

			String sqlUpdate = "UPDATE turnos SET estado=? WHERE id=?";
			try (PreparedStatement ps2 = c.prepareStatement(sqlUpdate)) {
				ps2.setString(1, nuevoEstado);
				ps2.setLong(2, idTurno);
				ps2.executeUpdate();
			}

		} catch (SQLException e) {
			throw new RuntimeException("Error actualizando turno", e);
		}
	}

	private void cambiarEstadoTurno(long idTurno, String nuevoEstado, String mensajeSiYa) {
		cambiarEstadoTurno(idTurno, nuevoEstado, mensajeSiYa, false);
	}

	// Favoritos
	public void agregarAFavoritos(String usernameMedico) {
		if (usernameMedico == null || usernameMedico.trim().isEmpty())
			throw new IllegalArgumentException("Debe seleccionar un médico válido.");

		String sql = """
				INSERT INTO pacientes_favoritos (id_paciente, id_medico)
				SELECT
				  (SELECT p.id FROM pacientes p JOIN usuarios up ON up.id_usuario=p.id_usuario WHERE up.usuario_login=?),
				  (SELECT m.id FROM medicos m JOIN usuarios um ON um.id_usuario=m.id_usuario WHERE um.usuario_login=?)
				FROM dual
				WHERE NOT EXISTS (
				  SELECT 1 FROM pacientes_favoritos pf
				  WHERE pf.id_paciente = (SELECT p.id FROM pacientes p JOIN usuarios up ON up.id_usuario=p.id_usuario WHERE up.usuario_login=?)
				    AND pf.id_medico = (SELECT m.id FROM medicos m JOIN usuarios um ON um.id_usuario=m.id_usuario WHERE um.usuario_login=?)
				)
				""";

		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, paciente.getUsuario());
			ps.setString(2, usernameMedico);
			ps.setString(3, paciente.getUsuario());
			ps.setString(4, usernameMedico);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error agregando a favoritos", e);
		}
	}

	public List<Medico> verFavoritos() {
		String sql = """
				SELECT um.nombre, um.apellido, um.usuario_login, um.contrasenia, m.matricula, m.especialidad
				FROM pacientes_favoritos pf
				JOIN pacientes p  ON p.id = pf.id_paciente
				JOIN medicos m    ON m.id = pf.id_medico
				JOIN usuarios um  ON um.id_usuario = m.id_usuario
				JOIN usuarios up  ON up.id_usuario = p.id_usuario
				WHERE up.usuario_login = ?
				ORDER BY um.apellido, um.nombre
				""";

		List<Medico> res = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, paciente.getUsuario());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					res.add(crearMedicoDesdeResultSet(rs));
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error listando favoritos", e);
		}

		if (res.isEmpty())
			throw new IllegalStateException("No tienes médicos favoritos.");
		return res;
	}

	public List<Medico> obtenerTodosMedicos() {
		String sql = """
				SELECT m.id, u.nombre, u.apellido, u.usuario_login, m.matricula, m.especialidad
				FROM medicos m
				JOIN usuarios u ON m.id_usuario = u.id_usuario
				ORDER BY u.apellido, u.nombre
				""";

		List<Medico> medicos = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection();
				PreparedStatement ps = c.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				Medico m = new Medico(rs.getString("nombre"), rs.getString("apellido"), rs.getString("usuario_login"),
						"dummyPass", rs.getString("matricula"), rs.getString("especialidad"));
				medicos.add(m);
			}

		} catch (SQLException e) {
			throw new RuntimeException("Error al obtener médicos", e);
		}

		return medicos;
	}

	// Recomendaciones
	public List<String> mostrarRecomendaciones() {
		String sql = """
				SELECT p.obra_social
				FROM pacientes p
				JOIN usuarios u ON u.id_usuario = p.id_usuario
				WHERE u.usuario_login = ?
				""";

		String obra = "general";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, paciente.getUsuario());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					obra = rs.getString("obra_social");
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error obteniendo obra social", e);
		}

		return List.of("Recomendaciones generales para obra social: " + obra);
	}

	// Métodos auxiliares
	private int parseMatricula(String matricula) {
		if (matricula == null)
			return 0;
		String digits = matricula.replaceAll("\\D+", "");
		return digits.isEmpty() ? 0 : Integer.parseInt(digits);
	}

	private Medico crearMedicoDesdeResultSet(ResultSet rs) throws SQLException {
		return new Medico(rs.getString("nombre"), rs.getString("apellido"), rs.getString("usuario_login"),
				rs.getString("contrasenia"), parseMatricula(rs.getString("matricula")), rs.getString("especialidad"));
	}
}
