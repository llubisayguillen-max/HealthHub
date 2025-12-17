package gui;

import bll.Paciente;
import bll.Medico;
import dll.ControllerHistorial;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static gui.UiPaleta.*;

public class CrearRegistroHistorialFrame extends JFrame {

    private JComboBox<Paciente> cmbPaciente;
    private JComboBox<Medico> cmbMedico;
    private JTextArea txtDescripcion;
    private final ControllerHistorial manager;

    private static final String UI_FONT_FAMILY = "Segoe UI";

    public CrearRegistroHistorialFrame(List<Paciente> pacientes, List<Medico> medicos, ControllerHistorial manager) {
        if (manager == null) throw new IllegalArgumentException("ControllerHistorial no puede ser null");
        this.manager = manager;

        setTitle("HealthHub - Nuevo Registro Médico");
        setSize(580, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI(pacientes, medicos);
    }

    private void initUI(List<Paciente> pacientes, List<Medico> medicos) {

        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COLOR_PRIMARY);
        topBar.setPreferredSize(new Dimension(getWidth(), 80));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblTitulo = new JLabel("Nuevo Registro Médico");
        lblTitulo.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);

        topBar.add(lblTitulo, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(COLOR_BACKGROUND);

        RoundedCardPanel card = new RoundedCardPanel(18);
        card.setBackground(COLOR_CARD_BG);
        card.setBorderColor(COLOR_CARD_BORDER);
        card.setPreferredSize(new Dimension(530, 380));
        card.setLayout(null);

        int y = 30;

        card.add(crearLabel("Paciente:", 30, y));

        cmbPaciente = new JComboBox<>(pacientes.toArray(new Paciente[0]));
        cmbPaciente.setBounds(150, y, 300, 35);
        cmbPaciente.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 14));
        cmbPaciente.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof Paciente p) {
                    setText(p.getNombre() + " " + p.getApellido());
                }
                return this;
            }
        });
        card.add(cmbPaciente);

        y += 60;

        card.add(crearLabel("Médico:", 30, y));

        cmbMedico = new JComboBox<>(medicos.toArray(new Medico[0]));
        cmbMedico.setBounds(150, y, 300, 35);
        cmbMedico.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 14));
        cmbMedico.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof Medico m) {
                    setText(m.getNombre() + " " + m.getApellido());
                }
                return this;
            }
        });
        card.add(cmbMedico);

        y += 60;

        card.add(crearLabel("Descripción:", 30, y));

        txtDescripcion = new JTextArea();
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 14));

        JScrollPane scroll = new JScrollPane(txtDescripcion);
        scroll.setBounds(150, y, 300, 120);
        card.add(scroll);

        y += 150;

        RoundedButton btnGuardar = new RoundedButton("Guardar Registro");
        btnGuardar.setBackground(COLOR_ACCENT);
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 16));
        btnGuardar.setBounds(150, y, 200, 40);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.addActionListener(e -> guardarRegistro());

        card.add(btnGuardar);

        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);
    }

    private JLabel crearLabel(String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 16));
        lbl.setForeground(MINT_DARK);
        lbl.setBounds(x, y, 120, 30);
        return lbl;
    }


    private void guardarRegistro() {
        Paciente paciente = (Paciente) cmbPaciente.getSelectedItem();
        Medico medico = (Medico) cmbMedico.getSelectedItem();
        String descripcion = txtDescripcion.getText().trim();

        if (paciente == null) {
            new MensajeFrame(this, "Debe seleccionar un paciente.", false);
            return;
        }
        if (medico == null) {
            new MensajeFrame(this, "Debe seleccionar un médico.", false);
            return;
        }
        if (descripcion.isEmpty()) {
            new MensajeFrame(this, "La descripción no puede estar vacía.", false);
            return;
        }

        try {
            boolean ok = manager.crearRegistro(paciente, medico, descripcion);
            if (ok) {
                new MensajeFrame(this, "Registro guardado con éxito.", true);
                dispose();
            } else {
                new MensajeFrame(this, "No se pudo guardar el registro.", false);
            }
        } catch (Exception ex) {
            new MensajeFrame(this, "Error: " + ex.getMessage(), false);
        }
    }


    private static class MensajeFrame extends JFrame {

        public MensajeFrame(JFrame parent, String mensaje, boolean exito) {
            setTitle(exito ? "Operación Exitosa" : "Error");
            setSize(420, 180);
            setLocationRelativeTo(parent);
            setResizable(false);

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(COLOR_BACKGROUND);
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel lblMensaje = new JLabel(
                    "<html><div style='text-align:center;'>" + mensaje + "</div></html>",
                    SwingConstants.CENTER
            );
            lblMensaje.setFont(new Font(UI_FONT_FAMILY, Font.PLAIN, 15));
            lblMensaje.setForeground(exito ? MINT_DARK : new Color(200, 60, 60));

            RoundedButton btnOk = new RoundedButton("Aceptar");
            btnOk.setBackground(COLOR_ACCENT);
            btnOk.setForeground(Color.WHITE);
            btnOk.setFont(new Font(UI_FONT_FAMILY, Font.BOLD, 14));
            btnOk.addActionListener(e -> dispose());

            JPanel btnPanel = new JPanel();
            btnPanel.setBackground(COLOR_BACKGROUND);
            btnPanel.add(btnOk);

            panel.add(lblMensaje, BorderLayout.CENTER);
            panel.add(btnPanel, BorderLayout.SOUTH);

            add(panel);
            setVisible(true);
        }
    }

    //CARD

    private static class RoundedCardPanel extends JPanel {
        private final int radius;
        private Color borderColor;

        public RoundedCardPanel(int radius) {
            this.radius = radius;
            this.borderColor = new Color(200, 200, 200);
            setOpaque(false);
        }

        public void setBorderColor(Color c) {
            this.borderColor = c;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }
    }
}
