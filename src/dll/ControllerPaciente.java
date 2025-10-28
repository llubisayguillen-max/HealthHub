package dll;

import bll.Medico;
import bll.Paciente;
import bll.Turno;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ControllerPaciente {

	private final Paciente paciente;

	public ControllerPaciente(Paciente paciente) {
		this.paciente = paciente;
	}

	// Filtra por especialidad

	public List<Medico> filtrarPorEspecialidad(String especialidad) {
		if (especialidad == null || especialidad.trim().isEmpty())
			throw new IllegalArgumentException("Debe ingresar una especialidad válida.");

		String sql = "SELECT um.nombre, um.apellido, um.usuario_login, um.contrasenia, m.matricula, m.especialidad "
				+ "FROM medicos m " + "JOIN usuarios um ON um.id_usuario = m.id_usuario "
				+ "WHERE m.especialidad LIKE ? " + "ORDER BY um.apellido, um.nombre";

		List<Medico> res = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setString(1, especialidad);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int mat;
					try {
						String m = rs.getString("matricula");
						String digits = (m == null) ? "" : m.replaceAll("\\D+", "");
						mat = digits.isEmpty() ? 0 : Integer.parseInt(digits);
					} catch (Exception ignore) {
						mat = 0;
					}

					res.add(new Medico(rs.getString("nombre"), rs.getString("apellido"), rs.getString("usuario_login"),
							rs.getString("contrasenia"), mat, rs.getString("especialidad")));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error filtrando por especialidad", e);
		}
		return res;
	}

	// Horarios disponibles médico

	public List<String> obtenerHorariosDisponibles(String usernameMedico) {
		if (usernameMedico == null || usernameMedico.trim().isEmpty())
			throw new IllegalArgumentException("Debe seleccionar un médico válido.");

		String sql = "SELECT md.hora_inicio, md.hora_fin " + "FROM medico_disponibilidad md "
				+ "JOIN medicos m ON m.id = md.id_medico " + "JOIN usuarios u ON u.id_usuario = m.id_usuario "
				+ "WHERE u.usuario_login = ? " + "ORDER BY md.hora_inicio";

		List<String> franjas = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, usernameMedico);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Time hi = rs.getTime("hora_inicio");
					Time hf = rs.getTime("hora_fin");
					franjas.add(hi + " - " + hf);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error obteniendo disponibilidad", e);
		}

		if (franjas.isEmpty())
			throw new IllegalStateException("No hay horarios disponibles para este médico.");

		return franjas;
	}

	// Solicitar Turno

	public long solicitarTurno(Medico medico, Date fechaHora) {
		if (medico == null)
			throw new IllegalArgumentException("Debe seleccionar un médico válido.");
		if (fechaHora == null)
			throw new IllegalArgumentException("Debe seleccionar una fecha y hora válidas.");
		if (fechaHora.before(new Date()))
			throw new IllegalArgumentException("No se puede reservar un turno en el pasado.");

		String usernameMedico = medico.getUsuario();

		// Verificar solapamiento
		String sqlSolape = "SELECT 1 FROM turnos t " + "JOIN medicos m ON m.id = t.id_medico "
				+ "JOIN usuarios u ON u.id_usuario = m.id_usuario "
				+ "WHERE u.usuario_login = ? AND t.fecha = ? AND t.hora = ? "
				+ "AND t.estado IN ('Reservado','Confirmado')";

		java.sql.Date f = new java.sql.Date(fechaHora.getTime());
		java.sql.Time h = new java.sql.Time(fechaHora.getTime());

		try (Connection c = Conexion.getInstance().getConnection()) {
			try (PreparedStatement ps = c.prepareStatement(sqlSolape)) {
				ps.setString(1, usernameMedico);
				ps.setDate(2, f);
				ps.setTime(3, h);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						throw new IllegalStateException("El médico ya tiene un turno en ese horario.");
					}
				}
			}

			for (Turno t : paciente.getTurnos()) {
				if (t.getFechaHora().equals(fechaHora))
					throw new IllegalStateException("Ya tienes un turno reservado en esa fecha y hora.");
			}

			// Insertar el turno
			String sqlInsert = "INSERT INTO turnos (fecha, hora, estado, id_paciente, id_medico) "
					+ "VALUES (?, ?, 'Reservado', "
					+ "(SELECT p.id FROM pacientes p JOIN usuarios up ON up.id_usuario=p.id_usuario WHERE up.usuario_login=?), "
					+ "(SELECT m.id FROM medicos m JOIN usuarios um ON um.id_usuario=m.id_usuario WHERE um.usuario_login=?))";

			try (PreparedStatement ps = c.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
				ps.setDate(1, f);
				ps.setTime(2, h);
				ps.setString(3, paciente.getUsuario());
				ps.setString(4, usernameMedico);
				ps.executeUpdate();

				try (ResultSet rs = ps.getGeneratedKeys()) {
					long id = rs.next() ? rs.getLong(1) : 0L;
					Turno t = new Turno(fechaHora, paciente, medico);
					t.setIdTurno(id);
					paciente.agregarTurno(t);
					return id;
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("Error al reservar turno", e);
		}
	}

	// turnos activos
	public List<Turno> turnosActivos() {
		String sql = "SELECT t.fecha, t.hora FROM turnos t " + "JOIN pacientes p ON p.id = t.id_paciente "
				+ "JOIN usuarios u ON u.id_usuario = p.id_usuario "
				+ "WHERE u.usuario_login = ? AND t.estado IN ('Reservado','Confirmado') " + "ORDER BY t.fecha, t.hora";

		List<Turno> list = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setString(1, paciente.getUsuario());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Date d = new Date(rs.getDate("fecha").getTime() + rs.getTime("hora").getTime());
					list.add(new Turno(d, paciente, null));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error listando turnos", e);
		}

		if (list.isEmpty())
			throw new IllegalStateException("No tienes turnos activos.");

		return list;
	}

	// cancelar turno
	public void cancelarTurno(long idTurno) {
		if (idTurno <= 0)
			throw new IllegalArgumentException("ID de turno inválido.");

		String sqlCheck = "SELECT t.estado FROM turnos t WHERE t.id = ?";
		try (Connection c = Conexion.getInstance().getConnection();
				PreparedStatement ps = c.prepareStatement(sqlCheck)) {
			ps.setLong(1, idTurno);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					throw new IllegalStateException("El turno no existe.");
				String estado = rs.getString("estado");
				if ("Cancelado".equalsIgnoreCase(estado))
					throw new IllegalStateException("El turno ya estaba cancelado.");
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error verificando turno", e);
		}

		String sql = "UPDATE turnos SET estado='Cancelado' WHERE id = ?";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, idTurno);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error cancelando turno", e);
		}
	}

	// Favoritos

	public void agregarAFavoritos(String usernameMedico) {
		if (usernameMedico == null || usernameMedico.trim().isEmpty())
			throw new IllegalArgumentException("Debe seleccionar un médico válido.");

		String sql = "INSERT INTO pacientes_favoritos (id_paciente, id_medico) " + "SELECT "
				+ "(SELECT p.id FROM pacientes p JOIN usuarios up ON up.id_usuario=p.id_usuario WHERE up.usuario_login=?), "
				+ "(SELECT m.id FROM medicos m JOIN usuarios um ON um.id_usuario=m.id_usuario WHERE um.usuario_login=?) "
				+ "FROM dual " + "WHERE NOT EXISTS ( " + "SELECT 1 FROM pacientes_favoritos pf "
				+ "WHERE pf.id_paciente = (SELECT p.id FROM pacientes p JOIN usuarios up ON up.id_usuario=p.id_usuario WHERE up.usuario_login=?) "
				+ "AND pf.id_medico = (SELECT m.id FROM medicos m JOIN usuarios um ON um.id_usuario=m.id_usuario WHERE um.usuario_login=?)"
				+ ")";

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
		String sql = "SELECT um.nombre, um.apellido, um.usuario_login, um.contrasenia, m.matricula, m.especialidad "
				+ "FROM pacientes_favoritos pf " + "JOIN pacientes p  ON p.id = pf.id_paciente "
				+ "JOIN medicos m    ON m.id = pf.id_medico " + "JOIN usuarios um  ON um.id_usuario = m.id_usuario "
				+ "JOIN usuarios up  ON up.id_usuario = p.id_usuario " + "WHERE up.usuario_login = ? "
				+ "ORDER BY um.apellido, um.nombre";

		List<Medico> res = new ArrayList<>();
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, paciente.getUsuario());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int mat;
					try {
						String m = rs.getString("matricula");
						String digits = (m == null) ? "" : m.replaceAll("\\D+", "");
						mat = digits.isEmpty() ? 0 : Integer.parseInt(digits);
					} catch (Exception ignore) {
						mat = 0;
					}

					res.add(new Medico(rs.getString("nombre"), rs.getString("apellido"), rs.getString("usuario_login"),
							rs.getString("contrasenia"), mat, rs.getString("especialidad")));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error listando favoritos", e);
		}

		if (res.isEmpty())
			throw new IllegalStateException("No tienes médicos favoritos.");
		return res;
	}

	// Recomendaciones

	public List<String> mostrarRecomendaciones() {
		String sql = "SELECT p.obra_social FROM pacientes p " + "JOIN usuarios u ON u.id_usuario = p.id_usuario "
				+ "WHERE u.usuario_login = ?";

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

		List<String> recomendaciones = new ArrayList<>();
		recomendaciones.add("Recomendaciones generales para obra social: " + obra);
		return recomendaciones;
	}

}
