package bll;

import javax.swing.JOptionPane;

public class Paciente extends Usuario implements Menu {

	private int nroContrato;
	private String obraSocial;
	private HistorialMedico historial;

	public Paciente(String nombre, String apellido, String usuario, String contrasenia, int nroContrato,
			String obraSocial, HistorialMedico historial) {
		super(nombre, apellido, usuario, contrasenia);
		this.nroContrato = nroContrato;
		this.obraSocial = obraSocial;
		this.historial = historial;
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

	public void reservarTurno() {
		JOptionPane.showMessageDialog(null, "Turno reservado.");
	}

	public void cancelarTurno() {
		JOptionPane.showMessageDialog(null, "Turno cancelado.");
	}

	public void consultarTurno() {
		System.out.println("Consultando el turno.");
	}

	public void verResultado() {
		JOptionPane.showMessageDialog(null, "Visualizando los resultados.");
	}

	// Menú
	@Override
	public void MostrarMenu() {
		String[] opciones = { "Reservar turno", "Cancelar turno", "Consultar turno", "Ver historial médico", "Salir" };
		int elegido;
		do {
			elegido = JOptionPane.showOptionDialog(null, "Elija opción", "Paciente", 0, 0, null, opciones, opciones[0]);

			switch (elegido) {
			case 0:
				reservarTurno();
				break;
			case 1:
				cancelarTurno();
				break;
			case 2:
				consultarTurno();
				break;
			case 3:
				verResultado();
				break;
			case 4:
				JOptionPane.showMessageDialog(null, "Saliendo...");
				break;
			default:
				JOptionPane.showMessageDialog(null, "Opción inválida.");
			}
		} while (elegido != 4);
	}

	@Override
	public String toString() {
		return super.toString() + "Paciente nro. de contrato=" + nroContrato + ", obra social=" + obraSocial + ".";
	}

}
