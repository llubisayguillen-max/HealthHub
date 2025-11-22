package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import dll.ControllerAdministrador;
import bll.Administrador;
import bll.Paciente;

public class ModificarPacienteFrame extends JFrame {

    private ControllerAdministrador controller;
    private Administrador admin;
    private String usuarioBuscado;

    private JTextField txtNombre, txtApellido, txtUsuario, txtContrato, txtOS;
    private JTextField txtPass;

    private RoundedButton btnGuardar;

    public ModificarPacienteFrame(ControllerAdministrador controller, Administrador admin, String usuario) {
        this.controller = controller;
        this.admin = admin;
        this.usuarioBuscado = usuario;

        setTitle("HealthHub - Modificar Paciente");
        setSize(500, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        cargarDatosDelPaciente();
    }

    private void initUI() {

        setLayout(new BorderLayout());

        //barra superior
        JPanel topBar = new JPanel();
        topBar.setBackground(new Color(0, 102, 204));
        topBar.setPreferredSize(new Dimension(500, 60));
        topBar.setLayout(new BorderLayout());

        JLabel lblTitulo = new JLabel("Modificar Paciente", SwingConstants.CENTER);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));

        topBar.add(lblTitulo, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(null);
        form.setBackground(Color.WHITE);

        int y = 30;

        // Usuario (no editable)
        form.add(crearLabel("Usuario:", 30, y));
        txtUsuario = crearCampo(150, y);
        txtUsuario.setEditable(false);
        form.add(txtUsuario);
        y += 50;

        // Nombre
        form.add(crearLabel("Nombre:", 30, y));
        txtNombre = crearCampo(150, y);
        form.add(txtNombre);
        y += 50;

        // Apellido
        form.add(crearLabel("Apellido:", 30, y));
        txtApellido = crearCampo(150, y);
        form.add(txtApellido);
        y += 50;

        // Contraseña
        form.add(crearLabel("Contraseña:", 30, y));
        txtPass = crearCampo(150, y);
        form.add(txtPass);
        y += 50;

        // Contrato
        form.add(crearLabel("N° Contrato:", 30, y));
        txtContrato = crearCampo(150, y);
        form.add(txtContrato);
        y += 50;

        // Obra social
        form.add(crearLabel("Obra Social:", 30, y));
        txtOS = crearCampo(150, y);
        form.add(txtOS);
        y += 60;

        // Botón guardar
        btnGuardar = new RoundedButton("Guardar Cambios");
        btnGuardar.setBackground(new Color(0, 102, 204));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnGuardar.setBounds(150, y, 200, 40);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.setFocusPainted(false);
        form.add(btnGuardar);

        btnGuardar.addActionListener(e -> guardarCambios());

        add(form, BorderLayout.CENTER);
    }

    private JLabel crearLabel(String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lbl.setBounds(x, y, 130, 30);
        return lbl;
    }

    private JTextField crearCampo(int x, int y) {
        JTextField txt = new RoundedTextField(12);
        txt.setBounds(x, y, 250, 35);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return txt;
    }


    private void cargarDatosDelPaciente() {

        Paciente p = controller.obtenerPaciente(usuarioBuscado);

        if (p == null) {
            JOptionPane.showMessageDialog(this, "Paciente no encontrado.");
            dispose();
            return;
        }

        txtUsuario.setText(p.getUsuario());
        txtNombre.setText(p.getNombre());
        txtApellido.setText(p.getApellido());
        txtPass.setText(p.getContrasenia());
        txtContrato.setText(String.valueOf(p.getNroContrato()));
        txtOS.setText(p.getObraSocial());
    }


    private void guardarCambios() {

        String usr = txtUsuario.getText().trim();
        String nom = txtNombre.getText().trim();
        String ape = txtApellido.getText().trim();
        String pass = txtPass.getText().trim();
        String nro = txtContrato.getText().trim();
        String os = txtOS.getText().trim();

        if (nom.isEmpty()) { mensaje("Ingrese el nombre."); return; }
        if (ape.isEmpty()) { mensaje("Ingrese el apellido."); return; }
        if (pass.isEmpty()) { mensaje("Ingrese la contraseña."); return; }
        if (!nro.matches("\\d+")) { mensaje("Contrato debe ser numérico."); return; }
        if (os.isEmpty()) { mensaje("Ingrese la obra social."); return; }

        controller.modificarPaciente(
                usr,
                nom,
                ape,
                pass,
                Integer.parseInt(nro),
                os
        );

        JOptionPane.showMessageDialog(this, "Paciente modificado correctamente.");
        dispose();
    }

    private void mensaje(String msg) {
        JOptionPane.showMessageDialog(this, msg);
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
