import javax.swing.*;

public class Administrador extends Usuario implements Menu { 
    private String sector;

    public Administrador(String nombre, String apellido, String usuario, String contrasenia, String sector) {
        super(nombre, apellido, usuario, contrasenia);
        this.sector = sector;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }
    

    public void altaPaciente(Paciente p) {
        Main.getInstancia().agregarUsuario(p);
        JOptionPane.showMessageDialog(null, "Paciente dado de alta: " + p.getNombre() + " " + p.getApellido());
    }
    
    public void altaMedico(Medico m) {
        Main.getInstancia().agregarUsuario(m);
        JOptionPane.showMessageDialog(null, "Médico dado de alta: " + m.getNombre() + " " + m.getApellido());
    }
    
    public void bajaPaciente(String usuario) {
        Main sistema = Main.getInstancia();
        sistema.getPacientes().removeIf(p -> p.getUsuario().equals(usuario));
        sistema.getUsuarios().removeIf(u -> (u instanceof Paciente) && u.getUsuario().equals(usuario));
        JOptionPane.showMessageDialog(null, "Paciente con usuario '" + usuario + "' eliminado.");
    }
    
    public void bajaMedico(String usuario) {
        Main sistema = Main.getInstancia();
        sistema.getMedicos().removeIf(m -> m.getUsuario().equals(usuario));
        sistema.getUsuarios().removeIf(u -> (u instanceof Medico) && u.getUsuario().equals(usuario));
        JOptionPane.showMessageDialog(null, "Médico con usuario '" + usuario + "' eliminado.");
    }
    
    public void crearHistoriaMedica(Paciente p, HistorialMedico historial) {
        p.setHistorial(historial);
        JOptionPane.showMessageDialog(null, "Historia médica creada para paciente: " + p.getNombre());
    }
    
    public String resetContrasenia(Usuario u) {
        String nueva = "NuevaContraseña123";
        u.setContrasenia(nueva);
        JOptionPane.showMessageDialog(null, "Contraseña reseteada para " + u.getUsuario());
        return nueva;
    }

    public boolean bloquearUsr(Usuario u) {
        JOptionPane.showMessageDialog(null, "Usuario bloqueado: " + u.getUsuario());
        return true; 
    }
    
    public boolean desbloquearUsr(Usuario u) {
        JOptionPane.showMessageDialog(null, "Usuario desbloqueado: " + u.getUsuario());
        return true; 
    }

    @Override
    public void MostrarMenu() {
        String[] opciones = {
            "Alta paciente",
            "Alta médico",
            "Baja paciente",
            "Baja médico",
            "Crear historia médica",
            "Reset contraseña",
            "Salir"
        };
        
        int elegido = JOptionPane.showOptionDialog(
            null,
            "Elija opción",
            "Administrador",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            opciones,
            opciones[0]
        );

        Main sistema = Main.getInstancia();

        switch(elegido) {
            case 0:
                String nomP = JOptionPane.showInputDialog("Nombre paciente:");
                String apeP = JOptionPane.showInputDialog("Apellido paciente:");
                String usrP = JOptionPane.showInputDialog("Usuario paciente:");
                String passP = JOptionPane.showInputDialog("Contraseña paciente:");
                int dni = Integer.parseInt(JOptionPane.showInputDialog("DNI paciente:"));
                String obra = JOptionPane.showInputDialog("Obra social:");
                Paciente p = new Paciente(nomP, apeP, usrP, passP, dni, obra, null);
                altaPaciente(p);
                break;
                
            case 1: 
                String nomM = JOptionPane.showInputDialog("Nombre médico:");
                String apeM = JOptionPane.showInputDialog("Apellido médico:");
                String usrM = JOptionPane.showInputDialog("Usuario médico:");
                String passM = JOptionPane.showInputDialog("Contraseña médico:");
                int id = Integer.parseInt(JOptionPane.showInputDialog("ID médico:"));
                String esp = JOptionPane.showInputDialog("Especialidad:");
                Medico m = new Medico(nomM, apeM, usrM, passM, id, esp);
                altaMedico(m);
                break;
                
            case 2: 
                String usuarioP = JOptionPane.showInputDialog("Usuario del paciente a eliminar:");
                bajaPaciente(usuarioP);
                break;
                
            case 3: 
                String usuarioM = JOptionPane.showInputDialog("Usuario del médico a eliminar:");
                bajaMedico(usuarioM);
                break;
                
            case 4: 
                String usuarioHist = JOptionPane.showInputDialog("Usuario del paciente:");
                Paciente pacHist = sistema.getPacientes()
                        .stream()
                        .filter(pa -> pa.getUsuario().equals(usuarioHist))
                        .findFirst()
                        .orElse(null);
                if (pacHist != null) {
                    String diag = JOptionPane.showInputDialog("Diagnóstico:");
                    HistorialMedico hist = new HistorialMedico(
                        0, 
                        new java.util.Date(),
                        diag,
                        new String[] {}
                    );
                    crearHistoriaMedica(pacHist, hist);
                } else {
                    JOptionPane.showMessageDialog(null, "Paciente no encontrado");
                }
                break;

                
            case 5: 
                String usuarioR = JOptionPane.showInputDialog("Usuario a resetear contraseña:");
                Usuario userR = sistema.getUsuarios()
                        .stream()
                        .filter(u -> u.getUsuario().equals(usuarioR))
                        .findFirst()
                        .orElse(null);
                if (userR != null) {
                    resetContrasenia(userR);
                } else {
                    JOptionPane.showMessageDialog(null, "Usuario no encontrado");
                }
                break;
                
            case 6: 
                JOptionPane.showMessageDialog(null, "Saliendo...");
                break;
                
            default:
                JOptionPane.showMessageDialog(null, "Opción inválida");
        }
    }

    @Override
    public String getRol() {
        return "Administrador";
    }
}
