package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.toedter.calendar.JDateChooser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import bll.Medico;
import dll.ControllerMedico;

import static gui.UiPaleta.*;
import static gui.UiFonts.*;

public class DispoMedicoFrame extends JFrame {

	private final Medico medico;
	private final ControllerMedico controllerMedico;

	private JDateChooser dcDesde;
	private JDateChooser dcHasta;
	private JTable tabla;
	private DefaultTableModel modeloTabla;

	private static final DateTimeFormatter F_DDMMYYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public DispoMedicoFrame(Medico medico) {
		this.medico = medico;
		this.controllerMedico = new ControllerMedico(medico);

		setTitle("HealthHub - Gestión de agenda");
		setSize(960, 560);
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

		JLabel lblTitulo = new JLabel("Gestión de agenda");
		lblTitulo.setForeground(Color.WHITE);
		lblTitulo.setFont(H1_APP);

		JLabel lblSub = new JLabel("Consulta y gestiona la disponibilidad de tu agenda");
		lblSub.setForeground(new Color(220, 235, 245));
		lblSub.setFont(BODY_SMALL);

		JPanel leftHeader = new JPanel();
		leftHeader.setOpaque(false);
		leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
		leftHeader.add(lblTitulo);
		leftHeader.add(Box.createVerticalStrut(3));
		leftHeader.add(lblSub);
		header.add(leftHeader, BorderLayout.WEST);

		RoundedButton btnVolver = new RoundedButton("Volver al menú");
		btnVolver.setBackground(Color.WHITE);
		btnVolver.setForeground(COLOR_PRIMARY);
		btnVolver.setFont(BUTTON);
		btnVolver.setFocusPainted(false);
		btnVolver.setBorder(BorderFactory.createLineBorder(COLOR_PRIMARY, 1, true));
		btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnVolver.addActionListener(e -> {
			new MenuMedicoFrame(medico).setVisible(true);
			dispose();
		});

		JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		rightHeader.setOpaque(false);
		rightHeader.add(btnVolver);
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

		RoundedButton btnBuscar = new RoundedButton("Buscar");
		btnBuscar.setBackground(COLOR_ACCENT);
		btnBuscar.setForeground(Color.WHITE);
		btnBuscar.setFont(BUTTON);
		btnBuscar.setFocusPainted(false);
		btnBuscar.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));
		btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnBuscar.addActionListener(this::onBuscarDisponibilidades);
		filtros.add(btnBuscar);

		JPanel top = new JPanel(new BorderLayout());
		top.setOpaque(false);
		top.add(header, BorderLayout.NORTH);
		top.add(filtros, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);

		// Tabla mas botones
		JPanel center = new JPanel(new BorderLayout());
		center.setBackground(COLOR_BACKGROUND);
		center.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

		String[] columnas = { "Fecha", "Hora inicio", "Hora fin", "idDisponibilidad" };
		modeloTabla = new DefaultTableModel(columnas, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		tabla = new JTable(modeloTabla);
		tabla.setRowHeight(26);
		tabla.setFont(BODY_SMALL);
		tabla.getTableHeader().setFont(BODY);
		tabla.getTableHeader().setReorderingAllowed(false);
		tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// ocultar id
		ocultarColumna(3);

		JScrollPane scroll = new JScrollPane(tabla);
		scroll.getViewport().setBackground(Color.WHITE);
		center.add(scroll, BorderLayout.CENTER);

		// Botones de acción
		JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		panelAcciones.setOpaque(false);

		RoundedButton btnNueva = new RoundedButton("Registrar disponibilidad");
		estiloBotonAccion(btnNueva, COLOR_ACCENT);

		RoundedButton btnEditar = new RoundedButton("Modificar disponibilidad");
		estiloBotonAccion(btnEditar, COLOR_ACCENT);

		RoundedButton btnEliminar = new RoundedButton("Eliminar disponibilidad");
		estiloBotonAccion(btnEliminar, COLOR_DANGER);

		btnNueva.addActionListener(e -> registrarDisponibilidad());
		btnEditar.addActionListener(e -> modificarDisponibilidad());
		btnEliminar.addActionListener(e -> eliminarDisponibilidad());

		panelAcciones.add(btnNueva);
		panelAcciones.add(btnEditar);
		panelAcciones.add(btnEliminar);

		center.add(panelAcciones, BorderLayout.SOUTH);

		add(center, BorderLayout.CENTER);
	}

	private void initDefaultDates() {
		Date hoy = new Date();
		dcDesde.setDate(hoy);
		dcHasta.setDate(hoy);
	}


	// Helpers UI
	private void estiloBotonAccion(RoundedButton btn, Color bg) {
		btn.setBackground(bg);
		btn.setForeground(Color.WHITE);
		btn.setFont(BUTTON);
		btn.setFocusPainted(false);
		btn.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private void ocultarColumna(int index) {
		TableColumnModel tcm = tabla.getColumnModel();
		if (index >= 0 && index < tcm.getColumnCount()) {
			TableColumn col = tcm.getColumn(index);
			col.setMinWidth(0);
			col.setMaxWidth(0);
			col.setPreferredWidth(0);
		}
	}

	private LocalTime parseHoraHHmm(String s) {
		s = s.trim();
		if (!s.matches("^\\d{2}:\\d{2}$")) {
			throw new IllegalArgumentException("Ingrese la hora en formato hh:mm");
		}
		try {
			return LocalTime.parse(s);
		} catch (Exception e) {
			throw new IllegalArgumentException("Hora inválida, use formato hh:mm");
		}
	}

	private void onBuscarDisponibilidades(ActionEvent e) {
		try {
			Date d1 = dcDesde.getDate();
			Date d2 = dcHasta.getDate();
			if (d1 == null || d2 == null) {
				mostrarInfo("Gestión de agenda", "Seleccione ambas fechas");
				return;
			}

			LocalDate desde = d1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate hasta = d2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			if (hasta.isBefore(desde)) {
				mostrarInfo("Gestión de agenda", "La fecha 'Hasta' no puede ser anterior a 'Desde'");
				return;
			}

			java.sql.Date sqlDesde = java.sql.Date.valueOf(desde);
			java.sql.Date sqlHasta = java.sql.Date.valueOf(hasta);

			List<ControllerMedico.DisponibilidadItem> items = controllerMedico.obtenerDisponibilidades(sqlDesde,
					sqlHasta);

			modeloTabla.setRowCount(0);

			if (items.isEmpty()) {
				mostrarInfo("Gestión de agenda", "No hay disponibilidades en ese rango");
				return;
			}

			for (ControllerMedico.DisponibilidadItem it : items) {
				LocalDate fecha = it.getFecha();

				String dia = fecha.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));
				String fechaStr = dia + " " + fecha.format(F_DDMMYYYY);

				String hi = it.getHoraInicio().toString();
				String hf = it.getHoraFin().toString();

				modeloTabla.addRow(new Object[] { fechaStr, hi, hf, it.getId() });
			}

		} catch (Exception ex) {
			mostrarError("Error al buscar disponibilidades", ex.getMessage());
		}
	}

	private int filaSeleccionada() {
		int row = tabla.getSelectedRow();
		if (row == -1) {
			mostrarInfo("Gestión de agenda", "Seleccione una disponibilidad de la lista");
		}
		return row;
	}

	private int getIdDisponibilidadSeleccionada() {
		int row = filaSeleccionada();
		if (row == -1)
			return -1;
		Object val = modeloTabla.getValueAt(row, 3); // col 3 = idDisponibilidad
		if (val instanceof Number n)
			return n.intValue();
		return Integer.parseInt(val.toString());
	}

	private void registrarDisponibilidad() {
		JDialog dlg = new JDialog(this, "Registrar disponibilidad", true);
		dlg.setSize(520, 360);
		dlg.setLocationRelativeTo(this);
		dlg.setLayout(new BorderLayout());
		dlg.getContentPane().setBackground(Color.WHITE);

		JPanel content = new JPanel(new GridBagLayout());
		content.setBackground(Color.WHITE);
		content.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 8, 8, 8);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;

		// Fecha desde
		gbc.gridx = 0;
		gbc.gridy = 0;
		content.add(new JLabel("Fecha desde:"), gbc);

		gbc.gridx = 1;
		JDateChooser dcDesdeR = new JDateChooser();
		dcDesdeR.setDateFormatString("dd/MM/yyyy");
		dcDesdeR.setPreferredSize(new Dimension(200, 32));
		if (dcDesde.getDate() != null) {
			dcDesdeR.setDate(dcDesde.getDate());
		}
		content.add(dcDesdeR, gbc);

		// Fecha hasta
		gbc.gridx = 0;
		gbc.gridy = 1;
		content.add(new JLabel("Fecha hasta:"), gbc);

		gbc.gridx = 1;
		JDateChooser dcHastaR = new JDateChooser();
		dcHastaR.setDateFormatString("dd/MM/yyyy");
		dcHastaR.setPreferredSize(new Dimension(200, 32));
		if (dcHasta.getDate() != null) {
			dcHastaR.setDate(dcHasta.getDate());
		}
		content.add(dcHastaR, gbc);

		// Hora inicio
		gbc.gridx = 0;
		gbc.gridy = 2;
		content.add(new JLabel("Hora inicio (hh:mm):"), gbc);

		gbc.gridx = 1;
		JTextField txtHoraInicio = new JTextField();
		txtHoraInicio.setPreferredSize(new Dimension(200, 32));
		content.add(txtHoraInicio, gbc);

		// Hora fin
		gbc.gridx = 0;
		gbc.gridy = 3;
		content.add(new JLabel("Hora fin (hh:mm):"), gbc);

		gbc.gridx = 1;
		JTextField txtHoraFin = new JTextField();
		txtHoraFin.setPreferredSize(new Dimension(200, 32));
		content.add(txtHoraFin, gbc);

		// Días de la semana
		gbc.gridx = 0;
		gbc.gridy = 4;
		content.add(new JLabel("Días:"), gbc);

		gbc.gridx = 1;
		JPanel panelDias = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		panelDias.setOpaque(false);

		JCheckBox chkLun = new JCheckBox("Lun");
		JCheckBox chkMar = new JCheckBox("Mar");
		JCheckBox chkMie = new JCheckBox("Mié");
		JCheckBox chkJue = new JCheckBox("Jue");
		JCheckBox chkVie = new JCheckBox("Vie");
		JCheckBox chkSab = new JCheckBox("Sáb");
		JCheckBox chkDom = new JCheckBox("Dom");

		for (JCheckBox chk : new JCheckBox[] { chkLun, chkMar, chkMie, chkJue, chkVie, chkSab, chkDom }) {
			chk.setBackground(Color.WHITE);
			chk.setFont(BODY_SMALL);
			panelDias.add(chk);
		}

		content.add(panelDias, gbc);

		dlg.add(content, BorderLayout.CENTER);

		// Botones
		JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		panelBtns.setBackground(Color.WHITE);

		RoundedButton btnCancelar = new RoundedButton("Cancelar");
		btnCancelar.setBackground(COLOR_DANGER);
		btnCancelar.setForeground(Color.WHITE);
		btnCancelar.setFont(BUTTON);
		btnCancelar.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
		btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));

		RoundedButton btnGuardar = new RoundedButton("Guardar");
		estiloBotonAccion(btnGuardar, COLOR_ACCENT);

		panelBtns.add(btnCancelar);
		panelBtns.add(btnGuardar);
		dlg.add(panelBtns, BorderLayout.SOUTH);

		btnCancelar.addActionListener(e -> dlg.dispose());

		btnGuardar.addActionListener(e -> {
			try {
				Date dDesde = dcDesdeR.getDate();
				Date dHasta = dcHastaR.getDate();
				if (dDesde == null || dHasta == null) {
					mostrarInfo("Disponibilidad", "Seleccione ambas fechas");
					return;
				}

				LocalDate desde = dDesde.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				LocalDate hasta = dHasta.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				if (hasta.isBefore(desde)) {
					mostrarInfo("Disponibilidad", "La fecha 'hasta' no puede ser anterior a 'desde'");
					return;
				}

				String sHi = txtHoraInicio.getText().trim();
				String sHf = txtHoraFin.getText().trim();
				if (!sHi.matches("^\\d{2}:\\d{2}$") || !sHf.matches("^\\d{2}:\\d{2}$")) {
					mostrarInfo("Disponibilidad", "Ingrese horas en formato hh:mm");
					return;
				}

				LocalTime hi = LocalTime.parse(sHi);
				LocalTime hf = LocalTime.parse(sHf);
				if (!hf.isAfter(hi)) {
					mostrarInfo("Disponibilidad", "La hora fin debe ser mayor que la hora inicio");
					return;
				}

				java.sql.Time sqlHi = java.sql.Time.valueOf(hi);
				java.sql.Time sqlHf = java.sql.Time.valueOf(hf);

				// Días seleccionados
				Set<DayOfWeek> diasSeleccionados = EnumSet.noneOf(DayOfWeek.class);

				if (chkLun.isSelected()) diasSeleccionados.add(DayOfWeek.MONDAY);
				if (chkMar.isSelected()) diasSeleccionados.add(DayOfWeek.TUESDAY);
				if (chkMie.isSelected()) diasSeleccionados.add(DayOfWeek.WEDNESDAY);
				if (chkJue.isSelected()) diasSeleccionados.add(DayOfWeek.THURSDAY);
				if (chkVie.isSelected()) diasSeleccionados.add(DayOfWeek.FRIDAY);
				if (chkSab.isSelected()) diasSeleccionados.add(DayOfWeek.SATURDAY);
				if (chkDom.isSelected()) diasSeleccionados.add(DayOfWeek.SUNDAY);

				if (diasSeleccionados.isEmpty()) {
				    mostrarInfo("Disponibilidad", "Seleccione al menos un día de la semana");
				    return;
				}

				int registrados = 0;
				int solapados = 0;
				LocalDate fechaReg = desde;

				while (!fechaReg.isAfter(hasta)) {
				    DayOfWeek dow = fechaReg.getDayOfWeek();
				    System.out.println("Fecha: " + fechaReg + " / Día: " + dow +
				            " / seleccionado? " + diasSeleccionados.contains(dow));

				    if (diasSeleccionados.contains(dow)) {
				        try {
				            controllerMedico.registrarDisponibilidad(
				                    java.sql.Date.valueOf(fechaReg), sqlHi, sqlHf
				            );
				            registrados++;
				        } catch (IllegalArgumentException exVal) {
				            solapados++;
				        }
				    }
				    fechaReg = fechaReg.plusDays(1);
				}


				if (registrados > 0) {
					String msg = "Disponibilidades registradas: " + registrados;
					if (solapados > 0) {
						msg += " (se omitieron " + solapados + " por solaparse con otras)";
					}
					mostrarInfo("Disponibilidad", msg);
				} else {
					mostrarInfo("Disponibilidad", "No se pudo registrar ninguna disponibilidad.\n"
							+ "Verifique que no se solapen con otras existentes.");
				}

				dcDesde.setDate(java.sql.Date.valueOf(desde));
				dcHasta.setDate(java.sql.Date.valueOf(hasta));

				dlg.dispose();
				onBuscarDisponibilidades(null);

			} catch (Exception ex) {
				mostrarError("Error", ex.getMessage());
			}
		});

		dlg.setVisible(true);
	}

	private void modificarDisponibilidad() {
		int row = tabla.getSelectedRow();
		if (row == -1) {
			mostrarInfo("Disponibilidad", "Seleccione una disponibilidad");
			return;
		}

		int idDisp = (int) modeloTabla.getValueAt(row, 3);

		String fechaCelda = modeloTabla.getValueAt(row, 0).toString();
		String soloFecha;
		int idxEspacio = fechaCelda.indexOf(' ');
		if (idxEspacio > 0) {
			soloFecha = fechaCelda.substring(idxEspacio + 1); // "27/11/2025"
		} else {
			soloFecha = fechaCelda;
		}

		String hiStr = modeloTabla.getValueAt(row, 1).toString();
		String hfStr = modeloTabla.getValueAt(row, 2).toString();

		JDialog dlg = new JDialog(this, "Modificar disponibilidad", true);
		dlg.setSize(420, 280);
		dlg.setLocationRelativeTo(this);
		dlg.setLayout(new BorderLayout());
		dlg.getContentPane().setBackground(Color.WHITE);

		JPanel content = new JPanel(new GridBagLayout());
		content.setBackground(Color.WHITE);
		content.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.insets = new Insets(8, 8, 8, 8);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;

		// Fecha
		gbc.gridx = 0;
		gbc.gridy = 0;
		content.add(new JLabel("Nueva fecha:"), gbc);

		gbc.gridx = 1;
		JDateChooser dcNuevaFecha = new JDateChooser();
		dcNuevaFecha.setDateFormatString("dd/MM/yyyy");
		dcNuevaFecha.setPreferredSize(new Dimension(200, 32));
		try {
			LocalDate ld = LocalDate.parse(soloFecha, F_DDMMYYYY);
			dcNuevaFecha.setDate(java.sql.Date.valueOf(ld));
		} catch (Exception ignored) {
		}
		content.add(dcNuevaFecha, gbc);

		// Hora inicio
		gbc.gridx = 0;
		gbc.gridy = 1;
		content.add(new JLabel("Hora inicio (hh:mm):"), gbc);

		gbc.gridx = 1;
		JTextField txtHi = new JTextField(hiStr);
		txtHi.setPreferredSize(new Dimension(200, 32));
		content.add(txtHi, gbc);

		// Hora fin
		gbc.gridx = 0;
		gbc.gridy = 2;
		content.add(new JLabel("Hora fin (hh:mm):"), gbc);

		gbc.gridx = 1;
		JTextField txtHf = new JTextField(hfStr);
		txtHf.setPreferredSize(new Dimension(200, 32));
		content.add(txtHf, gbc);

		dlg.add(content, BorderLayout.CENTER);

		// Botones
		JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		btns.setBackground(Color.WHITE);

		RoundedButton btnCancelar = new RoundedButton("Cancelar");
		btnCancelar.setBackground(COLOR_DANGER);
		btnCancelar.setForeground(Color.WHITE);

		RoundedButton btnGuardar = new RoundedButton("Guardar cambios");
		estiloBotonAccion(btnGuardar, COLOR_ACCENT);

		btns.add(btnCancelar);
		btns.add(btnGuardar);
		dlg.add(btns, BorderLayout.SOUTH);

		btnCancelar.addActionListener(e -> dlg.dispose());

		btnGuardar.addActionListener(e -> {
			try {
				Date nuevaD = dcNuevaFecha.getDate();
				if (nuevaD == null) {
					mostrarInfo("Modificar", "Seleccione la nueva fecha");
					return;
				}

				LocalTime hi = LocalTime.parse(txtHi.getText().trim());
				LocalTime hf = LocalTime.parse(txtHf.getText().trim());

				if (!hf.isAfter(hi)) {
					mostrarInfo("Modificar", "La hora fin debe ser mayor que la hora inicio");
					return;
				}

				controllerMedico.modificarDisponibilidadConFecha(idDisp, new java.sql.Date(nuevaD.getTime()),
						Time.valueOf(hi), Time.valueOf(hf));

				mostrarInfo("Modificar", "Disponibilidad modificada");
				dlg.dispose();
				onBuscarDisponibilidades(null);

			} catch (Exception ex) {
				mostrarError("Error", ex.getMessage());
			}
		});

		dlg.setVisible(true);
	}

	private void eliminarDisponibilidad() {
		int id = getIdDisponibilidadSeleccionada();
		if (id <= 0)
			return;

		boolean ok = mostrarDialogoConfirmacion("Eliminar disponibilidad", "¿Eliminar la disponibilidad seleccionada?");
		if (!ok)
			return;

		try {
			controllerMedico.eliminarDisponibilidad(id);
			mostrarInfo("Gestión de agenda", "Disponibilidad eliminada");
			onBuscarDisponibilidades(null);
		} catch (Exception ex) {
			mostrarError("Error al eliminar disponibilidad", ex.getMessage());
		}
	}

	//Pop-up
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
		mostrarDialogoMensaje(titulo, mensaje, COLOR_PRIMARY, COLOR_ACCENT);
	}

	private void mostrarError(String titulo, String mensaje) {
		mostrarDialogoMensaje(titulo, mensaje, COLOR_DANGER, COLOR_DANGER);
	}

	private void mostrarDialogoMensaje(String titulo, String mensaje, Color headerBg, Color buttonBg) {
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
