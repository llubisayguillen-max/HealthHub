package gui;

import javax.swing.*;
import java.awt.*;
import bll.Paciente;

public class MenuPacienteFrame extends JFrame {
	private Paciente pacienteLogueado;

	public MenuPacienteFrame(Paciente pacienteLogueado) {
		this.pacienteLogueado = pacienteLogueado;

		setTitle("Menú del Paciente");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setMinimumSize(new Dimension(900, 600));
		setLocationRelativeTo(null);

		// Panel principal
		JPanel panelPrincipal = new JPanel(new BorderLayout());
		panelPrincipal.setBackground(Color.WHITE);

		JPanel panelEncabezado = new JPanel();
		panelEncabezado.setBackground(new Color(91, 155, 213));
		panelEncabezado.setPreferredSize(new Dimension(0, 80));
		panelEncabezado.setLayout(new BorderLayout());
		panelEncabezado.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

		JLabel lblTitulo = new JLabel("Bienvenido/a " + pacienteLogueado.getNombre(), SwingConstants.CENTER);
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
		lblTitulo.setForeground(Color.WHITE);
		panelEncabezado.add(lblTitulo, BorderLayout.CENTER);

		// botones
		JPanel panelBotones = new JPanel();
		panelBotones.setLayout(new BoxLayout(panelBotones, BoxLayout.Y_AXIS));
		panelBotones.setBackground(Color.WHITE);
		panelBotones.setBorder(BorderFactory.createEmptyBorder(60, 400, 60, 400)); // márgenes laterales

		Font fuenteBoton = new Font("Segoe UI", Font.BOLD, 16);

		JButton btnGestionTurnos = crearBoton("Gestionar Turnos", fuenteBoton);
		JButton btnDisponibilidad = crearBoton("Médicos", fuenteBoton);
		JButton btnFavoritos = crearBoton("Favoritos y Recomendaciones", fuenteBoton);
		JButton btnCerrarSesion = crearBoton("Cerrar Sesión", fuenteBoton);

		// Agregar botones al panel
		panelBotones.add(btnGestionTurnos);
		panelBotones.add(Box.createVerticalStrut(40));
		panelBotones.add(btnDisponibilidad);
		panelBotones.add(Box.createVerticalStrut(40));
		panelBotones.add(btnFavoritos);
		panelBotones.add(Box.createVerticalStrut(40));
		panelBotones.add(btnCerrarSesion);

		// botones: acciones
		btnGestionTurnos.addActionListener(e -> {
			new GestionTurnosPacienteFrame(pacienteLogueado).setVisible(true);
			dispose();
		});

		btnDisponibilidad.addActionListener(e -> {
			new DisponibilidadMedicoPacienteFrame(pacienteLogueado).setVisible(true);
			dispose();
		});

		btnFavoritos.addActionListener(e -> {
			new FavoritosPacienteFrame(pacienteLogueado).setVisible(true);
			dispose();
		});

		btnCerrarSesion.addActionListener(e -> {
			new LoginFrame().setVisible(true);
			dispose();
		});

		panelPrincipal.add(panelEncabezado, BorderLayout.NORTH);
		panelPrincipal.add(panelBotones, BorderLayout.CENTER);

		setContentPane(panelPrincipal);
	}

	// estetica de botones
	private JButton crearBoton(String texto, Font fuente) {
		JButton boton = new JButton(texto);
		boton.setFont(fuente);
		boton.setAlignmentX(Component.CENTER_ALIGNMENT);
		boton.setMaximumSize(new Dimension(300, 45));
		boton.setFocusPainted(false);
		boton.setBackground(new Color(240, 240, 240));
		boton.setForeground(new Color(40, 40, 40));
		boton.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
		boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

		// hover
		boton.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				boton.setBackground(new Color(200, 220, 255));
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				boton.setBackground(new Color(240, 240, 240));
			}
		});
		return boton;
	}
}
