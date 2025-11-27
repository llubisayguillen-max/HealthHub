package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.toedter.calendar.JDateChooser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bll.Paciente;
import dll.ControllerPaciente;
import dll.ControllerPaciente.TurnoDisponible;

import static gui.UiPaleta.*;
import static gui.UiFonts.*;

public class NuevoTurnoPacienteFrame extends JFrame {

	private final Paciente paciente;
	private final ControllerPaciente controllerPaciente;

	private JComboBox<String> cmbEspecialidad;
	// CAMBIO: Dos selectores de fecha en lugar de uno
	private JDateChooser dcDesde;
	private JDateChooser dcHasta;

	private JTable tablaTurnosDisponibles;
	private DefaultTableModel modeloTabla;
	private List<TurnoDisponible> resultadosBusqueda;

	private static final DateTimeFormatter F_DDMMYYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public NuevoTurnoPacienteFrame(Paciente paciente) {
		this.paciente = paciente;
		this.controllerPaciente = new ControllerPaciente(paciente);
		this.resultadosBusqueda = new ArrayList<>();

		setTitle("HealthHub - Nuevo Turno");
		setSize(900, 650);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);

		initUI();
		initDefaultValues();
		cargarEspecialidades();
	}

	private void initUI() {
		getContentPane().setBackground(COLOR_BACKGROUND);
		setLayout(new BorderLayout());

		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(COLOR_PRIMARY);
		header.setPreferredSize(new Dimension(getWidth(), 80));
		header.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));

		JLabel lblTitulo = new JLabel("Solicitar nuevo turno");
		lblTitulo.setForeground(Color.WHITE);
		lblTitulo.setFont(H1_APP);

		JLabel lblSub = new JLabel("Búsqueda por especialidad y rango de fechas");
		lblSub.setForeground(new Color(220, 235, 245));
		lblSub.setFont(BODY_SMALL);

		JPanel leftHeader = new JPanel();
		leftHeader.setOpaque(false);
		leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
		leftHeader.add(lblTitulo);
		leftHeader.add(Box.createVerticalStrut(3));
		leftHeader.add(lblSub);

		header.add(leftHeader, BorderLayout.WEST);

		RoundedButton btnVolverHeader = new RoundedButton("Volver");
		btnVolverHeader.setBackground(COLOR_PRIMARY);
		btnVolverHeader.setForeground(Color.WHITE);
		btnVolverHeader.setFont(BUTTON);
		btnVolverHeader.setFocusPainted(false);

		btnVolverHeader.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(255, 255, 255, 200), 1, true),
				BorderFactory.createEmptyBorder(6, 16, 6, 16)));

		btnVolverHeader.setCursor(new Cursor(Cursor.HAND_CURSOR));

		// Hover
		btnVolverHeader.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {

				btnVolverHeader.setBackground(new Color(40, 140, 190));
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {

				btnVolverHeader.setBackground(COLOR_PRIMARY);
			}
		});

		btnVolverHeader.addActionListener(e -> {
			new GestionTurnosPacienteFrame(paciente).setVisible(true);
			dispose();
		});

		JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		rightHeader.setOpaque(false);
		rightHeader.add(btnVolverHeader);

		header.add(rightHeader, BorderLayout.EAST);

		JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
		filtros.setBackground(Color.WHITE);
		filtros.setBorder(BorderFactory.createEmptyBorder(0, 40, 5, 40));

		JLabel lblEspecialidad = new JLabel("Especialidad:");
		lblEspecialidad.setFont(BODY_SMALL);
		filtros.add(lblEspecialidad);

		cmbEspecialidad = new JComboBox<>();
		cmbEspecialidad.setPreferredSize(new Dimension(150, 26));
		filtros.add(cmbEspecialidad);

		JLabel lblDesde = new JLabel("Desde:");
		lblDesde.setFont(BODY_SMALL);
		filtros.add(lblDesde);

		dcDesde = new JDateChooser();
		dcDesde.setDateFormatString("dd/MM/yyyy");
		dcDesde.setPreferredSize(new Dimension(120, 26));
		filtros.add(dcDesde);

		JLabel lblHasta = new JLabel("Hasta:");
		lblHasta.setFont(BODY_SMALL);
		filtros.add(lblHasta);

		dcHasta = new JDateChooser();
		dcHasta.setDateFormatString("dd/MM/yyyy");
		dcHasta.setPreferredSize(new Dimension(120, 26));
		filtros.add(dcHasta);

		RoundedButton btnBuscar = new RoundedButton("Buscar");
		btnBuscar.setBackground(COLOR_ACCENT);
		btnBuscar.setForeground(Color.WHITE);
		btnBuscar.setFont(BUTTON);
		btnBuscar.setFocusPainted(false);
		btnBuscar.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));
		btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnBuscar.addActionListener(this::onBuscarTurnos);

		filtros.add(btnBuscar);

		JPanel topContainer = new JPanel(new BorderLayout());
		topContainer.setOpaque(false);
		topContainer.add(header, BorderLayout.NORTH);
		topContainer.add(filtros, BorderLayout.SOUTH);

		add(topContainer, BorderLayout.NORTH);

		JPanel centerWrapper = new JPanel(new BorderLayout());
		centerWrapper.setBackground(COLOR_BACKGROUND);
		centerWrapper.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

		String[] columnas = { "Médico", "Especialidad", "Fecha", "Hora Inicio", "idDisponibilidad" };

		modeloTabla = new DefaultTableModel(columnas, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		tablaTurnosDisponibles = new JTable(modeloTabla);
		tablaTurnosDisponibles.setRowHeight(26);
		tablaTurnosDisponibles.setFont(BODY_SMALL);
		tablaTurnosDisponibles.getTableHeader().setFont(BODY);
		tablaTurnosDisponibles.getTableHeader().setReorderingAllowed(false);
		tablaTurnosDisponibles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		ocultarColumna(4); // Ocultar ID

		JScrollPane scroll = new JScrollPane(tablaTurnosDisponibles);
		scroll.getViewport().setBackground(Color.WHITE);

		centerWrapper.add(scroll, BorderLayout.CENTER);

		// Botones Footer
		JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		panelAcciones.setOpaque(false);

		RoundedButton btnReservar = new RoundedButton("Reservar turno seleccionado");
		estiloBotonAccion(btnReservar, COLOR_ACCENT);
		btnReservar.addActionListener(e -> reservarTurnoSeleccionado());

		panelAcciones.add(btnReservar);
		centerWrapper.add(panelAcciones, BorderLayout.SOUTH);

		add(centerWrapper, BorderLayout.CENTER);
	}

	private void initDefaultValues() {
		Date hoy = new Date();
		dcDesde.setDate(hoy);
		dcHasta.setDate(new Date(hoy.getTime() + (1000L * 60 * 60 * 24 * 7)));
	}

	private void cargarEspecialidades() {
		try {
			String[] especialidades = controllerPaciente.obtenerEspecialidades();
			cmbEspecialidad.removeAllItems();

			if (especialidades.length == 0) {
				cmbEspecialidad.addItem("No hay especialidades");
				return;
			}
			for (String esp : especialidades) {
				cmbEspecialidad.addItem(esp);
			}
		} catch (Exception e) {
			cmbEspecialidad.addItem("Error");
		}
	}

	private void onBuscarTurnos(ActionEvent e) {
		modeloTabla.setRowCount(0);
		resultadosBusqueda.clear();

		try {
			String especialidad = (String) cmbEspecialidad.getSelectedItem();
			Date dDesde = dcDesde.getDate();
			Date dHasta = dcHasta.getDate();

			if (especialidad == null || especialidad.trim().isEmpty() || especialidad.equals("No hay especialidades")) {
				JOptionPane.showMessageDialog(this, "Seleccione una especialidad válida.");
				return;
			}
			if (dDesde == null || dHasta == null) {
				JOptionPane.showMessageDialog(this, "Debe seleccionar ambas fechas (Desde y Hasta).");
				return;
			}

			LocalDate desde = dDesde.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate hasta = dHasta.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			if (hasta.isBefore(desde)) {
				JOptionPane.showMessageDialog(this, "La fecha 'Hasta' debe ser mayor o igual a 'Desde'.");
				return;
			}

			List<TurnoDisponible> items = controllerPaciente.buscarTurnos(especialidad, desde, hasta);

			this.resultadosBusqueda = items;

			for (TurnoDisponible it : items) {
				String fechaStr = it.fecha().format(F_DDMMYYYY);
				String horaInicioStr = it.horaInicio().toString();

				modeloTabla.addRow(new Object[] { it.medicoNombre(), it.medicoEspecialidad(), fechaStr, horaInicioStr,
						it.idDisponibilidad() });
			}

		} catch (IllegalStateException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Sin Resultados", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error al buscar turnos: " + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private int filaSeleccionada() {
		int row = tablaTurnosDisponibles.getSelectedRow();
		if (row == -1) {
			JOptionPane.showMessageDialog(this, "Seleccione un turno de la lista para reservar.");
		}
		return row;
	}

	private void reservarTurnoSeleccionado() {
		int row = filaSeleccionada();
		if (row == -1)
			return;

		long idDisponibilidad = (long) modeloTabla.getValueAt(row, 4);

		int conf = JOptionPane.showConfirmDialog(this, "¿Confirma la reserva de este turno?", "Confirmar Reserva",
				JOptionPane.YES_NO_OPTION);
		if (conf != JOptionPane.YES_OPTION)
			return;

		try {
			long idTurno = controllerPaciente.solicitarTurno(idDisponibilidad);
			JOptionPane.showMessageDialog(this, "¡Turno reservado con éxito!");

			// Eliminar fila visualmente
			modeloTabla.removeRow(row);

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error al reservar: " + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void estiloBotonAccion(RoundedButton btn, Color bg) {
		btn.setBackground(bg);
		btn.setForeground(Color.WHITE);
		btn.setFont(BUTTON);
		btn.setFocusPainted(false);
		btn.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private void ocultarColumna(int index) {
		TableColumnModel tcm = tablaTurnosDisponibles.getColumnModel();
		if (index >= 0 && index < tcm.getColumnCount()) {
			TableColumn col = tcm.getColumn(index);
			col.setMinWidth(0);
			col.setMaxWidth(0);
			col.setPreferredWidth(0);
		}
	}
}