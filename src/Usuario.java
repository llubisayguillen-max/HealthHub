public class Usuario { 
    private String nombre;
    private String apellido;
    private String usuario;
    private String contrasenia;
    private boolean sesionIniciada;

    public Usuario(String nombre, String apellido, String usuario, String contrasenia) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.usuario = usuario;
        this.contrasenia = contrasenia;
        this.sesionIniciada = false;
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

   
    public boolean isSesionIniciada() {
        return sesionIniciada;
    }

    public boolean login(String usuario, String contrasenia) {
        if (this.usuario.equals(usuario) && this.contrasenia.equals(contrasenia)) {
            sesionIniciada = true;
            return true;
        }
        return false;
    }

    public boolean logout() {
        if (sesionIniciada) {
            sesionIniciada = false;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Usuario: " + nombre + " " + apellido + " (" + usuario + ")";
    }

    public String getRol() {
        return "Usuario genérico";
    }
}