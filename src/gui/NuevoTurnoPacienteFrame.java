package gui;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import bll.Medico;
import bll.Paciente;
import dll.ControllerPaciente;

public class NuevoTurnoPacienteFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private Paciente pacienteLogueado;
	private ControllerPaciente controllerPaciente;
	private GestionTurnosPacienteFrame gestionTurnosPadre;

	private JComboBox<String> comboEspecialidad;
	private JComboBox<String> comboMedico;
	private JComboBox<String> comboFecha;
	private JComboBox<String> comboHora;
	private JButton btnReservar;

	private List<Medico> medicosFiltrados = new ArrayList<>();
	// Guardamos fechas reales internamente
	private Map<String, List<Date>> fechasDisponiblesPorMedico = new HashMap<>();

	// Formato de fecha para mostrar y formato de hora
	private final SimpleDateFormat formatoDisplay = new SimpleDateFormat("EEEE dd 'de' MMMM yyyy",
			new Locale("es", "AR"));
	private final SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm");
	private final SimpleDateFormat formatoInternoFecha = new SimpleDateFormat("yyyy-MM-dd");

	public NuevoTurnoPacienteFrame(Paciente pacienteLogueado, GestionTurnosPacienteFrame gestionTurnosPadre) {
		this.pacienteLogueado = pacienteLogueado;
		this.gestionTurnosPadre = gestionTurnosPadre;
		this.controllerPaciente = new ControllerPaciente(pacienteLogueado);

		setTitle("Nuevo Turno - " + pacienteLogueado.getNombre());
		setSize(600, 400);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());

		JPanel panelPrincipal = new JPanel(new GridBagLayout());
		panelPrincipal.setBackground(Color.WHITE);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 20, 10, 20);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// === Especialidad ===
		JLabel lblEspecialidad = new JLabel("Especialidad:");
		gbc.gridx = 0;
		gbc.gridy = 0;
		panelPrincipal.add(lblEspecialidad, gbc);

		comboEspecialidad = new JComboBox<>(controllerPaciente.obtenerEspecialidades());
		gbc.gridx = 1;
		gbc.gridy = 0;
		panelPrincipal.add(comboEspecialidad, gbc);

		// === Médico ===
		JLabel lblMedico = new JLabel("Médico:");
		gbc.gridx = 0;
		gbc.gridy = 1;
		panelPrincipal.add(lblMedico, gbc);

		comboMedico = new JComboBox<>();
		gbc.gridx = 1;
		gbc.gridy = 1;
		panelPrincipal.add(comboMedico, gbc);

		// === Fecha ===
		JLabel lblFecha = new JLabel("Fecha:");
		gbc.gridx = 0;
		gbc.gridy = 2;
		panelPrincipal.add(lblFecha, gbc);

		comboFecha = new JComboBox<>();
		gbc.gridx = 1;
		gbc.gridy = 2;
		panelPrincipal.add(comboFecha, gbc);

		// === Hora ===
		JLabel lblHora = new JLabel("Hora:");
		gbc.gridx = 0;
		gbc.gridy = 3;
		panelPrincipal.add(lblHora, gbc);

		comboHora = new JComboBox<>();
		gbc.gridx = 1;
		gbc.gridy = 3;
		panelPrincipal.add(comboHora, gbc);

		// === Botón Reservar ===
		btnReservar = new JButton("Reservar Turno");
		btnReservar.setBackground(new Color(0, 102, 204));
		btnReservar.setForeground(Color.WHITE);
		btnReservar.setFont(new Font("Segoe UI", Font.BOLD, 15));
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		panelPrincipal.add(btnReservar, gbc);

		add(panelPrincipal, BorderLayout.CENTER);

		// === Listeners ===
		comboEspecialidad.addActionListener(e -> cargarMedicos());
		comboMedico.addActionListener(e -> cargarFechas());
		comboFecha.addActionListener(e -> cargarHoras());
		btnReservar.addActionListener(e -> reservarTurno());

		// Carga inicial
		if (comboEspecialidad.getItemCount() > 0)
			comboEspecialidad.setSelectedIndex(0);
		cargarMedicos();
	}

	private void cargarMedicos() {
		comboMedico.removeAllItems();
		medicosFiltrados.clear();
		fechasDisponiblesPorMedico.clear();
		comboFecha.removeAllItems();
		comboHora.removeAllItems();

		String especialidad = (String) comboEspecialidad.getSelectedItem();
		if (especialidad == null)
			return;

		medicosFiltrados = controllerPaciente.filtrarPorEspecialidad(especialidad);

		for (Medico m : medicosFiltrados) {
			comboMedico.addItem(m.getNombreCompleto());

			// Cargar fechas disponibles internas
			List<String> franjas = controllerPaciente.obtenerHorariosDisponibles(m.getUsuario());
			Set<Date> fechasUnicas = new LinkedHashSet<>();
			try {
				for (String f : franjas) {
					String fechaStr = f.split(" - ")[0].trim(); // Debe venir en formato yyyy-MM-dd
					Date fecha = formatoInternoFecha.parse(fechaStr);
					fechasUnicas.add(fecha);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			fechasDisponiblesPorMedico.put(m.getUsuario(), new ArrayList<>(fechasUnicas));
		}

		if (comboMedico.getItemCount() > 0)
			comboMedico.setSelectedIndex(0);
		cargarFechas();
	}

	private void cargarFechas() {
		comboFecha.removeAllItems();
		comboHora.removeAllItems();

		int idxMedico = comboMedico.getSelectedIndex();
		if (idxMedico < 0)
			return;

		Medico medico = medicosFiltrados.get(idxMedico);
		List<Date> fechas = fechasDisponiblesPorMedico.getOrDefault(medico.getUsuario(), new ArrayList<>());
		for (Date f : fechas) {
			comboFecha.addItem(formatoDisplay.format(f));
		}

		if (comboFecha.getItemCount() > 0)
			comboFecha.setSelectedIndex(0);
		cargarHoras();
	}

	private void cargarHoras() {
		comboHora.removeAllItems();

		int idxMedico = comboMedico.getSelectedIndex();
		int idxFecha = comboFecha.getSelectedIndex();
		if (idxMedico < 0 || idxFecha < 0)
			return;

		Medico medico = medicosFiltrados.get(idxMedico);
		Date fechaSeleccionada = fechasDisponiblesPorMedico.get(medico.getUsuario()).get(idxFecha);

		List<String> franjas = controllerPaciente.obtenerHorariosDisponibles(medico.getUsuario());
		for (String f : franjas) {
			String[] partes = f.split(" - ");
			try {
				Date fecha = formatoInternoFecha.parse(partes[0].trim());
				if (!fecha.equals(fechaSeleccionada))
					continue;

				String horaInicio = partes[1].substring(0, 5); // "HH:mm"
				comboHora.addItem(horaInicio);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (comboHora.getItemCount() > 0)
			comboHora.setSelectedIndex(0);
	}

	private void reservarTurno() {
		int idxMedico = comboMedico.getSelectedIndex();
		int idxFecha = comboFecha.getSelectedIndex();
		int idxHora = comboHora.getSelectedIndex();

		if (idxMedico < 0 || idxFecha < 0 || idxHora < 0) {
			JOptionPane.showMessageDialog(this, "Debe seleccionar médico, fecha y hora", "Atención",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		Medico medico = medicosFiltrados.get(idxMedico);
		Date fechaSeleccionada = fechasDisponiblesPorMedico.get(medico.getUsuario()).get(idxFecha);
		String horaStr = (String) comboHora.getSelectedItem();

		try {
			Date hora = formatoHora.parse(horaStr);

			Calendar cal = Calendar.getInstance();
			cal.setTime(fechaSeleccionada);
			Calendar calHora = Calendar.getInstance();
			calHora.setTime(hora);

			cal.set(Calendar.HOUR_OF_DAY, calHora.get(Calendar.HOUR_OF_DAY));
			cal.set(Calendar.MINUTE, calHora.get(Calendar.MINUTE));
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			Date fechaHoraTurno = cal.getTime();
			controllerPaciente.solicitarTurno(medico, fechaHoraTurno);

			JOptionPane.showMessageDialog(this, "Turno reservado correctamente!", "Éxito",
					JOptionPane.INFORMATION_MESSAGE);
			if (gestionTurnosPadre != null)
				gestionTurnosPadre.cargarTurnos();
			dispose();

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error al reservar el turno: " + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
