package gui;

import javax.swing.*;
import java.awt.*;
import bll.Administrador;
import dll.ControllerAdministrador;

public class AltaPacienteFrame extends JFrame {

    private JTextField txtNombre, txtApellido, txtUsuario, txtContrato, txtObraSocial;
    private JPasswordField txtPassword;
    private ControllerAdministrador controller;
    private Administrador admin;

    public AltaPacienteFrame(ControllerAdministrador controller, Administrador admin) {
        this.controller = controller;
        this.admin = admin;

        setTitle("HealthHub - Alta de Paciente");
        setSize(500, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        //barra 
        JPanel topBar = new JPanel();
        topBar.setBackground(new Color(0, 102, 204));
        topBar.setPreferredSize(new Dimension(500, 60));
        topBar.setLayout(new BorderLayout());

        JLabel lblTitulo = new JLabel("Alta de Paciente", SwingConstants.CENTER);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));

        topBar.add(lblTitulo, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);

        //campos
        JPanel form = new JPanel();
        form.setLayout(null);
        form.setBackground(Color.WHITE);

        int y = 30;

        form.add(crearLabel("Usuario:", 30, y));
        txtUsuario = crearCampo(150, y);
        form.add(txtUsuario);
        y += 50;

        form.add(crearLabel("Nombre:", 30, y));
        txtNombre = crearCampo(150, y);
        form.add(txtNombre);
        y += 50;

        form.add(crearLabel("Apellido:", 30, y));
        txtApellido = crearCampo(150, y);
        form.add(txtApellido);
        y += 50;

        form.add(crearLabel("Contraseña:", 30, y));
        txtPassword = new RoundedPasswordField(12);
        txtPassword.setBounds(150, y, 250, 35);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        form.add(txtPassword);
        y += 50;

        form.add(crearLabel("N° Contrato:", 30, y));
        txtContrato = crearCampo(150, y);
        form.add(txtContrato);
        y += 50;

        form.add(crearLabel("Obra Social:", 30, y));
        txtObraSocial = crearCampo(150, y);
        form.add(txtObraSocial);
        y += 60;

        RoundedButton btnGuardar = new RoundedButton("Guardar");
        btnGuardar.setBackground(new Color(0, 102, 204));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnGuardar.setBounds(150, y, 200, 40);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.setFocusPainted(false);
        form.add(btnGuardar);

        btnGuardar.addActionListener(e -> guardarPaciente());

        add(form, BorderLayout.CENTER);
    }

    private JLabel crearLabel(String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lbl.setBounds(x, y, 120, 30);
        return lbl;
    }

    private JTextField crearCampo(int x, int y) {
        JTextField txt = new RoundedTextField(12);
        txt.setBounds(x, y, 250, 35);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return txt;
    }

    private void guardarPaciente() {
        try {
            String usuario = txtUsuario.getText().trim();
            String nombre = txtNombre.getText().trim();
            String apellido = txtApellido.getText().trim();
            String pass = new String(txtPassword.getPassword()).trim();
            String contrato = txtContrato.getText().trim();
            String obra = txtObraSocial.getText().trim();

            if (usuario.isEmpty() || nombre.isEmpty() || apellido.isEmpty() ||
                    pass.isEmpty() || contrato.isEmpty() || obra.isEmpty()) {

                JOptionPane.showMessageDialog(this, "Debe completar todos los campos");
                return;
            }

            if (!contrato.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "El contrato debe ser numérico");
                return;
            }

            controller.registrarPaciente(
                    usuario,
                    nombre,
                    apellido,
                    pass,
                    Integer.parseInt(contrato),
                    obra
            );

            JOptionPane.showMessageDialog(this, "Paciente registrado correctamente");
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar: " + ex.getMessage());
        }
    }

    //bordes campos

    class RoundedTextField extends JTextField {
        private int radius;

        public RoundedTextField(int radius) {
            super();
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.setColor(new Color(180, 180, 180));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            super.paintComponent(g);
            g2.dispose();
        }
    }

    class RoundedPasswordField extends JPasswordField {
        private int radius;

        public RoundedPasswordField(int radius) {
            super();
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.setColor(new Color(180, 180, 180));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            super.paintComponent(g);
            g2.dispose();
        }
    }

    class RoundedButton extends JButton {
        private int radius = 15;

        public RoundedButton(String text) {
            super(text);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        public void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(getBackground());
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
        }
    }

}
