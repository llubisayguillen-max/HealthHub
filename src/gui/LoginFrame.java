package gui;

import javax.swing.*;
import java.awt.*;
import bll.Usuario;
import bll.Administrador;
import dll.ControllerUsuario;
import dll.ControllerAdministrador;

public class LoginFrame extends JFrame {

    private RoundedTextField txtUsuario;
    private RoundedPasswordField txtPassword;
    private ControllerUsuario usuarioController = new ControllerUsuario();

    public LoginFrame() {
        setTitle("HealthHub - Login");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        // panel izqui
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(0, 102, 204));
        leftPanel.setPreferredSize(new Dimension(300, 400));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JLabel lblLogo = new JLabel(" ");
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogo.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));
        leftPanel.add(lblLogo);

        JLabel lblAppName = new JLabel("HealthHub");
        lblAppName.setForeground(Color.WHITE);
        lblAppName.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblAppName.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftPanel.add(lblAppName);

        mainPanel.add(leftPanel, BorderLayout.WEST);

        // panel derecho
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel("Inicio de Sesión", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));

        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        rightPanel.add(lblTitulo, gbc);

        // Campo usuario
        gbc.gridy++;
        txtUsuario = new RoundedTextField(15);
        txtUsuario.setPreferredSize(new Dimension(240, 40));
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtUsuario.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        txtUsuario.setToolTipText("Usuario");
        rightPanel.add(txtUsuario, gbc);

        // Campo contraseña
        gbc.gridy++;
        txtPassword = new RoundedPasswordField(15);
        txtPassword.setPreferredSize(new Dimension(240, 40));
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtPassword.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        txtPassword.setToolTipText("Contraseña");
        rightPanel.add(txtPassword, gbc);

        // Boton Ingresar
        gbc.gridy++;
        RoundedButton btnLogin = new RoundedButton("Ingresar");
        btnLogin.setPreferredSize(new Dimension(160, 45));
        btnLogin.setBackground(new Color(0, 102, 204));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rightPanel.add(btnLogin, gbc);

        btnLogin.addActionListener(e -> iniciarSesion());
    }

    private void iniciarSesion() {
        String usuario = txtUsuario.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();

        if (usuario.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe completar todos los campos");
            return;
        }

        try {
            if (!usuarioController.existeUsuario(usuario)) {
                JOptionPane.showMessageDialog(this, "El usuario no existe");
                return;
            }

            var opt = usuarioController.login(usuario, pass);
            if (opt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Contraseña incorrecta");
                return;
            }

            Usuario u = opt.get();
            JOptionPane.showMessageDialog(this, "Bienvenido/a " + u.getNombre());

            switch (u.getClass().getSimpleName()) {

                case "Administrador" -> {
                    ControllerAdministrador controller = new ControllerAdministrador(u);
                    new MenuAdministradorFrame(controller, (Administrador) u).setVisible(true);
                }

                case "Medico" ->
                        JOptionPane.showMessageDialog(this, "Menú de Médico próximamente");

                case "Paciente" ->
                        JOptionPane.showMessageDialog(this, "Menú de Paciente próximamente");

                default ->
                        JOptionPane.showMessageDialog(this, "Rol desconocido");
            }

            dispose();

        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Usuario bloqueado", JOptionPane.ERROR_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en el login: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // campos redondos

    class RoundedTextField extends JTextField {
        private int radius;

        public RoundedTextField(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.setColor(new Color(180, 180, 180));
            g2.drawRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            super.paintComponent(g);
        }
    }

    class RoundedPasswordField extends JPasswordField {
        private int radius;

        public RoundedPasswordField(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.setColor(new Color(180, 180, 180));
            g2.drawRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            super.paintComponent(g);
        }
    }

    class RoundedButton extends JButton {
        private int radius = 15;

        public RoundedButton(String text) {
            super(text);
            setOpaque(false);
            setFocusPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            super.paintComponent(g);
        }

        @Override
        public void paintBorder(Graphics g) {
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
