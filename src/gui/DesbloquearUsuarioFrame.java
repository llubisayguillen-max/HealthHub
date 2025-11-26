package gui;

import javax.swing.*;
import java.awt.*;

import dll.ControllerAdministrador;
import static gui.UiPaleta.*;

public class DesbloquearUsuarioFrame extends JFrame {

    private JTextField txtUsuario;
    private ControllerAdministrador controller;

    private static final String UI_FONT_FAMILY = "Segoe UI";

    public DesbloquearUsuarioFrame(ControllerAdministrador controller) {
        this.controller = controller;

        setTitle("HealthHub - Desbloquear Usuario");
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

        JLabel lblTitulo = new JLabel("Desbloquear Usuario");
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

        int y = 40;

        //Usuario
        card.add(crearLabel("Usuario:", 30, y));
        txtUsuario = crearCampo(160, y);
        card.add(txtUsuario);
        y += 55;

        //Botón Desbloquear
        RoundedButton btnDesbloquear = new RoundedButton("Desbloquear");
        btnDesbloquear.setBackground(COLOR_ACCENT);
        btnDesbloquear.setForeground(Color.WHITE);
        btnDesbloquear.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 16));
        btnDesbloquear.setBounds(160, y, 200, 40);
        btnDesbloquear.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnDesbloquear.addActionListener(e -> desbloquearUsuario());
        card.add(btnDesbloquear);
        y += 55;


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

    private void desbloquearUsuario() {
        String usr = txtUsuario.getText().trim();

        if (usr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar un nombre de usuario.");
            return;
        }

        controller.desbloquearUsuario(usr);
        JOptionPane.showMessageDialog(this, "Usuario desbloqueado exitosamente.");
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

