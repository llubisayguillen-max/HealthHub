package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import static gui.UiPaleta.*;



import bll.Medico;

public class MenuMedicoFrame extends JFrame {

	private final Medico medico;

	private static final String UI_FONT_FAMILY = "Segoe UI";
	public MenuMedicoFrame(Medico medico) {
		this.medico = medico;

		setTitle("HealthHub - Panel del Médico");
		setSize(960, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);

		initUI();
	}

	private void initUI() {
		getContentPane().setBackground(COLOR_BACKGROUND);
		setLayout(new BorderLayout());

		// Header
		JPanel topBar = new JPanel(new BorderLayout());
		topBar.setBackground(COLOR_PRIMARY);
		topBar.setPreferredSize(new Dimension(getWidth(), 90));
		topBar.setBorder(BorderFactory.createEmptyBorder(10, 40, 15, 40));

		JLabel lblTitulo = new JLabel("Panel del Médico");
		lblTitulo.setForeground(Color.WHITE);
		lblTitulo.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 26));

		JLabel lblNombre = new JLabel("Dra. " + capitalizarNombre(medico.getNombreCompleto()) + "  |  "
				+ capitalizarNombre(medico.getEspecialidad()));
		lblNombre.setForeground(new Color(230, 245, 255));
		lblNombre.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 14));

		JPanel info = new JPanel();
		info.setOpaque(false);
		info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
		info.add(lblTitulo);
		info.add(Box.createVerticalStrut(4));
		info.add(lblNombre);

		topBar.add(info, BorderLayout.WEST);
		add(topBar, BorderLayout.NORTH);

		// Cards
		JPanel wrapperCenter = new JPanel(new BorderLayout());
		wrapperCenter.setOpaque(false);
		wrapperCenter.setBorder(BorderFactory.createEmptyBorder(10, 32, 10, 32));

		JPanel center = new JPanel();
		center.setOpaque(false);
		center.setLayout(new BoxLayout(center, BoxLayout.X_AXIS));

		JPanel cardDisp = crearCard("Gestión de agenda", "/gui/img/DispoMed.png", "Define tus horarios de atención",
				"Próximo día con turnos: —", "Disponibilidad", () -> new DispoMedicoFrame(medico).setVisible(true));

		JPanel cardAgenda = crearCard("Consulta turnos", "/gui/img/AgendaMed.png",
				"Consulta tus turnos en un rango de fechas", "Turnos para hoy: —", "Ver agenda",
				() -> new AgendaMedicoFrame(medico).setVisible(true));

		JPanel cardTurnos = crearCard("Gestión de turnos", "/gui/img/TurnosMed.png",
				"Confirma asistencia, cancela o reprograma", "Turnos pendientes: —", "Turnos",
				() -> new TurnosMedicoFrame(medico).setVisible(true));

		center.add(cardDisp);
		center.add(Box.createHorizontalStrut(22)); // separación entre tarjetas
		center.add(cardAgenda);
		center.add(Box.createHorizontalStrut(22));
		center.add(cardTurnos);

		wrapperCenter.add(center, BorderLayout.CENTER);
		add(wrapperCenter, BorderLayout.CENTER);

		// footer
		JPanel footer = new JPanel(new BorderLayout());
		footer.setBackground(COLOR_BACKGROUND);
		footer.setBorder(BorderFactory.createEmptyBorder(0, 40, 20, 40));

		RoundedButton btnCerrar = new RoundedButton("Cerrar sesión");
		btnCerrar.setBackground(COLOR_DANGER);
		btnCerrar.setForeground(Color.WHITE);
		btnCerrar.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 13));
		btnCerrar.setFocusPainted(false);
		btnCerrar.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
		btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));

		btnCerrar.addActionListener(e -> {
			new LoginFrame().setVisible(true);
			dispose();
		});

		JPanel rightFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		rightFooter.setOpaque(false);
		rightFooter.add(btnCerrar);

		footer.add(rightFooter, BorderLayout.EAST);
		add(footer, BorderLayout.SOUTH);
	}

	private JPanel crearCard(String titulo, String imagePath, String descripcion, String meta, String textoBoton,
			Runnable onClick) {

		RoundedCardPanel card = new RoundedCardPanel(16);
		card.setBackground(COLOR_CARD_BG);
		card.setBorderColor(COLOR_CARD_BORDER);
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

		Dimension cardSize = new Dimension(275, 230);
		card.setPreferredSize(cardSize);
		card.setMaximumSize(cardSize);
		card.setMinimumSize(cardSize);

		// Iconos
		JLabel lblIcon;
		try {
			ImageIcon rawIcon = new ImageIcon(getClass().getResource(imagePath));
			Image scaled = rawIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
			ImageIcon icon = new ImageIcon(scaled);
			lblIcon = new JLabel(icon, SwingConstants.CENTER);
		} catch (Exception ex) {
			lblIcon = new JLabel("?", SwingConstants.CENTER);
		}
		lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

		// Titulo
		JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
		lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblTitulo.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 17));
		lblTitulo.setForeground(MINT_DARK);

		// Descripción
		JLabel lblDesc = new JLabel("<html><center>" + descripcion + "</center></html>", SwingConstants.CENTER);
		lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblDesc.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 12));
		lblDesc.setForeground(COLOR_TEXT_MUTED);
		
		JLabel lblMeta = new JLabel("<html><center>" + meta + "</center></html>", SwingConstants.CENTER);
		lblMeta.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblMeta.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 11));
		lblMeta.setForeground(new Color(130, 130, 130));

		// Boton
		RoundedButton btn = new RoundedButton(textoBoton);
		btn.setAlignmentX(Component.CENTER_ALIGNMENT);
		btn.setBackground(COLOR_ACCENT);
		btn.setForeground(Color.WHITE);
		btn.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 13));
		btn.setFocusPainted(false);
		btn.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

		btn.addActionListener(e -> {
			if (onClick != null)
				onClick.run();
		});

		card.add(Box.createVerticalStrut(2)); 
		card.add(lblIcon);
		card.add(Box.createVerticalStrut(20)); 
		card.add(lblTitulo);
		card.add(Box.createVerticalStrut(4)); 
		card.add(lblDesc);
		card.add(Box.createVerticalStrut(4)); 
		card.add(lblMeta);
		card.add(Box.createVerticalStrut(30)); 
		card.add(btn);
		card.add(Box.createVerticalStrut(2)); 

		card.addMouseListener(new MouseAdapter() {
			final Color normalBg = COLOR_CARD_BG;
			final Color hoverBg = new Color(248, 252, 248);
			final Color normalBorder = COLOR_CARD_BORDER;
			final Color hoverBorder = COLOR_ACCENT;

			@Override
			public void mouseEntered(MouseEvent e) {
				card.setBackground(hoverBg);
				card.setBorderColor(hoverBorder);
				card.repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				card.setBackground(normalBg);
				card.setBorderColor(normalBorder);
				card.repaint();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (onClick != null)
					onClick.run();
			}
		});

		return card;
	}

	private static class RoundedCardPanel extends JPanel {
		private final int cornerRadius;
		private Color borderColor = COLOR_CARD_BORDER;

		public RoundedCardPanel(int cornerRadius) {
			this.cornerRadius = cornerRadius;
			setOpaque(false);
		}

		public void setBorderColor(Color borderColor) {
			this.borderColor = borderColor;
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int w = getWidth();
			int h = getHeight();

			g2.setColor(getBackground());
			g2.fillRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);

			g2.setColor(borderColor);
			g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);

			g2.dispose();
			super.paintComponent(g);
		}
	}

	private String capitalizarNombre(String s) {
		if (s == null || s.isBlank())
			return "";
		s = s.toLowerCase();
		String[] partes = s.split(" ");
		StringBuilder sb = new StringBuilder();
		for (String p : partes) {
			if (p.isBlank())
				continue;
			sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
		}
		return sb.toString().trim();
	}
}
