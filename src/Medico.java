package sistemaControl;

import java.util.ArrayList;
import java.util.List;

public class Medico extends Usuario {

	private int matricula;
	private String especialidad;
	private String[] disponibilidadSemanal;
	private Turno[] agenda;
	
	public Medico(String nombre, String apellido, String usuario, String contrasenia, int matricula,
			String especialidad) {
		super(nombre, apellido, usuario, contrasenia);
		this.matricula = matricula;
		this.especialidad = especialidad;
		this.disponibilidadSemanal = new String[7];
		this.agenda = new turno[100];
	}

	public int getMatricula() {
		return matricula;
	}

	public void setMatricula(int matricula) {
		this.matricula = matricula;
	}

	public String getEspecialidad() {
		return especialidad;
	}

	public void setEspecialidad(String especialidad) {
		this.especialidad = especialidad;
	}

	public String[] getDisponibilidadSemanal() {
		return disponibilidadSemanal;
	}

	public void setDisponibilidadSemanal(String[] disponibilidadSemanal) {
		this.disponibilidadSemanal = disponibilidadSemanal;
	}

	public Turno[] getAgenda() {
		return agenda;
	}

	public void setAgenda(Turno[] agenda) {
		this.agenda = agenda;
	}
	
	public void registrarDisponibilidad(int dia, String horario) {
		this.disponibilidadSemanal[dia] = horario;
	}
	
	public void modificarDisponibilidad(Consulta consulta) {
		System.out.println("Consulta registrada: " + consulta.getMotivo());;
	}
	
	public void confirmarAsistencia(Turno turno) {
        turno.confirmarAsistencia();
    }
	
	@Override
    public String toString() {
        return super.toString() + " | Medico [matricula=" + matricula + ", especialidad=" + especialidad + "]";
    }	

}
