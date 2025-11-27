package dll;

import bll.Usuario;
import bll.Medico;
import bll.Paciente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ControllerAdministrador {

    private final Usuario admin;
    private final Connection conn;
    private final ControllerHistorial historialManager;

    public ControllerAdministrador(Usuario admin) {
        if (admin == null || !"Administrador".equalsIgnoreCase(admin.getRol())) {
            throw new SecurityException("Acceso denegado: solo el usuario Administrador puede gestionar roles y permisos.");
        }
        this.admin = admin;

        // Inicializar conexión
        try {
            this.conn = Conexion.getInstance().getConnection();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo obtener la conexión a la base de datos.", e);
        }

        // Inicializar ControllerHistorial con la misma conexión
        this.historialManager = new ControllerHistorial(this.conn);
    }

    public ControllerHistorial getHistorialManager() {
        return historialManager;
    }

    // ====================== VALIDACIONES ======================
    private boolean existeUsuario(String usuario) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE usuario_login=?";
        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error verificando existencia de usuario", e);
        }
    }

    private boolean existeMatricula(String matricula) {
        String sql = "SELECT COUNT(*) FROM medicos WHERE matricula=?";
        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error verificando existencia de matrícula", e);
        }
    }

    private void validarNoVacio(String campo, String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede estar vacío.");
        }
    }

    // ====================== PACIENTES ======================

    public void registrarPaciente(String usuario, String nombre, String apellido, String contrasenia,
                                  int nroContrato, String obraSocial) {

        validarNoVacio("Usuario", usuario);
        validarNoVacio("Nombre", nombre);
        validarNoVacio("Apellido", apellido);
        validarNoVacio("Contraseña", contrasenia);
        validarNoVacio("Obra social", obraSocial);

        if (nroContrato <= 0)
            throw new IllegalArgumentException("El número de contrato debe ser mayor que cero.");

        if (existeUsuario(usuario))
            throw new IllegalArgumentException("El usuario '" + usuario + "' ya existe.");

        String sqlUsuario = "INSERT INTO usuarios(usuario_login, contrasenia, nombre, apellido, rol) VALUES (?, ?, ?, ?, 'Paciente')";
        String sqlPaciente = "INSERT INTO pacientes(id_usuario, nro_contrato, obra_social) VALUES (?, ?, ?)";

        try {
            conn.setAutoCommit(false);

            String passEncriptada = Encriptador.encriptar(contrasenia);
            long idUsuario;

            try (PreparedStatement psUser = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, usuario);
                psUser.setString(2, passEncriptada);
                psUser.setString(3, nombre);
                psUser.setString(4, apellido);
                psUser.executeUpdate();

                try (ResultSet keys = psUser.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No se pudo obtener id_usuario");
                    idUsuario = keys.getLong(1);
                }
            }

            try (PreparedStatement psPac = conn.prepareStatement(sqlPaciente)) {
                psPac.setLong(1, idUsuario);
                psPac.setInt(2, nroContrato);
                psPac.setString(3, obraSocial);
                psPac.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            throw new RuntimeException("Error registrando paciente: " + e.getMessage(), e);
        }
    }

    public void modificarPaciente(String usuario, String nombre, String apellido, String contrasenia,
                                  int nroContrato, String obraSocial) {

        if (!existeUsuario(usuario))
            throw new IllegalArgumentException("El usuario '" + usuario + "' no existe.");

        validarNoVacio("Nombre", nombre);
        validarNoVacio("Apellido", apellido);
        validarNoVacio("Contraseña", contrasenia);
        validarNoVacio("Obra social", obraSocial);

        if (nroContrato <= 0)
            throw new IllegalArgumentException("El número de contrato debe ser mayor que cero.");

        String sqlUsuario = "UPDATE usuarios SET nombre=?, apellido=?, contrasenia=? WHERE usuario_login=?";
        String sqlPaciente = "UPDATE pacientes SET nro_contrato=?, obra_social=? WHERE id_usuario=(SELECT id_usuario FROM usuarios WHERE usuario_login=?)";

        try (PreparedStatement psUser = conn.prepareStatement(sqlUsuario);
             PreparedStatement psPac = conn.prepareStatement(sqlPaciente)) {

            String passEncriptada = Encriptador.encriptar(contrasenia);

            psUser.setString(1, nombre);
            psUser.setString(2, apellido);
            psUser.setString(3, passEncriptada);
            psUser.setString(4, usuario);
            psUser.executeUpdate();

            psPac.setInt(1, nroContrato);
            psPac.setString(2, obraSocial);
            psPac.setString(3, usuario);
            psPac.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error modificando paciente", e);
        }
    }

    public void eliminarPaciente(String usuario) {
        if (!existeUsuario(usuario))
            throw new IllegalArgumentException("El paciente '" + usuario + "' no existe.");
        eliminarUsuario(usuario);
    }

    public Paciente obtenerPaciente(String usuarioLogin) {
        String sql = """
            SELECT u.nombre, u.apellido, u.usuario_login, u.contrasenia,
                   p.nro_contrato, p.obra_social
            FROM usuarios u
            JOIN pacientes p ON u.id_usuario = p.id_usuario
            WHERE u.usuario_login = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuarioLogin);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new Paciente(
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("usuario_login"),
                        rs.getString("contrasenia"),
                        rs.getInt("nro_contrato"),
                        rs.getString("obra_social"),
                        null
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener datos del paciente.", e);
        }
    }

    // ====================== MÉDICOS ======================

    public void registrarMedico(String usuario, String nombre, String apellido, String contrasenia,
                                String matricula, String especialidad) {
        validarNoVacio("Usuario", usuario);
        validarNoVacio("Nombre", nombre);
        validarNoVacio("Apellido", apellido);
        validarNoVacio("Contraseña", contrasenia);
        validarNoVacio("Matrícula", matricula);
        validarNoVacio("Especialidad", especialidad);

        if (!matricula.matches("\\d+"))
            throw new IllegalArgumentException("La matrícula debe contener solo números.");

        if (existeUsuario(usuario))
            throw new IllegalArgumentException("El usuario '" + usuario + "' ya existe.");

        if (existeMatricula(matricula))
            throw new IllegalArgumentException("La matrícula '" + matricula + "' ya está registrada.");

        String sqlUsuario = "INSERT INTO usuarios(usuario_login, contrasenia, nombre, apellido, rol) VALUES (?, ?, ?, ?, 'Medico')";
        String sqlMedico = "INSERT INTO medicos(id_usuario, matricula, especialidad) VALUES (?, ?, ?)";

        try {
            String passEncriptada = Encriptador.encriptar(contrasenia);
            long idUsuario;

            try (PreparedStatement psUser = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, usuario);
                psUser.setString(2, passEncriptada);
                psUser.setString(3, nombre);
                psUser.setString(4, apellido);
                psUser.executeUpdate();

                try (ResultSet keys = psUser.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No se pudo obtener id_usuario");
                    idUsuario = keys.getLong(1);
                }
            }

            try (PreparedStatement psMed = conn.prepareStatement(sqlMedico)) {
                psMed.setLong(1, idUsuario);
                psMed.setString(2, matricula);
                psMed.setString(3, especialidad);
                psMed.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error registrando médico", e);
        }
    }

    public void modificarMedico(String usuario, String nombre, String apellido, String contrasenia,
                                String matricula, String especialidad) {

        if (!existeUsuario(usuario))
            throw new IllegalArgumentException("El médico '" + usuario + "' no existe.");

        validarNoVacio("Nombre", nombre);
        validarNoVacio("Apellido", apellido);
        validarNoVacio("Contraseña", contrasenia);
        validarNoVacio("Matrícula", matricula);
        validarNoVacio("Especialidad", especialidad);

        if (!matricula.matches("\\d+"))
            throw new IllegalArgumentException("La matrícula debe contener solo números.");

        long idUsuarioMedico;
        try (PreparedStatement ps = conn.prepareStatement("SELECT id_usuario FROM usuarios WHERE usuario_login=?")) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("No se encontró el médico '" + usuario + "'.");
                idUsuarioMedico = rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error verificando médico", e);
        }

        // Verificar si la matrícula pertenece a otro médico
        try (PreparedStatement ps = conn.prepareStatement("SELECT id_usuario FROM medicos WHERE matricula=?")) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long idMat = rs.getLong(1);
                    if (idMat != idUsuarioMedico)
                        throw new IllegalArgumentException("La matrícula '" + matricula + "' ya está registrada.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error verificando matrícula", e);
        }

        // Actualizar usuario y médico
        String sqlUsuario = "UPDATE usuarios SET nombre=?, apellido=?, contrasenia=? WHERE usuario_login=?";
        String sqlMedico = "UPDATE medicos SET matricula=?, especialidad=? WHERE id_usuario=?";

        try (PreparedStatement psUser = conn.prepareStatement(sqlUsuario);
             PreparedStatement psMed = conn.prepareStatement(sqlMedico)) {

            String passEncriptada = Encriptador.encriptar(contrasenia);

            psUser.setString(1, nombre);
            psUser.setString(2, apellido);
            psUser.setString(3, passEncriptada);
            psUser.setString(4, usuario);
            psUser.executeUpdate();

            psMed.setString(1, matricula);
            psMed.setString(2, especialidad);
            psMed.setLong(3, idUsuarioMedico);
            psMed.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error modificando médico", e);
        }
    }

    public Medico obtenerMedico(String usuarioLogin) {
        String sql = """
            SELECT  u.nombre, u.apellido, u.usuario_login, u.contrasenia,
                    m.matricula, m.especialidad
            FROM usuarios u
            JOIN medicos m ON u.id_usuario = m.id_usuario
            WHERE u.usuario_login = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuarioLogin);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new Medico(
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("usuario_login"),
                        rs.getString("contrasenia"),
                        rs.getString("matricula"),
                        rs.getString("especialidad")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener datos del médico.", e);
        }
    }

    public Medico obtenerMedicoPorMatricula(String matricula) {
        if (matricula == null || matricula.trim().isEmpty())
            throw new IllegalArgumentException("Debe ingresar una matrícula.");

        String sql = """
            SELECT u.usuario_login, u.nombre, u.apellido, u.contrasenia,
                   m.matricula, m.especialidad
            FROM medicos m
            JOIN usuarios u ON m.id_usuario = u.id_usuario
            WHERE m.matricula = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Medico(
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            rs.getString("usuario_login"),
                            rs.getString("contrasenia"),
                            rs.getString("matricula"),
                            rs.getString("especialidad")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando médico por matrícula", e);
        }

        return null;
    }

    public void eliminarMedico(String usuario) {
        if (!existeUsuario(usuario))
            throw new IllegalArgumentException("El médico '" + usuario + "' no existe.");
        eliminarUsuario(usuario);
    }

    // ====================== USUARIOS ======================

    public void eliminarUsuario(String usuario) {
        String sql = "DELETE FROM usuarios WHERE usuario_login=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0)
                throw new IllegalArgumentException("No se encontró ningún usuario con el nombre '" + usuario + "'.");
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando usuario", e);
        }
    }

    public List<String> listarUsuariosPorRol(String rol) {
        String sql = "SELECT usuario_login, nombre, apellido FROM usuarios WHERE rol=?";
        List<String> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rol);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String login = rs.getString("usuario_login");
                    String nom = rs.getString("nombre");
                    String ape = rs.getString("apellido");
                    if (login != null)
                        list.add(login + " | " + nom + " " + ape);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listando usuarios", e);
        }
        return list;
    }

    public void resetearContrasenia(String usuario, String nuevaContrasenia) {
        validarNoVacio("Usuario", usuario);
        validarNoVacio("Nueva contraseña", nuevaContrasenia);

        if (!existeUsuario(usuario))
            throw new IllegalArgumentException("El usuario '" + usuario + "' no existe.");

        String sql = "UPDATE usuarios SET contrasenia=? WHERE usuario_login=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String passEncriptada = Encriptador.encriptar(nuevaContrasenia);
            ps.setString(1, passEncriptada);
            ps.setString(2, usuario);

            int filas = ps.executeUpdate();
            if (filas == 0) throw new RuntimeException("No se pudo resetear la contraseña. Intente nuevamente.");

        } catch (SQLException e) {
            throw new RuntimeException("Error al resetear contraseña: " + e.getMessage(), e);
        }
    }

    public void bloquearUsuario(String usuario) {
        validarNoVacio("Usuario", usuario);
        if (!existeUsuario(usuario)) throw new IllegalArgumentException("El usuario '" + usuario + "' no existe.");

        String sql = "UPDATE usuarios SET bloqueado = 1 WHERE usuario_login=? AND bloqueado = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new IllegalStateException("El usuario ya está bloqueado o no se pudo bloquear.");
        } catch (SQLException e) {
            throw new RuntimeException("Error al bloquear usuario", e);
        }
    }

    public void desbloquearUsuario(String usuario) {
        validarNoVacio("Usuario", usuario);
        if (!existeUsuario(usuario)) throw new IllegalArgumentException("El usuario '" + usuario + "' no existe.");

        String sql = "UPDATE usuarios SET bloqueado = 0 WHERE usuario_login=? AND bloqueado = 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new IllegalStateException("El usuario no está bloqueado o no se pudo desbloquear.");
        } catch (SQLException e) {
            throw new RuntimeException("Error al desbloquear usuario", e);
        }
    }
    
 // Lista todos los pacientes como objetos Paciente
    public List<Paciente> listarPacientes() {
        String sql = """
            SELECT u.nombre, u.apellido, u.usuario_login, u.contrasenia,
                   p.nro_contrato, p.obra_social
            FROM usuarios u
            JOIN pacientes p ON u.id_usuario = p.id_usuario
        """;

        List<Paciente> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Paciente p = new Paciente(
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("usuario_login"),
                        rs.getString("contrasenia"),
                        rs.getInt("nro_contrato"),
                        rs.getString("obra_social"),
                        null
                );
                lista.add(p);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error listando pacientes", e);
        }
        return lista;
    }

    // Lista todos los médicos como objetos Medico
    public List<Medico> listarMedicos() {
        String sql = """
            SELECT u.nombre, u.apellido, u.usuario_login, u.contrasenia,
                   m.matricula, m.especialidad
            FROM usuarios u
            JOIN medicos m ON u.id_usuario = m.id_usuario
        """;

        List<Medico> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Medico m = new Medico(
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("usuario_login"),
                        rs.getString("contrasenia"),
                        rs.getString("matricula"),
                        rs.getString("especialidad")
                );
                lista.add(m);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error listando médicos", e);
        }
        return lista;
    }

}
