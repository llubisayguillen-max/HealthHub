package gui;

import javax.swing.*;
import java.awt.*;
import bll.Paciente;

public class DisponibilidadMedicoPacienteFrame extends JFrame {

    private final Paciente paciente;

    public DisponibilidadMedicoPacienteFrame(Paciente paciente) {
        this.paciente = paciente;

        setTitle("Disponibilidad de Médicos - Health Hub");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Ejemplo de contenido temporal (después lo reemplazamos por lo real)
        JLabel lbl = new JLabel("Aquí se mostrará la disponibilidad de médicos.", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        add(lbl, BorderLayout.CENTER);

        setVisible(true);
    }
}

