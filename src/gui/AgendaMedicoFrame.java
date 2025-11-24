package gui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import bll.Medico;
import dll.ControllerMedico;
import com.toedter.calendar.JDateChooser;

public class AgendaMedicoFrame extends JFrame {

    private final Medico medico;
    private final ControllerMedico controllerMedico;

    private JDateChooser dcDesde;
    private JDateChooser dcHasta;
    private JTextArea txtResultado;

    public AgendaMedicoFrame(Medico medico) {
        this.medico = medico;
        this.controllerMedico = new ControllerMedico(medico);

        setTitle("Health Hub - Agenda del Médico");
        setSize(900, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // encabezado
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 153));
        header.setPreferredSize(new Dimension(getWidth(), 70));

        JLabel lbl = new JLabel("Agenda del Médico", SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(lbl, BorderLayout.CENTER);

        // filtro fechas
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filtros.setBackground(Color.WHITE);

        filtros.add(new JLabel("Desde:"));
        dcDesde = new JDateChooser();
        dcDesde.setDateFormatString("dd/MM/yyyy");
        dcDesde.setPreferredSize(new Dimension(130, 25));
        filtros.add(dcDesde);

        filtros.add(new JLabel("Hasta:"));
        dcHasta = new JDateChooser();
        dcHasta.setDateFormatString("dd/MM/yyyy");
        dcHasta.setPreferredSize(new Dimension(130, 25));
        filtros.add(dcHasta);

        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setBackground(new Color(0, 120, 215));
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFocusPainted(false);
        filtros.add(btnBuscar);

        JPanel panelNorth = new JPanel(new BorderLayout());
        panelNorth.add(header, BorderLayout.NORTH);
        panelNorth.add(filtros, BorderLayout.SOUTH);

        add(panelNorth, BorderLayout.NORTH);

        // resultados
        txtResultado = new JTextArea();
        txtResultado.setEditable(false);
        txtResultado.setFont(new Font("Consolas", Font.PLAIN, 13));

        add(new JScrollPane(txtResultado), BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> buscarAgenda());

        // footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        footer.setBackground(Color.WHITE);

        JButton btnVolver = new JButton("Volver al menú");
        JButton btnCerrar = new JButton("Cerrar sesión");

        footer.add(btnVolver);
        footer.add(btnCerrar);

        add(footer, BorderLayout.SOUTH);

        btnVolver.addActionListener(e -> {
            new MenuMedicoFrame(medico).setVisible(true);
            dispose();
        });

        btnCerrar.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        Date hoy = new Date();
        dcDesde.setDate(hoy);
        dcHasta.setDate(hoy);
    }

    private void buscarAgenda() {
        try {
            Date d1 = dcDesde.getDate();
            Date d2 = dcHasta.getDate();

            if (d1 == null || d2 == null) {
                JOptionPane.showMessageDialog(this, "Seleccione ambas fechas");
                return;
            }

            LocalDate desde = d1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate hasta = d2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (hasta.isBefore(desde)) {
                JOptionPane.showMessageDialog(this,
                        "La fecha 'Hasta' no puede ser anterior a 'Desde'");
                return;
            }

            java.sql.Date sqlDesde = java.sql.Date.valueOf(desde);
            java.sql.Date sqlHasta = java.sql.Date.valueOf(hasta);

            List<String> filas = controllerMedico.visualizarAgenda(sqlDesde, sqlHasta, true);

            txtResultado.setText("");
            if (filas.isEmpty()) {
                txtResultado.setText("No hay turnos en ese rango");
            } else {
                for (String f : filas) {
                    txtResultado.append(f + "\n");
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
