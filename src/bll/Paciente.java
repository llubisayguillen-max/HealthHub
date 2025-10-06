package bll;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Paciente extends Usuario implements Menu {

	private int nroContrato;
	private String obraSocial;
	private HistorialMedico historial;
	private List<Medico> favoritos;

	public Paciente(String nombre, String apellido, String usuario, String contrasenia, int nroContrato,
			String obraSocial, HistorialMedico historial) {
		super(nombre, apellido, usuario, contrasenia);
		this.nroContrato = nroContrato;
		this.obraSocial = obraSocial;
		this.historial = historial;
		this.favoritos = new ArrayList<>();
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

	public List<Medico> getFavoritos() {
		return favoritos;
	}

	public void reservarTurno(Medico m, String horario) {
		JOptionPane.showMessageDialog(null, "Turno reservado con " + m.getNombre() + " a las " + horario);
	}

	public void cancelarTurno(Medico m, String horario) {
		JOptionPane.showMessageDialog(null, "Turno cancelado con " + m.getNombre() + " a las " + horario);
	}

	public void verResultado() {
		if (historial != null) {
			JOptionPane.showMessageDialog(null, historial.toString());
		} else {
			JOptionPane.showMessageDialog(null, "No hay resultados disponibles.");
		}
	}

	public void recibirRecomendaciones() {
		JOptionPane.showMessageDialog(null, "Recomendaciones para obra social: " + obraSocial);
	}

	public void agregarFavorito(Medico m) {
		if (!favoritos.contains(m)) {
			favoritos.add(m);
			JOptionPane.showMessageDialog(null, m.getNombre() + " agregado a favoritos.");
		} else {
			JOptionPane.showMessageDialog(null, m.getNombre() + " ya está en favoritos.");
		}
	}

	public void verFavoritos() {
		if (favoritos.isEmpty()) {
			JOptionPane.showMessageDialog(null, "No tenes médicos favoritos.");
		} else {
			StringBuilder sb = new StringBuilder("Médicos favoritos:\n");
			for (Medico m : favoritos)
				sb.append(m.getNombre()).append(" - ").append(m.getEspecialidad()).append("\n");
			JOptionPane.showMessageDialog(null, sb.toString());
		}
	}

	@Override
	public void MostrarMenu() {
		String[] opciones = { "Reservar turno", "Cancelar turno", "Consultar turno", "Ver historial médico",
				"Ver favoritos", "Recibir recomendaciones", "Salir" };
		int elegido;
		do {
			elegido = JOptionPane.showOptionDialog(null, "Elija opción", "Paciente", 0, 0, null, opciones, opciones[0]);
			switch (elegido) {
			case 0:
				JOptionPane.showMessageDialog(null, "Reservar turno");
				break;
			case 1:
				JOptionPane.showMessageDialog(null, "Cancelar turno");
				break;
			case 2:
				JOptionPane.showMessageDialog(null, "Consultar turno");
				break;
			case 3:
				verResultado();
				break;
			case 4:
				verFavoritos();
				break;
			case 5:
				recibirRecomendaciones();
				break;
			case 6:
				JOptionPane.showMessageDialog(null, "Saliendo...");
				break;
			default:
				JOptionPane.showMessageDialog(null, "Opción inválida.");
				break;
			}
		} while (elegido != 6);
	}

	@Override
	public String toString() {
		return super.toString() + " Paciente nro. de contrato=" + nroContrato + ", obra social=" + obraSocial + ".";
	}
}
