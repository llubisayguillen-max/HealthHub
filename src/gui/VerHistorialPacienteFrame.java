package gui;

import bll.HistorialMedico;
import bll.Paciente;
import dll.ControllerHistorial;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VerHistorialPacienteFrame extends JFrame {

    private final Paciente paciente;
    private final ControllerHistorial historialManager;
    private JTable tablaHistorial;
    private DefaultTableModel modeloTabla;

    public VerHistorialPacienteFrame(Paciente paciente, ControllerHistorial historialManager) {
        this.paciente = paciente;
        this.historialManager = historialManager;

        setTitle("Historial Médico de: " + paciente.getNombre());
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        cargarHistorial();
    }

    private void initUI() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modeloTabla = new DefaultTableModel(
                new Object[]{"Fecha", "Observaciones", "Médico"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaHistorial = new JTable(modeloTabla);
        tablaHistorial.setFillsViewportHeight(true);

        JScrollPane scroll = new JScrollPane(tablaHistorial);
        panelPrincipal.add(scroll, BorderLayout.CENTER);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.add(btnCerrar);
        panelPrincipal.add(panelBoton, BorderLayout.SOUTH);

        setContentPane(panelPrincipal);
    }

    private void cargarHistorial() {
        modeloTabla.setRowCount(0);

        List<HistorialMedico> registros =
                historialManager.listarPorPaciente(paciente.getUsuario());

        if (registros == null || registros.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay registros de historial para este paciente.");
            return;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (HistorialMedico h : registros) {
            modeloTabla.addRow(new Object[]{
                    h.getFechaCreacion().format(fmt),
                    h.getObservaciones(),
                    h.getMedico() != null ? h.getMedico().getNombre() : "N/A"
            });
        }
    }
}
