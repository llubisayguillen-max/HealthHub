package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

import dll.ControllerUsuario;
import dll.ControllerAdministrador;
import bll.Usuario;
import bll.Administrador;
import bll.Paciente;
import bll.Medico;

import static gui.UiPaleta.*;
import static gui.UiFonts.*;

public class LoginFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private JTextField txtUsuario;
	private JPasswordField txtPassword;
	private final ControllerUsuario usuarioController = new ControllerUsuario();

	public LoginFrame() {
		setTitle("HealthHub - Inicio de sesión");
		setSize(900, 450);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		setLayout(new BorderLayout());

		initUI();
	}

	private void initUI() {
		// Panel izquierdo
		JPanel leftPanel = new JPanel();
		leftPanel.setBackground(COLOR_PRIMARY);
		leftPanel.setPreferredSize(new Dimension(300, getHeight()));
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

		// Logo
		ImageIcon rawLogo = new ImageIcon(getClass().getResource("/gui/img/logo.png"));
		Image scaledLogo = rawLogo.getImage().getScaledInstance(400, 400, Image.SCALE_SMOOTH);
		JLabel lblLogo = new JLabel(new ImageIcon(scaledLogo));
		lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

		leftPanel.add(Box.createVerticalGlue());
		leftPanel.add(lblLogo);
		leftPanel.add(Box.createVerticalStrut(10));
		leftPanel.add(Box.createVerticalGlue());

		// Panel derecho (login)
		JPanel rightPanel = new JPanel(new GridBagLayout());
		rightPanel.setBackground(COLOR_BACKGROUND);

		LoginCardPanel card = new LoginCardPanel(18);
		card.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 10, 6, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.weightx = 1.0;

		int row = 0;

		// Título
		JLabel lblTitulo = new JLabel("Inicio de Sesión", SwingConstants.CENTER);
		lblTitulo.setFont(H2_SECTION);
		lblTitulo.setForeground(new Color(60, 60, 60));
		gbc.gridy = row++;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		card.add(lblTitulo, gbc);

		// Subtítulo
		JLabel lblSubtitulo = new JLabel("Ingresá con tu usuario y contraseña", SwingConstants.CENTER);
		lblSubtitulo.setFont(BODY);
		lblSubtitulo.setForeground(new Color(130, 130, 130));
		gbc.gridy = row++;
		card.add(lblSubtitulo, gbc);

		// Usuario
		JLabel lblUsuario = new JLabel("Usuario:");
		lblUsuario.setFont(BODY);
		lblUsuario.setForeground(new Color(80, 80, 80));
		gbc.gridy = row++;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		card.add(lblUsuario, gbc);

		txtUsuario = new JTextField();
		txtUsuario.setFont(BODY);
		txtUsuario.setPreferredSize(new Dimension(260, 32));
		txtUsuario.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 200, 200), 1, true),
				new EmptyBorder(0, 10, 0, 10)));

		JPanel userFieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		userFieldPanel.setOpaque(false);
		userFieldPanel.add(txtUsuario);

		gbc.gridy = row++;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		card.add(userFieldPanel, gbc);

		// Contraseña
		JLabel lblPassword = new JLabel("Contraseña:");
		lblPassword.setFont(BODY);
		lblPassword.setForeground(new Color(80, 80, 80));
		gbc.gridy = row++;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		card.add(lblPassword, gbc);

		txtPassword = new JPasswordField();
		txtPassword.setFont(BODY);
		txtPassword.setPreferredSize(new Dimension(260, 32));
		txtPassword.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 200, 200), 1, true),
				new EmptyBorder(0, 10, 0, 10)));

		JPanel passFieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		passFieldPanel.setOpaque(false);
		passFieldPanel.add(txtPassword);

		gbc.gridy = row++;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		card.add(passFieldPanel, gbc);

		// Botón ingresar
		RoundedButton btnLogin = new RoundedButton("Ingresar");
		btnLogin.setBackground(COLOR_ACCENT);
		btnLogin.setForeground(Color.WHITE);
		btnLogin.setFont(BUTTON);
		btnLogin.setFocusPainted(false);
		btnLogin.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
		btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
		btnPanel.setOpaque(false);
		btnPanel.add(btnLogin);

		gbc.gridy = row++;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		card.add(btnPanel, gbc);

		// Acción del botón + Enter
		btnLogin.addActionListener(e -> iniciarSesion());
		getRootPane().setDefaultButton(btnLogin);

		GridBagConstraints gbcRight = new GridBagConstraints();
		gbcRight.gridx = 0;
		gbcRight.gridy = 0;
		gbcRight.anchor = GridBagConstraints.CENTER;
		rightPanel.add(card, gbcRight);

		add(leftPanel, BorderLayout.WEST);
		add(rightPanel, BorderLayout.CENTER);
	}

	// Login
	private void iniciarSesion() {
		String usuario = txtUsuario.getText().trim();
		String pass = new String(txtPassword.getPassword()).trim();

		if (usuario.isEmpty() || pass.isEmpty()) {
			mostrarInfo("Inicio de sesión", "Debés completar todos los campos.");
			return;
		}

		try {
			if (!usuarioController.existeUsuario(usuario)) {
				mostrarError("Inicio de sesión", "El usuario ingresado no existe.");
				return;
			}

			var opt = usuarioController.login(usuario, pass);
			if (opt.isEmpty()) {
				mostrarError("Inicio de sesión", "La contraseña no es correcta.");
				return;
			}

			Usuario u = opt.get();
			mostrarInfo("Bienvenida", "Bienvenido/a <b>" + (u.getNombre() != null ? u.getNombre() : usuario) + "</b>.");

			switch (u.getClass().getSimpleName()) {
			case "Administrador" -> {
				Administrador admin = (Administrador) u;
				ControllerAdministrador adminController = new ControllerAdministrador(admin);
				new MenuAdministradorFrame(adminController, admin).setVisible(true);
			}
			case "Medico" -> {
				Medico med = (Medico) u;
				new MenuMedicoFrame(med).setVisible(true);
			}
			case "Paciente" -> {
				Paciente pac = (Paciente) u;
				new MenuPacienteFrame(pac).setVisible(true);
			}
			default -> {
				mostrarError("Inicio de sesión", "No se reconoce el rol del usuario.");
				return;
			}
			}

			dispose();

		} catch (Exception ex) {
			mostrarError("Error", ex.getMessage());
		}
	}

	// Pop-up

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

		// Contenido
		JPanel content = new JPanel(new BorderLayout());
		content.setBackground(Color.WHITE);
		content.setBorder(BorderFactory.createEmptyBorder(14, 18, 10, 18));

		JLabel lblMsg = new JLabel("<html>" + mensaje + "</html>");
		lblMsg.setFont(BODY);
		content.add(lblMsg, BorderLayout.CENTER);

		dlg.add(content, BorderLayout.CENTER);

		// Boton Aceptar
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
		dlg.getRootPane().setDefaultButton(btnOk);
		dlg.setVisible(true);
	}

	// Card de login
	private static class LoginCardPanel extends JPanel {
		private final int radius;

		public LoginCardPanel(int radius) {
			this.radius = radius;
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			int w = getWidth();
			int h = getHeight();

			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// Fondo blanco
			g2.setColor(Color.WHITE);
			g2.fillRoundRect(0, 0, w - 1, h - 1, radius, radius);

			// Borde gris clarito
			g2.setColor(new Color(220, 220, 220));
			g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);

			g2.dispose();
			super.paintComponent(g);
		}

		@Override
		public Insets getInsets() {
			return new Insets(20, 28, 20, 28);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
	}
}
