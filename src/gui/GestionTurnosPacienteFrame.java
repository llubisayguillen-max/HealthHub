package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import bll.Paciente;
import bll.Turno;
import dll.ControllerPaciente;

public class GestionTurnosPacienteFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private Paciente pacienteLogueado;
	private JTable tablaTurnos;
	private DefaultTableModel modeloTabla;
	private ControllerPaciente controllerPaciente;

	public GestionTurnosPacienteFrame(Paciente pacienteLogueado) {
		this.pacienteLogueado = pacienteLogueado;
		this.controllerPaciente = new ControllerPaciente(pacienteLogueado);

		setTitle("Gestión de Turnos");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setMinimumSize(new Dimension(900, 600));
		setLocationRelativeTo(null);

		// === PANEL PRINCIPAL ===
		JPanel panelPrincipal = new JPanel(new BorderLayout());
		panelPrincipal.setBackground(Color.WHITE);

		// === ENCABEZADO ===
		JPanel panelEncabezado = new JPanel();
		panelEncabezado.setBackground(new Color(91, 155, 213));
		panelEncabezado.setPreferredSize(new Dimension(0, 80));
		panelEncabezado.setLayout(new BorderLayout());
		panelEncabezado.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

		JLabel lblTitulo = new JLabel("Gestión de Turnos - " + pacienteLogueado.getNombre(), SwingConstants.CENTER);
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
		lblTitulo.setForeground(Color.WHITE);
		panelEncabezado.add(lblTitulo, BorderLayout.CENTER);

		// === TABLA DE TURNOS ===
		modeloTabla = new DefaultTableModel(new Object[] { "Especialidad", "Médico", "Fecha y Hora", "Estado" }, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false; // Bloquear edición de celdas
			}
		};

		tablaTurnos = new JTable(modeloTabla);
		JScrollPane scrollTabla = new JScrollPane(tablaTurnos);
		scrollTabla.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

		// === Alinear columnas ===
		DefaultTableCellRenderer izquierda = new DefaultTableCellRenderer();
		izquierda.setHorizontalAlignment(SwingConstants.LEFT);

		DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
		centrado.setHorizontalAlignment(SwingConstants.CENTER);

		tablaTurnos.getColumnModel().getColumn(0).setCellRenderer(izquierda); // Especialidad
		tablaTurnos.getColumnModel().getColumn(1).setCellRenderer(izquierda); // Médico
		tablaTurnos.getColumnModel().getColumn(2).setCellRenderer(centrado); // Fecha y Hora
		tablaTurnos.getColumnModel().getColumn(3).setCellRenderer(centrado); // Estado

		// === Ajustar ancho de columnas ===
		tablaTurnos.getColumnModel().getColumn(0).setPreferredWidth(100); // Especialidad
		tablaTurnos.getColumnModel().getColumn(1).setPreferredWidth(150); // Médico
		tablaTurnos.getColumnModel().getColumn(2).setPreferredWidth(180); // Fecha y Hora
		tablaTurnos.getColumnModel().getColumn(3).setPreferredWidth(100); // Estado

		// === BOTONES ABAJO ===
		JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
		panelBotones.setBackground(Color.WHITE);

		JButton btnNuevo = new JButton("Nuevo Turno");
		btnNuevo.setBackground(new Color(0, 102, 204));
		btnNuevo.setForeground(Color.WHITE);

		JButton btnConfirmar = new JButton("Confirmar Turno");
		btnConfirmar.setBackground(new Color(0, 153, 0));
		btnConfirmar.setForeground(Color.WHITE);

		JButton btnCancelar = new JButton("Cancelar Turno");
		btnCancelar.setBackground(new Color(204, 0, 0));
		btnCancelar.setForeground(Color.WHITE);

		JButton btnVolver = new JButton("Volver al Menú");
		btnVolver.setBackground(new Color(100, 100, 100));
		btnVolver.setForeground(Color.WHITE);

		Font fuenteBoton = new Font("Segoe UI", Font.BOLD, 15);
		for (JButton b : new JButton[] { btnNuevo, btnConfirmar, btnCancelar, btnVolver }) {
			b.setFont(fuenteBoton);
			b.setFocusPainted(false);
			b.setPreferredSize(new Dimension(200, 40));
			b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		}

		panelBotones.add(btnNuevo);
		panelBotones.add(btnConfirmar);
		panelBotones.add(btnCancelar);
		panelBotones.add(btnVolver);

		// === ACCIONES DE BOTONES ===
		btnNuevo.addActionListener(e -> {
			NuevoTurnoPacienteFrame nuevoTurno = new NuevoTurnoPacienteFrame(pacienteLogueado, this);
			nuevoTurno.setVisible(true);
		});

		btnConfirmar.addActionListener(e -> {
			int fila = tablaTurnos.getSelectedRow();
			if (fila < 0) {
				JOptionPane.showMessageDialog(this, "Seleccione un turno para confirmar", "Atención",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			try {
				Turno turno = controllerPaciente.turnosActivos().get(fila);
				controllerPaciente.confirmarAsistencia(turno.getIdTurno()); // Pasar el ID
				JOptionPane.showMessageDialog(this, "Turno confirmado correctamente", "Éxito",
						JOptionPane.INFORMATION_MESSAGE);
				cargarTurnos(); // Refresca la tabla
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "Error al confirmar turno: " + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		});

		btnCancelar.addActionListener(e -> {
			int fila = tablaTurnos.getSelectedRow();
			if (fila < 0) {
				JOptionPane.showMessageDialog(this, "Seleccione un turno para cancelar", "Atención",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			try {
				Turno turno = controllerPaciente.turnosActivos().get(fila);
				controllerPaciente.cancelarTurno(turno.getIdTurno()); // Pasar el ID
				JOptionPane.showMessageDialog(this, "Turno cancelado correctamente", "Éxito",
						JOptionPane.INFORMATION_MESSAGE);
				cargarTurnos(); // Refresca la tabla
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "Error al cancelar turno: " + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		});

		btnVolver.addActionListener(e -> {
			new MenuPacienteFrame(pacienteLogueado).setVisible(true);
			dispose();
		});

		// === ARMADO FINAL ===
		panelPrincipal.add(panelEncabezado, BorderLayout.NORTH);
		panelPrincipal.add(scrollTabla, BorderLayout.CENTER);
		panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

		setContentPane(panelPrincipal);

		// Cargar los turnos al iniciar
		cargarTurnos();
	}

	public void cargarTurnos() {
		modeloTabla.setRowCount(0);
		try {
			List<Turno> turnos = controllerPaciente.turnosActivos();

			// === Formatos de fecha y hora en español (usar forLanguageTag para evitar el
			// constructor deprecated) ===
			SimpleDateFormat formatoFecha = new SimpleDateFormat("EEEE dd 'de' MMMM yyyy",
					Locale.forLanguageTag("es-AR"));
			SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm");

			for (Turno t : turnos) {
				String especialidad = t.getMedico() != null && t.getMedico().getEspecialidad() != null
						? t.getMedico().getEspecialidad()
						: "Sin especialidad";

				String medico = t.getMedico() != null ? t.getMedico().getNombreCompleto() : "Sin médico";

				// formatear fecha y hora
				String fecha = formatoFecha.format(t.getFechaHora());
				// capitalizar la primera letra (opcional, para que muestre "Miércoles" en lugar
				// de "miércoles")
				if (fecha != null && !fecha.isEmpty()) {
					fecha = fecha.substring(0, 1).toUpperCase() + fecha.substring(1);
				}
				String hora = formatoHora.format(t.getFechaHora());
				String fechaYHora = fecha + " - " + hora;

				modeloTabla.addRow(new Object[] { especialidad, medico, fechaYHora, t.getEstado() });
			}

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Atención", JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
