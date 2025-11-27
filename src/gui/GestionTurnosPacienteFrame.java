package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import bll.Paciente;
import bll.Turno;
import dll.ControllerPaciente;
import dll.Conexion;

import static gui.UiPaleta.*;
import static gui.UiFonts.*;

public class GestionTurnosPacienteFrame extends JFrame {

	private final Paciente paciente;
	private final ControllerPaciente controller;

	private JTable tablaProximos;
	private DefaultTableModel modeloProximos;
	private JTable tablaHistorial;
	private DefaultTableModel modeloHistorial;
	private List<ControllerPaciente.ItemHistorial> listaHistorialMemoria;

	public GestionTurnosPacienteFrame(Paciente paciente) {
		this.paciente = paciente;
		this.controller = new ControllerPaciente(paciente); // Inicializa el controlador

		setTitle("HealthHub - Mis Turnos");
		setSize(1000, 650);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);

		initUI();
		cargarDatos(); // Cargamos los datos reales
	}

	private void initUI() {
		getContentPane().setBackground(COLOR_BACKGROUND);
		setLayout(new BorderLayout());

		// ---------- HEADER ----------
		JPanel topBar = new JPanel(new BorderLayout());
		topBar.setBackground(COLOR_PRIMARY);
		topBar.setPreferredSize(new Dimension(getWidth(), 80));
		topBar.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

		JLabel lblTitulo = new JLabel("Gestión de Turnos");
		lblTitulo.setForeground(Color.WHITE);
		lblTitulo.setFont(H2_SECTION);

		// Botón Volver al Menú Principal
		RoundedButton btnVolver = new RoundedButton("Volver al Menú");
		btnVolver.setBackground(COLOR_PRIMARY);
		btnVolver.setForeground(Color.WHITE);
		btnVolver.setFont(BUTTON);
		btnVolver.setFocusPainted(false);

		btnVolver.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(255, 255, 255, 200), 1, true),
				BorderFactory.createEmptyBorder(6, 16, 6, 16)));

		btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));

		btnVolver.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				btnVolver.setBackground(new Color(40, 140, 190));
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				btnVolver.setBackground(COLOR_PRIMARY);
			}
		});

		btnVolver.addActionListener(e -> {
			new MenuPacienteFrame(paciente).setVisible(true);
			dispose();
		});

		topBar.add(lblTitulo, BorderLayout.WEST);
		topBar.add(btnVolver, BorderLayout.EAST);
		add(topBar, BorderLayout.NORTH);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setFont(BODY_BOLD);
		tabbedPane.setBackground(Color.WHITE);
		tabbedPane.setFocusable(false);

		// Pestaña 1: Próximos
		tabbedPane.addTab("Próximos Turnos", crearPanelProximos());

		// Pestaña 2: Historial
		tabbedPane.addTab("Historial Médico", crearPanelHistorial());

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setOpaque(false);
		wrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		wrapper.add(tabbedPane, BorderLayout.CENTER);

		add(wrapper, BorderLayout.CENTER);
	}

	private JPanel crearPanelProximos() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.WHITE);
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		// Tabla
		String[] columnas = { "ID", "Fecha", "Hora", "Médico", "Especialidad", "Estado" };
		modeloProximos = new DefaultTableModel(columnas, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		tablaProximos = new JTable(modeloProximos);
		estilizarTabla(tablaProximos);

		ocultarColumna(tablaProximos, 0);

		JScrollPane scroll = new JScrollPane(tablaProximos);
		scroll.setBorder(BorderFactory.createLineBorder(COLOR_CARD_BORDER));

		// Botones de acción
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnPanel.setBackground(Color.WHITE);

		RoundedButton btnConfirmar = new RoundedButton("Confirmar Asistencia");

		estiloBotonAccion(btnConfirmar, new Color(40, 167, 69));
		btnConfirmar.addActionListener(e -> confirmarTurnoSeleccionado());

		RoundedButton btnCancelar = new RoundedButton("Cancelar Turno");
		estiloBotonAccion(btnCancelar, COLOR_DANGER);
		btnCancelar.addActionListener(e -> cancelarTurnoSeleccionado());

		RoundedButton btnNuevo = new RoundedButton("Solicitar Nuevo Turno");
		estiloBotonAccion(btnNuevo, COLOR_ACCENT);
		btnNuevo.addActionListener(e -> abrirSolicitudTurno());

		// Agregamos los botones al panel
		btnPanel.add(btnConfirmar);
		btnPanel.add(Box.createHorizontalStrut(10));
		btnPanel.add(btnCancelar);
		btnPanel.add(Box.createHorizontalStrut(10));
		btnPanel.add(btnNuevo);

		panel.add(scroll, BorderLayout.CENTER);
		panel.add(btnPanel, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel crearPanelHistorial() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.WHITE);
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		// Tabla
		String[] columnas = { "Fecha", "Médico", "Especialidad", "Motivo" };
		modeloHistorial = new DefaultTableModel(columnas, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		tablaHistorial = new JTable(modeloHistorial);
		estilizarTabla(tablaHistorial);

		ocultarColumna(tablaHistorial, 0);

		JScrollPane scroll = new JScrollPane(tablaHistorial);
		scroll.setBorder(BorderFactory.createLineBorder(COLOR_CARD_BORDER));

		// Botones
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnPanel.setBackground(Color.WHITE);

		RoundedButton btnVerDetalle = new RoundedButton("Ver Resultados/Receta");
		btnVerDetalle.setBackground(COLOR_PRIMARY);
		btnVerDetalle.setForeground(Color.WHITE);
		estiloBotonAccion(btnVerDetalle, COLOR_PRIMARY);
		btnVerDetalle.addActionListener(e -> verDetalleHistorial());

		btnPanel.add(btnVerDetalle);

		panel.add(scroll, BorderLayout.CENTER);
		panel.add(btnPanel, BorderLayout.SOUTH);

		return panel;
	}

	private void cargarDatos() {

		modeloProximos.setRowCount(0);
		modeloHistorial.setRowCount(0);

		DateTimeFormatter fmtFechaLarga = DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM", Locale.of("es", "ES"));
		DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("HH:mm");

		DateTimeFormatter fmtFechaCorta = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		// cargar proximos turnos
		try {
			List<Turno> turnosActivos = controller.turnosActivos();

			for (Turno t : turnosActivos) {
				// Filtramos solo los activos
				if ("Reservado".equalsIgnoreCase(t.getEstado()) || "Confirmado".equalsIgnoreCase(t.getEstado())) {

					LocalDateTime ldt = t.getFechaHora().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

					String fechaStr = ldt.format(fmtFechaLarga);
					fechaStr = fechaStr.substring(0, 1).toUpperCase() + fechaStr.substring(1);

					String horaStr = ldt.format(fmtHora);

					modeloProximos.addRow(new Object[] { t.getIdTurno(), fechaStr, horaStr,
							t.getMedico().getNombreCompleto(), t.getMedico().getEspecialidad(), t.getEstado() });
				}
			}
		} catch (IllegalStateException e) {

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error al cargar próximos turnos: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}

		try {

			this.listaHistorialMemoria = controller.obtenerHistorialMedico();

			for (ControllerPaciente.ItemHistorial h : listaHistorialMemoria) {
				modeloHistorial.addRow(new Object[] { h.fecha().format(fmtFechaCorta), h.nombreMedico(),
						h.especialidad(), h.motivo() });
			}
		} catch (Exception e) {
			// System.out.println("Error cargando historial: " + e.getMessage());
		}
	}

	private void cancelarTurnoSeleccionado() {
		int row = tablaProximos.getSelectedRow();
		if (row == -1) {
			JOptionPane.showMessageDialog(this, "Seleccioná un turno de la lista para cancelar.");
			return;
		}

		long idTurno = (long) modeloProximos.getValueAt(row, 0);

		int confirm = JOptionPane.showConfirmDialog(this,
				"¿Estás seguro que deseas cancelar este turno?\nEl horario quedará liberado para otro paciente.",
				"Confirmar Cancelación", JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {
			try {

				controller.cancelarTurno(idTurno);
				JOptionPane.showMessageDialog(this, "Turno cancelado correctamente y horario liberado.");
				cargarDatos(); // Refrescar tablas
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error al cancelar: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void confirmarTurnoSeleccionado() {
		int row = tablaProximos.getSelectedRow();
		if (row == -1) {
			JOptionPane.showMessageDialog(this, "Seleccioná un turno de la lista para confirmar.");
			return;
		}

		String estadoActual = (String) modeloProximos.getValueAt(row, 5);

		if ("Confirmado".equalsIgnoreCase(estadoActual)) {
			JOptionPane.showMessageDialog(this, "Este turno ya se encuentra confirmado.", "Información",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		if (!"Reservado".equalsIgnoreCase(estadoActual)) {
			JOptionPane.showMessageDialog(this, "Solo se pueden confirmar turnos que estén en estado 'Reservado'.",
					"Aviso", JOptionPane.WARNING_MESSAGE);
			return;
		}

		long idTurno = (long) modeloProximos.getValueAt(row, 0);

		int confirm = JOptionPane.showConfirmDialog(this, "¿Confirmar asistencia al turno seleccionado?",
				"Confirmar Turno", JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {
			try {
				controller.confirmarAsistencia(idTurno);
				JOptionPane.showMessageDialog(this, "¡Asistencia confirmada exitosamente!");
				cargarDatos();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error al confirmar: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void abrirSolicitudTurno() {

		new NuevoTurnoPacienteFrame(paciente).setVisible(true);
		dispose();
	}

	private void verDetalleHistorial() {
		int row = tablaHistorial.getSelectedRow();
		if (row == -1) {
			JOptionPane.showMessageDialog(this, "Seleccioná un registro.");
			return;
		}

		ControllerPaciente.ItemHistorial item = listaHistorialMemoria.get(row);

		JDialog dialog = new JDialog(this, "Detalle de Consulta - "
				+ item.fecha().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), true);
		dialog.setSize(520, 650);
		dialog.setLocationRelativeTo(this);
		dialog.setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

		// informe
		JLabel lblTitulo = new JLabel("Informe Médico:");
		lblTitulo.setFont(H3_LABEL);
		lblTitulo.setForeground(COLOR_PRIMARY);
		lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(lblTitulo);
		mainPanel.add(Box.createVerticalStrut(10));

		RoundedCardPanel panelInforme = new RoundedCardPanel(12);
		panelInforme.setBackground(new Color(248, 250, 252));
		panelInforme.setBorderColor(new Color(200, 200, 200));
		panelInforme.setLayout(new BorderLayout());
		panelInforme.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		StringBuilder sb = new StringBuilder();
		sb.append("MOTIVO DE CONSULTA:\n");
		sb.append(item.motivo()).append("\n\n");

		sb.append("DIAGNÓSTICO:\n");
		sb.append(item.diagnostico()).append("\n\n");

		sb.append("TRATAMIENTO E INDICACIONES:\n");
		sb.append(item.tratamiento());

		if (item.seguimiento() != null && !item.seguimiento().isBlank()) {
			sb.append("\n\nOBSERVACIONES / SEGUIMIENTO:\n");
			sb.append(item.seguimiento());
		}

		JTextArea txtInforme = crearTextAreaEstilizado(sb.toString());

		txtInforme.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		JScrollPane scrollInforme = new JScrollPane(txtInforme);
		scrollInforme.setBorder(BorderFactory.createEmptyBorder());
		scrollInforme.getViewport().setOpaque(false);
		scrollInforme.setOpaque(false);

		panelInforme.add(scrollInforme, BorderLayout.CENTER);

		panelInforme.setPreferredSize(new Dimension(450, 250));
		panelInforme.setMaximumSize(new Dimension(2000, 300));
		panelInforme.setAlignmentX(Component.LEFT_ALIGNMENT);

		mainPanel.add(panelInforme);
		mainPanel.add(Box.createVerticalStrut(25));

		// archivos simulación
		JLabel lblArchivos = new JLabel("Documentos Adjuntos");
		lblArchivos.setFont(H3_LABEL);
		lblArchivos.setForeground(COLOR_PRIMARY);
		lblArchivos.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(lblArchivos);
		mainPanel.add(Box.createVerticalStrut(10));

		JPanel panelArchivos = new JPanel();
		panelArchivos.setLayout(new BoxLayout(panelArchivos, BoxLayout.Y_AXIS));
		panelArchivos.setBackground(Color.WHITE);
		panelArchivos.setAlignmentX(Component.LEFT_ALIGNMENT);

		// Archivos simulados con fecha coherente
		panelArchivos.add(crearItemArchivo("Receta_" + item.fecha().toString() + ".pdf", "150 KB"));
		panelArchivos.add(Box.createVerticalStrut(8));
		panelArchivos.add(crearItemArchivo("Indicaciones.pdf", "45 KB"));

		mainPanel.add(panelArchivos);

		JScrollPane mainScroll = new JScrollPane(mainPanel);
		mainScroll.setBorder(null);
		dialog.add(mainScroll, BorderLayout.CENTER);

		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		footer.setBackground(Color.WHITE);
		RoundedButton btnCerrar = new RoundedButton("Cerrar");
		estiloBotonAccion(btnCerrar, COLOR_ACCENT);
		btnCerrar.addActionListener(e -> dialog.dispose());
		footer.add(btnCerrar);
		dialog.add(footer, BorderLayout.SOUTH);

		dialog.setVisible(true);
	}

	private void estiloBotonAccion(RoundedButton btn, Color bg) {
		btn.setBackground(bg);
		btn.setForeground(Color.WHITE);
		btn.setFont(BUTTON);
		btn.setFocusPainted(false);
		btn.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private void ocultarColumna(JTable table, int index) {
		TableColumnModel tcm = table.getColumnModel();
		if (index >= 0 && index < tcm.getColumnCount()) {
			TableColumn col = tcm.getColumn(index);
			col.setMinWidth(0);
			col.setMaxWidth(0);
			col.setPreferredWidth(0);
		}
	}

	private void estilizarTabla(JTable table) {
		table.setRowHeight(30);
		table.setFont(BODY);
		table.setSelectionBackground(new Color(230, 245, 255));
		table.setSelectionForeground(Color.BLACK);
		table.setShowVerticalLines(false);
		table.setGridColor(new Color(240, 240, 240));

		JTableHeader header = table.getTableHeader();
		header.setFont(H3_LABEL);
		header.setBackground(new Color(245, 245, 245));
		header.setForeground(COLOR_TEXT_MUTED);
		header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_CARD_BORDER));
		((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
	}

	private JTextArea crearTextAreaEstilizado(String texto) {
		JTextArea txt = new JTextArea(texto);

		// Configuración visual
		txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		txt.setForeground(new Color(60, 60, 60));
		txt.setEditable(false);

		txt.setFocusable(false);

		txt.setLineWrap(true);
		txt.setWrapStyleWord(true);
		txt.setOpaque(false);
		txt.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		return txt;
	}

	private JPanel crearItemArchivo(String nombre, String peso) {
		RoundedCardPanel panel = new RoundedCardPanel(8);
		panel.setBackground(Color.WHITE);
		panel.setBorderColor(new Color(220, 220, 230));
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
		panel.setMaximumSize(new Dimension(2000, 50));
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel lblNombre = new JLabel(nombre + "  (" + peso + ")");
		lblNombre.setFont(BODY);
		lblNombre.setForeground(new Color(60, 60, 60));

		JButton btnDescargar = new JButton();
		btnDescargar.setToolTipText("Descargar archivo");
		btnDescargar.setContentAreaFilled(false);
		btnDescargar.setBorderPainted(false);
		btnDescargar.setFocusPainted(false);
		btnDescargar.setCursor(new Cursor(Cursor.HAND_CURSOR));

		try {

			ImageIcon rawIcon = new ImageIcon(getClass().getResource("/gui/img/descarga.png"));

			Image scaled = rawIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
			btnDescargar.setIcon(new ImageIcon(scaled));
		} catch (Exception ex) {

			btnDescargar.setText("⬇");
			btnDescargar.setForeground(COLOR_PRIMARY);
			btnDescargar.setFont(new Font("Segoe UI", Font.BOLD, 18));
		}

		btnDescargar.addActionListener(e -> {
			try {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				Thread.sleep(600);
				setCursor(Cursor.getDefaultCursor());
				JOptionPane.showMessageDialog(this, "Archivo '" + nombre + "' guardado en Descargas.",
						"Descarga Completa", JOptionPane.INFORMATION_MESSAGE);
			} catch (InterruptedException ex) {
			}
		});

		JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		left.setOpaque(false);
		left.add(Box.createHorizontalStrut(10));
		left.add(lblNombre);

		panel.add(left, BorderLayout.WEST);
		panel.add(btnDescargar, BorderLayout.EAST);

		return panel;
	}
}