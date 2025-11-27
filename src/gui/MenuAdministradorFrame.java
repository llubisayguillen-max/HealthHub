package gui;

import javax.swing.*;
import java.awt.*;
import bll.Administrador;
import bll.Paciente;
import dll.ControllerAdministrador;
import static gui.UiPaleta.*;

public class MenuAdministradorFrame extends JFrame {

    private Administrador admin;
    private ControllerAdministrador controller;

    private static final String UI_FONT_FAMILY = "Segoe UI";

    public MenuAdministradorFrame(ControllerAdministrador controller, Administrador admin) {
        this.admin = admin;
        this.controller = controller;

        setTitle("HealthHub - Panel del Administrador");
        setSize(980, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
    }

    private void initUI() {

        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        //HEADER
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COLOR_PRIMARY);
        topBar.setPreferredSize(new Dimension(getWidth(), 85));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 40, 15, 40));

        JLabel lblTitulo = new JLabel("Panel del Administrador");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 26));

        JLabel lblNombre = new JLabel(
                capitalizarNombre(admin.getNombreCompleto()) + "  |  Administrador"
        );
        lblNombre.setForeground(new Color(230, 245, 255));
        lblNombre.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 14));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(lblTitulo);
        info.add(Box.createVerticalStrut(4));
        info.add(lblNombre);

        topBar.add(info, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        //CENTRO
        JPanel wrapperCenter = new JPanel(new BorderLayout());
        wrapperCenter.setOpaque(false);
        wrapperCenter.setBorder(BorderFactory.createEmptyBorder(10, 32, 10, 32));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new GridLayout(1, 3, 22, 0));

        // CARD PACIENTES
        JPanel cardPacientes = crearCardConBotones(
                "Gestión de Pacientes",
                "/gui/img/gestionPacientes.png",
                new String[]{"Alta Paciente", "Modificar Paciente"},
                new Runnable[]{
                        () -> new AltaPacienteFrame(controller, admin).setVisible(true),
                        () -> {
                            String usuario = JOptionPane.showInputDialog(
                                    this,
                                    "Ingrese el usuario del paciente:",
                                    "Modificar Paciente",
                                    JOptionPane.QUESTION_MESSAGE
                            );
                            if (usuario != null && !usuario.isBlank())
                                new ModificarPacienteFrame(controller, admin, usuario.trim()).setVisible(true);
                        }
                }
        );

        // CARD MÉDICOS 
        JPanel cardMedicos = crearCardConBotones(
                "Gestión de Médicos",
                "/gui/img/gestionMedicos.png",
                new String[]{"Alta Médico", "Modificar Médico"},
                new Runnable[]{
                        () -> new AltaMedicoFrame(controller, admin).setVisible(true),
                        () -> {
                            String usuario = JOptionPane.showInputDialog(
                                    this,
                                    "Ingrese el usuario del médico:",
                                    "Modificar Médico",
                                    JOptionPane.QUESTION_MESSAGE
                            );
                            if (usuario != null && !usuario.isBlank())
                                new ModificarMedicoFrame(controller, usuario.trim()).setVisible(true);
                        }
                }
        );

        //CARD USUARIOS
        JPanel cardUsuarios = crearCardConBotones(
                "Funciones de Usuarios",
                "/gui/img/gestionUsuarios.png",
                new String[]{
                        "Lista de usuarios",
                        "Resetear Contraseña",
                        "Bloquear Usuario",
                        "Desbloquear Usuario",
                        "Eliminar Usuario"
                },
                new Runnable[]{
                        () -> new ListarUsuariosFrame(controller,admin).setVisible(true),                        
                        () -> new ResetearContraseniaFrame(controller, admin).setVisible(true),
                        () -> new BloquearUsuarioFrame(controller).setVisible(true),
                        () -> new DesbloquearUsuarioFrame(controller).setVisible(true),
                        () -> new EliminarUsuarioFrame(controller).setVisible(true),

                }

        );

     // CARD HISTORIAL MÉDICO
        JPanel cardHistorial = crearCardConBotones(
                "Historial Médico",
                "/gui/img/historial.png",
                new String[]{"Crear Registro", "Ver Historial"},
                new Runnable[]{

                        // Acción Crear Registro
                        () -> {
                            var pacientes = controller.listarPacientes();
                            var medicos = controller.listarMedicos();
                            var historialManager = controller.getHistorialManager();

                            new CrearRegistroHistorialFrame(pacientes, medicos, historialManager).setVisible(true);
                        },

                        // Acción Ver Historial
                        () -> {
                            var pacientes = controller.listarPacientes();
                            var historialManager = controller.getHistorialManager();

                            if (pacientes.isEmpty()) {
                                JOptionPane.showMessageDialog(this, "No hay pacientes registrados.");
                                return;
                            }

                            // Crear array de nombres de pacientes
                            String[] nombres = pacientes.stream()
                                                        .map(Paciente::getNombre)
                                                        .toArray(String[]::new);

                            // Seleccionar paciente
                            String seleccionado = (String) JOptionPane.showInputDialog(
                                    this,
                                    "Seleccione un paciente:",
                                    "Ver Historial",
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    nombres,
                                    nombres[0]
                            );

                            if (seleccionado != null) {
                                // Buscar el paciente correspondiente
                                Paciente paciente = pacientes.stream()
                                                             .filter(p -> p.getNombre().equals(seleccionado))
                                                             .findFirst()
                                                             .orElse(null);

                                if (paciente != null) {
                                    // Abrir frame de historial del paciente
                                    new VerHistorialPacienteFrame(paciente, historialManager).setVisible(true);
                                } else {
                                    JOptionPane.showMessageDialog(this, "Paciente no encontrado.");
                                }
                            }
                        }
                }
        );



        center.add(cardPacientes);
        center.add(cardMedicos);
        center.add(cardUsuarios);
        center.add(cardHistorial);


        wrapperCenter.add(center, BorderLayout.CENTER);
        add(wrapperCenter, BorderLayout.CENTER);

        // FOOTER
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(COLOR_BACKGROUND);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 40, 20, 40));

        RoundedButton btnCerrar = new RoundedButton("Cerrar sesión");
        btnCerrar.setBackground(COLOR_DANGER);
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 13));
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnCerrar.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        JPanel rightFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightFooter.setOpaque(false);
        rightFooter.add(btnCerrar);

        footer.add(rightFooter, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    //CARD CON LISTA DE BOTONES
    private JPanel crearCardConBotones(
            String titulo,
            String imagePath,
            String[] botones,
            Runnable[] acciones
    ) {

        RoundedCardPanel card = new RoundedCardPanel(16);
        card.setBackground(COLOR_CARD_BG);
        card.setBorderColor(COLOR_CARD_BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        card.setPreferredSize(new Dimension(285, 260));

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 17));
        lblTitulo.setForeground(MINT_DARK);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblIcon = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
            Image scaled = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            lblIcon.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            lblIcon.setText("?");
        }
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel lista = new JPanel();
        lista.setOpaque(false);
        lista.setLayout(new GridLayout(botones.length, 1, 6, 6));
        lista.setMaximumSize(new Dimension(240, botones.length * 42));

        for (int i = 0; i < botones.length; i++) {
            RoundedButton b = new RoundedButton(botones[i]);
            b.setBackground(COLOR_ACCENT);
            b.setForeground(Color.WHITE);
            b.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 13));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            int finalI = i;
            b.addActionListener(e -> acciones[finalI].run());
            lista.add(b);
        }

        card.add(lblIcon);
        card.add(Box.createVerticalStrut(15));
        card.add(lblTitulo);
        card.add(Box.createVerticalStrut(15));
        card.add(lista);

        return card;
    }

    private String capitalizarNombre(String s) {
        if (s == null || s.isBlank()) return "";
        String[] partes = s.toLowerCase().split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : partes) {
            if (!p.isBlank())
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    // PANEL REDONDEADO
    private static class RoundedCardPanel extends JPanel {
        private final int radius;
        private Color borderColor = new Color(200, 200, 200);

        public RoundedCardPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        public void setBorderColor(Color c) {
            borderColor = c;
            repaint();
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
