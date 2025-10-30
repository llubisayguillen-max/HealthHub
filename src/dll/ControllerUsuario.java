package dll;

import bll.Administrador;
import bll.Medico;
import bll.Paciente;
import bll.Usuario;

import java.sql.*;
import java.util.Optional;

public class ControllerUsuario {

	// Login

	public Optional<Usuario> login(String username, String password) {

		// validación de campos vacíos
		if (username == null || username.trim().isEmpty()) {
			throw new IllegalArgumentException("Ingrese el nombre de usuario");
		}
		if (password == null || password.trim().isEmpty()) {
			throw new IllegalArgumentException("Ingrese la contraseña");
		}

		username = username.trim();

		String sql = "SELECT id_usuario, usuario_login, contrasenia, nombre, apellido, rol, bloqueado "
				+ "FROM usuarios WHERE usuario_login=?";

		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setString(1, username);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return Optional.empty();
				}

				String passBD = rs.getString("contrasenia");
				boolean bloqueado = rs.getBoolean("bloqueado");

				if (bloqueado)
					throw new IllegalStateException("El usuario está bloqueado");

				if (!password.equals(passBD))
					return Optional.empty();

				String rol = rs.getString("rol");
				String nom = rs.getString("nombre");
				String ape = rs.getString("apellido");
				long idUsuario = rs.getLong("id_usuario");

				switch (rol == null ? "" : rol.trim().toLowerCase()) {
				case "paciente" -> {
					String q = "SELECT nro_contrato, obra_social FROM pacientes WHERE id_usuario=?";
					try (PreparedStatement ps2 = c.prepareStatement(q)) {
						ps2.setLong(1, idUsuario);
						try (ResultSet rp = ps2.executeQuery()) {
							int nro = 0;
							String os = "";
							if (rp.next()) {
								nro = rp.getInt("nro_contrato");
								os = rp.getString("obra_social");
							}
							return Optional.of(new Paciente(nom, ape, username, passBD, nro, os, null));
						}
					}
				}
				case "medico" -> {
					String q = "SELECT matricula, especialidad FROM medicos WHERE id_usuario=?";
					try (PreparedStatement ps2 = c.prepareStatement(q)) {
						ps2.setLong(1, idUsuario);
						try (ResultSet rm = ps2.executeQuery()) {
							int mat = 0;
							String esp = "";
							if (rm.next()) {
								String mStr = rm.getString("matricula");
								try {
									String digits = mStr == null ? "" : mStr.replaceAll("\\D+", "");
									mat = digits.isEmpty() ? 0 : Integer.parseInt(digits);
								} catch (Exception ignore) {
									mat = 0;
								}
								esp = rm.getString("especialidad");
							}
							return Optional.of(new Medico(nom, ape, username, passBD, mat, esp));
						}
					}
				}
				case "administrador" -> {
					String sector = "Administración";
					return Optional.of(new Administrador(nom, ape, username, passBD, sector));
				}
				default -> {
					return Optional.empty();
				}
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("Error en login", e);
		}
	}

	// Validación Usuario
	public boolean existeUsuario(String username) {
		String sql = "SELECT COUNT(*) FROM usuarios WHERE usuario_login=?";
		try (Connection c = Conexion.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, username);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error verificando existencia de usuario", e);
		}
	}
}