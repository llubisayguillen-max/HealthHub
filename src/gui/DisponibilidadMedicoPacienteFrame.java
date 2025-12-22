package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import bll.Medico;
import bll.Paciente;
import dll.ControllerPaciente;

import static gui.UiPaleta.*;
import static gui.UiFonts.*;

public class DisponibilidadMedicoPacienteFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private final Paciente paciente;
	private final ControllerPaciente controller;

	private JComboBox<String> cmbEspecialidad;
	private JPanel panelResultados;

	public DisponibilidadMedicoPacienteFrame(Paciente paciente) {
		this.paciente = paciente;
		this.controller = new ControllerPaciente(paciente);

		setTitle("HealthHub - Buscar Médicos");
		setSize(1000, 700);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);

		initUI();
		cargarEspecialidades();
	}

	private void initUI() {
		getContentPane().setBackground(COLOR_BACKGROUND);
		setLayout(new BorderLayout());

		// Header
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(COLOR_PRIMARY);
		header.setPreferredSize(new Dimension(getWidth(), 80));
		header.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));

		JLabel lblTitulo = new JLabel("Directorio de Médicos");
		lblTitulo.setForeground(Color.WHITE);
		lblTitulo.setFont(H1_APP);

		JLabel lblSub = new JLabel("Encontrá especialistas y guardalos en favoritos");
		lblSub.setForeground(new Color(220, 235, 245));
		lblSub.setFont(BODY_SMALL);

		JPanel leftHeader = new JPanel();
		leftHeader.setOpaque(false);
		leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
		leftHeader.add(lblTitulo);
		leftHeader.add(Box.createVerticalStrut(3));
		leftHeader.add(lblSub);

		header.add(leftHeader, BorderLayout.WEST);

		RoundedButton btnVolver = new RoundedButton("Volver al Menú");
		btnVolver.setBackground(COLOR_PRIMARY);
		btnVolver.setForeground(Color.WHITE);
		btnVolver.setFont(BUTTON);
		btnVolver.setFocusPainted(false);
		btnVolver.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(255, 255, 255, 200), 1, true),
				BorderFactory.createEmptyBorder(6, 16, 6, 16)));
		btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnVolver.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent evt) {
				btnVolver.setBackground(new Color(40, 140, 190));
			}

			public void mouseExited(MouseEvent evt) {
				btnVolver.setBackground(COLOR_PRIMARY);
			}
		});
		btnVolver.addActionListener(e -> {
			new MenuPacienteFrame(paciente).setVisible(true);
			dispose();
		});

		JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		rightHeader.setOpaque(false);
		rightHeader.add(btnVolver);
		header.add(rightHeader, BorderLayout.EAST);

		add(header, BorderLayout.NORTH);

		// Filtros
		JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
		panelFiltros.setBackground(Color.WHITE);
		panelFiltros.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_CARD_BORDER));

		JLabel lblEsp = new JLabel("Filtrar por Especialidad:");
		lblEsp.setFont(BODY_BOLD);

		cmbEspecialidad = new JComboBox<>();
		cmbEspecialidad.setPreferredSize(new Dimension(250, 30));
		cmbEspecialidad.setFont(BODY);

		RoundedButton btnBuscar = new RoundedButton("Buscar");
		estiloBoton(btnBuscar, COLOR_ACCENT);
		btnBuscar.addActionListener(e -> buscarMedicos());

		// boton: Mostrar todos
		RoundedButton btnVerTodos = new RoundedButton("Ver Todos");
		btnVerTodos.setBackground(Color.WHITE);
		btnVerTodos.setForeground(new Color(60, 60, 60));
		btnVerTodos.setFont(BUTTON);
		btnVerTodos.setFocusPainted(false);
		btnVerTodos.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true));
		btnVerTodos.setCursor(new Cursor(Cursor.HAND_CURSOR));

		btnVerTodos.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnVerTodos.setBackground(new Color(245, 245, 245));
				btnVerTodos.setBorder(BorderFactory.createLineBorder(COLOR_PRIMARY, 1, true));
			}

			public void mouseExited(MouseEvent e) {
				btnVerTodos.setBackground(Color.WHITE);
				btnVerTodos.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true));
			}
		});

		btnVerTodos.addActionListener(e -> buscarTodos());

		panelFiltros.add(lblEsp);
		panelFiltros.add(cmbEspecialidad);
		panelFiltros.add(btnBuscar);
		panelFiltros.add(btnVerTodos);

		panelResultados = new JPanel();
		panelResultados.setBackground(COLOR_BACKGROUND);
		panelResultados.setLayout(new GridLayout(0, 3, 20, 20));
		panelResultados.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JScrollPane scroll = new JScrollPane(panelResultados);
		scroll.setBorder(null);
		scroll.getVerticalScrollBar().setUnitIncrement(16);

		JPanel centerContainer = new JPanel(new BorderLayout());
		centerContainer.add(panelFiltros, BorderLayout.NORTH);
		centerContainer.add(scroll, BorderLayout.CENTER);

		add(centerContainer, BorderLayout.CENTER);
	}

	private void cargarEspecialidades() {
		try {
			String[] esps = controller.obtenerEspecialidades();
			cmbEspecialidad.removeAllItems();
			for (String s : esps)
				cmbEspecialidad.addItem(s);
		} catch (Exception e) {
			// Fail silent
		}
	}

	private void buscarTodos() {
		try {
			List<Medico> medicos = controller.obtenerTodosMedicos();
			mostrarResultados(medicos);
		} catch (Exception e) {
			mostrarError("Error de Sistema", e.getMessage());
		}
	}

	private void buscarMedicos() {
		String esp = (String) cmbEspecialidad.getSelectedItem();
		if (esp == null)
			return;

		try {
			List<Medico> medicos = controller.buscarMedicosPorEspecialidad(esp);
			mostrarResultados(medicos);
		} catch (Exception e) {
			mostrarError("Gestión de Médicos", "No se pudieron buscar: " + e.getMessage());
		}
	}

	private void mostrarResultados(List<Medico> medicos) {
		panelResultados.removeAll();

		if (medicos.isEmpty()) {
			JLabel lblVacio = new JLabel("No se encontraron médicos.", SwingConstants.CENTER);
			lblVacio.setFont(H2_SECTION);
			lblVacio.setForeground(COLOR_TEXT_MUTED);
			JPanel vacioPanel = new JPanel(new BorderLayout());
			vacioPanel.setOpaque(false);
			vacioPanel.add(lblVacio);
			panelResultados.setLayout(new BorderLayout());
			panelResultados.add(vacioPanel);
		} else {
			panelResultados.setLayout(new GridLayout(0, 3, 20, 20));
			for (Medico m : medicos) {
				JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
				wrapper.setOpaque(false);
				JPanel card = crearTarjetaMedico(m);
				wrapper.add(card);
				panelResultados.add(wrapper);
			}
		}
		panelResultados.revalidate();
		panelResultados.repaint();
	}

	private JPanel crearTarjetaMedico(Medico m) {
		RoundedCardPanel card = new RoundedCardPanel(16);
		card.setBackground(COLOR_CARD_BG);
		card.setBorderColor(COLOR_CARD_BORDER);
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		Dimension cardSize = new Dimension(260, 210);
		card.setPreferredSize(cardSize);
		card.setMaximumSize(cardSize);
		card.setMinimumSize(cardSize);

		// Icono Principal
		JLabel lblIcon = new JLabel();
		lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
		try {
			ImageIcon rawIcon = new ImageIcon(getClass().getResource("/gui/img/gestionMedicos.png"));
			Image scaled = rawIcon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
			lblIcon.setIcon(new ImageIcon(scaled));
		} catch (Exception ex) {
			lblIcon.setText("👨‍⚕️");
			lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
		}

		// Datos del Médico
		JLabel lblApellido = new JLabel("Dr/a. " + m.getApellido());
		lblApellido.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 16));
		lblApellido.setForeground(MINT_DARK);
		lblApellido.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel lblNombre = new JLabel(m.getNombre());
		lblNombre.setFont(BODY);
		lblNombre.setForeground(new Color(80, 80, 80));
		lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel lblEsp = new JLabel(m.getEspecialidad());
		lblEsp.setFont(BODY_SMALL);
		lblEsp.setForeground(COLOR_TEXT_MUTED);
		lblEsp.setAlignmentX(Component.CENTER_ALIGNMENT);

		// Botones
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
		btnPanel.setOpaque(false);

		JButton btnFav = new JButton();
		btnFav.setToolTipText("Favoritos");
		btnFav.setContentAreaFilled(false);
		btnFav.setBorderPainted(false);
		btnFav.setFocusPainted(false);
		btnFav.setCursor(new Cursor(Cursor.HAND_CURSOR));
		try {
			ImageIcon starRaw = new ImageIcon(getClass().getResource("/gui/img/estrella.png"));
			Image starScaled = starRaw.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
			btnFav.setIcon(new ImageIcon(starScaled));
		} catch (Exception ex) {
			btnFav.setText("⭐");
		}
		btnFav.addActionListener(e -> agregarFavorito(m));

		RoundedButton btnDispo = new RoundedButton("Ver Disponibilidad");
		estiloBoton(btnDispo, COLOR_ACCENT);
		btnDispo.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 12));
		btnDispo.setPreferredSize(new Dimension(135, 30));
		btnDispo.addActionListener(e -> mostrarPopupDisponibilidad(m));

		btnPanel.add(btnFav);
		btnPanel.add(btnDispo);

		card.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				card.setBackground(new Color(248, 252, 248));
				card.setBorderColor(COLOR_ACCENT);
				card.repaint();
			}

			public void mouseExited(MouseEvent e) {
				card.setBackground(COLOR_CARD_BG);
				card.setBorderColor(COLOR_CARD_BORDER);
				card.repaint();
			}
		});

		card.add(Box.createVerticalStrut(5));
		card.add(lblIcon);
		card.add(Box.createVerticalStrut(8));
		card.add(lblApellido);
		card.add(Box.createVerticalStrut(2));
		card.add(lblNombre);
		card.add(Box.createVerticalStrut(2));
		card.add(lblEsp);
		card.add(Box.createVerticalGlue());
		card.add(btnPanel);
		card.add(Box.createVerticalStrut(8));

		return card;
	}

	private void agregarFavorito(Medico m) {
		try {
			controller.agregarAFavoritos(m.getUsuario());
			mostrarInfo("Favoritos", "Dr/a. " + m.getApellido() + " agregado correctamente.");
		} catch (Exception e) {
			mostrarError("Error", e.getMessage());
		}
	}

	private void estiloBoton(RoundedButton btn, Color bg) {
		btn.setBackground(bg);
		btn.setForeground(Color.WHITE);
		btn.setFont(BUTTON);
		btn.setFocusPainted(false);
		btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private void mostrarPopupDisponibilidad(Medico m) {
		JDialog dialog = new JDialog(this, "Disponibilidad", true);
		dialog.setSize(340, 450);
		dialog.setLocationRelativeTo(this);
		dialog.setLayout(new BorderLayout());

		JPanel content = new JPanel(new BorderLayout());
		content.setBackground(Color.WHITE);
		dialog.setContentPane(content);

		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(COLOR_PRIMARY);
		header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

		JLabel lblTitulo = new JLabel("Horarios Disponibles");
		lblTitulo.setFont(BODY);
		lblTitulo.setForeground(new Color(180, 220, 255));

		JLabel lblSub = new JLabel("Dr/a. " + m.getApellido());
		lblSub.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 22));
		lblSub.setForeground(Color.WHITE);

		JPanel headerText = new JPanel();
		headerText.setOpaque(false);
		headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
		headerText.add(lblTitulo);
		headerText.add(Box.createVerticalStrut(4));
		headerText.add(lblSub);

		header.add(headerText, BorderLayout.CENTER);
		content.add(header, BorderLayout.NORTH);

		List<String> horarios;
		try {
			horarios = controller.obtenerProximaDisponibilidad(m.getUsuario());
		} catch (Exception e) {
			mostrarError("Disponibilidad", e.getMessage());
			return;
		}

		DefaultListModel<String> listModel = new DefaultListModel<>();
		if (horarios.isEmpty()) {
			listModel.addElement("Sin horarios próximos");
		} else {
			for (String h : horarios)
				listModel.addElement(h);
		}

		JList<String> lista = new JList<>(listModel);
		lista.setCellRenderer(new HorarioRenderer());
		lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane scroll = new JScrollPane(lista);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getViewport().setBackground(Color.WHITE);
		content.add(scroll, BorderLayout.CENTER);

		JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
		footer.setBackground(Color.WHITE);
		footer.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

		RoundedButton btnAgendar = new RoundedButton("Ir a agendar Turno");
		estiloBoton(btnAgendar, COLOR_ACCENT);
		btnAgendar.setPreferredSize(new Dimension(200, 40));
		btnAgendar.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 14));
		btnAgendar.addActionListener(e -> {
			dialog.dispose();
			dispose();
			new NuevoTurnoPacienteFrame(paciente).setVisible(true);
		});

		footer.add(btnAgendar);
		content.add(footer, BorderLayout.SOUTH);

		dialog.setVisible(true);
	}

	// Mensajes personalizados

	private void mostrarInfo(String titulo, String mensaje) {
		mostrarDialogoMensaje(titulo, mensaje, COLOR_ACCENT);
	}

	private void mostrarError(String titulo, String mensaje) {
		mostrarDialogoMensaje(titulo, mensaje, COLOR_DANGER);
	}

	private void mostrarDialogoMensaje(String titulo, String mensaje, Color colorBoton) {
		JDialog dlg = new JDialog(this, titulo, true);
		dlg.setUndecorated(true);
		dlg.setSize(400, 180);
		dlg.setLocationRelativeTo(this);

		RoundedCardPanel mainPanel = new RoundedCardPanel(20);
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setBorderColor(new Color(220, 220, 220));
		mainPanel.setLayout(new BorderLayout());

		// Header con Título e Icono
		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);
		header.setBorder(BorderFactory.createEmptyBorder(12, 15, 5, 12));

		JPanel titleContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		titleContainer.setOpaque(false);

		JLabel lblIconApp = new JLabel();
		try {
			ImageIcon raw = new ImageIcon(getClass().getResource("/gui/img/gestionMedicos.png")); // O el icono de tu
																									// app
			Image scaled = raw.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
			lblIconApp.setIcon(new ImageIcon(scaled));
		} catch (Exception e) {
			/* Sin icono */ }

		JLabel lblT = new JLabel(titulo);
		lblT.setFont(BODY_BOLD);
		titleContainer.add(lblIconApp);
		titleContainer.add(lblT);
		header.add(titleContainer, BorderLayout.WEST);

		JButton btnX = new JButton("✕");
		btnX.setBorderPainted(false);
		btnX.setContentAreaFilled(false);
		btnX.setFocusPainted(false);
		btnX.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnX.addActionListener(e -> dlg.dispose());
		header.add(btnX, BorderLayout.EAST);

		mainPanel.add(header, BorderLayout.NORTH);

		// Cuerpo
		JPanel body = new JPanel(new BorderLayout());
		body.setOpaque(false);
		body.setBorder(BorderFactory.createEmptyBorder(15, 25, 5, 25));
		JLabel lblMsg = new JLabel("<html><body style='width: 300px; text-align: left;'>" + mensaje + "</body></html>");
		lblMsg.setFont(BODY);
		body.add(lblMsg, BorderLayout.CENTER);
		mainPanel.add(body, BorderLayout.CENTER);

		// Footer
		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
		footer.setOpaque(false);
		RoundedButton btnOk = new RoundedButton("Aceptar");
		btnOk.setBackground(colorBoton);
		btnOk.setForeground(Color.WHITE);
		btnOk.setFont(BUTTON);
		btnOk.setPreferredSize(new Dimension(100, 35));
		btnOk.addActionListener(e -> dlg.dispose());
		footer.add(btnOk);
		mainPanel.add(footer, BorderLayout.SOUTH);

		dlg.setContentPane(mainPanel);
		dlg.setBackground(new Color(0, 0, 0, 0));
		dlg.setVisible(true);
	}

	private class HorarioRenderer extends DefaultListCellRenderer {
		private final Font font = new Font("Segoe UI", Font.PLAIN, 14);
		private final Color selectionBg = new Color(230, 245, 255);
		private final Color textCol = new Color(60, 60, 60);
		private ImageIcon calendarIcon;

		public HorarioRenderer() {
			try {
				ImageIcon raw = new ImageIcon(getClass().getResource("/gui/img/calendario.png"));
				Image scaled = raw.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
				calendarIcon = new ImageIcon(scaled);
			} catch (Exception e) {
				calendarIcon = null;
			}
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			label.setFont(font);
			label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
			if (calendarIcon != null && !value.toString().startsWith("Sin")) {
				label.setIcon(calendarIcon);
				label.setIconTextGap(12);
			} else {
				label.setIcon(null);
			}
			label.setText(value.toString());
			if (isSelected) {
				label.setBackground(selectionBg);
				label.setForeground(COLOR_PRIMARY);
			} else {
				label.setBackground(Color.WHITE);
				label.setForeground(textCol);
			}
			label.setOpaque(true);
			return label;
		}
	}
}