public class Paciente extends Usuario {
 
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
		System.out.println("Turno reservado.");
	}

	public void cancelarTurno() {
		System.out.println("Turno cancelado.");
	}

	public void consultarTurno() {
		System.out.println("Consultando sobre el turno.");
	}

	public void verResultado() {
		System.out.println("Visualizando los resultados.");
	}

	
	@Override
	public String toString() {
		return super.toString() + "Paciente nro. de Contrato:" + nroContrato + ", obraSocial:" + obraSocial + ".";
	}
}


