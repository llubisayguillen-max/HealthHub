package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.toedter.calendar.JDateChooser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import bll.Medico;
import bll.Consulta;
import dll.ControllerMedico;

import static gui.UiPaleta.*;
import static gui.UiFonts.*;

public class AgendaMedicoFrame extends JFrame {

	private final Medico medico;
	private final ControllerMedico controllerMedico;

	private JDateChooser dcDesde;
	private JDateChooser dcHasta;
	private JCheckBox chkIncluirCancelados;
	private JTable tablaTurnos;
	private DefaultTableModel modeloTabla;

	private static final DateTimeFormatter F_DDMMYYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public AgendaMedicoFrame(Medico medico) {
		this.medico = medico;
		this.controllerMedico = new ControllerMedico(medico);

		setTitle("HealthHub - Agenda del Médico");
		setSize(960, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);

		initUI();
		initDefaultDates();
	}

	private void initUI() {
		getContentPane().setBackground(COLOR_BACKGROUND);
		setLayout(new BorderLayout());

		// Header
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(COLOR_PRIMARY);
		header.setPreferredSize(new Dimension(getWidth(), 80));
		header.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));

		JLabel lblTitulo = new JLabel("Agenda del médico");
		lblTitulo.setForeground(Color.WHITE);
		lblTitulo.setFont(H1_APP);

		JLabel lblSub = new JLabel("Turnos por rango de fechas");
		lblSub.setForeground(new Color(220, 235, 245));
		lblSub.setFont(BODY_SMALL);

		JPanel leftHeader = new JPanel();
		leftHeader.setOpaque(false);
		leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
		leftHeader.add(lblTitulo);
		leftHeader.add(Box.createVerticalStrut(3));
		leftHeader.add(lblSub);

		header.add(leftHeader, BorderLayout.WEST);

		// Botón Volver al menu
		RoundedButton btnVolverHeader = new RoundedButton("Volver al menú");
		btnVolverHeader.setBackground(Color.WHITE);
		btnVolverHeader.setForeground(COLOR_PRIMARY);
		btnVolverHeader.setFont(BUTTON);
		btnVolverHeader.setFocusPainted(false);
		btnVolverHeader.setBorder(BorderFactory.createLineBorder(COLOR_PRIMARY, 1, true));
		btnVolverHeader.setCursor(new Cursor(Cursor.HAND_CURSOR));

		btnVolverHeader.addActionListener(e -> {
			new MenuMedicoFrame(medico).setVisible(true);
			dispose();
		});

		JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		rightHeader.setOpaque(false);
		rightHeader.add(btnVolverHeader);

		header.add(rightHeader, BorderLayout.EAST);

		// Filtros
		JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
		filtros.setBackground(Color.WHITE);
		filtros.setBorder(BorderFactory.createEmptyBorder(0, 40, 5, 40));

		JLabel lblDesde = new JLabel("Desde:");
		lblDesde.setFont(BODY_SMALL);
		filtros.add(lblDesde);

		dcDesde = new JDateChooser();
		dcDesde.setDateFormatString("dd/MM/yyyy");
		dcDesde.setPreferredSize(new Dimension(130, 26));
		filtros.add(dcDesde);

		JLabel lblHasta = new JLabel("Hasta:");
		lblHasta.setFont(BODY_SMALL);
		filtros.add(lblHasta);

		dcHasta = new JDateChooser();
		dcHasta.setDateFormatString("dd/MM/yyyy");
		dcHasta.setPreferredSize(new Dimension(130, 26));
		filtros.add(dcHasta);

		chkIncluirCancelados = new JCheckBox("Incluir cancelados");
		chkIncluirCancelados.setBackground(Color.WHITE);
		chkIncluirCancelados.setFont(BODY_SMALL);
		filtros.add(chkIncluirCancelados);

		RoundedButton btnBuscar = new RoundedButton("Buscar");
		btnBuscar.setBackground(COLOR_ACCENT);
		btnBuscar.setForeground(Color.WHITE);
		btnBuscar.setFont(BUTTON);
		btnBuscar.setFocusPainted(false);
		btnBuscar.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));
		btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnBuscar.addActionListener(this::onBuscarAgenda);

		filtros.add(btnBuscar);

		JPanel topContainer = new JPanel(new BorderLayout());
		topContainer.setOpaque(false);
		topContainer.add(header, BorderLayout.NORTH);
		topContainer.add(filtros, BorderLayout.SOUTH);

		add(topContainer, BorderLayout.NORTH);

		// Tabla mas botones
		JPanel centerWrapper = new JPanel(new BorderLayout());
		centerWrapper.setBackground(COLOR_BACKGROUND);
		centerWrapper.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

		String[] columnas = { "Fecha", "Hora", "Paciente", "Estado", "idTurno", "idPaciente", "usuarioPaciente" };

		modeloTabla = new DefaultTableModel(columnas, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		tablaTurnos = new JTable(modeloTabla);
		tablaTurnos.setRowHeight(26);
		tablaTurnos.setFont(BODY_SMALL);
		tablaTurnos.getTableHeader().setFont(BODY);
		tablaTurnos.getTableHeader().setReorderingAllowed(false);
		tablaTurnos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Ocultar columnas
		ocultarColumna(4);
		ocultarColumna(5);
		ocultarColumna(6);

		JScrollPane scroll = new JScrollPane(tablaTurnos);
		scroll.getViewport().setBackground(Color.WHITE);

		centerWrapper.add(scroll, BorderLayout.CENTER);

		// Botones de acción
		JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		panelAcciones.setOpaque(false);

		RoundedButton btnConfirmar = new RoundedButton("Confirmar asistencia");
		estiloBotonAccion(btnConfirmar, COLOR_ACCENT);

		RoundedButton btnReprogramar = new RoundedButton("Reprogramar turno");
		estiloBotonAccion(btnReprogramar, COLOR_ACCENT);

		RoundedButton btnConsulta = new RoundedButton("Registrar consulta");
		estiloBotonAccion(btnConsulta, COLOR_ACCENT);

		RoundedButton btnCancelar = new RoundedButton("Cancelar turno");
		estiloBotonAccion(btnCancelar, COLOR_DANGER);

		btnConfirmar.addActionListener(e -> confirmarTurnoSeleccionado());
		btnReprogramar.addActionListener(e -> reprogramarTurnoSeleccionado());
		btnConsulta.addActionListener(e -> registrarConsultaTurnoSeleccionado());
		btnCancelar.addActionListener(e -> cancelarTurnoSeleccionado());

		panelAcciones.add(btnConfirmar);
		panelAcciones.add(btnReprogramar);
		panelAcciones.add(btnConsulta);
		panelAcciones.add(btnCancelar);

		centerWrapper.add(panelAcciones, BorderLayout.SOUTH);

		add(centerWrapper, BorderLayout.CENTER);
	}

	private void initDefaultDates() {
		Date hoy = new Date();
		dcDesde.setDate(hoy);
		dcHasta.setDate(hoy);
	}

	// Helper UI
	private void estiloBotonAccion(RoundedButton btn, Color bg) {
		btn.setBackground(bg);
		btn.setForeground(Color.WHITE);
		btn.setFont(BUTTON);
		btn.setFocusPainted(false);
		btn.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private void ocultarColumna(int index) {
		TableColumnModel tcm = tablaTurnos.getColumnModel();
		if (index >= 0 && index < tcm.getColumnCount()) {
			TableColumn col = tcm.getColumn(index);
			col.setMinWidth(0);
			col.setMaxWidth(0);
			col.setPreferredWidth(0);
		}
	}

	// Acciones
	private void onBuscarAgenda(ActionEvent e) {
		try {
			Date d1 = dcDesde.getDate();
			Date d2 = dcHasta.getDate();
			if (d1 == null || d2 == null) {
				mostrarInfo("Agenda", "Seleccione ambas fechas");
				return;
			}

			LocalDate desde = d1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate hasta = d2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			if (hasta.isBefore(desde)) {
				mostrarInfo("Agenda", "La fecha 'Hasta' no puede ser anterior a 'Desde'");
				return;
			}

			java.sql.Date sqlDesde = java.sql.Date.valueOf(desde);
			java.sql.Date sqlHasta = java.sql.Date.valueOf(hasta);
			boolean incluirCancelados = chkIncluirCancelados.isSelected();

			List<ControllerMedico.AgendaItem> items = controllerMedico.visualizarAgendaDetallada(sqlDesde, sqlHasta,
					incluirCancelados);

			modeloTabla.setRowCount(0);

			if (items.isEmpty()) {
				mostrarInfo("Agenda", "No hay turnos en ese rango");
				return;
			}

			for (ControllerMedico.AgendaItem it : items) {
				String fechaStr = it.fecha().format(F_DDMMYYYY);
				String horaStr = it.hora().toString();
				String pacStr = it.pacienteNombre();
				String estado = it.estado();

				modeloTabla.addRow(new Object[] { fechaStr, horaStr, pacStr, estado, it.idTurno(), it.idPaciente(),
						it.usuarioPaciente() });
			}

		} catch (Exception ex) {
			mostrarError("Error al buscar agenda", ex.getMessage());
		}
	}

	private int filaSeleccionada() {
		int row = tablaTurnos.getSelectedRow();
		if (row == -1) {
			mostrarInfo("Agenda", "Seleccione un turno de la lista");
		}
		return row;
	}

	private long getIdTurnoSeleccionado() {
		int row = filaSeleccionada();
		if (row == -1)
			return -1;
		Object val = modeloTabla.getValueAt(row, 4); // idTurno
		if (val instanceof Number n)
			return n.longValue();
		return Long.parseLong(val.toString());
	}

	private String getUsuarioPacienteSeleccionado() {
		int row = filaSeleccionada();
		if (row == -1)
			return null;
		Object val = modeloTabla.getValueAt(row, 6); // usuarioPaciente
		return val != null ? val.toString() : null;
	}

	// Confirmar asitencia
	private void confirmarTurnoSeleccionado() {
		long idTurno = getIdTurnoSeleccionado();
		if (idTurno <= 0)
			return;

		boolean ok = mostrarDialogoConfirmacion("Confirmar asistencia",
				"¿Confirmar asistencia para el turno seleccionado?");
		if (!ok)
			return;

		try {
			controllerMedico.confirmarAsistencia(idTurno);
			mostrarInfo("Agenda", "Asistencia confirmada");
			onBuscarAgenda(null);
		} catch (Exception ex) {
			mostrarError("Error al confirmar", ex.getMessage());
		}
	}

	// Cancelar turno
	private void cancelarTurnoSeleccionado() {
		long idTurno = getIdTurnoSeleccionado();
		if (idTurno <= 0)
			return;

		boolean ok = mostrarDialogoConfirmacion("Cancelar turno", "¿Cancelar el turno seleccionado?");
		if (!ok)
			return;

		try {
			controllerMedico.cancelarTurno(idTurno);
			mostrarInfo("Agenda", "Turno cancelado");
			onBuscarAgenda(null);
		} catch (Exception ex) {
			mostrarError("Error al cancelar", ex.getMessage());
		}
	}

	// Reprogramar turno
	private void reprogramarTurnoSeleccionado() {
		long idTurno = getIdTurnoSeleccionado();
		if (idTurno <= 0)
			return;

		JDialog dlg = new JDialog(this, "Reprogramar turno", true);
		dlg.setSize(360, 220);
		dlg.setLocationRelativeTo(this);
		dlg.setLayout(new BorderLayout());
		dlg.getContentPane().setBackground(Color.WHITE);

		JPanel content = new JPanel();
		content.setBackground(Color.WHITE);
		content.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
		content.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 6, 6, 6);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;

		JLabel lblFecha = new JLabel("Nueva fecha:");
		lblFecha.setFont(BODY_SMALL);
		content.add(lblFecha, gbc);

		gbc.gridx = 1;
		JDateChooser dcNuevaFecha = new JDateChooser();
		dcNuevaFecha.setDateFormatString("dd/MM/yyyy");
		dcNuevaFecha.setPreferredSize(new Dimension(140, 26));
		content.add(dcNuevaFecha, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		JLabel lblHora = new JLabel("Nueva hora (hh:mm):");
		lblHora.setFont(BODY_SMALL);
		content.add(lblHora, gbc);

		gbc.gridx = 1;
		JTextField txtHora = new JTextField();
		txtHora.setFont(BODY_SMALL);
		content.add(txtHora, gbc);

		dlg.add(content, BorderLayout.CENTER);

		JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		panelBtns.setBackground(Color.WHITE);

		RoundedButton btnAceptar = new RoundedButton("Aceptar");
		estiloBotonAccion(btnAceptar, COLOR_ACCENT);

		RoundedButton btnCancelar = new RoundedButton("Cancelar");
		btnCancelar.setBackground(COLOR_DANGER);
		btnCancelar.setForeground(Color.WHITE);
		btnCancelar.setFont(BUTTON);
		btnCancelar.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
		btnCancelar.setFocusPainted(false);
		btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));

		panelBtns.add(btnCancelar);
		panelBtns.add(btnAceptar);

		dlg.add(panelBtns, BorderLayout.SOUTH);

		btnCancelar.addActionListener(e -> dlg.dispose());

		btnAceptar.addActionListener(e -> {
			try {
				Date dNueva = dcNuevaFecha.getDate();
				if (dNueva == null) {
					mostrarInfo("Reprogramar turno", "Seleccione la nueva fecha");
					return;
				}
				String sHora = txtHora.getText().trim();
				if (!sHora.matches("^\\d{2}:\\d{2}$")) {
					mostrarInfo("Reprogramar turno", "Ingrese la hora en formato hh:mm");
					return;
				}
				LocalTime hora = LocalTime.parse(sHora);
				LocalDate fecha = dNueva.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

				Date nueva = Date.from(fecha.atTime(hora).atZone(ZoneId.systemDefault()).toInstant());

				controllerMedico.reprogramarTurno(idTurno, nueva);
				mostrarInfo("Agenda", "Turno reprogramado");
				dlg.dispose();
				onBuscarAgenda(null);

			} catch (Exception ex) {
				mostrarError("Error al reprogramar", ex.getMessage());
			}
		});

		dlg.setVisible(true);
	}

	// Tab entre campos
	private void habilitarTabEntreCampos(JTextArea origen, JComponent destino) {
		origen.setFocusTraversalKeysEnabled(false);
		origen.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("TAB"), "moverSiguiente");

		origen.getActionMap().put("moverSiguiente", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				destino.requestFocusInWindow();
			}
		});
	}

	// Registrar consulta
	private void registrarConsultaTurnoSeleccionado() {
		long idTurno = getIdTurnoSeleccionado();
		if (idTurno <= 0)
			return;

		String usuarioPac = getUsuarioPacienteSeleccionado();
		if (usuarioPac == null || usuarioPac.isBlank()) {
			mostrarError("Registrar consulta", "No se pudo identificar al paciente del turno");
			return;
		}

		JDialog dlg = new JDialog(this, "Registrar consulta", true);
		dlg.setSize(620, 560);
		dlg.setLocationRelativeTo(this);
		dlg.setLayout(new BorderLayout());
		dlg.getContentPane().setBackground(COLOR_BACKGROUND);

		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(COLOR_PRIMARY);
		header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

		JLabel lblTitulo = new JLabel("Registrar consulta");
		lblTitulo.setForeground(Color.WHITE);
		lblTitulo.setFont(H2_SECTION);
		header.add(lblTitulo, BorderLayout.WEST);

		dlg.add(header, BorderLayout.NORTH);

		// Card central con campos
		JPanel wrapper = new JPanel(new GridBagLayout());
		wrapper.setBackground(COLOR_BACKGROUND);

		JPanel card = new JPanel(null);
		card.setBackground(Color.WHITE);
		card.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true));
		card.setPreferredSize(new Dimension(540, 400));

		int y = 20;

		JLabel lblMotivo = new JLabel("Motivo:");
		lblMotivo.setFont(BODY_SMALL);
		lblMotivo.setBounds(20, y, 200, 18);
		card.add(lblMotivo);
		y += 22;

		JTextArea txtMotivo = new JTextArea();
		txtMotivo.setFont(BODY_SMALL);
		txtMotivo.setLineWrap(true);
		txtMotivo.setWrapStyleWord(true);
		JScrollPane spMotivo = new JScrollPane(txtMotivo);
		spMotivo.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
		spMotivo.setBounds(20, y, 500, 60);
		card.add(spMotivo);
		y += 70;

		JLabel lblDiag = new JLabel("Diagnóstico:");
		lblDiag.setFont(BODY_SMALL);
		lblDiag.setBounds(20, y, 200, 18);
		card.add(lblDiag);
		y += 22;

		JTextArea txtDiag = new JTextArea();
		txtDiag.setFont(BODY_SMALL);
		txtDiag.setLineWrap(true);
		txtDiag.setWrapStyleWord(true);
		JScrollPane spDiag = new JScrollPane(txtDiag);
		spDiag.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
		spDiag.setBounds(20, y, 500, 60);
		card.add(spDiag);
		y += 70;

		JLabel lblTrat = new JLabel("Tratamiento:");
		lblTrat.setFont(BODY_SMALL);
		lblTrat.setBounds(20, y, 200, 18);
		card.add(lblTrat);
		y += 22;

		JTextArea txtTrat = new JTextArea();
		txtTrat.setFont(BODY_SMALL);
		txtTrat.setLineWrap(true);
		txtTrat.setWrapStyleWord(true);
		JScrollPane spTrat = new JScrollPane(txtTrat);
		spTrat.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
		spTrat.setBounds(20, y, 500, 60);
		card.add(spTrat);
		y += 70;

		JLabel lblSeg = new JLabel("Recomendaciones:");
		lblSeg.setFont(BODY_SMALL);
		lblSeg.setBounds(20, y, 200, 18);
		card.add(lblSeg);
		y += 22;

		JTextArea txtSeg = new JTextArea();
		txtSeg.setFont(BODY_SMALL);
		txtSeg.setLineWrap(true);
		txtSeg.setWrapStyleWord(true);
		JScrollPane spSeg = new JScrollPane(txtSeg);
		spSeg.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
		spSeg.setBounds(20, y, 500, 60);
		card.add(spSeg);

		wrapper.add(card);
		dlg.add(wrapper, BorderLayout.CENTER);

		// Footer con botones
		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		footer.setBackground(COLOR_BACKGROUND);
		footer.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));

		RoundedButton btnCancelar = new RoundedButton("Cancelar");
		btnCancelar.setBackground(COLOR_DANGER);
		btnCancelar.setForeground(Color.WHITE);
		btnCancelar.setFont(BUTTON);
		btnCancelar.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
		btnCancelar.setFocusPainted(false);
		btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));

		RoundedButton btnGuardar = new RoundedButton("Guardar consulta");
		estiloBotonAccion(btnGuardar, COLOR_ACCENT);

		footer.add(btnCancelar);
		footer.add(btnGuardar);

		habilitarTabEntreCampos(txtMotivo, txtDiag);
		habilitarTabEntreCampos(txtDiag, txtTrat);
		habilitarTabEntreCampos(txtTrat, txtSeg);
		habilitarTabEntreCampos(txtSeg, btnGuardar);

		dlg.add(footer, BorderLayout.SOUTH);

		btnCancelar.addActionListener(e -> dlg.dispose());

		btnGuardar.addActionListener(e -> {
			String motivo = txtMotivo.getText().trim();
			String diag = txtDiag.getText().trim();
			String trat = txtTrat.getText().trim();
			String seg = txtSeg.getText().trim();

			if (motivo.isEmpty()) {
				mostrarInfo("Registrar consulta", "El motivo es obligatorio");
				return;
			}

			try {
				Consulta c = Consulta.hoy(motivo, diag, trat, seg);
				long idConsulta = controllerMedico.registrarConsulta(usuarioPac, c);
				mostrarInfo("Registrar consulta", "Consulta registrada (Nro " + idConsulta + ")");
				dlg.dispose();
			} catch (Exception ex) {
				mostrarError("Error al registrar consulta", ex.getMessage());
			}
		});

		dlg.setVisible(true);
	}

	// Pop up de confirmación
	private boolean mostrarDialogoConfirmacion(String titulo, String mensaje) {
		final boolean[] resultado = { false };

		JDialog dlg = new JDialog(this, titulo, true);
		dlg.setSize(420, 170);
		dlg.setLocationRelativeTo(this);
		dlg.setLayout(new BorderLayout());
		dlg.getContentPane().setBackground(Color.WHITE);

		JPanel content = new JPanel(new BorderLayout());
		content.setBackground(Color.WHITE);
		content.setBorder(BorderFactory.createEmptyBorder(16, 20, 10, 20));

		JLabel lbl = new JLabel("<html>" + mensaje + "</html>");
		lbl.setFont(BODY);
		content.add(lbl, BorderLayout.CENTER);

		dlg.add(content, BorderLayout.CENTER);

		JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		panelBtns.setBackground(Color.WHITE);

		RoundedButton btnNo = new RoundedButton("No");
		btnNo.setBackground(COLOR_DANGER);
		btnNo.setForeground(Color.WHITE);
		btnNo.setFont(BUTTON);
		btnNo.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
		btnNo.setFocusPainted(false);
		btnNo.setCursor(new Cursor(Cursor.HAND_CURSOR));

		RoundedButton btnSi = new RoundedButton("Sí");
		btnSi.setBackground(COLOR_ACCENT);
		btnSi.setForeground(Color.WHITE);
		btnSi.setFont(BUTTON);
		btnSi.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
		btnSi.setFocusPainted(false);
		btnSi.setCursor(new Cursor(Cursor.HAND_CURSOR));

		btnNo.addActionListener(e -> {
			resultado[0] = false;
			dlg.dispose();
		});

		btnSi.addActionListener(e -> {
			resultado[0] = true;
			dlg.dispose();
		});

		panelBtns.add(btnNo);
		panelBtns.add(btnSi);

		dlg.add(panelBtns, BorderLayout.SOUTH);

		dlg.setVisible(true);
		return resultado[0];
	}

	private void mostrarInfo(String titulo, String mensaje) {
	    mostrarDialogoMensaje(titulo, mensaje, COLOR_ACCENT);
	}

	private void mostrarError(String titulo, String mensaje) {
	    mostrarDialogoMensaje(titulo, mensaje, COLOR_DANGER);
	}

	private void mostrarDialogoMensaje(String titulo, String mensaje, Color buttonBg) {

		JDialog dlg = new JDialog(this, titulo, true);
		dlg.setSize(380, 160);
		dlg.setLocationRelativeTo(this);
		dlg.setLayout(new BorderLayout());
		dlg.getContentPane().setBackground(Color.WHITE);

		JPanel content = new JPanel(new BorderLayout());
		content.setBackground(Color.WHITE);
		content.setBorder(BorderFactory.createEmptyBorder(14, 18, 10, 18));

		JLabel lblMsg = new JLabel("<html>" + mensaje + "</html>");
		lblMsg.setFont(BODY);
		content.add(lblMsg, BorderLayout.CENTER);

		dlg.add(content, BorderLayout.CENTER);

		// Botón Aceptar
		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		footer.setBackground(Color.WHITE);

		RoundedButton btnOk = new RoundedButton("Aceptar");
		btnOk.setBackground(buttonBg);
		btnOk.setForeground(Color.WHITE);
		btnOk.setFont(BUTTON);
		btnOk.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
		btnOk.setFocusPainted(false);
		btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));

		btnOk.addActionListener(e -> dlg.dispose());

		footer.add(btnOk);
		dlg.add(footer, BorderLayout.SOUTH);

		dlg.setVisible(true);
	}

}
