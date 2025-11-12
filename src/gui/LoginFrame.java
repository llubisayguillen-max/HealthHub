package gui;

import javax.swing.*;
import java.awt.*;
import dll.ControllerUsuario;
import bll.Usuario;

public class LoginFrame extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private ControllerUsuario usuarioController = new ControllerUsuario();

    public LoginFrame() {
        setTitle("Sistema de Salud - Login");
        setSize(720, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // panel izquierda
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(91, 155, 213)); 
        leftPanel.setPreferredSize(new Dimension(280, getHeight()));
        leftPanel.setLayout(new GridBagLayout());

        // agg logo de health si hacemos (ya tiene el espacio arriba del nombre del sistema en el panel izqui)
        JLabel lblLogo = new JLabel("");
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));

        JLabel lblNombreSistema = new JLabel("<html><center>Sistema de Gestión<br/>Health Hub</center></html>");
        lblNombreSistema.setForeground(Color.WHITE);
        lblNombreSistema.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JPanel leftContent = new JPanel(new GridLayout(2, 1, 0, 20));
        leftContent.setOpaque(false);
        leftContent.add(lblLogo);
        leftContent.add(lblNombreSistema);

        leftPanel.add(leftContent);

        // panel derecho
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new GridBagLayout());

        // Form derecho
        JPanel formPanel = new JPanel(null);
        formPanel.setPreferredSize(new Dimension(300, 330));
        formPanel.setBackground(Color.WHITE);

        // agg img o logo si hacemos
        JLabel iconUser = new JLabel(UIManager.getIcon(""));
        iconUser.setBounds(120, 10, 60, 60);
        formPanel.add(iconUser);

        JLabel lblTitulo = new JLabel("Inicio de Sesión", SwingConstants.CENTER);
        lblTitulo.setBounds(70, 75, 160, 30);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formPanel.add(lblTitulo);

        JLabel lblUsuario = new JLabel("Usuario:");
        lblUsuario.setBounds(20, 120, 200, 20);
        formPanel.add(lblUsuario);

        txtUsuario = new JTextField();
        txtUsuario.setBounds(20, 145, 260, 35);
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsuario.setBorder(BorderFactory.createLineBorder(new Color(180,180,180), 1, true));
        formPanel.add(txtUsuario);

        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setBounds(20, 190, 200, 20);
        formPanel.add(lblPassword);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(20, 215, 260, 35);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createLineBorder(new Color(180,180,180), 1, true));
        formPanel.add(txtPassword);

        JButton btnLogin = new JButton("Ingresar");
        btnLogin.setBounds(20, 265, 260, 40);
        btnLogin.setBackground(new Color(0, 120, 215));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorder(BorderFactory.createEmptyBorder());
        formPanel.add(btnLogin);

        btnLogin.addActionListener(e -> iniciarSesion());

        rightPanel.add(formPanel);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
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
                case "Administrador" -> new MenuAdministradorFrame().setVisible(true);
                case "Medico"       -> new MenuMedicoFrame().setVisible(true);
                case "Paciente"     -> new MenuPacienteFrame().setVisible(true);
                default -> JOptionPane.showMessageDialog(this, "Rol desconocido");
            }

            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
