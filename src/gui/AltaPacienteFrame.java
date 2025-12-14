package gui;

import javax.swing.*;
import java.awt.*;
import bll.Administrador;
import dll.ControllerAdministrador;
import static gui.UiPaleta.*;

public class AltaPacienteFrame extends JFrame {

    private JTextField txtNombre, txtApellido, txtUsuario, txtContrato, txtObraSocial;
    private JPasswordField txtPassword;
    private ControllerAdministrador controller;
    private Administrador admin;

    private static final String UI_FONT_FAMILY = "Segoe UI";

    public AltaPacienteFrame(ControllerAdministrador controller, Administrador admin) {
        this.controller = controller;
        this.admin = admin;

        setTitle("HealthHub - Alta de Paciente");
        setSize(580, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {

        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        // ---------- TOP BAR ----------
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COLOR_PRIMARY);
        topBar.setPreferredSize(new Dimension(getWidth(), 80));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblTitulo = new JLabel("Alta de Paciente");
        lblTitulo.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);

        topBar.add(lblTitulo, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        // ---------- FORM CARD ----------
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(COLOR_BACKGROUND);

        RoundedCardPanel card = new RoundedCardPanel(18);
        card.setBackground(COLOR_CARD_BG);
        card.setBorderColor(COLOR_CARD_BORDER);
        card.setPreferredSize(new Dimension(530, 430));
        card.setLayout(null); 
        int y = 30;

        // === Usuario ===
        card.add(crearLabel("Usuario:", 30, y));
        txtUsuario = crearCampo(150, y);
        card.add(txtUsuario);
        y += 50;

        // === Nombre ===
        card.add(crearLabel("Nombre:", 30, y));
        txtNombre = crearCampo(150, y);
        card.add(txtNombre);
        y += 50;

        // === Apellido ===
        card.add(crearLabel("Apellido:", 30, y));
        txtApellido = crearCampo(150, y);
        card.add(txtApellido);
        y += 50;

        // === Contraseña ===
        card.add(crearLabel("Contraseña:", 30, y));
        txtPassword = new RoundedPasswordField(12);
        txtPassword.setBounds(150, y, 250, 35);
        txtPassword.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        card.add(txtPassword);
        y += 50;

        // === Contrato ===
        card.add(crearLabel("N° Contrato:", 30, y));
        txtContrato = crearCampo(150, y);
        card.add(txtContrato);
        y += 50;

        // === Obra Social ===
        card.add(crearLabel("Obra Social:", 30, y));
        txtObraSocial = crearCampo(150, y);
        card.add(txtObraSocial);
        y += 70;

        //guardar
        RoundedButton btnGuardar = new RoundedButton("Guardar");
        btnGuardar.setBackground(COLOR_ACCENT);
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 16));
        btnGuardar.setBounds(155, y, 200, 40);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnGuardar.addActionListener(e -> guardarPaciente());

        card.add(btnGuardar);

        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);
    }

    //helpers

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

                mostrarDialogoMensaje(
                        "Validación",
                        "Debe completar todos los campos.",
                        COLOR_ACCENT
                );
                return;
            }

            if (!contrato.matches("\\d+")) {
                mostrarDialogoMensaje(
                        "Validación",
                        "El número de contrato debe ser numérico.",
                        COLOR_ACCENT
                );
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

            mostrarDialogoMensaje(
                    "Alta exitosa",
                    "Paciente registrado correctamente.",
                    COLOR_PRIMARY
            );

            dispose();

        } catch (Exception ex) {
            mostrarDialogoMensaje(
                    "Error",
                    "Error al registrar el paciente:<br>" + ex.getMessage(),
                    new Color(180, 60, 60)
            );
        }
    }

    
    private void mostrarDialogoMensaje(String titulo, String mensaje, Color buttonBg) {

        JDialog dlg = new JDialog(this, titulo, true);
        dlg.setSize(380, 160);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(14, 18, 10, 18));

        JLabel lblMsg = new JLabel(
                "<html><div style='text-align:center;'>" + mensaje + "</div></html>",
                SwingConstants.CENTER
        );
        lblMsg.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 14));
        content.add(lblMsg, BorderLayout.CENTER);

        dlg.add(content, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        footer.setBackground(Color.WHITE);

        RoundedButton btnOk = new RoundedButton("Aceptar");
        btnOk.setBackground(buttonBg);
        btnOk.setForeground(Color.WHITE);
        btnOk.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 14));
        btnOk.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        btnOk.setFocusPainted(false);
        btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOk.addActionListener(e -> dlg.dispose());

        footer.add(btnOk);
        dlg.add(footer, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }



    // ---------- Componentes redondeados ----------

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

            g2.setColor(new Color(200, 200, 200));
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
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
        }
    }
}
