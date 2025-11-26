package bll;

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
    public String getNombreCompleto() {
        return getNombre() + " " + getApellido();
    }

    @Override
    public String getRol() {
        return "Administrador";
    }

    @Override
    public String toString() {
        return "Administrador{" +
                "nombre='" + getNombre() + '\'' +
                ", apellido='" + getApellido() + '\'' +
                ", usuario='" + getUsuario() + '\'' +
                ", sector='" + sector + '\'' +
                '}';
    }
}
