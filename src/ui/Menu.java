package ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import bll.Usuario;

import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextArea;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Menu extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPasswordField inpPassword;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Menu frame = new Menu();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Menu() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 311, 302);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Login");
		lblNewLabel.setFont(new Font("Roboto", Font.BOLD, 14));
		lblNewLabel.setBounds(121, 11, 72, 43);
		contentPane.add(lblNewLabel);
		
		JTextArea inpMail = new JTextArea();
		inpMail.setBounds(45, 84, 178, 31);
		contentPane.add(inpMail);
		
		JLabel lblEmail = new JLabel("Email");
		lblEmail.setFont(new Font("Roboto", Font.BOLD, 12));
		lblEmail.setBounds(45, 47, 72, 43);
		contentPane.add(lblEmail);
		
		JLabel lblContrasea = new JLabel("Contraseña");
		lblContrasea.setFont(new Font("Roboto", Font.BOLD, 12));
		lblContrasea.setBounds(45, 115, 72, 43);
		contentPane.add(lblContrasea);
		
		inpPassword = new JPasswordField();
		inpPassword.setBounds(45, 152, 178, 31);
		contentPane.add(inpPassword);
		
		JButton btnIngresar = new JButton("Ingresar");
		btnIngresar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			}
		});
		btnIngresar.setBounds(100, 209, 89, 23);
		contentPane.add(btnIngresar);

	}
}
