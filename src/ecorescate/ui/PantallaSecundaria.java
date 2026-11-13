package ecorescate.ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

/** Pantalla con barra superior (volver, título y EcoMonedas): base de Tienda, Medallas y Ranking. */
class PantallaSecundaria extends JPanel {

    private final PildoraMonedas monedas = new PildoraMonedas();
    private JLabel etiquetaTitulo;

    void setTitulo(String titulo) {
        etiquetaTitulo.setText(titulo);
    }

    PantallaSecundaria(String titulo, Runnable volver) {
        super(new BorderLayout());
        setBackground(Tema.FONDO);
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE), new EmptyBorder(10, 16, 10, 24)));
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        izquierda.setOpaque(false);
        BotonEco atras = new BotonEco("← Volver al menú", Tema.PRINCIPAL, false);
        atras.addActionListener(e -> volver.run());
        JLabel l = new JLabel(titulo);
        etiquetaTitulo = l;
        l.setFont(Tema.fuente(Font.BOLD, 24f));
        l.setForeground(new Color(0x2D1B69));
        izquierda.add(atras);
        izquierda.add(l);
        barra.add(izquierda, BorderLayout.WEST);
        barra.add(monedas, BorderLayout.EAST);
        add(barra, BorderLayout.NORTH);
    }

    void setContenido(JComponent c) {
        add(c, BorderLayout.CENTER);
    }

    void setMonedas(int cantidad) {
        monedas.setCantidad(cantidad);
    }

    /** Píldora dorada con la cantidad de EcoMonedas. */
    static class PildoraMonedas extends JComponent {
        private int cantidad;

        PildoraMonedas() {
            setPreferredSize(new Dimension(150, 40));
            setToolTipText("EcoMonedas: gánalas jugando y úsalas en la Tienda");
        }

        void setCantidad(int c) {
            cantidad = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            double h = 38, y = (getHeight() - h) / 2;
            RoundRectangle2D r = new RoundRectangle2D.Double(1, y, getWidth() - 2, h, h, h);
            g.setColor(new Color(0xFFF3C4));
            g.fill(r);
            g.setColor(new Color(0xE0A800));
            g.draw(r);
            Iconos.moneda(g, 22, y + h / 2, 13);
            g.setColor(new Color(0x5A3D00));
            g.setFont(Tema.fuente(Font.BOLD, 20f));
            g.drawString(String.valueOf(cantidad), 44, (float) (y + h / 2 + 7));
            g.dispose();
        }
    }
}
