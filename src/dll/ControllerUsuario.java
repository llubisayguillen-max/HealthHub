package dll;

import bll.Administrador;
import bll.Medico;
import bll.Paciente;
import bll.Usuario;

import java.sql.*;
import java.util.Optional;

public class ControllerUsuario {

    public Optional<Usuario> login(String username, String password) {
        String sql = "SELECT id_usuario, usuario_login, contrasenia, nombre, apellido, rol " +
                     "FROM usuarios WHERE usuario_login=? AND contrasenia=?";
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                String rol = rs.getString("rol");
                String nom = rs.getString("nombre");
                String ape = rs.getString("apellido");
                String usr = rs.getString("usuario_login");
                String pass = rs.getString("contrasenia");

                switch (rol == null ? "" : rol.trim().toLowerCase()) {
                    case "paciente" -> {
                        // trae datos del paciente
                        String q = "SELECT p.nro_contrato, p.obra_social " +
                                   "FROM pacientes p WHERE p.id_usuario=?";
                        try (PreparedStatement ps2 = c.prepareStatement(q)) {
                            ps2.setLong(1, rs.getLong("id_usuario"));
                            try (ResultSet rp = ps2.executeQuery()) {
                                int nro = 0; String os = "";
                                if (rp.next()) { nro = rp.getInt("nro_contrato"); os = rp.getString("obra_social"); }
                                return Optional.of(new Paciente(nom, ape, usr, pass, nro, os, null));
                            }
                        }
                    }
                    case "medico" -> {
                        String q = "SELECT m.matricula, m.especialidad FROM medicos m WHERE m.id_usuario=?";
                        try (PreparedStatement ps2 = c.prepareStatement(q)) {
                            ps2.setLong(1, rs.getLong("id_usuario"));
                            try (ResultSet rm = ps2.executeQuery()) {
                                int mat = 0; String esp = "";
                                if (rm.next()) {
                                    String mStr = rm.getString("matricula");
                                    try {
                                        String digits = mStr == null ? "" : mStr.replaceAll("\\D+", "");
                                        mat = digits.isEmpty() ? 0 : Integer.parseInt(digits);
                                    } catch (Exception ignore) { mat = 0; }
                                    esp = rm.getString("especialidad");
                                }
                                return Optional.of(new Medico(nom, ape, usr, pass, mat, esp));
                            }
                        }
                    }
                    case "administrador" -> {
                    	
                        String sector = "Administración";
                        return Optional.of(new Administrador(nom, ape, usr, pass, sector));
                    }
                    default -> {
                        // si el rol es desconocido
                        return Optional.empty();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en login", e);
        }
    }
}
