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

	private static final long serialVersionUID = 1L;
	private final Paciente paciente;
	private final ControllerPaciente controller;

	private JTable tablaProximos;
	private DefaultTableModel modeloProximos;
	private JTable tablaHistorial;
	private DefaultTableModel modeloHistorial;
	private List<ControllerPaciente.ItemHistorial> listaHistorialMemoria;

	public GestionTurnosPacienteFrame(Paciente paciente) {
		this.paciente = paciente;
		this.controller = new ControllerPaciente(paciente);

		setTitle("HealthHub - Mis turnos");
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

		// Header
		JPanel topBar = new JPanel(new BorderLayout());
		topBar.setBackground(COLOR_PRIMARY);
		topBar.setPreferredSize(new Dimension(getWidth(), 80));
		topBar.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

		JLabel lblTitulo = new JLabel("Gestión de turnos");
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
		tabbedPane.addTab("Próximos turnos", crearPanelProximos());

		// Pestaña 2: Historial
		tabbedPane.addTab("Historial médico", crearPanelHistorial());

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
			public boolean isCellEditable(int row, int col) {
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

		RoundedButton btnCancelar = new RoundedButton("Cancelar turno");
		estiloBotonAccion(btnCancelar, COLOR_DANGER);
		btnCancelar.addActionListener(e -> cancelarTurnoSeleccionado());

		RoundedButton btnNuevo = new RoundedButton("Solicitar nuevo turno");
		estiloBotonAccion(btnNuevo, COLOR_ACCENT);
		btnNuevo.addActionListener(e -> abrirSolicitudTurno());

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
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		tablaHistorial = new JTable(modeloHistorial);
		estilizarTabla(tablaHistorial);

		JScrollPane scroll = new JScrollPane(tablaHistorial);
		scroll.setBorder(BorderFactory.createLineBorder(COLOR_CARD_BORDER));

		// Botones
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnPanel.setBackground(Color.WHITE);

		RoundedButton btnVerDetalle = new RoundedButton("Ver Resultados/Receta");
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
		} catch (Exception e) {
			mostrarError("Error de carga", "No se encontraron turnos próximos.");
		}

		try {
			this.listaHistorialMemoria = controller.obtenerHistorialMedico();
			for (ControllerPaciente.ItemHistorial h : listaHistorialMemoria) {
				modeloHistorial.addRow(new Object[] { h.fecha().format(fmtFechaCorta), h.nombreMedico(),
						h.especialidad(), h.motivo() });
			}
		} catch (Exception e) {
			/* Fail silent */ }
	}

	private void cancelarTurnoSeleccionado() {
		int row = tablaProximos.getSelectedRow();
		if (row == -1) {
			mostrarInfo("Atención", "Seleccioná un turno de la lista para cancelar.");
			return;
		}

		long idTurno = (long) modeloProximos.getValueAt(row, 0);

		boolean ok = mostrarDialogoConfirmacion("Confirmar Cancelación",
				"¿Estás seguro que deseas cancelar este turno?<br>El horario quedará liberado.");

		if (ok) {
			try {
				controller.cancelarTurno(idTurno);
				mostrarInfo("Éxito", "Turno cancelado correctamente.");
				cargarDatos(); // Refrescar tablas
			} catch (Exception e) {
				mostrarError("Error", "No se pudo cancelar el turno: " + e.getMessage());
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
			mostrarInfo("Atención", "Seleccioná un registro del historial.");
			return;
		}

		ControllerPaciente.ItemHistorial item = listaHistorialMemoria.get(row);

		JDialog dialog = new JDialog(this, true);
		dialog.setUndecorated(true);
		dialog.setSize(550, 720);
		dialog.setLocationRelativeTo(this);
		dialog.setBackground(new Color(0, 0, 0, 0));

		RoundedCardPanel mainPanel = new RoundedCardPanel(24);
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setBorderColor(new Color(230, 235, 240));
		mainPanel.setLayout(new BorderLayout());

		// Header
		JPanel detailHeader = new JPanel(new BorderLayout());
		detailHeader.setOpaque(false);
		detailHeader.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

		JLabel lblDetalleTit = new JLabel("Detalle de Consulta");
		lblDetalleTit.setFont(H2_SECTION);
		lblDetalleTit.setForeground(COLOR_PRIMARY);

		JLabel lblFecha = new JLabel(item.fecha().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		lblFecha.setFont(BODY);
		lblFecha.setForeground(COLOR_TEXT_MUTED);

		JPanel titleGroup = new JPanel(new GridLayout(2, 1));
		titleGroup.setOpaque(false);
		titleGroup.add(lblDetalleTit);
		titleGroup.add(lblFecha);

		JButton btnX = new JButton("\u2715");
		btnX.setFont(new Font("SansSerif", Font.PLAIN, 20));
		btnX.setForeground(new Color(150, 150, 150));
		btnX.setBorder(null);
		btnX.setBorderPainted(false);
		btnX.setContentAreaFilled(false);
		btnX.setFocusPainted(false);
		btnX.setOpaque(false);
		btnX.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnX.addActionListener(e -> dialog.dispose());

		detailHeader.add(titleGroup, BorderLayout.WEST);
		detailHeader.add(btnX, BorderLayout.EAST);
		mainPanel.add(detailHeader, BorderLayout.NORTH);

		// Contenido
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBackground(Color.WHITE);
		content.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));

		// Informe médico
		JLabel lblInf = new JLabel("INFORME MÉDICO");
		lblInf.setFont(BODY_BOLD);
		lblInf.setForeground(COLOR_PRIMARY);
		lblInf.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.add(lblInf);
		content.add(Box.createVerticalStrut(12));

		RoundedCardPanel panelInforme = new RoundedCardPanel(16);
		panelInforme.setBackground(new Color(249, 251, 253));
		panelInforme.setBorderColor(new Color(230, 235, 240));
		panelInforme.setLayout(new BorderLayout());
		panelInforme.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		panelInforme.setAlignmentX(Component.LEFT_ALIGNMENT);

		String infoHtml = "<html><body style='font-family: Segoe UI; font-size: 13pt; color: #444444;'>"
				+ "<div style='margin-bottom: 10px;'><b style='color:#222;'>MOTIVO:</b><br>" + item.motivo() + "</div>"
				+ "<div style='margin-bottom: 10px;'><b style='color:#222;'>DIAGNÓSTICO:</b><br>" + item.diagnostico()
				+ "</div>" + "<div><b style='color:#222;'>TRATAMIENTO:</b><br>" + item.tratamiento() + "</div>"
				+ "</body></html>";

		JLabel lblCuerpo = new JLabel(infoHtml);
		lblCuerpo.setVerticalAlignment(SwingConstants.TOP);
		JScrollPane scroll = new JScrollPane(lblCuerpo);
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		scroll.setBorder(null);

		panelInforme.add(scroll, BorderLayout.CENTER);

		panelInforme.setPreferredSize(new Dimension(480, 350));
		panelInforme.setMaximumSize(new Dimension(480, 400));
		content.add(panelInforme);

		content.add(Box.createVerticalStrut(25));

		// Archivos simulación
		JLabel lblAdj = new JLabel("DOCUMENTOS ADJUNTOS");
		lblAdj.setFont(BODY_BOLD);
		lblAdj.setForeground(COLOR_PRIMARY);
		lblAdj.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.add(lblAdj);
		content.add(Box.createVerticalStrut(12));

		JPanel fileContainer = new JPanel();
		fileContainer.setLayout(new BoxLayout(fileContainer, BoxLayout.Y_AXIS));
		fileContainer.setOpaque(false);
		fileContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

		fileContainer.add(crearItemArchivo("Receta_" + item.fecha().toString() + ".pdf", "150 KB"));
		fileContainer.add(Box.createVerticalStrut(8));
		fileContainer.add(crearItemArchivo("Indicaciones.pdf", "45 KB"));

		content.add(fileContainer);
		mainPanel.add(content, BorderLayout.CENTER);

		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 20));
		footer.setOpaque(false);
		RoundedButton btnCerrar = new RoundedButton("Cerrar");
		btnCerrar.setBackground(COLOR_ACCENT);
		btnCerrar.setForeground(Color.WHITE);
		btnCerrar.setPreferredSize(new Dimension(100, 38));
		btnCerrar.addActionListener(e -> dialog.dispose());
		footer.add(btnCerrar);
		mainPanel.add(footer, BorderLayout.SOUTH);

		dialog.setContentPane(mainPanel);
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
		table.setRowHeight(35);
		table.setFont(BODY);
		table.setSelectionBackground(new Color(235, 245, 255));
		table.setSelectionForeground(Color.BLACK);
		table.setShowVerticalLines(false);
		table.setGridColor(new Color(245, 245, 245));

		JTableHeader header = table.getTableHeader();
		header.setFont(BODY_BOLD);
		header.setBackground(new Color(250, 250, 250));
		header.setPreferredSize(new Dimension(0, 40));
		((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
	}

	private JTextArea crearTextAreaEstilizado(String texto) {
		JTextArea txt = new JTextArea(texto);
		txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		txt.setForeground(new Color(70, 70, 70));
		txt.setEditable(false);
		txt.setFocusable(false);
		txt.setLineWrap(true);
		txt.setWrapStyleWord(true);
		txt.setOpaque(false);
		return txt;
	}

	private JPanel crearItemArchivo(String nombre, String peso) {
		RoundedCardPanel panel = new RoundedCardPanel(8);
		panel.setBackground(Color.WHITE);
		panel.setBorderColor(new Color(230, 230, 230));
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
		panel.setMaximumSize(new Dimension(2000, 50));
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel lblNombre = new JLabel(nombre + " (" + peso + ")");
		lblNombre.setFont(BODY);

		JButton btnDescargar = new JButton();
		btnDescargar.setContentAreaFilled(false);
		btnDescargar.setBorderPainted(false);
		btnDescargar.setCursor(new Cursor(Cursor.HAND_CURSOR));
		try {
			ImageIcon rawIcon = new ImageIcon(getClass().getResource("/gui/img/descarga.png"));
			Image scaled = rawIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
			btnDescargar.setIcon(new ImageIcon(scaled));
		} catch (Exception ex) {
			btnDescargar.setText("⬇");
		}

		btnDescargar.addActionListener(e -> {
			mostrarInfo("Descarga", "Archivo '" + nombre + "' guardado en Descargas.");
		});

		panel.add(lblNombre, BorderLayout.WEST);
		panel.add(btnDescargar, BorderLayout.EAST);
		return panel;
	}

	// ---------- SISTEMA DE DIÁLOGOS PERSONALIZADOS ----------

	private boolean mostrarDialogoConfirmacion(String titulo, String mensaje) {
		final boolean[] resultado = { false };
		JDialog dlg = new JDialog(this, titulo, true);
		dlg.setUndecorated(true);
		dlg.setSize(420, 180);
		dlg.setLocationRelativeTo(this);
		dlg.setBackground(new Color(0, 0, 0, 0));

		RoundedCardPanel mainPanel = new RoundedCardPanel(20);
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setBorderColor(new Color(220, 220, 220));
		mainPanel.setLayout(new BorderLayout());

		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);
		header.setBorder(BorderFactory.createEmptyBorder(12, 15, 5, 12));
		JLabel lblT = new JLabel(titulo);
		lblT.setFont(BODY_BOLD);
		header.add(lblT, BorderLayout.WEST);

		mainPanel.add(header, BorderLayout.NORTH);

		JPanel body = new JPanel(new BorderLayout());
		body.setOpaque(false);
		body.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
		JLabel lblMsg = new JLabel("<html><body style='width: 300px;'>" + mensaje + "</body></html>");
		lblMsg.setFont(BODY);
		body.add(lblMsg, BorderLayout.CENTER);
		mainPanel.add(body, BorderLayout.CENTER);

		JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
		panelBtns.setOpaque(false);

		RoundedButton btnNo = new RoundedButton("No");
		btnNo.setBackground(COLOR_DANGER);
		btnNo.setForeground(Color.WHITE);
		btnNo.setFocusPainted(false);
		btnNo.setPreferredSize(new Dimension(80, 35));
		btnNo.addActionListener(e -> {
			resultado[0] = false;
			dlg.dispose();
		});

		RoundedButton btnSi = new RoundedButton("Sí");
		btnSi.setBackground(COLOR_ACCENT);
		btnSi.setForeground(Color.WHITE);
		btnSi.setFocusPainted(false);
		btnSi.setPreferredSize(new Dimension(80, 35));
		btnSi.addActionListener(e -> {
			resultado[0] = true;
			dlg.dispose();
		});

		panelBtns.add(btnNo);
		panelBtns.add(btnSi);
		mainPanel.add(panelBtns, BorderLayout.SOUTH);

		dlg.setContentPane(mainPanel);
		dlg.setVisible(true);
		return resultado[0];
	}

	private void mostrarInfo(String titulo, String mensaje) {
		mostrarDialogoMensaje(titulo, mensaje, COLOR_ACCENT);
	}

	private void mostrarError(String titulo, String mensaje) {
		mostrarDialogoMensaje(titulo, mensaje, COLOR_DANGER);
	}

	private void mostrarDialogoMensaje(String titulo, String mensaje, Color colorBoton) {
		JDialog dlg = new JDialog(this, titulo, true);
		dlg.setUndecorated(true);
		// Agrandamos un poco el ancho base para textos largos
		dlg.setSize(450, 180);
		dlg.setLocationRelativeTo(this);
		dlg.setBackground(new Color(0, 0, 0, 0));

		RoundedCardPanel mainPanel = new RoundedCardPanel(20);
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setBorderColor(new Color(220, 220, 220));
		mainPanel.setLayout(new BorderLayout());

		JPanel body = new JPanel(new BorderLayout());
		body.setOpaque(false);
		body.setBorder(BorderFactory.createEmptyBorder(35, 30, 10, 30));

		// Quitamos el ancho fijo (width) del HTML para que use todo el espacio
		JLabel lblMsg = new JLabel(
				"<html><body style='text-align: center; font-family: Segoe UI;'>" + mensaje + "</body></html>");
		lblMsg.setFont(BODY);
		lblMsg.setHorizontalAlignment(SwingConstants.CENTER);
		body.add(lblMsg, BorderLayout.CENTER);
		mainPanel.add(body, BorderLayout.CENTER);

		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
		footer.setOpaque(false);
		RoundedButton btnOk = new RoundedButton("Aceptar");
		btnOk.setBackground(colorBoton);
		btnOk.setForeground(Color.WHITE);
		btnOk.setFocusPainted(false);
		btnOk.setPreferredSize(new Dimension(100, 35));
		btnOk.addActionListener(e -> dlg.dispose());
		footer.add(btnOk);
		mainPanel.add(footer, BorderLayout.SOUTH);

		dlg.setContentPane(mainPanel);
		dlg.setVisible(true);
	}
}