public class Usuario { 
	
    private String nombre;
    private String apellido;
    private String usuario;
    private String contrasenia;
    
    public Usuario(String nombre, String apellido, String usuario, String contrasenia) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.usuario = usuario;
        this.contrasenia = contrasenia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    
 // AGREGADO
    public boolean login() {          
        return true;
    }

    public boolean logout() {       
        return true;
    }

    @Override
    public String toString() {        
        return "Nombre=" + nombre + ". /nApellido=" + apellido + ". /nUsuario=" + usuario + ".";
    }
}
