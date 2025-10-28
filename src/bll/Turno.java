package bll;

import java.util.Date;

public class Turno {
	private long idTurno;
	private Date fechaHora;
	private Paciente paciente;
	private Medico medico;
	private String estado;

	public Turno(Date fechaHora, Paciente paciente, Medico medico) {
		this.fechaHora = fechaHora;
		this.paciente = paciente;
		this.medico = medico;
		this.estado = "Reservado";
	}

	public long getIdTurno() {
		return idTurno;
	}

	public void setIdTurno(long idTurno) {
		this.idTurno = idTurno;
	}

	public Date getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(Date fechaHora) {
		this.fechaHora = fechaHora;
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

	public String getEstado() {
		return estado;
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
		return "Turno{" + "idTurno=" + idTurno + ", fechaHora=" + fechaHora + ", paciente=" + paciente.getNombre()
				+ ", medico=" + (medico != null ? medico.getNombreCompleto() : "N/D") + ", estado='" + estado + '\''
				+ '}';
	}
}
