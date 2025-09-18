import javax.swing.JOptionPane;

public class Administrador extends Usuario implements Menu{ 
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
    
    // METODOS
    public void altaPaciente() {
    	
    }
    
    public void altaMedico() {

    }
    
    public void bajaPaciente() {

    }
    
    public void bajaMedico() {

    }
    
    public String gestionRol() {
        return "Rol gestionado";
    }
    
    public void crearHistoriaMedica() {

    }
    
    public boolean bloquearUsr() {
        return true; 
    }
    
    public boolean desbloquearUsr() {

        return true; 
    }
    
    public String resetContrasenia() {
        return "NuevaContraseña123";
    }
    

    @Override
    public void MostrarMenu() {
        String[] opciones = {"Crear historia médica","Reset contraseña", "Salir"};
        int elegido = JOptionPane.showOptionDialog(null, "Elija opcion", "Administrador", 0, 0, null, opciones, opciones[0]);

        switch(elegido) {
            case 0:
                crearHistoriaMedica();
                break;
            case 1:
                resetContrasenia();
                break;
            case 2:
                JOptionPane.showMessageDialog(null, "Saliendo...");
                break;
            default:
                JOptionPane.showMessageDialog(null, "Opción inválida");
        }
    }

}
