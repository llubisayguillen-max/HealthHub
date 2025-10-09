package dll;

import bll.Paciente;
import bll.HistorialMedico;
import bll.Medico;
import bll.Turno;
import java.util.ArrayList;
import java.util.List;

public class ControllerPaciente {

    private Paciente paciente;
    private List<Turno> turnosDisponibles;
    private List<Medico> medicosDisponibles;

  
    public ControllerPaciente(Paciente paciente, List<Medico> medicos, List<Turno> turnos) {
        this.paciente = paciente;
        this.medicosDisponibles = medicos != null ? medicos : new ArrayList<>();
        this.turnosDisponibles = turnos != null ? turnos : new ArrayList<>();
    }

    // Iniciar sesión 
    public boolean iniciarSesion(String email, String password) {
        if (paciente.getEmail().equalsIgnoreCase(email) && paciente.getPassword().equals(password)) {
            System.out.println("Sesión iniciada correctamente. ¡Bienvenido, " + paciente.getNombre() + "!");
            return true;
        } else {
            System.out.println("Credenciales incorrectas. Intente nuevamente.");
            return false;
        }
    }

    // Filtrar médicos por especialidad
    public List<Medico> filtrarPorEspecialidad(String especialidad) {
        List<Medico> resultado = new ArrayList<>();
        for (Medico m : medicosDisponibles) {
            if (m.getEspecialidad().equalsIgnoreCase(especialidad)) {
                resultado.add(m);
            }
        }
        return resultado;
    }

    // Mostrar horarios disponibles
    public void mostrarHorariosDisponibles(Medico medico) {
        if (medico != null) {
            medico.mostrarHorariosDisponibles();
        } else {
            System.out.println("Médico no encontrado.");
        }
    }

    // Solicitar turno
    public void solicitarTurno(Turno turno) {
        if (turnosDisponibles.contains(turno)) {
            paciente.agregarTurno(turno);
            turnosDisponibles.remove(turno);
            System.out.println("Turno solicitado correctamente con el Dr. " + turno.getMedico().getNombre());
        } else {
            System.out.println("El turno seleccionado ya no está disponible.");
        }
    }

    // Cancelar turno
    public void cancelarTurno(Turno turno) {
        if (paciente.getTurnos().contains(turno)) {
            paciente.cancelarTurno(turno);
            turnosDisponibles.add(turno);
            System.out.println("Turno cancelado correctamente.");
        } else {
            System.out.println("No se encontró el turno especificado.");
        }
    }

    // Ver información de turnos activos
    public void verTurnosActivos() {
        System.out.println("Turnos activos del paciente:");
        if (paciente.getTurnos().isEmpty()) {
            System.out.println("No hay turnos activos.");
            return;
        }
        for (Turno t : paciente.getTurnos()) {
            System.out.println(t);
        }
    }

    // Ver resultados de estudios / recetas
    public void verResultadosMedicos() {
        HistorialMedico historial = paciente.getHistorialMedico();
        if (historial != null) {
            System.out.println(historial);
        } else {
            System.out.println("No hay resultados médicos cargados.");
        }
    }

    // Recomendaciones según obra social
    public void mostrarRecomendaciones() {
        String obra = paciente.getObraSocial();
        System.out.println("Recomendaciones para afiliados de " + obra + ":");
        switch (obra.toLowerCase()) {
            case "osde":
                System.out.println("- Podés solicitar turnos online con prioridad en especialistas.");
                break;
            case "swiss medical":
                System.out.println("- Consultas virtuales sin cargo las 24hs.");
                break;
            case "pami":
                System.out.println("- Control gratuito de salud cada 6 meses.");
                break;
            default:
                System.out.println("- Consultá beneficios específicos con tu obra social.");
                break;
        }
    }

    // Guardar médico en favoritos
    public void agregarAFavoritos(Medico medico) {
        if (!paciente.getFavoritos().contains(medico)) {
            paciente.getFavoritos().add(medico);
            System.out.println( medico.getNombre() + " agregado a favoritos.");
        } else {
            System.out.println("El médico ya está en tu lista de favoritos.");
        }
    }

    // Ver médicos favoritos
    public void verFavoritos() {
        System.out.println("Médicos favoritos:");
        if (paciente.getFavoritos().isEmpty()) {
            System.out.println("No hay médicos favoritos aún.");
            return;
        }
        for (Medico m : paciente.getFavoritos()) {
            System.out.println("- " + m.getNombre() + " (" + m.getEspecialidad() + ")");
        }
    }
}