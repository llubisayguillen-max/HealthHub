package gui;

import javax.swing.*;
import java.awt.*;

import dll.ControllerAdministrador;
import bll.Administrador;
import bll.Paciente;

import static gui.UiPaleta.*;

public class ModificarPacienteFrame extends JFrame {

    private ControllerAdministrador controller;
    private Administrador admin;
    private String usuarioBuscado;

    private JTextField txtNombre, txtApellido, txtUsuario, txtContrato, txtOS;
    private JPasswordField txtPass;

    private RoundedButton btnGuardar;

    private static final String UI_FONT_FAMILY = "Segoe UI";

    public ModificarPacienteFrame(ControllerAdministrador controller, Administrador admin, String usuario) {
        this.controller = controller;
        this.admin = admin;
        this.usuarioBuscado = usuario;

        setTitle("HealthHub - Modificar Paciente");
        setSize(580, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        cargarDatosDelPaciente();
    }

    private void initUI() {

        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COLOR_PRIMARY);
        topBar.setPreferredSize(new Dimension(getWidth(), 80));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblTitulo = new JLabel("Modificar Paciente");
        lblTitulo.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);

        topBar.add(lblTitulo, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        //card
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(COLOR_BACKGROUND);

        RoundedCardPanel card = new RoundedCardPanel(18);
        card.setBackground(COLOR_CARD_BG);
        card.setBorderColor(COLOR_CARD_BORDER);
        card.setPreferredSize(new Dimension(530, 430));
        card.setLayout(null);

        int y = 30;

        //Usuario (no editable)
        card.add(crearLabel("Usuario:", 30, y));
        txtUsuario = crearCampo(150, y);
        txtUsuario.setEditable(false);
        txtUsuario.setBackground(new Color(240, 240, 240));
        card.add(txtUsuario);
        y += 50;

        //Nombre
        card.add(crearLabel("Nombre:", 30, y));
        txtNombre = crearCampo(150, y);
        card.add(txtNombre);
        y += 50;

        //Apellido
        card.add(crearLabel("Apellido:", 30, y));
        txtApellido = crearCampo(150, y);
        card.add(txtApellido);
        y += 50;

        //Contraseña
        card.add(crearLabel("Contraseña:", 30, y));
        txtPass = new RoundedPasswordField(12);
        txtPass.setBounds(150, y, 250, 35);
        txtPass.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 14));
        txtPass.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        card.add(txtPass);
        y += 50;

        //Contrato
        card.add(crearLabel("N° Contrato:", 30, y));
        txtContrato = crearCampo(150, y);
        card.add(txtContrato);
        y += 50;

        //Obra Social
        card.add(crearLabel("Obra Social:", 30, y));
        txtOS = crearCampo(150, y);
        card.add(txtOS);
        y += 70;

        //Guardar
        btnGuardar = new RoundedButton("Guardar Cambios");
        btnGuardar.setBackground(COLOR_ACCENT);
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 16));
        btnGuardar.setBounds(155, y, 200, 40);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnGuardar.addActionListener(e -> guardarCambios());

        card.add(btnGuardar);

        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);
    }

    private JLabel crearLabel(String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 16));
        lbl.setForeground(MINT_DARK);
        lbl.setBounds(x, y, 120, 30);
        return lbl;
    }

    private JTextField crearCampo(int x, int y) {
        JTextField txt = new RoundedTextField(12);
        txt.setBounds(x, y, 250, 35);
        txt.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
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
        String pass = new String(txtPass.getPassword()).trim();
        String nro = txtContrato.getText().trim();
        String os = txtOS.getText().trim();

        if (nom.isEmpty()) { mensaje("Ingrese el nombre."); return; }
        if (ape.isEmpty()) { mensaje("Ingrese el apellido."); return; }
        if (pass.isEmpty()) { mensaje("Ingrese la contraseña."); return; }
        if (!nro.matches("\\d+")) { mensaje("El contrato debe ser numérico."); return; }
        if (os.isEmpty()) { mensaje("Ingrese la obra social."); return; }

        controller.modificarPaciente(
                usr, nom, ape, pass, Integer.parseInt(nro), os
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
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.setColor(new Color(200, 200, 200));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            super.paintComponent(g);
            g2.dispose();
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
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.setColor(new Color(200, 200, 200));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            super.paintComponent(g);
            g2.dispose();
        }
    }

    private static class RoundedCardPanel extends JPanel {
        private final int radius;
        private Color borderColor;

        public RoundedCardPanel(int radius) {
            this.radius = radius;
            this.borderColor = new Color(200, 200, 200);
            setOpaque(false);
        }

        public void setBorderColor(Color c) {
            this.borderColor = c;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }
    }
}
