package gui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import bll.Medico;
import dll.ControllerMedico;
import com.toedter.calendar.JDateChooser;

public class TurnosMedicoFrame extends JFrame {

    private final Medico medico;
    private final ControllerMedico controllerMedico;

    private JTextField txtIdTurno;
    private JDateChooser dcFechaNueva;
    private JTextField txtHoraNueva;

    private static final DateTimeFormatter F_DDMMYYYY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public TurnosMedicoFrame(Medico medico) {
        this.medico = medico;
        this.controllerMedico = new ControllerMedico(medico);

        setTitle("Health Hub - Gestión de Turnos");
        setSize(900, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // encabezado
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 153));
        header.setPreferredSize(new Dimension(getWidth(), 70));

        JLabel lbl = new JLabel("Gestión de Turnos", SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(lbl, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        // Centro
        JPanel center = new JPanel();
        center.setBackground(Color.WHITE);
        center.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // ID turno
        gbc.gridx = 0; gbc.gridy = row;
        center.add(new JLabel("ID Turno:"), gbc);
        gbc.gridx = 1;
        txtIdTurno = new JTextField(10);
        center.add(txtIdTurno, gbc);
        row++;

        // Nueva fecha
        gbc.gridx = 0; gbc.gridy = row;
        center.add(new JLabel("Nueva fecha:"), gbc);
        gbc.gridx = 1;
        dcFechaNueva = new JDateChooser();
        dcFechaNueva.setDateFormatString("dd/MM/yyyy");
        dcFechaNueva.setPreferredSize(new Dimension(140, 25));
        dcFechaNueva.setDate(new Date()); 
        center.add(dcFechaNueva, gbc);
        row++;

        // Nueva hora
        gbc.gridx = 0; gbc.gridy = row;
        center.add(new JLabel("Nueva hora (hh:mm):"), gbc);
        gbc.gridx = 1;
        txtHoraNueva = new JTextField(10);
        center.add(txtHoraNueva, gbc);
        row++;

        // Botones acciones
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel panelBtns = new JPanel(new FlowLayout());
        JButton btnConfirmar = new JButton("Confirmar asistencia");
        JButton btnCancelar = new JButton("Cancelar turno");
        JButton btnReprogramar = new JButton("Reprogramar");

        panelBtns.add(btnConfirmar);
        panelBtns.add(btnCancelar);
        panelBtns.add(btnReprogramar);

        center.add(panelBtns, gbc);

        add(center, BorderLayout.CENTER);

        // Eventos
        btnConfirmar.addActionListener(e -> confirmar());
        btnCancelar.addActionListener(e -> cancelar());
        btnReprogramar.addActionListener(e -> reprogramar());

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

    private Long leerIdTurno() {
        String s = txtIdTurno.getText().trim();
        if (!s.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Ingrese un ID de turno válido (solo números)");
            return null;
        }
        return Long.parseLong(s);
    }

    private void confirmar() {
        try {
            Long id = leerIdTurno();
            if (id == null) return;

            controllerMedico.confirmarAsistencia(id);
            JOptionPane.showMessageDialog(this, "Asistencia confirmada");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelar() {
        try {
            Long id = leerIdTurno();
            if (id == null) return;

            controllerMedico.cancelarTurno(id);
            JOptionPane.showMessageDialog(this, "Turno cancelado");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reprogramar() {
        try {
            Long id = leerIdTurno();
            if (id == null) return;

            Date fechaSeleccionada = dcFechaNueva.getDate();
            String hStr = txtHoraNueva.getText().trim();

            if (fechaSeleccionada == null || hStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese nueva fecha y hora");
                return;
            }

            LocalDate fecha = fechaSeleccionada.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalTime hora = LocalTime.parse(hStr);

            Date nueva = Date.from(
                    fecha.atTime(hora).atZone(ZoneId.systemDefault()).toInstant()
            );

            controllerMedico.reprogramarTurno(id, nueva);
            JOptionPane.showMessageDialog(this, "Turno reprogramado");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
