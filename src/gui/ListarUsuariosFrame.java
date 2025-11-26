package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

import bll.Administrador;
import dll.ControllerAdministrador;

import static gui.UiPaleta.*;

public class ListarUsuariosFrame extends JFrame {

    private ControllerAdministrador controller;
    private Administrador admin;

    private JTable tablaPacientes;
    private JTable tablaMedicos;
    private DefaultTableModel modeloPacientes;
    private DefaultTableModel modeloMedicos;

    private static final String UI_FONT = "Segoe UI";

    public ListarUsuariosFrame(ControllerAdministrador controller, Administrador admin) {
        this.controller = controller;
        this.admin = admin;

        setTitle("HealthHub - Lista de Usuarios");
        setSize(980, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        cargarListas();
    }

    private void initUI() {

        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        //header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COLOR_PRIMARY);
        topBar.setPreferredSize(new Dimension(getWidth(), 85));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 40, 15, 40));

        JLabel lblTitulo = new JLabel("Listado de Usuarios");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font(UI_FONT, Font.BOLD, 26));

        topBar.add(lblTitulo, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        //cards
        JPanel wrapperCenter = new JPanel(new BorderLayout());
        wrapperCenter.setOpaque(false);
        wrapperCenter.setBorder(BorderFactory.createEmptyBorder(10, 32, 10, 32));

        JPanel center = new JPanel(new GridLayout(1, 2, 22, 0));
        center.setOpaque(false);

        center.add(crearCardTabla("Pacientes Registrados", true));
        center.add(crearCardTabla("Médicos Registrados", false));

        wrapperCenter.add(center, BorderLayout.CENTER);
        add(wrapperCenter, BorderLayout.CENTER);

        //footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(COLOR_BACKGROUND);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 40, 20, 40));

        RoundedButton btnVolver = new RoundedButton("Volver al Menú");
        btnVolver.setBackground(COLOR_ACCENT);
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFont(new Font(UI_FONT, Font.PLAIN, 13));
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        btnVolver.addActionListener(e -> {
            dispose();
            new MenuAdministradorFrame(controller, admin).setVisible(true);
        });

        JPanel rightFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightFooter.setOpaque(false);
        rightFooter.add(btnVolver);

        footer.add(rightFooter, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    //tabla
    private JPanel crearCardTabla(String titulo, boolean esPaciente) {

        RoundedCardPanel card = new RoundedCardPanel(16);
        card.setBackground(COLOR_CARD_BG);
        card.setBorderColor(COLOR_CARD_BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font(UI_FONT, Font.BOLD, 17));
        lblTitulo.setForeground(MINT_DARK);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(8, 0, 15, 0));

        card.add(lblTitulo, BorderLayout.NORTH);

        if (esPaciente) {
            modeloPacientes = new DefaultTableModel(new String[] {"Usuario", "Nombre"}, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            tablaPacientes = new JTable(modeloPacientes);
            estilizarTabla(tablaPacientes);
            card.add(new JScrollPane(tablaPacientes), BorderLayout.CENTER);

        } else {
            modeloMedicos = new DefaultTableModel(new String[] {"Usuario", "Nombre"}, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            tablaMedicos = new JTable(modeloMedicos);
            estilizarTabla(tablaMedicos);
            card.add(new JScrollPane(tablaMedicos), BorderLayout.CENTER);
        }

        return card;
    }

    private void estilizarTabla(JTable tabla) {
        tabla.setFont(new Font(UI_FONT, Font.PLAIN, 15));
        tabla.setRowHeight(30);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(220, 220, 220));

        JTableHeader header = tabla.getTableHeader();
        header.setBackground(COLOR_ACCENT);
        header.setForeground(Color.WHITE);
        header.setFont(new Font(UI_FONT, Font.BOLD, 15));
        header.setOpaque(true);

        tabla.setSelectionBackground(MINT_DARK);
        tabla.setSelectionForeground(Color.WHITE);
    }

    private void cargarListas() {

        List<String> pacientes = controller.listarUsuariosPorRol("Paciente");
        List<String> medicos = controller.listarUsuariosPorRol("Medico");

        if (pacientes.isEmpty()) {
            modeloPacientes.addRow(new Object[]{"-", "No hay pacientes registrados"});
        } else {
            for (String p : pacientes) {
                String[] partes = p.split(" - ", 2);
                modeloPacientes.addRow(new Object[]{
                        partes[0],
                        partes.length > 1 ? partes[1] : ""
                });
            }
        }

        if (medicos.isEmpty()) {
            modeloMedicos.addRow(new Object[]{"-", "No hay médicos registrados"});
        } else {
            for (String m : medicos) {
                String[] partes = m.split(" - ", 2);
                modeloMedicos.addRow(new Object[]{
                        partes[0],
                        partes.length > 1 ? partes[1] : ""
                });
            }
        }
    }
}
