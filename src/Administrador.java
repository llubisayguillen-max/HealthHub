

public class Administrador extends Usuario{
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
    
    public void altaPaciente(){
    }
    
    public void altaMedico(){
    }
    
    public void bajaPaciente() {
    }
    
    public void bajaMedico() {
    }
    
    public void gestionRol() {
    }
    
    public void crearHistoriaMedica() {
    }
    
    public void bloquearUsr() {
    }
    
    public void desbloquearUsr() {
    }
    
    public void resetContrasenia() {
    }
}