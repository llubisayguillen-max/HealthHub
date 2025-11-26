package gui;

import javax.swing.*;
import java.awt.*;

import dll.ControllerAdministrador;
import bll.Administrador;

public class ResetearContraseniaFrame extends JFrame {

    private ControllerAdministrador controller;
    private Administrador admin;

    private JTextField txtUsuario;
    private JPasswordField txtNuevaPass;
    private JCheckBox chkMostrarPass;

    public ResetearContraseniaFrame(ControllerAdministrador controller, Administrador admin) {
        this.controller = controller;
        this.admin = admin;

        setTitle("HealthHub - Resetear Contraseña");
        setSize(450, 380);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {

        Color azul = new Color(0, 102, 204);

        setLayout(new BorderLayout());

        // --- Barra superior ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(azul);
        topBar.setPreferredSize(new Dimension(450, 60));

        JLabel lblTitulo = new JLabel("Resetear Contraseña", SwingConstants.CENTER);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        topBar.add(lblTitulo, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);

        // --- Panel central con BoxLayout ---
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        txtUsuario = new JTextField();
        txtUsuario.setBorder(BorderFactory.createTitledBorder("Usuario"));
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        txtNuevaPass = new JPasswordField();
        txtNuevaPass.setBorder(BorderFactory.createTitledBorder("Nueva Contraseña"));
        txtNuevaPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNuevaPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        chkMostrarPass = new JCheckBox("Mostrar contraseña");
        chkMostrarPass.setBackground(Color.WHITE);
        chkMostrarPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkMostrarPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        chkMostrarPass.addActionListener(e -> {
            if (chkMostrarPass.isSelected()) {
                txtNuevaPass.setEchoChar((char) 0);
            } else {
                txtNuevaPass.setEchoChar('\u2022');
            }
        });

        RoundedButton btnResetear = new RoundedButton("Resetear");
        btnResetear.setBackground(azul);
        btnResetear.setForeground(Color.WHITE);
        btnResetear.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnResetear.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnResetear.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnResetear.setMaximumSize(new Dimension(200, 45)); 

        btnResetear.addActionListener(e -> resetearPassword());


        // Agregar elementos con separación
        card.add(txtUsuario);
        card.add(Box.createVerticalStrut(15));
        card.add(txtNuevaPass);
        card.add(Box.createVerticalStrut(10));
        card.add(chkMostrarPass);
        card.add(Box.createVerticalStrut(20));
        card.add(btnResetear);

        add(card, BorderLayout.CENTER);

        // --- BOTÓN VOLVER ---
        JButton btnVolver = new RoundedButton("Volver al Menú");
        btnVolver.setBackground(azul);
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnVolver.addActionListener(e -> {
            dispose();
            new MenuAdministradorFrame(controller, admin).setVisible(true);
        });

        JPanel bottom = new JPanel();
        bottom.setBackground(Color.WHITE);
        bottom.add(btnVolver);

        add(bottom, BorderLayout.SOUTH);
    }

    private void resetearPassword() {
        String usr = txtUsuario.getText().trim();
        String nuevaPass = new String(txtNuevaPass.getPassword()).trim();
        controller.resetearContrasenia(usr, nuevaPass);
        JOptionPane.showMessageDialog(this, "Contraseña reseteada exitosamente.");
    }
}
