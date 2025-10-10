package bll;

import java.util.Objects;


public class Medico extends Usuario {

    private String matricula; 
    private String especialidad;


    public Medico(String nombre, String apellido, String usuario, String contrasenia,
                  String matricula, String especialidad) {
        super(nombre, apellido, usuario, contrasenia);
        this.matricula = matricula;
        this.especialidad = especialidad;
    }

    // permite crear un médico con matrícula numérica (int) y la almacena internamente como String
    //para mantener consistencia con la base de datos (VARCHAR)
    
    public Medico(String nombre, String apellido, String usuario, String contrasenia,
                  int matricula, String especialidad) {
        this(nombre, apellido, usuario, contrasenia, String.valueOf(matricula), especialidad);
    }

    public String getMatricula() { 
    	return matricula; 
    	}
    public void setMatricula(String matricula) { 
    	this.matricula = matricula; 
    	}

    public String getEspecialidad() { 
    	return especialidad; 
    	}
    public void setEspecialidad(String especialidad) { 
    	this.especialidad = especialidad; 
    	}

    public String getNombreCompleto() {
        return getNombre() + " " + getApellido();
    }

    @Override
    public String toString() {
        return "Medico{usuario=" + getUsuario() +
               ", nombre=" + getNombreCompleto() +
               ", matricula=" + matricula +
               ", especialidad=" + especialidad + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Medico)) return false;
        Medico medico = (Medico) o;
        return Objects.equals(getUsuario(), medico.getUsuario());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUsuario());
    }
}

