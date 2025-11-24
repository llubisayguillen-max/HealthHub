package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

import bll.Administrador;
import dll.ControllerAdministrador;

public class ListarUsuariosFrame extends JFrame {

    private ControllerAdministrador controller;
    private Administrador admin;

    private JTable tablaPacientes;
    private JTable tablaMedicos;

    private DefaultTableModel modeloPacientes;
    private DefaultTableModel modeloMedicos;

    public ListarUsuariosFrame(ControllerAdministrador controller, Administrador admin) {
        this.controller = controller;
        this.admin = admin;

        setTitle("HealthHub - Lista de Usuarios");
        setSize(950, 650);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        cargarListas();
    }

    private void initUI() {

        Color azul = new Color(0, 102, 204);
        Color celeste = new Color(225, 240, 255);

        setLayout(new BorderLayout());

        // --- Barra superior ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(azul);
        topBar.setPreferredSize(new Dimension(950, 70));

        JLabel lblTitulo = new JLabel("Listado de Usuarios", SwingConstants.CENTER);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        topBar.add(lblTitulo, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);

        // --- Panel central ---
        JPanel center = new JPanel(new GridLayout(1, 2, 20, 0));
        center.setBackground(celeste);
        center.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        center.add(crearCardUsuarios("PACIENTES", true));
        center.add(crearCardUsuarios("MÉDICOS", false));

        add(center, BorderLayout.CENTER);

        // --- BOTÓN VOLVER ---
        JButton btnVolver = new RoundedButton("Volver al Menú");
        btnVolver.setBackground(azul);
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnVolver.addActionListener(e -> {
            dispose();
            new MenuAdministradorFrame(controller, admin).setVisible(true);
        });

        JPanel bottom = new JPanel();
        bottom.setBackground(Color.WHITE);
        bottom.add(btnVolver);

        add(bottom, BorderLayout.SOUTH);
    }


    private JPanel crearCardUsuarios(String titulo, boolean esPaciente) {

        Color borde = new Color(180, 200, 220);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borde, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lbl = new JLabel(titulo, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(240, 240, 240));
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        card.add(lbl, BorderLayout.NORTH);

        // Tabla
        if (esPaciente) {
            modeloPacientes = new DefaultTableModel(new String[]{"Usuario", "Nombre"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            tablaPacientes = new JTable(modeloPacientes);
            estilizarTabla(tablaPacientes);
            JScrollPane scroll = new JScrollPane(tablaPacientes);
            card.add(scroll, BorderLayout.CENTER);

        } else {
            modeloMedicos = new DefaultTableModel(new String[]{"Usuario", "Nombre"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            tablaMedicos = new JTable(modeloMedicos);
            estilizarTabla(tablaMedicos);
            JScrollPane scroll = new JScrollPane(tablaMedicos);
            card.add(scroll, BorderLayout.CENTER);
        }

        return card;
    }


    private void estilizarTabla(JTable tabla) {
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tabla.setRowHeight(28);
        tabla.setGridColor(new Color(220, 220, 220));
        tabla.setShowVerticalLines(false);

        JTableHeader header = tabla.getTableHeader();
        header.setBackground(new Color(230, 230, 230));
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setForeground(new Color(50, 50, 50));
    }


    private void cargarListas() {

        List<String> pacientes = controller.listarUsuariosPorRol("Paciente");
        List<String> medicos = controller.listarUsuariosPorRol("Medico");


        if (pacientes.isEmpty()) {
            modeloPacientes.addRow(new Object[]{"-", "No hay pacientes registrados"});
        } else {
            for (String p : pacientes) {
                String[] partes = p.split(" - ", 2);
                String user = partes.length >= 1 ? partes[0] : "";
                String nombre = partes.length == 2 ? partes[1] : "";
                modeloPacientes.addRow(new Object[]{user, nombre});
            }
        }


        if (medicos.isEmpty()) {
            modeloMedicos.addRow(new Object[]{"-", "No hay médicos registrados"});
        } else {
            for (String m : medicos) {
                String[] partes = m.split(" - ", 2);
                String user = partes.length >= 1 ? partes[0] : "";
                String nombre = partes.length == 2 ? partes[1] : "";
                modeloMedicos.addRow(new Object[]{user, nombre});
            }
        }
    }
}



