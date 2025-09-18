import javax.swing.JOptionPane;

public class ClinicaApp {
    public static void main(String[] args) {
        boolean salir = false;

        while (!salir) {
            // 🔹 Login simulado
            String usuario = JOptionPane.showInputDialog("Ingrese usuario:");
            String pass = JOptionPane.showInputDialog("Ingrese contraseña:");

            String[] roles = {"Administrador", "Médico", "Paciente"};
            String rol = (String) JOptionPane.showInputDialog(
                    null,
                    "Seleccione su rol:",
                    "Login",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    roles,
                    roles[0]
            );

            // 🔹 Mostrar menú según rol
            switch (rol) {
                case "Administrador" -> menuAdministrador();
                case "Médico" -> menuMedico();
                case "Paciente" -> menuPaciente();
                default -> JOptionPane.showMessageDialog(null, "Rol inválido.");
            }

            int opcionSalir = JOptionPane.showConfirmDialog(null, "¿Desea salir del sistema?", "Salir", JOptionPane.YES_NO_OPTION);
            salir = (opcionSalir == JOptionPane.YES_OPTION);
        }
    }

    private static void menuAdministrador() {
        String[] opciones = {"Registrar Paciente", "Registrar Médico", "Bloquear Usuario", "Salir"};
        String opcion = "";
        while (!opcion.equals("Salir")) {
            opcion = (String) JOptionPane.showInputDialog(
                    null,
                    "Seleccione una opción:",
                    "Menú Administrador",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opciones,
                    opciones[0]
            );
            if (opcion != null && !opcion.equals("Salir")) {
                JOptionPane.showMessageDialog(null, "Ejecutando: " + opcion);
            } else {
                opcion = "Salir";
            }
        }
    }

    private static void menuMedico() {
        String[] opciones = {"Registrar Disponibilidad", "Ver Agenda", "Subir Estudio", "Salir"};
        String opcion = "";
        while (!opcion.equals("Salir")) {
            opcion = (String) JOptionPane.showInputDialog(
                    null,
                    "Seleccione una opción:",
                    "Menú Médico",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opciones,
                    opciones[0]
            );
            if (opcion != null && !opcion.equals("Salir")) {
                JOptionPane.showMessageDialog(null, "Ejecutando: " + opcion);
            } else {
                opcion = "Salir";
            }
        }
    }

    private static void menuPaciente() {
        String[] opciones = {"Reservar Turno", "Cancelar Turno", "Ver Estudios", "Salir"};
        String opcion = "";
        while (!opcion.equals("Salir")) {
            opcion = (String) JOptionPane.showInputDialog(
                    null,
                    "Seleccione una opción:",
                    "Menú Paciente",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opciones,
                    opciones[0]
            );
            if (opcion != null && !opcion.equals("Salir")) {
                JOptionPane.showMessageDialog(null, "Ejecutando: " + opcion);
            } else {
                opcion = "Salir";
            }
        }
    }
}
