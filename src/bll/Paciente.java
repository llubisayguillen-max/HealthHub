package bll;

import dll.ControllerPaciente;

import javax.swing.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Paciente extends Usuario implements Menu {

	private int nroContrato;
	private String obraSocial;
	private HistorialMedico historial;
	private ControllerPaciente controller;
	private List<Turno> turnos;

	public Paciente(String nombre, String apellido, String usuario, String contrasenia, int nroContrato,
			String obraSocial, HistorialMedico historial) {
		super(nombre, apellido, usuario, contrasenia);
		this.nroContrato = nroContrato;
		this.obraSocial = obraSocial;
		this.historial = historial;
		this.controller = new ControllerPaciente(this);
		this.turnos = new ArrayList<>();
	}

	public int getNroContrato() {
		return nroContrato;
	}

	public void setNroContrato(int nroContrato) {
		this.nroContrato = nroContrato;
	}

	public String getObraSocial() {
		return obraSocial;
	}

	public void setObraSocial(String obraSocial) {
		this.obraSocial = obraSocial;
	}

	public HistorialMedico getHistorial() {
		return historial;
	}

	public void setHistorial(HistorialMedico historial) {
		this.historial = historial;
	}

	public String getEmail() {
		return getUsuario();
	}

	public String getPassword() {
		return getContrasenia();
	}

	public List<Turno> getTurnos() {
		return turnos;
	}

	public void agregarTurno(Turno turno) {
		this.turnos.add(turno);
	}

	// Menú

	@Override
	public void MostrarMenu() {
		String[] opciones = { "Reservar turno", "Cancelar turno", "Consultar turno", "Ver historial médico",
				"Ver favoritos", "Agregar favorito", "Recibir recomendaciones", "Salir" };
		int elegido;

		do {
			elegido = JOptionPane.showOptionDialog(null, "Elija opción", "Paciente", 0, 0, null, opciones, opciones[0]);

			switch (elegido) {
			case 0 -> reservarTurnoMenu();
			case 1 -> cancelarTurnoMenu();
			case 2 -> consultarTurnoMenu();
			case 3 -> verResultado();
			case 4 -> verFavoritos();
			case 5 -> agregarFavoritoMenu();
			case 6 -> recibirRecomendacionesMenu();
			case 7 -> JOptionPane.showMessageDialog(null, "Saliendo...");
			default -> JOptionPane.showMessageDialog(null, "Opción inválida.");
			}
		} while (elegido != 7);
	}

	// Funciones

	private void reservarTurnoMenu() {
		try {
			String especialidad = JOptionPane.showInputDialog("Ingrese la especialidad deseada:");
			if (especialidad == null || especialidad.isBlank())
				return;

			List<Medico> medicos = controller.filtrarPorEspecialidad(especialidad);
			if (medicos.isEmpty()) {
				JOptionPane.showMessageDialog(null, "No se encontraron médicos para esa especialidad.");
				return;
			}

			StringBuilder sb = new StringBuilder("Médicos disponibles:\n");
			for (int i = 0; i < medicos.size(); i++)
				sb.append(i + 1).append(". ").append(medicos.get(i).getNombreCompleto()).append(" - ")
						.append(medicos.get(i).getEspecialidad()).append("\n");

			int selMedico;
			try {
				selMedico = Integer.parseInt(JOptionPane.showInputDialog(sb + "Ingrese el número del médico:")) - 1;
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "Debes ingresar un número válido.");
				return;
			}

			if (selMedico < 0 || selMedico >= medicos.size()) {
				JOptionPane.showMessageDialog(null, "Selección inválida.");
				return;
			}

			Medico m = medicos.get(selMedico);
			List<String> horarios = controller.obtenerHorariosDisponibles(m.getUsuario());
			if (horarios.isEmpty()) {
				JOptionPane.showMessageDialog(null, "No hay horarios disponibles para este médico.");
				return;
			}

			StringBuilder sbHorarios = new StringBuilder("Horarios disponibles:\n");
			for (int i = 0; i < horarios.size(); i++)
				sbHorarios.append(i + 1).append(". ").append(horarios.get(i)).append("\n");

			int selHorario;
			try {
				selHorario = Integer
						.parseInt(JOptionPane.showInputDialog(sbHorarios + "Ingrese el número del horario:")) - 1;
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "Debes ingresar un número válido.");
				return;
			}

			if (selHorario < 0 || selHorario >= horarios.size()) {
				JOptionPane.showMessageDialog(null, "Selección inválida.");
				return;
			}

			// P/ armar la fecha y hora reales
			String horarioStr = horarios.get(selHorario);
			String horaInicio = horarioStr.split(" - ")[0]; // hora inicio
			String fechaHoy = new SimpleDateFormat("yyyy-MM-dd").format(new Date()); // fecha actual
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date fechaHora = sdf.parse(fechaHoy + " " + horaInicio);

			long idTurno = controller.solicitarTurno(m.getUsuario(), fechaHora);
			JOptionPane.showMessageDialog(null, "Turno reservado con éxito. ID: " + idTurno);

		} catch (IllegalStateException | ParseException e) {
			JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
		}
	}

	private void cancelarTurnoMenu() {
		List<Turno> turnos = controller.turnosActivos();
		if (turnos.isEmpty()) {
			JOptionPane.showMessageDialog(null, "No tiene turnos activos.");
			return;
		}

		StringBuilder sb = new StringBuilder("Turnos activos:\n");
		for (int i = 0; i < turnos.size(); i++)
			sb.append(i + 1).append(". ").append(turnos.get(i).getMedico().getNombreCompleto()).append(" - ")
					.append(turnos.get(i).getFechaHora()).append(" - ").append(turnos.get(i).getEstado()).append("\n");

		int selTurno;
		try {
			selTurno = Integer.parseInt(JOptionPane.showInputDialog(sb + "Ingrese el número del turno a cancelar:"))
					- 1;
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Debes ingresar un número válido.");
			return;
		}

		if (selTurno < 0 || selTurno >= turnos.size()) {
			JOptionPane.showMessageDialog(null, "Selección inválida.");
			return;
		}

		Turno t = turnos.get(selTurno);
		try {
			t.cancelar(); // metodo de turno
			controller.cancelarTurno(t.getPaciente().getNroContrato());
			JOptionPane.showMessageDialog(null, "Turno cancelado con éxito.");
		} catch (IllegalStateException | IllegalArgumentException e) {
			JOptionPane.showMessageDialog(null, "Error cancelando turno: " + e.getMessage());
		}
	}

	private void consultarTurnoMenu() {
		List<Turno> turnos;
		try {
			turnos = controller.turnosActivos();
		} catch (IllegalStateException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			return;
		}

		if (turnos.isEmpty()) {
			JOptionPane.showMessageDialog(null, "No tiene turnos activos.");
			return;
		}

		StringBuilder sb = new StringBuilder("Turnos activos:\n");
		for (int i = 0; i < turnos.size(); i++) {
			Turno t = turnos.get(i);
			sb.append(i + 1).append(". Médico: ")
					.append(t.getMedico() != null ? t.getMedico().getNombreCompleto() : "N/D").append(" - Fecha: ")
					.append(t.getFechaHora()).append(" - Estado: ").append(t.getEstado()).append("\n");
		}

		JOptionPane.showMessageDialog(null, sb.toString());
	}

	private void agregarFavoritoMenu() {
		String usernameMedico = JOptionPane.showInputDialog("Ingrese el username del médico a agregar como favorito:");
		if (usernameMedico == null || usernameMedico.isBlank())
			return;

		try {
			controller.agregarAFavoritos(usernameMedico);
			JOptionPane.showMessageDialog(null, "Médico agregado a favoritos correctamente.");
		} catch (IllegalArgumentException | IllegalStateException e) {
			JOptionPane.showMessageDialog(null, "Error agregando favorito: " + e.getMessage());
		}
	}

	private void verFavoritos() {
		List<Medico> favoritos;
		try {
			favoritos = controller.verFavoritos();
		} catch (IllegalStateException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			return;
		}

		StringBuilder sb = new StringBuilder("Médicos favoritos:\n");
		for (Medico m : favoritos)
			sb.append(m.getNombreCompleto()).append(" - ").append(m.getEspecialidad()).append("\n");

		JOptionPane.showMessageDialog(null, sb.toString());
	}

	private void verResultado() {
		if (historial != null && historial.getObservaciones() != null) {
			JOptionPane.showMessageDialog(null, "Historial Médico:\n" + historial.toString(), "Resultados",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, "No hay resultados disponibles en tu historial médico.");
		}
	}

	private void recibirRecomendacionesMenu() {
		List<String> recs = controller.mostrarRecomendaciones();
		StringBuilder sb = new StringBuilder("Recomendaciones:\n");
		for (String r : recs)
			sb.append("- ").append(r).append("\n");
		JOptionPane.showMessageDialog(null, sb.toString());
	}

}
