public class Administrador extends Usuario { 
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
}
