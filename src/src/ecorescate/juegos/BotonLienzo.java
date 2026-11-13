package ecorescate.juegos;

import ecorescate.ui.Tema;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

/** Botón dibujado dentro del lienzo del juego (escala junto con la escena). */
class BotonLienzo {

    String texto;
    Color color = Tema.PRINCIPAL;
    boolean primario;
    double x, y, ancho, alto;

    BotonLienzo(String texto, boolean primario) {
        this.texto = texto;
        this.primario = primario;
    }

    void ubicar(double x, double y, double ancho, double alto) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
    }

    boolean contiene(double px, double py) {
        return px >= x && px <= x + ancho && py >= y && py <= y + alto;
    }

    void dibujar(Graphics2D g, double ratonX, double ratonY) {
        boolean encima = contiene(ratonX, ratonY);
        RoundRectangle2D forma = new RoundRectangle2D.Double(x, y, ancho, alto, alto, alto);
        if (primario) {
            g.setColor(encima ? Tema.oscurecer(color, 0.15) : color);
            g.fill(forma);
            g.setColor(Color.WHITE);
        } else {
            g.setColor(encima ? Tema.alfa(color, 30) : Color.WHITE);
            g.fill(forma);
            g.setColor(Tema.alfa(color, 160));
            g.setStroke(new BasicStroke(1.5f));
            g.draw(forma);
            g.setColor(color);
        }
        g.setFont(Tema.fuente(Font.BOLD, (float) Math.min(19, alto * 0.48)));
        Tema.centrado(g, texto, x + ancho / 2, y + alto / 2 + g.getFontMetrics().getAscent() * 0.36);
    }
}
