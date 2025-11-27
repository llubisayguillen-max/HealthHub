package bll;

import dll.ControllerPaciente;
import dll.ControllerPaciente.TurnoDisponible;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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

	// --- Getters y Setters ---

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
			default -> {
				if (elegido != -1)
					JOptionPane.showMessageDialog(null, "Opción inválida.");
				else
					elegido = 7; // Si cierra la ventana, salir
			}
			}
		} while (elegido != 7);
	}

	private void reservarTurnoMenu() {
		try {
			// Obtener Especialidades
			String[] especialidades = controller.obtenerEspecialidades();
			if (especialidades.length == 0) {
				JOptionPane.showMessageDialog(null, "No hay especialidades disponibles.");
				return;
			}

			// Seleccionar Especialidad
			String especialidad = (String) JOptionPane.showInputDialog(null, "Seleccione la especialidad:",
					"Reservar Turno", JOptionPane.QUESTION_MESSAGE, null, especialidades, especialidades[0]);

			if (especialidad == null)
				return;

			// Ingresar Fecha
			String fechaStr = JOptionPane.showInputDialog("Ingrese fecha (dd/MM/yyyy):");
			if (fechaStr == null || fechaStr.isBlank())
				return;

			LocalDate fecha;
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
				fecha = LocalDate.parse(fechaStr, formatter);
			} catch (DateTimeParseException e) {
				JOptionPane.showMessageDialog(null, "Formato de fecha inválido. Use dd/MM/yyyy.");
				return;
			}

			// Buscar Turnos Disponibles
			List<TurnoDisponible> disponibles;
			try {
				disponibles = controller.buscarTurnos(especialidad, fecha, fecha);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e.getMessage());
				return;
			}

			// Mostrar lista y seleccionar
			StringBuilder sb = new StringBuilder("Horarios disponibles:\n");
			for (int i = 0; i < disponibles.size(); i++) {
				TurnoDisponible td = disponibles.get(i);
				sb.append(i + 1).append(". ").append(td.medicoNombre()).append(" - ").append(td.horaInicio())
						.append("\n");
			}

			String seleccionStr = JOptionPane.showInputDialog(sb.toString() + "Ingrese el número del horario:");
			if (seleccionStr == null)
				return;

			int index = Integer.parseInt(seleccionStr) - 1;
			if (index < 0 || index >= disponibles.size()) {
				JOptionPane.showMessageDialog(null, "Selección inválida.");
				return;
			}

			// Reservar usando ID de Disponibilidad
			long idDisponibilidad = disponibles.get(index).idDisponibilidad();
			long idTurno = controller.solicitarTurno(idDisponibilidad);

			JOptionPane.showMessageDialog(null, "Turno reservado con éxito. ID: " + idTurno);

		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Debe ingresar un número válido.");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
		}
	}

	private void cancelarTurnoMenu() {
		try {
			List<Turno> turnos = controller.turnosActivos();

			// Construir menú de selección
			StringBuilder sb = new StringBuilder("Turnos activos:\n");
			for (int i = 0; i < turnos.size(); i++) {
				Turno t = turnos.get(i);
				String medico = (t.getMedico() != null) ? t.getMedico().getNombreCompleto() : "Sin médico";
				sb.append(i + 1).append(". ").append(medico).append(" - ").append(t.getFechaHora()).append(" - ")
						.append(t.getEstado()).append("\n");
			}

			String input = JOptionPane.showInputDialog(sb + "Ingrese el número del turno a cancelar:");
			if (input == null)
				return;

			int selTurno = Integer.parseInt(input) - 1;

			if (selTurno < 0 || selTurno >= turnos.size()) {
				JOptionPane.showMessageDialog(null, "Selección inválida.");
				return;
			}

			Turno t = turnos.get(selTurno);

			controller.cancelarTurno(t.getIdTurno());

			t.cancelar();
			JOptionPane.showMessageDialog(null, "Turno cancelado con éxito.");

		} catch (IllegalStateException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Debes ingresar un número válido.");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error cancelando turno: " + e.getMessage());
		}
	}

	private void consultarTurnoMenu() {
		try {
			List<Turno> turnos = controller.turnosActivos();

			StringBuilder sb = new StringBuilder("Turnos activos:\n");
			for (int i = 0; i < turnos.size(); i++) {
				Turno t = turnos.get(i);
				String medico = (t.getMedico() != null) ? t.getMedico().getNombreCompleto() : "N/D";
				sb.append(i + 1).append(". Médico: ").append(medico).append(" - Fecha: ").append(t.getFechaHora())
						.append(" - Estado: ").append(t.getEstado()).append("\n");
			}
			JOptionPane.showMessageDialog(null, sb.toString());

		} catch (IllegalStateException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
		}
	}

	private void agregarFavoritoMenu() {
		String usernameMedico = JOptionPane.showInputDialog("Ingrese el username del médico a agregar como favorito:");
		if (usernameMedico == null || usernameMedico.isBlank())
			return;

		try {
			controller.agregarAFavoritos(usernameMedico);
			JOptionPane.showMessageDialog(null, "Médico agregado a favoritos correctamente.");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error agregando favorito: " + e.getMessage());
		}
	}

	private void verFavoritos() {
		try {
			List<Medico> favoritos = controller.verFavoritos();

			StringBuilder sb = new StringBuilder("Médicos favoritos:\n");
			for (Medico m : favoritos)
				sb.append(m.getNombreCompleto()).append(" - ").append(m.getEspecialidad()).append("\n");

			JOptionPane.showMessageDialog(null, sb.toString());

		} catch (IllegalStateException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
		}
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
		try {
			List<String> recs = controller.mostrarRecomendaciones();
			StringBuilder sb = new StringBuilder("Recomendaciones:\n");
			for (String r : recs)
				sb.append("- ").append(r).append("\n");
			JOptionPane.showMessageDialog(null, sb.toString());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error al obtener recomendaciones: " + e.getMessage());
		}
	}
}