package gui;

import bll.Paciente;
import bll.Medico;
import dll.ControllerHistorial;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static gui.UiPaleta.*;

public class CrearRegistroHistorialFrame extends JFrame {

    private final ControllerHistorial manager;

    public CrearRegistroHistorialFrame(List<Paciente> pacientes, List<Medico> medicos, ControllerHistorial manager) {
        if (manager == null) throw new IllegalArgumentException("HistorialMedicoManager no puede ser null");
        this.manager = manager;

        setTitle("Nuevo Registro de Historial Médico");
        setSize(550, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // PANEL CENTRAL

        JPanel panelCentro = new JPanel(new GridLayout(3, 2, 10, 10));
        panelCentro.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JComboBox<Paciente> cmbPac = new JComboBox<>(pacientes.toArray(new Paciente[0]));
        JComboBox<Medico> cmbMed = new JComboBox<>(medicos.toArray(new Medico[0]));
        JTextArea txtDesc = new JTextArea(5, 20);
        JScrollPane scroll = new JScrollPane(txtDesc);

        panelCentro.add(new JLabel("Paciente:"));
        panelCentro.add(cmbPac);

        panelCentro.add(new JLabel("Médico:"));
        panelCentro.add(cmbMed);

        panelCentro.add(new JLabel("Descripción:"));
        panelCentro.add(scroll);

        add(panelCentro, BorderLayout.CENTER);

        // BOTONES

        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnGuardar = new JButton("Guardar");
        JButton btnVolver = new JButton("Volver");

        panelBotones.add(btnGuardar);
        panelBotones.add(btnVolver);

        add(panelBotones, BorderLayout.SOUTH);


        // ACCIONES

        btnGuardar.addActionListener(e -> guardarRegistro(cmbPac, cmbMed, txtDesc));
        btnVolver.addActionListener(e -> dispose());

        setVisible(true);
    }

    private void guardarRegistro(JComboBox<Paciente> cmbPac, JComboBox<Medico> cmbMed, JTextArea txtDesc) {
        Paciente paciente = (Paciente) cmbPac.getSelectedItem();
        Medico medico = (Medico) cmbMed.getSelectedItem();
        String descripcion = txtDesc.getText().trim();

        if (paciente == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un paciente.");
            return;
        }
        if (medico == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un médico.");
            return;
        }
        if (descripcion.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La descripción no puede estar vacía.");
            return;
        }

        try {
            boolean ok = manager.crearRegistro(paciente, medico, descripcion);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Registro agregado con éxito.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo guardar el registro.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar registro: " + ex.getMessage());
        }
    }
}
