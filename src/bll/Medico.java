package bll;

import javax.swing.JOptionPane;

public class Medico extends Usuario implements Menu {

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
		this.agenda = new Turno[100];
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

	public void modificarDisponibilidad(int dia, String nuevoHorario) {
		this.disponibilidadSemanal[dia] = nuevoHorario;
	}
	
	public void visualizarAgenda() {
        System.out.println("Agenda del médico " + getNombre() + " " + getApellido() + ":");
        for (Turno t : agenda) {
            if (t != null)
                System.out.println(t);
        }
    }

	public void registrarConsulta(Turno turno, Consulta consulta) {
        Paciente p = turno.getPaciente();
        if (p != null && p.getHistorial() != null) {
            String obs = p.getHistorial().getObservaciones();
            String nueva = (obs == null ? "" : obs + "\n") + 
                    "Consulta [" + consulta.getFecha() + "]: " +
                    "\nMotivo: " + consulta.getMotivo() +
                    "\nDiagnóstico: " + consulta.getDiagnostico() +
                    "\nTratamiento: " + consulta.getTratamiento() +
                    "\nSeguimiento: " + consulta.getSeguimiento();
            p.getHistorial().setObservaciones(nueva);
        }
        System.out.println("Consulta registrada para " + p.getNombre());
    }

	public void confirmarAsistencia(Turno turno) {
        turno.confirmarAsistencia();
        System.out.println("Asistencia confirmada para: " + turno.getPaciente().getNombre());
    }

	public void MostrarMenu() {
        JOptionPane.showMessageDialog(null,
                "MENÚ MÉDICO \n1) Iniciar sesión \n2) Registrar disponibilidad \n3) Modificar disponibilidad " +
                "\n4) Visualizar agenda \n5) Confirmar asistencia \n6) Registrar consulta " +
                " \n10) Salir");
    }

    @Override
    public String toString() {
        return super.toString() + " | Medico [matricula=" + matricula + ", especialidad=" + especialidad + "]";
    }
}
