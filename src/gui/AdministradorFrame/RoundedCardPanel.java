package gui.AdministradorFrame;

import javax.swing.*;
import java.awt.*;


public class RoundedCardPanel extends JPanel {

    private int cornerRadius = 16;
    private Color borderColor = new Color(210, 210, 210);

    public RoundedCardPanel() {
        this(16);
    }

    public RoundedCardPanel(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        setOpaque(false);
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        repaint();
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // pinta fondo y borde redondeado
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            // fondo 
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);

            // borde
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);
        } finally {
            g2.dispose();
        }

        super.paintComponent(g);
    }
}
