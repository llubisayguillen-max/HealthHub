package gui;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.sql.Time;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.time.format.DateTimeFormatter;

import bll.Medico;
import dll.ControllerMedico;
import com.toedter.calendar.JDateChooser;

public class DispoMedicoFrame extends JFrame {

    private final Medico medico;
    private final ControllerMedico controllerMedico;

    private JDateChooser dcFecha;
    private JFormattedTextField txtHoraInicio;
    private JFormattedTextField txtHoraFin;

    private static final DateTimeFormatter F_DDMMYYYY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DispoMedicoFrame(Medico medico) {
        this.medico = medico;
        this.controllerMedico = new ControllerMedico(medico);

        setTitle("Health Hub - Disponibilidad del Médico");
        setSize(900, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // encabezado
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 153));
        header.setPreferredSize(new Dimension(getWidth(), 70));

        JLabel lbl = new JLabel("Gestión disponibilidad", SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(lbl, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        // formulario
        JPanel center = new JPanel();
        center.setBackground(Color.WHITE);
        center.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Fecha
        gbc.gridx = 0; gbc.gridy = row;
        center.add(new JLabel("Fecha:"), gbc);

        gbc.gridx = 1;
        dcFecha = new JDateChooser();
        dcFecha.setDateFormatString("dd/MM/yyyy");
        dcFecha.setPreferredSize(new Dimension(140, 25));
        dcFecha.setDate(new Date()); 
        center.add(dcFecha, gbc);

        row++;

        // Hora inicio
        gbc.gridx = 0; gbc.gridy = row;
        center.add(new JLabel("Hora inicio (hh:mm):"), gbc);

        gbc.gridx = 1;
        txtHoraInicio = createMaskField("##:##");
        center.add(txtHoraInicio, gbc);

        row++;

        // Hora fin
        gbc.gridx = 0; gbc.gridy = row;
        center.add(new JLabel("Hora fin (hh:mm):"), gbc);

        gbc.gridx = 1;
        txtHoraFin = createMaskField("##:##");
        center.add(txtHoraFin, gbc);

        row++;

        // registrar
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JButton btnRegistrar = new JButton("Registrar disponibilidad");
        btnRegistrar.setBackground(new Color(0, 120, 215));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.addActionListener(e -> registrarDisponibilidad());
        center.add(btnRegistrar, gbc);

        add(center, BorderLayout.CENTER);

        // Volver y Cerrar sesión
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
    }

    private JFormattedTextField createMaskField(String mask) {
        try {
            MaskFormatter mf = new MaskFormatter(mask);
            mf.setPlaceholderCharacter('_');
            return new JFormattedTextField(mf);
        } catch (ParseException e) {
            return new JFormattedTextField();
        }
    }

    private void registrarDisponibilidad() {
        try {
            Date fechaSeleccionada = dcFecha.getDate();
            String hiStr = txtHoraInicio.getText().trim();
            String hfStr = txtHoraFin.getText().trim();

            if (fechaSeleccionada == null ||
                hiStr.contains("_") || hfStr.contains("_")) {
                JOptionPane.showMessageDialog(this, "Complete todos los campos correctamente");
                return;
            }

            LocalDate fecha = fechaSeleccionada.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            LocalTime hi = LocalTime.parse(hiStr);
            LocalTime hf = LocalTime.parse(hfStr);

            if (!hf.isAfter(hi)) {
                JOptionPane.showMessageDialog(this, "La hora fin debe ser mayor que la hora inicio");
                return;
            }

            java.sql.Date sqlFecha = java.sql.Date.valueOf(fecha);
            Time sqlHi = Time.valueOf(hi);
            Time sqlHf = Time.valueOf(hf);

            controllerMedico.registrarDisponibilidad(sqlFecha, sqlHi, sqlHf);

            JOptionPane.showMessageDialog(this, "Disponibilidad registrada correctamente");
            dcFecha.setDate(new Date());
            txtHoraInicio.setValue(null);
            txtHoraFin.setValue(null);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
