package gui;

import javax.swing.*;
import java.awt.*;
import bll.Paciente;

public class FavoritosPacienteFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private final Paciente paciente;

	public FavoritosPacienteFrame(Paciente paciente) {
		this.paciente = paciente;

		setTitle("Favoritos y Recomendaciones - Health Hub");
		setSize(800, 500);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		setLayout(new BorderLayout());

		// Botón volver
		JButton btnVolver = new JButton("Volver al menú");
		btnVolver.setBackground(new Color(100, 100, 100));
		btnVolver.setForeground(Color.WHITE);
		btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 14));
		btnVolver.setFocusPainted(false);
		btnVolver.setBorder(BorderFactory.createEmptyBorder());
		btnVolver.addActionListener(e -> {
			new MenuPacienteFrame(paciente).setVisible(true);
			dispose();
		});

		setVisible(true);
	}
}
