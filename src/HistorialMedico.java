import java.util.Date;

public class HistorialMedico {

	private int id;
	private Date fechaCreacion;
	private String observaciones;
	private String[] archivos;

	public HistorialMedico(int id, Date fechaCreacion, String observaciones, String[] archivos) {
		this.id = id;
		this.fechaCreacion = fechaCreacion;
		this.observaciones = observaciones;
		this.archivos = archivos;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

	public String[] getArchivos() {
		return archivos;
	}

	public void setArchivos(String[] archivos) {
		this.archivos = archivos;
	}

	public void agregarHistoria() {
		System.out.println("Agregando historial médico.");
	}

	@Override
	public String toString() {
		return "Historial Médico: \nId=" + id + "\nFecha de creación=" + fechaCreacion + "\nObservaciones="
				+ observaciones;
	}
}
