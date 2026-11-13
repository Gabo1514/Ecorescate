package ecorescate.juegos;

import ecorescate.ui.Tema;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** Textos flotantes ("+10") y chispas que acompañan las acciones del jugador. */
class Efectos {

    private static class TextoFlotante {
        final String texto;
        final Color color;
        final double x;
        double y;
        double vida = 1.3;

        TextoFlotante(String texto, Color color, double x, double y) {
            this.texto = texto;
            this.color = color;
            this.x = x;
            this.y = y;
        }
    }

    private static class Particula {
        double x, y, vx, vy, vida, tam, giro, vgiro;
        boolean papel;
        Color color;
    }

    private static final Color[] CONFETI = {
        new Color(0xFF6B9D), new Color(0xFFD84D), new Color(0x6EE7B7), new Color(0x56CCF2), new Color(0x7C4DFF), new Color(0xFF8C42)
    };
    private static final Color CHISPA_DORADA = new Color(0xFFD45C);

    /** Lluvia de papelitos de colores desde arriba. */
    void confeti(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            Particula p = new Particula();
            p.papel = true;
            p.x = azar.nextDouble() * Minijuego.ANCHO;
            p.y = Minijuego.HUD + 40 - azar.nextDouble() * 120;
            p.vx = (azar.nextDouble() - 0.5) * 80;
            p.vy = 60 + azar.nextDouble() * 120;
            p.vida = 2.2 + azar.nextDouble() * 1.2;
            p.tam = 7 + azar.nextDouble() * 6;
            p.giro = azar.nextDouble() * Math.PI;
            p.vgiro = (azar.nextDouble() - 0.5) * 10;
            p.color = CONFETI[azar.nextInt(CONFETI.length)];
            particulas.add(p);
        }
    }

    private final List<TextoFlotante> textos = new ArrayList<>();
    private final List<Particula> particulas = new ArrayList<>();
    private final Random azar = new Random();
    private final Ellipse2D.Double circuloTemp = new Ellipse2D.Double();
    private final Rectangle2D.Double rectTemp = new Rectangle2D.Double();

    void texto(String texto, Color color, double x, double y) {
        textos.add(new TextoFlotante(texto, color, x, y));
    }

    void chispas(double x, double y, Color color, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            Particula p = new Particula();
            double ang = azar.nextDouble() * Math.PI * 2;
            double vel = 60 + azar.nextDouble() * 140;
            p.x = x;
            p.y = y;
            p.vx = Math.cos(ang) * vel;
            p.vy = Math.sin(ang) * vel - 80;
            p.vida = 0.6 + azar.nextDouble() * 0.5;
            p.tam = 4 + azar.nextDouble() * 5;
            p.color = azar.nextInt(3) == 0 ? CHISPA_DORADA : color;
            particulas.add(p);
        }
    }

    void actualizar(double dt) {
        for (Iterator<TextoFlotante> it = textos.iterator(); it.hasNext(); ) {
            TextoFlotante t = it.next();
            t.vida -= dt;
            t.y -= 42 * dt;
            if (t.vida <= 0) it.remove();
        }
        for (Iterator<Particula> it = particulas.iterator(); it.hasNext(); ) {
            Particula p = it.next();
            p.vida -= dt;
            if (p.papel) {
                p.vy = Math.min(p.vy + 60 * dt, 170);
                p.vx += Math.sin(p.giro * 2) * 30 * dt;
                p.giro += p.vgiro * dt;
            } else {
                p.vy += 380 * dt;
            }
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            if (p.vida <= 0) it.remove();
        }
    }

    void limpiar() {
        textos.clear();
        particulas.clear();
    }

    void dibujar(Graphics2D g) {
        if (!particulas.isEmpty()) {
            AffineTransform base = g.getTransform();
            for (Particula p : particulas) {
                g.setColor(Tema.alfa(p.color, (int) (255 * Math.min(1, p.vida * 2))));
                if (p.papel) {
                    g.translate(p.x, p.y);
                    g.rotate(p.giro);
                    g.scale(1, Math.abs(Math.cos(p.giro * 1.7)) * 0.8 + 0.2);
                    rectTemp.setRect(-p.tam / 2, -p.tam / 3, p.tam, p.tam * 0.66);
                    g.fill(rectTemp);
                    g.setTransform(base);
                } else {
                    circuloTemp.setFrame(p.x - p.tam / 2, p.y - p.tam / 2, p.tam, p.tam);
                    g.fill(circuloTemp);
                }
            }
        }
        if (!textos.isEmpty()) {
            g.setFont(Tema.fuente(Font.BOLD, 22f));
            for (TextoFlotante t : textos) {
                int a = (int) (255 * Math.min(1, t.vida * 1.6));
                int ancho = g.getFontMetrics().stringWidth(t.texto);
                double x = Tema.limitar(t.x, ancho / 2.0 + 8, Minijuego.ANCHO - ancho / 2.0 - 8);
                float fx = (float) (x - ancho / 2.0);
                float fy = (float) t.y;
                g.setColor(Tema.alfa(Color.WHITE, (int) (a * 0.85)));
                g.drawString(t.texto, fx - 2f, fy);
                g.drawString(t.texto, fx + 2f, fy);
                g.drawString(t.texto, fx, fy - 2f);
                g.drawString(t.texto, fx, fy + 2f);
                g.setColor(Tema.alfa(t.color, a));
                g.drawString(t.texto, fx, fy);
            }
        }
    }
}
