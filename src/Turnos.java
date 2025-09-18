package sistemaControl;

import java.util.Date;

public class Turnos {
	
	private Date fechaHora;
	private String estado;
	private Paciente paciente;
	private Medico medico;
	
	public Turnos(Date fechaHora, Paciente paciente, Medico medico) {		
		this.fechaHora = fechaHora;		
		this.paciente = paciente;
		this.medico = medico;
		this.estado = "Reservado";
	}
	
	public void reservar() {
		this.estado = "Reservado";
	}
	
	public void cancelar() {
		this.estado = "Cancelado";
	}
	
	public void confirmarAsistencia() {
		this.estado = "Confirmado";
	}
	
	

}
