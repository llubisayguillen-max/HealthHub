package gui;

import javax.swing.*;
import java.awt.*;
import bll.Administrador;
import bll.Paciente;
import dll.ControllerAdministrador;
import static gui.UiPaleta.*;
import javax.swing.border.Border;


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
                            String usuario = mostrarDialogoInput(
                                    "Modificar Paciente",
                                    "Ingrese el usuario del paciente:"
                            );

                            if (usuario != null && !usuario.isBlank()) {
                                new ModificarPacienteFrame(
                                        controller,
                                        admin,
                                        usuario.trim()
                                ).setVisible(true);
                            }
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
                        	String usuario = mostrarDialogoInput(
                        	        "Modificar Médico",
                        	        "Ingrese el usuario del médico:"
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

                            new CrearRegistroHistorialFrame(
                                    pacientes,
                                    medicos,
                                    historialManager
                            ).setVisible(true);
                        },

                        // Acción Ver Historial
                        () -> {
                            var pacientes = controller.listarPacientes();
                            var historialManager = controller.getHistorialManager();

                            if (pacientes.isEmpty()) {
                                mostrarMensaje("Historial Médico", "No hay pacientes registrados.");
                                return;
                            }


                            JFrame selectorFrame = new JFrame("Ver Historial");
                            selectorFrame.setSize(420, 240);
                            selectorFrame.setLocationRelativeTo(this);
                            selectorFrame.setResizable(false);
                            selectorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            selectorFrame.getContentPane().setBackground(COLOR_BACKGROUND);
                            selectorFrame.setLayout(new BorderLayout());


                            // Centro
                            JPanel selectorCenter = new JPanel(new GridBagLayout());
                            selectorCenter.setOpaque(false);

                            JPanel card = new JPanel(null);
                            card.setBackground(COLOR_CARD_BG);
                            card.setPreferredSize(new Dimension(360, 120));
                            card.setBorder(BorderFactory.createLineBorder(COLOR_CARD_BORDER));

                            JLabel lblPaciente = new JLabel("Paciente:");
                            lblPaciente.setBounds(20, 25, 100, 25);
                            lblPaciente.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 14));
                            lblPaciente.setForeground(Color.BLACK);
                            card.add(lblPaciente);

                            JComboBox<String> comboPacientes = new JComboBox<>();
                            comboPacientes.setBounds(120, 25, 210, 30);
                            comboPacientes.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 14));

                            pacientes.forEach(p -> comboPacientes.addItem(p.getNombre()));
                            card.add(comboPacientes);

                            RoundedButton btnAceptar = new RoundedButton("Aceptar");
                            btnAceptar.setBounds(80, 70, 90, 32);
                            btnAceptar.setBackground(COLOR_ACCENT);
                            btnAceptar.setForeground(Color.WHITE);
                            btnAceptar.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 13));

                            RoundedButton btnCancelar = new RoundedButton("Cancelar");
                            btnCancelar.setBounds(190, 70, 90, 32);
                            btnCancelar.setBackground(new Color(200, 200, 200));
                            btnCancelar.setForeground(Color.BLACK);
                            btnCancelar.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 13));


                            btnAceptar.addActionListener(e -> {
                                String seleccionado = (String) comboPacientes.getSelectedItem();

                                Paciente paciente = pacientes.stream()
                                        .filter(p -> p.getNombre().equals(seleccionado))
                                        .findFirst()
                                        .orElse(null);

                                if (paciente != null) {
                                    new VerHistorialPacienteFrame(
                                            paciente,
                                            historialManager
                                    ).setVisible(true);
                                } else {
                                    mostrarMensaje("Historial Médico", "Paciente no encontrado.");
                                }

                                selectorFrame.dispose();
                            });

                            btnCancelar.addActionListener(e -> selectorFrame.dispose());

                            card.add(btnAceptar);
                            card.add(btnCancelar);

                            selectorCenter.add(card);
                            selectorFrame.add(selectorCenter, BorderLayout.CENTER);

                            selectorFrame.setVisible(true);
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
    
	    private void mostrarMensaje(String titulo, String mensaje) {
	
	        JDialog dlg = new JDialog(this, titulo, true);
	        dlg.setSize(360, 150);
	        dlg.setLocationRelativeTo(this);
	        dlg.setLayout(new BorderLayout());
	        dlg.getContentPane().setBackground(Color.WHITE);
	
	        JPanel content = new JPanel(new BorderLayout());
	        content.setBackground(Color.WHITE);
	        content.setBorder(BorderFactory.createEmptyBorder(16, 18, 10, 18));
	
	        JLabel lblMsg = new JLabel("<html><center>" + mensaje + "</center></html>");
	        lblMsg.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 14));
	        content.add(lblMsg, BorderLayout.CENTER);
	
	        dlg.add(content, BorderLayout.CENTER);
	
	        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
	        footer.setBackground(Color.WHITE);
	
	        RoundedButton btnOk = new RoundedButton("Aceptar");
	        btnOk.setBackground(COLOR_ACCENT);
	        btnOk.setForeground(Color.WHITE);
	        btnOk.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 14));
	        btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
	        btnOk.addActionListener(e -> dlg.dispose());
	
	        footer.add(btnOk);
	        dlg.add(footer, BorderLayout.SOUTH);
	
	        dlg.setVisible(true);
	    }
    
	    private String mostrarDialogoInput(String titulo, String mensaje) {
	
	        JDialog dlg = new JDialog(this, titulo, true);
	        dlg.setSize(400, 190);
	        dlg.setLocationRelativeTo(this);
	        dlg.setLayout(new BorderLayout());
	        dlg.getContentPane().setBackground(Color.WHITE);
	
	        JPanel content = new JPanel(new BorderLayout(0, 10));
	        content.setBackground(Color.WHITE);
	        content.setBorder(BorderFactory.createEmptyBorder(16, 18, 10, 18));
	
	        JLabel lblMsg = new JLabel(mensaje, SwingConstants.CENTER);
	        lblMsg.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 14));
	        content.add(lblMsg, BorderLayout.NORTH);
	
	        RoundedTextField txtInput = new RoundedTextField(12);
	        txtInput.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 14));
	        txtInput.setPreferredSize(new Dimension(260, 38));
	        txtInput.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
	        txtInput.setCaretColor(COLOR_ACCENT);


	        content.add(txtInput, BorderLayout.CENTER);

	
	        dlg.add(content, BorderLayout.CENTER);
	
	        final String[] resultado = new String[1];
	
	        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
	        footer.setBackground(Color.WHITE);
	
	        RoundedButton btnAceptar = new RoundedButton("Aceptar");
	        btnAceptar.setBackground(COLOR_ACCENT);
	        btnAceptar.setForeground(Color.WHITE);
	        btnAceptar.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 14));
	        btnAceptar.setCursor(new Cursor(Cursor.HAND_CURSOR));
	        btnAceptar.addActionListener(e -> {
	            resultado[0] = txtInput.getText();
	            dlg.dispose();
	        });
	
	        RoundedButton btnCancelar = new RoundedButton("Cancelar");
	        btnCancelar.setBackground(new Color(180, 180, 180));
	        btnCancelar.setForeground(Color.WHITE);
	        btnCancelar.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 14));
	        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
	        btnCancelar.addActionListener(e -> {
	            resultado[0] = null;
	            dlg.dispose();
	        });
	
	        footer.add(btnAceptar);
	        footer.add(btnCancelar);
	
	        dlg.add(footer, BorderLayout.SOUTH);
	        dlg.setVisible(true);
	
	        return resultado[0];
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

	            g2.setColor(new Color(200, 200, 200));
	            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

	            super.paintComponent(g);
	            g2.dispose();
	        }
	    }


}

