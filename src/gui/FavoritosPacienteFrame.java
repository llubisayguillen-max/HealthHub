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

public class FavoritosPacienteFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private final Paciente paciente;
	private final ControllerPaciente controller;
	private JPanel panelResultados;

	public FavoritosPacienteFrame(Paciente paciente) {
		this.paciente = paciente;
		this.controller = new ControllerPaciente(paciente);

		setTitle("HealthHub - Mis Favoritos");
		setSize(1000, 700);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);

		initUI();
		cargarFavoritos();
	}

	private void initUI() {
		getContentPane().setBackground(COLOR_BACKGROUND);
		setLayout(new BorderLayout());

		// Header
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(COLOR_PRIMARY);
		header.setPreferredSize(new Dimension(getWidth(), 80));
		header.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));

		JLabel lblTitulo = new JLabel("Mis Favoritos");
		lblTitulo.setForeground(Color.WHITE);
		lblTitulo.setFont(H1_APP);

		JLabel lblSub = new JLabel("Tus médicos guardados y recomendaciones");
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

		JPanel mainContainer = new JPanel();
		mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
		mainContainer.setBackground(COLOR_BACKGROUND);
		mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

		// Recomendaciones
		JPanel panelRec = crearPanelRecomendaciones();
		mainContainer.add(panelRec);

		mainContainer.add(Box.createVerticalStrut(20));

		// Favoritos
		JLabel lblFavTitulo = new JLabel("Médicos Guardados:");
		lblFavTitulo.setFont(H2_SECTION);
		lblFavTitulo.setForeground(COLOR_PRIMARY);
		lblFavTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainContainer.add(lblFavTitulo);

		mainContainer.add(Box.createVerticalStrut(15));

		panelResultados = new JPanel();
		panelResultados.setBackground(COLOR_BACKGROUND);
		panelResultados.setLayout(new GridLayout(0, 3, 20, 20));
		panelResultados.setAlignmentX(Component.LEFT_ALIGNMENT);

		mainContainer.add(panelResultados);

		JScrollPane scroll = new JScrollPane(mainContainer);
		scroll.setBorder(null);
		scroll.getVerticalScrollBar().setUnitIncrement(16);

		add(scroll, BorderLayout.CENTER);
	}

	private JPanel crearPanelRecomendaciones() {
		RoundedCardPanel panel = new RoundedCardPanel(16);
		panel.setBackground(Color.WHITE);
		panel.setBorderColor(new Color(130, 200, 160));
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

		panel.setMinimumSize(new Dimension(200, 120));
		panel.setMaximumSize(new Dimension(2000, 140));
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel lblTitulo = new JLabel("Información de tu Cobertura:");
		lblTitulo.setFont(H3_LABEL);
		lblTitulo.setForeground(new Color(30, 100, 60));

		List<String> recs = controller.mostrarRecomendaciones();
		String textoRec = String.join("\n\n", recs);

		JTextArea txt = new JTextArea(textoRec);
		txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		txt.setForeground(new Color(80, 80, 80));
		txt.setEditable(false);
		txt.setOpaque(false);
		txt.setLineWrap(true);
		txt.setWrapStyleWord(true);

		panel.add(lblTitulo, BorderLayout.NORTH);
		panel.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
		panel.add(txt, BorderLayout.SOUTH);

		return panel;
	}

	private void cargarFavoritos() {
		panelResultados.removeAll();
		try {
			List<Medico> favoritos = controller.verFavoritos();

			if (favoritos.isEmpty()) {
				panelResultados.setLayout(new GridBagLayout());
				JLabel lblVacio = new JLabel(
						"<html><center>Aún no tienes médicos favoritos.<br>Los que guardes aparecerán acá.</center></html>");
				lblVacio.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 16));
				lblVacio.setForeground(new Color(160, 160, 160));
				panelResultados.setPreferredSize(new Dimension(0, 200));
				panelResultados.add(lblVacio);
			} else {
				panelResultados.setLayout(new GridLayout(0, 3, 20, 20));
				panelResultados.setPreferredSize(null);
				for (Medico m : favoritos) {
					JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
					wrapper.setOpaque(false);
					wrapper.add(crearTarjetaFavorito(m));
					panelResultados.add(wrapper);
				}
			}
		} catch (Exception e) {
			panelResultados.setLayout(new GridBagLayout());
			JLabel lblError = new JLabel("No tienes médicos en favoritos.");
			lblError.setFont(BODY_BOLD);
			lblError.setForeground(COLOR_TEXT_MUTED);
			panelResultados.add(lblError);
		}

		panelResultados.revalidate();
		panelResultados.repaint();
	}

	private JPanel crearTarjetaFavorito(Medico m) {
		RoundedCardPanel card = new RoundedCardPanel(16);
		card.setBackground(Color.WHITE);
		card.setBorderColor(COLOR_CARD_BORDER);
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		Dimension cardSize = new Dimension(260, 210);
		card.setPreferredSize(cardSize);
		card.setMinimumSize(cardSize);
		card.setMaximumSize(cardSize);

		// Icono
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

		// Datos
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

		// Eliminar
		JButton btnDelete = new JButton();
		btnDelete.setToolTipText("Eliminar de Favoritos");
		btnDelete.setContentAreaFilled(false);
		btnDelete.setBorderPainted(false);
		btnDelete.setFocusPainted(false);
		btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
		try {
			ImageIcon rawIcon = new ImageIcon(getClass().getResource("/gui/img/basura.png"));
			Image scaled = rawIcon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
			btnDelete.setIcon(new ImageIcon(scaled));
		} catch (Exception ex) {
			btnDelete.setText("❌");
			btnDelete.setForeground(COLOR_DANGER);
		}
		btnDelete.addActionListener(e -> eliminarFavorito(m));

		RoundedButton btnAgendar = new RoundedButton("Agendar Turno");
		estiloBoton(btnAgendar, COLOR_ACCENT);
		btnAgendar.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 12));
		btnAgendar.setPreferredSize(new Dimension(135, 30));
		btnAgendar.addActionListener(e -> {
			new NuevoTurnoPacienteFrame(paciente).setVisible(true);
			dispose();
		});

		btnPanel.add(btnDelete);
		btnPanel.add(btnAgendar);

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

	private void eliminarFavorito(Medico m) {

		boolean ok = mostrarDialogoConfirmacion("Confirmar",
				"¿Eliminar al Dr/a. " + m.getApellido() + " de favoritos?");

		if (ok) {
			try {
				controller.eliminarFavorito(m.getUsuario());
				cargarFavoritos();
				mostrarInfo("Favoritos", "Médico eliminado de tus favoritos.");
			} catch (Exception e) {
				mostrarError("Error", e.getMessage());
			}
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

	private boolean mostrarDialogoConfirmacion(String titulo, String mensaje) {
		final boolean[] resultado = { false };
		JDialog dlg = new JDialog(this, titulo, true);
		dlg.setUndecorated(true);
		dlg.setSize(420, 180);
		dlg.setLocationRelativeTo(this);

		RoundedCardPanel mainPanel = new RoundedCardPanel(20);
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setBorderColor(new Color(220, 220, 220));
		mainPanel.setLayout(new BorderLayout());

		// Header
		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);
		header.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 10));
		JLabel lblT = new JLabel(titulo);
		lblT.setFont(BODY_BOLD);
		header.add(lblT, BorderLayout.WEST);

		JButton btnX = new JButton("✕");
		btnX.setBorderPainted(false);
		btnX.setContentAreaFilled(false);
		btnX.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnX.addActionListener(e -> dlg.dispose());
		header.add(btnX, BorderLayout.EAST);
		mainPanel.add(header, BorderLayout.NORTH);

		// Cuerpo
		JPanel body = new JPanel(new BorderLayout());
		body.setOpaque(false);
		body.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
		JLabel lblMsg = new JLabel("<html><body style='width: 300px;'>" + mensaje + "</body></html>");
		lblMsg.setFont(BODY);
		body.add(lblMsg, BorderLayout.CENTER);
		mainPanel.add(body, BorderLayout.CENTER);

		// Botones
		JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
		panelBtns.setOpaque(false);

		RoundedButton btnNo = new RoundedButton("No");
		btnNo.setBackground(COLOR_DANGER);
		btnNo.setForeground(Color.WHITE);
		btnNo.setPreferredSize(new Dimension(80, 35));
		btnNo.setFont(BUTTON);
		btnNo.addActionListener(e -> {
			resultado[0] = false;
			dlg.dispose();
		});

		RoundedButton btnSi = new RoundedButton("Sí");
		btnSi.setBackground(COLOR_ACCENT);
		btnSi.setForeground(Color.WHITE);
		btnSi.setPreferredSize(new Dimension(80, 35));
		btnSi.setFont(BUTTON);
		btnSi.addActionListener(e -> {
			resultado[0] = true;
			dlg.dispose();
		});

		panelBtns.add(btnNo);
		panelBtns.add(btnSi);
		mainPanel.add(panelBtns, BorderLayout.SOUTH);

		dlg.setContentPane(mainPanel);
		dlg.setBackground(new Color(0, 0, 0, 0));
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
		dlg.setSize(400, 170);
		dlg.setLocationRelativeTo(this);

		RoundedCardPanel mainPanel = new RoundedCardPanel(20);
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setBorderColor(new Color(220, 220, 220));
		mainPanel.setLayout(new BorderLayout());

		JPanel body = new JPanel(new BorderLayout());
		body.setOpaque(false);
		body.setBorder(BorderFactory.createEmptyBorder(30, 25, 10, 25));
		JLabel lblMsg = new JLabel(
				"<html><body style='width: 280px; text-align: center;'>" + mensaje + "</body></html>");
		lblMsg.setFont(BODY);
		lblMsg.setHorizontalAlignment(SwingConstants.CENTER);
		body.add(lblMsg, BorderLayout.CENTER);
		mainPanel.add(body, BorderLayout.CENTER);

		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
		footer.setOpaque(false);
		RoundedButton btnOk = new RoundedButton("Aceptar");
		btnOk.setBackground(colorBoton);
		btnOk.setForeground(Color.WHITE);
		btnOk.setFont(BUTTON);
		btnOk.setFocusPainted(false);
		btnOk.setPreferredSize(new Dimension(100, 35));
		btnOk.addActionListener(e -> dlg.dispose());
		footer.add(btnOk);
		mainPanel.add(footer, BorderLayout.SOUTH);

		dlg.setContentPane(mainPanel);
		dlg.setBackground(new Color(0, 0, 0, 0));
		dlg.setVisible(true);
	}
}
