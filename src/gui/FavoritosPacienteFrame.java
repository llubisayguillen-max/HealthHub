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

		// favoritos
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
				lblVacio.setIconTextGap(15);

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
			JLabel lblError = new JLabel("Aún no tienes médicos favoritos.");
			lblError.setFont(BODY_BOLD);
			lblError.setForeground(COLOR_TEXT_MUTED);
			panelResultados.setPreferredSize(new Dimension(0, 100));
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

		// Botón Eliminar

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
			btnDelete.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
		}

		btnDelete.addActionListener(e -> eliminarFavorito(m));

		RoundedButton btnAgendar = new RoundedButton("Agendar Turno");
		estiloBoton(btnAgendar, COLOR_ACCENT);
		btnAgendar.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 12));
		btnAgendar.setPreferredSize(new Dimension(135, 30));

		// Acción directa: Abrir la pantalla de reservar
		btnAgendar.addActionListener(e -> {
			// Cerramos favoritos y vamos a Nuevo Turno
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
		int conf = JOptionPane.showConfirmDialog(this, "¿Eliminar al Dr/a. " + m.getApellido() + " de favoritos?",
				"Confirmar", JOptionPane.YES_NO_OPTION);

		if (conf == JOptionPane.YES_OPTION) {
			try {
				controller.eliminarFavorito(m.getUsuario());
				cargarFavoritos();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
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

}
