package bll;

import java.util.Date;
import java.text.SimpleDateFormat;

public class Consulta {

    private Date fecha;
    private String motivo;
    private String diagnostico;
    private String tratamiento;
    private String seguimiento;

    public Consulta(Date fecha, String motivo, String diagnostico, String tratamiento, String seguimiento) {
        this.fecha = (fecha != null) ? fecha : new Date();
        this.motivo = motivo;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.seguimiento = seguimiento;
    }


    public static Consulta hoy(String motivo, String diagnostico, String tratamiento, String seguimiento) {
        return new Consulta(new Date(), motivo, diagnostico, tratamiento, seguimiento);
    }


    public Date getFecha() { 
    	return fecha; 
    	}
    
    public void setFecha(Date fecha) { 
    	this.fecha = fecha; 
    	}

    public String getMotivo() { 
    	return motivo; 
    	}
    public void setMotivo(String motivo) { 
    	this.motivo = motivo; 
    	}

    public String getDiagnostico() { 
    	return diagnostico; 
    	}
    public void setDiagnostico(String diagnostico) {
    	this.diagnostico = diagnostico;
    	}

    public String getTratamiento() {
    	return tratamiento; 
    	}
    public void setTratamiento(String tratamiento) {
    	this.tratamiento = tratamiento; 
    	}

    public String getSeguimiento() { 
    	return seguimiento; 
    	}
    public void setSeguimiento(String seguimiento) { 
    	this.seguimiento = seguimiento; 
    	}


    public String getFechaFormateada() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(fecha);
    }

    @Override
    public String toString() {
        return "Consulta{" +
                "fecha=" + getFechaFormateada() +
                ", motivo='" + motivo + '\'' +
                ", diagnostico='" + diagnostico + '\'' +
                ", tratamiento='" + tratamiento + '\'' +
                ", seguimiento='" + seguimiento + '\'' +
                '}';
    }
}

