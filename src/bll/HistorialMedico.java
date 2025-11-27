package bll;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;

public class HistorialMedico {

    private int id;
    private LocalDate fechaCreacion;
    private String observaciones;
    private String[] archivos;


    private Paciente paciente;
    private Medico medico;

    public HistorialMedico(int id, Paciente paciente, Medico medico,
                           LocalDate fechaCreacion, String observaciones, String[] archivos) {

        this.id = id;
        this.paciente = paciente;
        this.medico = medico;

        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDate.now();
        this.observaciones = observaciones;
        this.archivos = archivos != null ? archivos : new String[0];
    }


    public HistorialMedico(int id, LocalDate fechaCreacion, String observaciones, String[] archivos) {
        this.id = id;
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDate.now();
        this.observaciones = observaciones;
        this.archivos = archivos != null ? archivos : new String[0];
    }


    public HistorialMedico(int id, Date fecha, String observaciones, String[] archivos) {
        this.id = id;
        if (fecha != null) {
            this.fechaCreacion = new java.sql.Date(fecha.getTime()).toLocalDate();
        } else {
            this.fechaCreacion = LocalDate.now();
        }
        this.observaciones = observaciones;
        this.archivos = archivos != null ? archivos : new String[0];
    }



    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String[] getArchivos() { return archivos; }
    public void setArchivos(String[] archivos) { this.archivos = archivos != null ? archivos : new String[0]; }


    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }
    // AGREGADO ↑↑↑


    // Método para agregar archivos
    public void agregarArchivo(String archivo) {
        if (archivo != null && !archivo.isEmpty()) {
            archivos = Arrays.copyOf(archivos, archivos.length + 1);
            archivos[archivos.length - 1] = archivo;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("ID: ").append(id).append("\n");
        sb.append("Fecha de creación: ").append(fechaCreacion).append("\n");
        sb.append("Paciente: ").append(paciente != null ? paciente.getNombre() : "(no asignado)").append("\n");
        sb.append("Médico: ").append(medico != null ? medico.getNombre() : "(no asignado)").append("\n");
        sb.append("Observaciones: ").append(observaciones).append("\n");

        if (archivos.length > 0) {
            sb.append("Archivos:\n");
            for (String a : archivos)
                sb.append("- ").append(a).append("\n");
        } else {
            sb.append("Archivos: ninguno\n");
        }

        return sb.toString();
    }
}
