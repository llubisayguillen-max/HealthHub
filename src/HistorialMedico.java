import java.time.LocalDate;
import java.util.Date;



public class HistorialMedico {

	private int id;
	private LocalDate fechaCreacion;
	private String observaciones;
	private String[] archivos;

	public HistorialMedico(int id, LocalDate date, String observaciones, String[] archivos) {
		this.id = id;
		this.fechaCreacion = date;
		this.observaciones = observaciones;
		this.archivos = archivos;
	}


	public HistorialMedico(int id2, Date date, String observaciones2, String[] archivos2) {
		// TODO Auto-generated constructor stub
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public LocalDate getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDate fechaCreacion) {
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
		System.out.println("Agregando al historial médico.");
	}

	@Override
	public String toString() {
		return "HISTORIAL MÉDICO: \nId=" + id + "\nFecha de creación=" + fechaCreacion + "\nObservaciones="
				+ observaciones;
	}
}
