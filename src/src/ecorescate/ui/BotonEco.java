package ecorescate.ui;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/** Botón redondeado con los colores de la aplicación. */
public class BotonEco extends JButton {

    private final Color color;
    private final boolean primario;
    private boolean encima;

    public BotonEco(String texto, Color color, boolean primario) {
        super(texto);
        this.color = color;
        this.primario = primario;
        setFont(Tema.fuente(Font.BOLD, 16f));
        setForeground(primario ? Color.WHITE : color);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setBorder(new EmptyBorder(9, 20, 9, 20));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addActionListener(e -> Sonido.get().tocar(Sonido.Efecto.CLIC));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                encima = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                encima = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Tema.suavizar(g);
        RoundRectangle2D forma = new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2,
                getHeight() - 2, getHeight() - 2);
        if (primario) {
            g.setColor(encima || getModel().isPressed() ? Tema.oscurecer(color, 0.15) : color);
            g.fill(forma);
        } else {
            g.setColor(encima ? Tema.alfa(color, 28) : Color.WHITE);
            g.fill(forma);
            g.setColor(Tema.alfa(color, 150));
            g.draw(forma);
        }
        g.dispose();
        super.paintComponent(g0);
    }
}
