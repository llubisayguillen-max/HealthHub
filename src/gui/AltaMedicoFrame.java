package gui;

import javax.swing.*;
import java.awt.*;
import bll.Administrador;
import dll.ControllerAdministrador;

public class AltaMedicoFrame extends JFrame {

    private JTextField txtUsuario, txtNombre, txtApellido, txtMatricula, txtEspecialidad;
    private JPasswordField txtPassword;

    private ControllerAdministrador controller;
    private Administrador admin;

    public AltaMedicoFrame(ControllerAdministrador controller, Administrador admin) {
        this.controller = controller;
        this.admin = admin;

        setTitle("HealthHub - Alta de Médico");
        setSize(500, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ==== Barra Superior ====
        JPanel topBar = new JPanel();
        topBar.setBackground(new Color(0, 102, 204));
        topBar.setPreferredSize(new Dimension(500, 60));
        topBar.setLayout(new BorderLayout());

        JLabel lblTitulo = new JLabel("Alta de Médico", SwingConstants.CENTER);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));

        topBar.add(lblTitulo, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);

        // ==== Formulario ====
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

        form.add(crearLabel("Matrícula:", 30, y));
        txtMatricula = crearCampo(150, y);
        form.add(txtMatricula);
        y += 50;

        form.add(crearLabel("Especialidad:", 30, y));
        txtEspecialidad = crearCampo(150, y);
        form.add(txtEspecialidad);
        y += 60;

        // ==== Botón Guardar ====
        RoundedButton btnGuardar = new RoundedButton("Guardar");
        btnGuardar.setBackground(new Color(0, 102, 204));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnGuardar.setBounds(150, y, 200, 40);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.setFocusPainted(false);
        form.add(btnGuardar);

        btnGuardar.addActionListener(e -> guardarMedico());

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

    private void guardarMedico() {
        try {
            String usuario = txtUsuario.getText().trim();
            String nombre = txtNombre.getText().trim();
            String apellido = txtApellido.getText().trim();
            String contrasenia = new String(txtPassword.getPassword()).trim();
            String matricula = txtMatricula.getText().trim();
            String especialidad = txtEspecialidad.getText().trim();

            if (usuario.isEmpty() || nombre.isEmpty() || apellido.isEmpty() ||
                contrasenia.isEmpty() || matricula.isEmpty() || especialidad.isEmpty()) {

                JOptionPane.showMessageDialog(this, "Debe completar todos los campos");
                return;
            }

            if (!matricula.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "La matrícula debe ser numérica");
                return;
            }

            controller.registrarMedico(
                    usuario,
                    nombre,
                    apellido,
                    contrasenia,
                    matricula,
                    especialidad
            );

            JOptionPane.showMessageDialog(this, "Médico registrado correctamente");
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar: " + ex.getMessage());
        }
    }

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

