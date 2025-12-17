package gui;

import bll.HistorialMedico;
import bll.Paciente;
import dll.ControllerHistorial;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static gui.UiPaleta.*;

public class VerHistorialPacienteFrame extends JFrame {

    private final Paciente paciente;
    private final ControllerHistorial historialManager;

    private JTable tablaHistorial;
    private DefaultTableModel modeloTabla;

    private static final String UI_FONT = "Segoe UI";

    public VerHistorialPacienteFrame(Paciente paciente, ControllerHistorial historialManager) {
        this.paciente = paciente;
        this.historialManager = historialManager;

        setTitle("Historial Médico de: " + paciente.getNombre() + " " + paciente.getApellido());
        setSize(700, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        cargarHistorial();
    }

    private void initUI() {
        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COLOR_PRIMARY);
        topBar.setPreferredSize(new Dimension(getWidth(), 85));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 40, 15, 40));

        JLabel lblTitulo = new JLabel(
                "Historial de " + paciente.getNombre() + " " + paciente.getApellido()
        );
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font(UI_FONT, Font.BOLD, 24));

        topBar.add(lblTitulo, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        // Card
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 32, 20, 32));

        RoundedCardPanel card = new RoundedCardPanel(16);
        card.setBackground(COLOR_CARD_BG);
        card.setBorderColor(COLOR_CARD_BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel lblSubtitulo = new JLabel("Registros Médicos");
        lblSubtitulo.setFont(new Font(UI_FONT, Font.BOLD, 17));
        lblSubtitulo.setForeground(MINT_DARK);
        lblSubtitulo.setBorder(BorderFactory.createEmptyBorder(8, 0, 15, 0));
        card.add(lblSubtitulo, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(
                new Object[]{"Fecha", "Observaciones", "Médico"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaHistorial = new JTable(modeloTabla);
        estilizarTabla(tablaHistorial);

        JScrollPane scroll = new JScrollPane(tablaHistorial);
        card.add(scroll, BorderLayout.CENTER);

        wrapper.add(card, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(COLOR_BACKGROUND);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        RoundedButton btnCerrar = new RoundedButton("Cerrar");
        btnCerrar.setBackground(COLOR_ACCENT);
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFont(new Font(UI_FONT, Font.PLAIN, 13));
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> dispose());

        footer.add(btnCerrar);
        add(footer, BorderLayout.SOUTH);
    }

    private void estilizarTabla(JTable tabla) {
        tabla.setFont(new Font(UI_FONT, Font.PLAIN, 14));
        tabla.setRowHeight(28);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(220, 220, 220));

        JTableHeader header = tabla.getTableHeader();
        header.setBackground(COLOR_ACCENT);
        header.setForeground(Color.WHITE);
        header.setFont(new Font(UI_FONT, Font.BOLD, 15));
        header.setOpaque(true);

        tabla.setSelectionBackground(MINT_DARK);
        tabla.setSelectionForeground(Color.WHITE);
    }


    private void cargarHistorial() {
        modeloTabla.setRowCount(0);

        List<HistorialMedico> registros =
                historialManager.listarPorPaciente(paciente.getUsuario());

        if (registros == null || registros.isEmpty()) {
            new MensajeFrame(
                    this,
                    "No hay registros de historial para este paciente."
            );
            return;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (HistorialMedico h : registros) {
            String nombreMedico = "N/A";
            if (h.getMedico() != null) {
                nombreMedico = h.getMedico().getNombre()
                        + " " + h.getMedico().getApellido();
            }

            modeloTabla.addRow(new Object[]{
                    h.getFechaCreacion().format(fmt),
                    h.getObservaciones(),
                    nombreMedico
            });
        }
    }


    private static class MensajeFrame extends JFrame {

        public MensajeFrame(JFrame parent, String mensaje) {
            setTitle("Información");
            setSize(420, 170);
            setLocationRelativeTo(parent);
            setResizable(false);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(COLOR_BACKGROUND);
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel lblMensaje = new JLabel(
                    "<html><div style='text-align:center;'>" + mensaje + "</div></html>",
                    SwingConstants.CENTER
            );
            lblMensaje.setFont(new Font(UI_FONT, Font.PLAIN, 14));
            lblMensaje.setForeground(Color.BLACK);

            RoundedButton btnOk = new RoundedButton("Aceptar");
            btnOk.setBackground(COLOR_ACCENT);
            btnOk.setForeground(Color.WHITE);
            btnOk.setFont(new Font(UI_FONT, Font.PLAIN, 13));
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


    private static class RoundedCardPanel extends JPanel {
        private final int radius;
        private Color borderColor;

        public RoundedCardPanel(int radius) {
            this.radius = radius;
            this.borderColor = new Color(200, 200, 200);
            setOpaque(false);
        }

        public void setBorderColor(Color c) { this.borderColor = c; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.setColor(borderColor);
            g2.drawRoundRect(
                    0, 0,
                    getWidth() - 1,
                    getHeight() - 1,
                    radius, radius
            );
        }
    }
}
