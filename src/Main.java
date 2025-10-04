import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class Main {
    private static Main instancia;

    private List<Usuario> usuarios;
    private List<Paciente> pacientes;
    private List<Medico> medicos;
    private List<Turno> turnos;

    private Main() {
        usuarios = new ArrayList<>();
        pacientes = new ArrayList<>();
        medicos = new ArrayList<>();
        turnos = new ArrayList<>();
    }

    public static Main getInstancia() {
        if (instancia == null) {
            instancia = new Main();
        }
        return instancia;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public List<Paciente> getPacientes() {
        return pacientes;
    }

    public List<Medico> getMedicos() {
        return medicos;
    }

    public List<Turno> getTurnos() {
        return turnos;
    }

    public void agregarUsuario(Usuario u) {
        usuarios.add(u);
        if (u instanceof Paciente)
            pacientes.add((Paciente) u);
        if (u instanceof Medico)
            medicos.add((Medico) u);
    }

    public Usuario login(String user, String pass) {
        for (Usuario u : usuarios) {
            if (u.getUsuario().equals(user) && u.getContrasenia().equals(pass)) {
                return u;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        Main sistema = Main.getInstancia();

        Administrador admin = new Administrador("Ana", "Perez", "admin", "1234", "Administración");
        HistorialMedico historial = new HistorialMedico(1, new Date(), "Sin antecedentes", new String[] {});
        Paciente paciente = new Paciente("Juan", "Pérez", "juan", "abcd", 12345678, "ObraSocialEjemplo", historial);
        Medico medico = new Medico("Laura", "Gómez", "laura", "med1", 1, "Clínica General");

        sistema.agregarUsuario(admin);
        sistema.agregarUsuario(paciente);
        sistema.agregarUsuario(medico);

        Usuario logueado = null;
        while (logueado == null) {
            String user = JOptionPane.showInputDialog("Ingrese usuario:");
            if (user == null)
                System.exit(0);
            String pass = JOptionPane.showInputDialog("Ingrese contraseña:");
            if (pass == null)
                System.exit(0);
            logueado = sistema.login(user, pass);

            if (logueado == null) {
                JOptionPane.showMessageDialog(null, "Usuario o contraseña incorrectos");
            }
        }

        JOptionPane.showMessageDialog(null, "Bienvenido/a " + logueado.getNombre());

        if (logueado instanceof Menu) {
            ((Menu) logueado).MostrarMenu();
        } else {
            JOptionPane.showMessageDialog(null, "Menú no disponible");
        }
    }
}
