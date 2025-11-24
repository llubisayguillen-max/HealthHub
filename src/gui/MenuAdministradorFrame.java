package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import bll.Administrador;
import dll.ControllerAdministrador;
import javax.swing.JFrame;

public class MenuAdministradorFrame extends JFrame {

    private Administrador admin;
    private ControllerAdministrador controller;

    public MenuAdministradorFrame(ControllerAdministrador controller, Administrador admin) {
        this.admin = admin;
        this.controller = controller;

        setTitle("HealthHub - Panel del Administrador");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
    }

    public MenuAdministradorFrame() {
        setTitle("Menú Administrador");
        setSize(400,300);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // barra superior
        
        JPanel topBar = new JPanel();
        topBar.setBackground(new Color(0, 102, 204));
        topBar.setPreferredSize(new Dimension(900, 60));
        topBar.setLayout(new BorderLayout());

        JLabel lblTitulo = new JLabel("Panel del Administrador", SwingConstants.CENTER);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));

        topBar.add(lblTitulo, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);

        //panel paciente/medico
        JPanel center = new JPanel(new GridLayout(1, 2));
        center.setBackground(Color.WHITE);

        center.add(crearPanelSeccion("PACIENTES", new String[]{
                "Alta de Paciente",
                "Modificar Paciente",
        }, "PAC"));

        center.add(crearPanelSeccion("MÉDICOS", new String[]{
                "Alta de Médico",
                "Modificar Médico",
        	}, "MED"));


        add(center, BorderLayout.CENTER);

        //separadpr
        add(crearPanelFuncionesGenerales(), BorderLayout.SOUTH);
    }

    private JPanel crearPanelSeccion(String titulo, String[] opciones, String tipo) {

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(titulo, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setForeground(new Color(0, 102, 204));
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(20));

        for (String op : opciones) {
            JButton btn = crearBotonSeccion(op);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Alta paciente
            if (tipo.equals("PAC") && op.contains("Alta")) {
                btn.addActionListener(e -> new AltaPacienteFrame(controller, admin).setVisible(true));
            }

            // Modificar paciente
            if (tipo.equals("PAC") && op.contains("Modificar")) {
                btn.addActionListener(e -> {
                    String usuario = JOptionPane.showInputDialog(
                            this,
                            "Ingrese el usuario del paciente a modificar:",
                            "Modificar Paciente",
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (usuario == null || usuario.trim().isEmpty())
                        return;

                    new ModificarPacienteFrame(controller, admin, usuario.trim()).setVisible(true);
                });
            }

            // Alta médico
            if (tipo.equals("MED") && op.contains("Alta")) {
                btn.addActionListener(e -> new AltaMedicoFrame(controller, admin).setVisible(true));
            }

            // Modificar médico 
            if (tipo.equals("MED") && op.contains("Modificar")) {
            	btn.addActionListener(e -> {
            	    String usuario = JOptionPane.showInputDialog(
            	            this,
            	            "Ingrese el usuario del médico a modificar:",
            	            "Modificar Médico",
            	            JOptionPane.QUESTION_MESSAGE
            	    );

            	    if (usuario == null || usuario.trim().isEmpty()) return;

            	    new ModificarMedicoFrame(controller, usuario.trim()).setVisible(true);
            	});

            }

            panel.add(btn);
            panel.add(Box.createVerticalStrut(10));
        }
        

        return panel;
    }

    //funciones de usr
    
    private JPanel crearPanelFuncionesGenerales() {

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));

        JLabel titulo = new JLabel("FUNCIONES GENERALES", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(new Color(0, 102, 204));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sepTop = new JSeparator();
        sepTop.setForeground(new Color(0, 102, 204));
        sepTop.setMaximumSize(new Dimension(800, 2));

        JSeparator sepBottom = new JSeparator();
        sepBottom.setForeground(new Color(0, 102, 204));
        sepBottom.setMaximumSize(new Dimension(800, 2));

        //botones
        JPanel grid = new JPanel(new GridLayout(2, 3, 15, 15));
        grid.setBackground(Color.WHITE);
        grid.setBorder(BorderFactory.createEmptyBorder(15, 150, 15, 150));

        JButton btnListar = crearBotonGeneral("Listar Usuarios");
        btnListar.addActionListener(e -> 
            new ListarUsuariosFrame(controller, admin).setVisible(true)
        );
        grid.add(btnListar);

        
        grid.add(crearBotonGeneral("Resetear contraseña"));
        grid.add(crearBotonGeneral("Cerrar Sesión"));
        grid.add(crearBotonGeneral("Bloquear Usuario"));
        grid.add(crearBotonGeneral("Desbloquear Usuario"));
        grid.add(crearBotonGeneral("Eliminar Usuario"));

        wrapper.add(Box.createVerticalStrut(10));
        wrapper.add(titulo);
        wrapper.add(Box.createVerticalStrut(5));
        wrapper.add(sepTop);
        wrapper.add(Box.createVerticalStrut(15));
        wrapper.add(grid);
        wrapper.add(Box.createVerticalStrut(10));
        wrapper.add(sepBottom);

        return wrapper;
    }

    private JButton crearBotonSeccion(String texto) {
        RoundedButton btn = new RoundedButton(texto);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setBackground(new Color(230, 230, 230));
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(250, 40));
        btn.setMaximumSize(new Dimension(250, 40));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }



    private JButton crearBotonGeneral(String texto) { 
    	RoundedButton btn = new RoundedButton(texto); 
    	btn.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
    	btn.setBackground(new Color(0, 102, 204)); 
    	btn.setForeground(Color.WHITE); 
    	btn.setFocusPainted(false); 
    	btn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
    	btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
    	return btn; }

}
