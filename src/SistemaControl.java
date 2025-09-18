package SistemaControl;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


import SistemaControl.Usuario;
import SistemaControl.Paciente;
import SistemaControl.Medico;
import SistemaControl.Administrador;
import SistemaControl.Turnos;

public class SistemaControl {
    private static SistemaControl instancia;

    private List<Usuario> usuarios;
    private List<Paciente> pacientes;
    private List<Medico> medicos;
    private List<Turnos> turnos;

    private SistemaControl() {
        usuarios = new ArrayList<>();
        pacientes = new ArrayList<>();
        medicos = new ArrayList<>();
        turnos = new ArrayList<>();
    }

    public static SistemaControl getInstancia() {
        if (instancia == null) {
            instancia = new SistemaControl();
        }
        return instancia;
    }



    public void agregarUsuario(Usuario u) {
        usuarios.add(u);
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
        SistemaControl sistema = SistemaControl.getInstancia();

        
        Administrador admin = new Administrador("Ana", "Admin", "admin", "1234");
        Paciente paciente = new Paciente("Juan", "Pérez", "juan", "abcd", "12345678");
        Medico medico = new Medico("Laura", "Gómez", "laura", "med1", "Clínica General");

        sistema.agregarUsuario(admin);
        sistema.agregarUsuario(paciente);
        sistema.agregarUsuario(medico);

      
        Usuario logueado = null;
        while (logueado == null) {
            String user = JOptionPane.showInputDialog("Ingrese usuario:");
            if (user == null) System.exit(0);
            String pass = JOptionPane.showInputDialog("Ingrese contraseña:");
            if (pass == null) System.exit(0);
            logueado = sistema.login(user, pass);

            if (logueado == null) {
                JOptionPane.showMessageDialog(null, "Usuario o contraseña incorrectos.");
            }
        }

        JOptionPane.showMessageDialog(null, "Bienvenido/a " + logueado.getNombre());

        
        if (logueado instanceof Administrador) {
            menuAdministrador((Administrador) logueado);
        } else if (logueado instanceof Paciente) {
            menuPaciente((Paciente) logueado);
        } else if (logueado instanceof Medico) {
            menuMedico((Medico) logueado);
        }
    }

    
    private static void menuAdministrador(Administrador admin) {
        String opcion;
        do {
            opcion = JOptionPane.showInputDialog(
                "=== MENÚ ADMINISTRADOR ===\n" +
                "1. Registrar paciente\n" +
                "2. Registrar médico\n" +
                "3. Listar usuarios\n" +
                "0. Salir"
            );

            switch (opcion) {
                case "1":
                    JOptionPane.showMessageDialog(null, "Funcionalidad registrar paciente");
                    break;
                case "2":
                    JOptionPane.showMessageDialog(null, "Funcionalidad registrar médico");
                    break;
                case "3":
                    JOptionPane.showMessageDialog(null, "Funcionalidad listar usuarios");
                    break;
                case "0":
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Opción inválida");
            }
        } while (!opcion.equals("0"));
    }

    private static void menuPaciente(Paciente paciente) {
        String opcion;
        do {
            opcion = JOptionPane.showInputDialog(
                "=== MENÚ PACIENTE ===\n" +
                "1. Reservar turno\n" +
                "2. Cancelar turno\n" +
                "3. Ver historial médico\n" +
                "0. Salir"
            );

            switch (opcion) {
                case "1":
                    JOptionPane.showMessageDialog(null, "Funcionalidad reservar turno");
                    break;
                case "2":
                    JOptionPane.showMessageDialog(null, "Funcionalidad cancelar turno");
                    break;
                case "3":
                    JOptionPane.showMessageDialog(null, "Funcionalidad ver historial médico");
                    break;
                case "0":
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Opción inválida");
            }
        } while (!opcion.equals("0"));
    }

    private static void menuMedico(Medico medico) {
        String opcion;
        do {
            opcion = JOptionPane.showInputDialog(
                "=== MENÚ MÉDICO ===\n" +
                "1. Registrar disponibilidad\n" +
                "2. Ver agenda\n" +
                "3. Subir estudio\n" +
                "0. Salir"
            );

            switch (opcion) {
                case "1":
                    JOptionPane.showMessageDialog(null, "Funcionalidad registrar disponibilidad");
                    break;
                case "2":
                    JOptionPane.showMessageDialog(null, "Funcionalidad ver agenda");
                    break;
                case "3":
                    JOptionPane.showMessageDialog(null, "Funcionalidad subir estudio");
                    break;
                case "0":
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Opción inválida");
            }
        } while (!opcion.equals("0"));
    }
}
