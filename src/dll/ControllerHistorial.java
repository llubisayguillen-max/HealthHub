package dll;

import bll.HistorialMedico;
import bll.Medico;
import bll.Paciente;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ControllerHistorial {

    private final Connection conn;

    public ControllerHistorial(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("La conexión no puede ser null");
        }
        this.conn = conn;
    }

    //CREAR REGISTRO

    public boolean crearRegistro(Paciente paciente, Medico medico, String descripcion) {
        if (paciente == null || medico == null)
            throw new IllegalArgumentException("Paciente y médico son requeridos.");
        if (descripcion == null) descripcion = "";

        try {
            Long idPaciente = obtenerIdPacientePorUsuario(paciente.getUsuario());
            Long idMedico = obtenerIdMedicoPorUsuario(medico.getUsuario());

            if (idPaciente == null)
                throw new IllegalStateException("Paciente no encontrado en DB: " + paciente.getUsuario());
            if (idMedico == null)
                throw new IllegalStateException("Médico no encontrado en DB: " + medico.getUsuario());

            String sql = "INSERT INTO historial_medico (id_paciente, id_medico, fecha, descripcion) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idPaciente);
                ps.setLong(2, idMedico);
                ps.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                ps.setString(4, descripcion);
                return ps.executeUpdate() > 0;
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Error creando registro de historial: " + ex.getMessage(), ex);
        }
    }

    //OBTENER ID

    private Long obtenerIdPacientePorUsuario(String usuario) throws SQLException {
        if (usuario == null || usuario.isBlank()) return null;
        String q = "SELECT p.id FROM pacientes p JOIN usuarios u ON p.id_usuario = u.id_usuario WHERE u.usuario_login = ?";
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("id") : null;
            }
        }
    }

    private Long obtenerIdMedicoPorUsuario(String usuario) throws SQLException {
        if (usuario == null || usuario.isBlank()) return null;
        String q = "SELECT m.id FROM medicos m JOIN usuarios u ON m.id_usuario = u.id_usuario WHERE u.usuario_login = ?";
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("id") : null;
            }
        }
    }

    //LISTAR REGISTROS

    public List<HistorialMedico> listarPorPaciente(long idPaciente) {
        List<HistorialMedico> lista = new ArrayList<>();

        String sql = """
            SELECT h.id_registro, h.fecha, h.descripcion,
                   m.id_usuario AS id_medico_usuario,
                   u.usuario_login AS medico_usuario,
                   u.nombre AS medico_nombre,
                   u.apellido AS medico_apellido,
                   m.matricula, m.especialidad
            FROM historial_medico h
            LEFT JOIN medicos m ON h.id_medico = m.id
            LEFT JOIN usuarios u ON m.id_usuario = u.id_usuario
            WHERE h.id_paciente = ?
            ORDER BY h.fecha DESC
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPaciente);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearRegistro(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listando historial por paciente", e);
        }

        return lista;
    }

    public List<HistorialMedico> listarPorPaciente(String usuario) {
        try {
            Long idPaciente = obtenerIdPacientePorUsuario(usuario);
            if (idPaciente == null) return new ArrayList<>();
            return listarPorPaciente(idPaciente);
        } catch (SQLException e) {
            throw new RuntimeException("Error obteniendo historial por usuario: " + usuario, e);
        }
    }

    public List<HistorialMedico> listarPorMedico(long idMedico) {
        List<HistorialMedico> lista = new ArrayList<>();
        String sql = "SELECT * FROM historial_medico WHERE id_medico = ? ORDER BY fecha DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idMedico);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearRegistro(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listando historial por médico", e);
        }

        return lista;
    }


    public HistorialMedico obtenerPorId(long idRegistro) {
        String sql = "SELECT * FROM historial_medico WHERE id_registro = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idRegistro);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapearRegistro(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error obteniendo historial por ID", e);
        }

        return null;
    }

    //ELIMINAR 

    public boolean eliminar(long idRegistro) {
        String sql = "DELETE FROM historial_medico WHERE id_registro = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idRegistro);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando registro de historial", e);
        }
    }

    // MAPEAR REGISTRO

    private HistorialMedico mapearRegistro(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_registro");
        LocalDate fecha = rs.getDate("fecha").toLocalDate();
        String descripcion = rs.getString("descripcion");

       
        Medico medico = null;
        String nombre = rs.getString("medico_nombre");
        String apellido = rs.getString("medico_apellido");
        String usuario = rs.getString("medico_usuario");
        String matricula = rs.getString("matricula");
        String especialidad = rs.getString("especialidad");

        if (nombre != null && apellido != null && usuario != null) {
            medico = new Medico(nombre, apellido, usuario, "", matricula != null ? matricula : "", especialidad != null ? especialidad : "");
        }

        
        return new HistorialMedico(id, null, medico, fecha, descripcion, new String[0]);
    }
}
