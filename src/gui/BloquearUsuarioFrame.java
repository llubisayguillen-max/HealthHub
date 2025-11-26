package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import dll.ControllerAdministrador;

public class BloquearUsuarioFrame extends JFrame {

    private JTextField txtUsuario;
    private JButton btnBloquear;
    private JButton btnCancelar;

    private ControllerAdministrador ca; // referencia al controlador

    public BloquearUsuarioFrame(ControllerAdministrador ca) {
        this.ca = ca;

        setTitle("Bloquear Usuario");
        setSize(450, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        RoundedCardPanel panel = new RoundedCardPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ==== TÍTULO ====
        JLabel lblTitulo = new JLabel("Bloquear Usuario", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(60, 60, 60));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);

        // ==== CAMPO USUARIO ====
        gbc.gridwidth = 1;

        JLabel lblUsuario = new JLabel("Usuario a bloquear:");
        lblUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(lblUsuario, gbc);

        txtUsuario = new JTextField();
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridx = 1;
        panel.add(txtUsuario, gbc);

        // ==== BOTONES ====
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);

        btnBloquear = new JButton("Bloquear");
        btnBloquear.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnBloquear.setPreferredSize(new Dimension(130, 40));

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnCancelar.setPreferredSize(new Dimension(130, 40));

        btnPanel.add(btnBloquear);
        btnPanel.add(btnCancelar);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        add(panel, BorderLayout.CENTER);

        // ==== ACCIONES ====

        // Bloquear usuario
        btnBloquear.addActionListener(e -> bloquearUsuario());

        // Cerrar Frame
        btnCancelar.addActionListener(e -> dispose());
    }

    private void bloquearUsuario() {
        String usr = txtUsuario.getText().trim();

        if (usr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar un nombre de usuario.", 
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ca.bloquearUsuario(usr);

        JOptionPane.showMessageDialog(this, 
                "Usuario bloqueado exitosamente.",
                "Éxito", 
                JOptionPane.INFORMATION_MESSAGE);

        dispose();
    }
}

