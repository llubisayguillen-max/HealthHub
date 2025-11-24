package gui;

import javax.swing.*;
import java.awt.*;

import bll.Medico;
import dll.ControllerAdministrador;

public class ModificarMedicoFrame extends JFrame {

    private ControllerAdministrador controller;
    private String usuarioBuscado;

    private JTextField txtUsuario, txtNombre, txtApellido, txtPass, txtMatricula, txtEspecialidad;
    private RoundedButton btnGuardar;

    public ModificarMedicoFrame(ControllerAdministrador controller, String usuarioBuscado) {
        this.controller = controller;
        this.usuarioBuscado = usuarioBuscado;

        setTitle("HealthHub - Modificar Médico");
        setSize(500, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        cargarDatosDelMedico();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Barra superior
        JPanel topBar = new JPanel();
        topBar.setBackground(new Color(0, 102, 204));
        topBar.setPreferredSize(new Dimension(500, 60));
        topBar.setLayout(new BorderLayout());

        JLabel lblTitulo = new JLabel("Modificar Médico", SwingConstants.CENTER);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        topBar.add(lblTitulo, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);

        // Formulario
        JPanel form = new JPanel();
        form.setLayout(null);
        form.setBackground(Color.WHITE);

        int y = 30;

        // Usuario (NO editable)
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

        // Matrícula
        form.add(crearLabel("Matrícula:", 30, y));
        txtMatricula = crearCampo(150, y);
        form.add(txtMatricula);
        y += 50;

        // Especialidad
        form.add(crearLabel("Especialidad:", 30, y));
        txtEspecialidad = crearCampo(150, y);
        form.add(txtEspecialidad);
        y += 60;

        // Botón Guardar
        btnGuardar = new RoundedButton("Guardar Cambios");
        btnGuardar.setBackground(new Color(0, 102, 204));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnGuardar.setBounds(150, y, 200, 40);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        form.add(btnGuardar);

        btnGuardar.addActionListener(e -> guardarCambios());

        add(form, BorderLayout.CENTER);
    }

    private JLabel crearLabel(String txt, int x, int y) {
        JLabel lbl = new JLabel(txt);
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

    private void cargarDatosDelMedico() {
        Medico m = controller.obtenerMedico(usuarioBuscado);

        if (m == null) {
            JOptionPane.showMessageDialog(this, "Médico no encontrado.");
            dispose();
            return;
        }

        txtUsuario.setText(m.getUsuario());
        txtNombre.setText(m.getNombre());
        txtApellido.setText(m.getApellido());
        txtPass.setText(m.getContrasenia());
        txtMatricula.setText(m.getMatricula());
        txtEspecialidad.setText(m.getEspecialidad());
    }

    private void guardarCambios() {
        String usr = txtUsuario.getText().trim();
        String nom = txtNombre.getText().trim();
        String ape = txtApellido.getText().trim();
        String pass = txtPass.getText().trim();
        String mat = txtMatricula.getText().trim();
        String esp = txtEspecialidad.getText().trim();

        if (nom.isEmpty()) { mensaje("Ingrese el nombre."); return; }
        if (ape.isEmpty()) { mensaje("Ingrese el apellido."); return; }
        if (pass.isEmpty()) { mensaje("Ingrese la contraseña."); return; }
        if (mat.isEmpty()) { mensaje("Ingrese la matrícula."); return; }
        if (esp.isEmpty()) { mensaje("Ingrese la especialidad."); return; }

        controller.modificarMedico(usr, nom, ape, pass, mat, esp);

        JOptionPane.showMessageDialog(this, "Médico modificado correctamente.");
        dispose();
    }

    private void mensaje(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }



    class RoundedTextField extends JTextField {
        private int radius;

        public RoundedTextField(int radius) {
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

}
