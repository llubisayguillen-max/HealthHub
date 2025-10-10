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

	// busca medico por especialidad
	public List<Medico> filtrarPorEspecialidad(String especialidad) {
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

	// devuelve las franjas horarias disponibles del medico
	public List<String> obtenerHorariosDisponibles(String usernameMedico) {
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
		return franjas;
	}

	// reserva de turno
	public long solicitarTurno(String usernameMedico, Date fechaHora) {
		// verificar disponibilidad en ese horario
		String sqlSolape = "SELECT 1 " + "FROM turnos t " + "JOIN medicos m ON m.id = t.id_medico "
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

			// inserta el turno
			String sqlInsert = "INSERT INTO turnos (fecha, hora, estado, id_paciente, id_medico) "
					+ "VALUES (?, ?, 'Reservado', "
					+ " (SELECT p.id FROM pacientes p JOIN usuarios up ON up.id_usuario=p.id_usuario WHERE up.usuario_login=?), "
					+ " (SELECT m.id FROM medicos   m JOIN usuarios um ON um.id_usuario=m.id_usuario WHERE um.usuario_login=?) )";

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

	// lista de turnos del paciente logueado
	public List<Turno> turnosActivos() {
		String sql = "SELECT t.fecha, t.hora " + "FROM turnos t " + "JOIN pacientes p ON p.id = t.id_paciente "
				+ "JOIN usuarios u ON u.id_usuario = p.id_usuario " + "WHERE u.usuario_login = ? "
				+ "AND t.estado IN ('Reservado','Confirmado') " + "ORDER BY t.fecha, t.hora";

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
		return list;
	}

	// cancela el turno por id
	public void cancelarTurno(long idTurno) {
		String sql = "UPDATE turnos SET estado='Cancelado' WHERE id = ?";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, idTurno);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error cancelando turno", e);
		}
	}

	// confirma asistencia del tueno por id
	public void confirmarAsistencia(long idTurno) {
		String sql = "UPDATE turnos SET estado='Confirmado' WHERE id = ?";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, idTurno);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error confirmando asistencia", e);
		}
	}

	// agrega un medico a favoritos
	public void agregarAFavoritos(String usernameMedico) {
		String sql = "INSERT INTO pacientes_favoritos (id_paciente, id_medico) " + "SELECT "
				+ "  (SELECT p.id FROM pacientes p JOIN usuarios up ON up.id_usuario=p.id_usuario WHERE up.usuario_login=?), "
				+ "  (SELECT m.id FROM medicos   m JOIN usuarios um ON um.id_usuario=m.id_usuario WHERE um.usuario_login=?) "
				+ "FROM dual " + "WHERE NOT EXISTS ( " + "  SELECT 1 FROM pacientes_favoritos pf "
				+ "  WHERE pf.id_paciente = (SELECT p.id FROM pacientes p JOIN usuarios up ON up.id_usuario=p.id_usuario WHERE up.usuario_login=?) "
				+ "    AND pf.id_medico   = (SELECT m.id FROM medicos   m JOIN usuarios um ON um.id_usuario=m.id_usuario WHERE um.usuario_login=?) "
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

	// devuelve la lista de medicos favoritos
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
		return res;
	}

	// recomendaciones
	public List<String> mostrarRecomendaciones() {
		String sql = "SELECT p.obra_social " + "FROM pacientes p " + "JOIN usuarios u ON u.id_usuario = p.id_usuario "
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

		List<String> recs = new ArrayList<>();
		if (obra == null)
			obra = "general";
		String os = obra.trim().toLowerCase();

		switch (os) {
		case "osde" -> {
			recs.add("Turnos online con prioridad en especialistas.");
			recs.add("Chequeo anual con reintegros ampliados.");
		}
		case "swiss medical" -> {
			recs.add("Consultas virtuales 24/7 sin cargo.");
			recs.add("Cobertura extendida en estudios de alta complejidad.");
		}
		case "pami" -> {
			recs.add("Control de salud gratuito cada 6 meses.");
			recs.add("Plan de medicación crónica con descuentos.");
		}
		default -> {
			recs.add("Consultá beneficios específicos con tu obra social.");
			recs.add("Aprovechá clínicas y laboratorios en cartilla para menor copago.");
		}
		}
		return recs;
	}
}
