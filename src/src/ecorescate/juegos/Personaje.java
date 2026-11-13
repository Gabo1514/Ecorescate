package ecorescate.juegos;

import ecorescate.modelo.Accesorio;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/** El personaje del alumno, con el color de polo y el accesorio que eligió en la tienda. */
public final class Personaje {

    /** Colores de polo que el alumno puede elegir. */
    public static final Color[] POLOS = {
        Color.WHITE, new Color(0xFFD84D), new Color(0x6FCF97), new Color(0xEB5757), new Color(0x56CCF2), new Color(0xBB6BD9)
    };

    private Personaje() {
    }

    /**
     * Dibuja al personaje con los pies en (x, y). Mide unos 82 px por escala.
     * El brazo delantero termina en (3 + brazoDx, -47 + brazoDy) en coordenadas del personaje.
     */
    public static void dibujar(Graphics2D g0, double x, double y, double escala, Accesorio accesorio, int polo,
                               boolean derecha, double paso, boolean mochila, double brazoDx, double brazoDy) {
        Estilo.Apariencia a = new Estilo.Apariencia(POLOS[Math.floorMod(polo, POLOS.length)], new Color(0x5B3A1E),
                new Color(0xF5C9A0), new Color(0x2E3A59), false);
        if (accesorio == Accesorio.CAPA) capa(g0, x, y, escala, derecha, paso);
        Estilo.nino(g0, x, y, escala, a, derecha, paso, mochila, brazoDx, brazoDy);
        if (accesorio == null || accesorio == Accesorio.NINGUNO || accesorio == Accesorio.CAPA) return;

        Graphics2D g = (Graphics2D) g0.create();
        g.translate(x, y);
        g.scale(escala, escala);
        if (!derecha) g.scale(-1, 1);
        switch (accesorio) {
            case GORRA: {
                Estilo.contorno(g, new RoundRectangle2D.Double(6, -79, 20, 6, 5, 5), new Color(0xB71C1C), 1.8f);
                Estilo.contorno(g, new Arc2D.Double(-16, -91, 33, 24, 0, 180, Arc2D.CHORD), new Color(0xE53935), 1.8f);
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(-1, -92, 5, 5));
                break;
            }
            case LENTES: {
                g.setColor(Estilo.TRAZO);
                g.setStroke(new BasicStroke(2f));
                g.draw(new Line2D.Double(-12, -68, 3, -68));
                Estilo.contorno(g, new RoundRectangle2D.Double(2, -72, 14, 9, 4, 4), new Color(0x1F2937), 1.6f);
                g.setColor(new Color(255, 255, 255, 170));
                g.fill(new RoundRectangle2D.Double(5, -71, 4, 3, 2, 2));
                break;
            }
            case AUDIFONOS: {
                g.setColor(Estilo.TRAZO);
                g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Arc2D.Double(-15, -86, 31, 34, 20, 140, Arc2D.OPEN));
                g.setColor(new Color(0x7C4DFF));
                g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Arc2D.Double(-15, -86, 31, 34, 20, 140, Arc2D.OPEN));
                Estilo.contorno(g, new RoundRectangle2D.Double(-8, -74, 11, 16, 5, 5), new Color(0x7C4DFF), 1.8f);
                break;
            }
            case SOMBRERO: {
                Estilo.contorno(g, new Ellipse2D.Double(-22, -83, 46, 9), new Color(0xA0784C), 1.8f);
                Estilo.contorno(g, new RoundRectangle2D.Double(-12, -97, 26, 18, 8, 8), new Color(0xB98A57), 1.8f);
                g.setColor(new Color(0x5B3A1E));
                g.fill(new RoundRectangle2D.Double(-12, -85, 26, 4, 2, 2));
                break;
            }
            case CORONA:
            default: {
                Path2D corona = new Path2D.Double();
                corona.moveTo(-12, -80);
                corona.lineTo(-14, -96);
                corona.lineTo(-6, -88);
                corona.lineTo(1, -100);
                corona.lineTo(8, -88);
                corona.lineTo(16, -96);
                corona.lineTo(14, -80);
                corona.closePath();
                Estilo.contorno(g, corona, new Color(0xFFD84D), 1.8f);
                g.setColor(new Color(0xE53935));
                g.fill(new Ellipse2D.Double(-1.5, -88, 5, 5));
                g.setColor(new Color(0x3C8DC5));
                g.fill(new Ellipse2D.Double(-10, -86, 4, 4));
                g.fill(new Ellipse2D.Double(9, -86, 4, 4));
                break;
            }
        }
        g.dispose();
    }

    private static void capa(Graphics2D g0, double x, double y, double escala, boolean derecha, double paso) {
        Graphics2D g = (Graphics2D) g0.create();
        g.translate(x, y);
        g.scale(escala, escala);
        if (!derecha) g.scale(-1, 1);
        double vuelo = Math.abs(paso) * 6;
        Path2D capa = new Path2D.Double();
        capa.moveTo(-8, -52);
        capa.lineTo(6, -52);
        capa.quadTo(2, -30, -6, -14);
        capa.lineTo(-30 - vuelo, -20 + vuelo * 0.3);
        capa.quadTo(-22, -36, -8, -52);
        capa.closePath();
        Estilo.contorno(g, capa, new Color(0xE53935), 2f);
        g.setColor(new Color(0xFFD84D));
        g.fill(Estilo.estrella(-16, -30, 5, 0.45, 5, 0));
        g.dispose();
    }
}
