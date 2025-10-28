package bll;

import java.util.Date;

public class Turno {

	private Date fechaHora;
	private String estado;
	private Paciente paciente;
	private Medico medico;

	public Turno(Date fechaHora, Paciente paciente, Medico medico) {
		this.fechaHora = fechaHora;
		this.paciente = paciente;
		this.medico = medico;
		this.estado = "Reservado";
	}

	public Date getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(Date fechaHora) {
		this.fechaHora = fechaHora;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Paciente getPaciente() {
		return paciente;
	}

	public void setPaciente(Paciente paciente) {
		this.paciente = paciente;
	}

	public Medico getMedico() {
		return medico;
	}

	public void setMedico(Medico medico) {
		this.medico = medico;
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

	@Override
	public String toString() {
		return "Turno{" + "fechaHora=" + fechaHora + ", paciente="
				+ (paciente != null ? paciente.getNombre() + " " + paciente.getApellido() : "N/D") + ", medico="
				+ (medico != null ? medico.getNombreCompleto() : "N/D") + ", estado='" + estado + '\'' + '}';
	}
}