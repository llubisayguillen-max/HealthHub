package gui.AdministradorFrame;

import javax.swing.*;
import java.awt.*;
import bll.Administrador;
import dll.ControllerAdministrador;

import static gui.UiPaleta.*;

public class ResetearContraseniaFrame extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtNuevaPass;
    private JCheckBox chkMostrarPass;
    private ControllerAdministrador controller;
    private Administrador admin;

    private static final String UI_FONT_FAMILY = "Segoe UI";

    public ResetearContraseniaFrame(ControllerAdministrador controller, Administrador admin) {
        this.controller = controller;
        this.admin = admin;

        setTitle("HealthHub - Resetear Contraseña");
        setSize(580, 430);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {

        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COLOR_PRIMARY);
        topBar.setPreferredSize(new Dimension(getWidth(), 80));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblTitulo = new JLabel("Resetear Contraseña");
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
        card.setPreferredSize(new Dimension(530, 260));
        card.setLayout(null);

        int y = 30;

        //Usuario
        card.add(crearLabel("Usuario:", 30, y));
        txtUsuario = crearCampo(160, y);
        card.add(txtUsuario);
        y += 50;

        //Nueva contraseña
        card.add(crearLabel("Nueva Contraseña:", 30, y));
        txtNuevaPass = new RoundedPasswordField(12);
        txtNuevaPass.setBounds(160, y, 250, 35);
        txtNuevaPass.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 14));
        txtNuevaPass.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        card.add(txtNuevaPass);
        y += 45;

        //Mostrar password
        chkMostrarPass = new JCheckBox("Mostrar contraseña");
        chkMostrarPass.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 14));
        chkMostrarPass.setBackground(Color.WHITE);
        chkMostrarPass.setBounds(160, y, 200, 30);

        chkMostrarPass.addActionListener(e ->
                txtNuevaPass.setEchoChar(chkMostrarPass.isSelected() ? 0 : '\u2022')
        );

        card.add(chkMostrarPass);
        y += 60;

        //Botón Resetear
        RoundedButton btnResetear = new RoundedButton("Resetear");
        btnResetear.setBackground(COLOR_ACCENT);
        btnResetear.setForeground(Color.WHITE);
        btnResetear.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 16));
        btnResetear.setBounds(160, y, 200, 40);
        btnResetear.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnResetear.addActionListener(e -> resetearPassword());
        card.add(btnResetear);

        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);
    }


    private JLabel crearLabel(String txt, int x, int y) {
        JLabel lbl = new JLabel(txt);
        lbl.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 16));
        lbl.setForeground(MINT_DARK);
        lbl.setBounds(x, y, 150, 30);
        return lbl;
    }

    private JTextField crearCampo(int x, int y) {
        JTextField txt = new RoundedTextField(12);
        txt.setBounds(x, y, 250, 35);
        txt.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        return txt;
    }

    private void resetearPassword() {
        String usr = txtUsuario.getText().trim();
        String nueva = new String(txtNuevaPass.getPassword()).trim();

        if (usr.isEmpty() || nueva.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe completar todos los campos");
            return;
        }

        controller.resetearContrasenia(usr, nueva);
        JOptionPane.showMessageDialog(this, "Contraseña actualizada correctamente.");
        dispose();
    }


    class RoundedTextField extends JTextField {
        private final int radius;

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
        private final int radius;

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
