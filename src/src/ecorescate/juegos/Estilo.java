package ecorescate.juegos;

import ecorescate.ui.Tema;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

/** Paleta y dibujos estilo caricatura (con contorno oscuro) compartidos por los cuatro minijuegos. */
final class Estilo {

    static final Color MORADO = new Color(0x2D1B69);
    static final Color MORADO_MEDIO = new Color(0x4B3F8A);
    static final Color VIOLETA = new Color(0x7C4DFF);
    static final Color LAVANDA = new Color(0xEFEEFC);
    static final Color TRAZO = new Color(0x2A2340);
    static final Color AMARILLO = new Color(0xFFD84D);
    static final Color MENTA = new Color(0x6EE7B7);
    static final Color ROSADO = new Color(0xFF6B9D);
    static final Color ROJO = new Color(0xFF6B6B);
    static final Color BLANCO_SUAVE = new Color(255, 255, 255, 195);

    static final BasicStroke STROKE_1_5 = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    static final BasicStroke STROKE_1_6 = new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    static final BasicStroke STROKE_1_8 = new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    static final BasicStroke STROKE_2_0 = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    static final BasicStroke STROKE_2_2 = new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    static final BasicStroke STROKE_2_5 = new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    static final BasicStroke STROKE_3_0 = new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    static final BasicStroke STROKE_5_5 = new BasicStroke(5.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private static final Color[] POLOS = {
        new Color(0xFFFFFF), new Color(0xF2C94C), new Color(0x6FCF97), new Color(0xEB5757), new Color(0x56CCF2), new Color(0xBB6BD9)
    };
    private static final Color[] PELOS = {new Color(0x5B3A1E), new Color(0x2B1D14), new Color(0x7A4A24), new Color(0x1E1E1E)};
    private static final Color[] PIELES = {new Color(0xF5C9A0), new Color(0xE0AC7E), new Color(0xC68A5E), new Color(0xF2D0B0)};

    private static final Color SOMBRA_NINO = new Color(0, 0, 0, 40);
    private static final Color AZUL_ZAPATO = new Color(0x3B82F6);
    private static final Color AZUL_CORREA = new Color(0x2563EB);
    private static final Color FONDO_BURBUJA = new Color(0xF4F1EA);

    private Estilo() {
    }

    static BasicStroke strokeRedondo(float grosor) {
        if (Math.abs(grosor - 2.2f) < 0.05f) return STROKE_2_2;
        if (Math.abs(grosor - 2.0f) < 0.05f) return STROKE_2_0;
        if (Math.abs(grosor - 1.8f) < 0.05f) return STROKE_1_8;
        if (Math.abs(grosor - 1.6f) < 0.05f) return STROKE_1_6;
        if (Math.abs(grosor - 2.5f) < 0.05f) return STROKE_2_5;
        if (Math.abs(grosor - 3.0f) < 0.05f) return STROKE_3_0;
        if (Math.abs(grosor - 1.5f) < 0.05f) return STROKE_1_5;
        return new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    /** Rellena y contornea una figura. */
    static void contorno(Graphics2D g, Shape forma, Color relleno, float grosor) {
        g.setColor(relleno);
        g.fill(forma);
        g.setColor(TRAZO);
        g.setStroke(strokeRedondo(grosor));
        g.draw(forma);
    }

    static void contorno(Graphics2D g, Shape forma, Color relleno) {
        contorno(g, forma, relleno, 2.2f);
    }

    static Path2D corazon(double cx, double cy, double s) {
        Path2D p = new Path2D.Double();
        p.moveTo(cx, cy + s * 0.55);
        p.curveTo(cx - s * 1.1, cy - s * 0.1, cx - s * 0.55, cy - s * 0.85, cx, cy - s * 0.3);
        p.curveTo(cx + s * 0.55, cy - s * 0.85, cx + s * 1.1, cy - s * 0.1, cx, cy + s * 0.55);
        p.closePath();
        return p;
    }

    static Path2D estrella(double cx, double cy, double r, double interior, int puntas, double giro) {
        Path2D p = new Path2D.Double();
        for (int i = 0; i < puntas * 2; i++) {
            double radio = i % 2 == 0 ? r : r * interior;
            double a = -Math.PI / 2 + giro + i * Math.PI / puntas;
            double x = cx + Math.cos(a) * radio, y = cy + Math.sin(a) * radio;
            if (i == 0) p.moveTo(x, y);
            else p.lineTo(x, y);
        }
        p.closePath();
        return p;
    }

    /** Globo de diálogo con colita apuntando hacia abajo, a (cx, y). */
    static void burbuja(Graphics2D g, String texto, double cx, double y, double minX, double maxX) {
        g.setFont(Tema.fuente(Font.BOLD, 13.5f));
        int w = g.getFontMetrics().stringWidth(texto) + 26;
        double x = Tema.limitar(cx - w / 2.0, minX, maxX - w);
        RoundRectangle2D r = new RoundRectangle2D.Double(x, y - 28, w, 27, 12, 12);
        g.setColor(FONDO_BURBUJA);
        g.fill(r);
        Path2D cola = new Path2D.Double();
        cola.moveTo(cx - 6, y - 2);
        cola.lineTo(cx - 9, y + 9);
        cola.lineTo(cx + 6, y - 2);
        g.fill(cola);
        g.setColor(TRAZO);
        g.setStroke(STROKE_1_8);
        g.draw(r);
        g.draw(new Line2D.Double(cx - 6, y - 1, cx - 9, y + 9));
        g.draw(new Line2D.Double(cx - 9, y + 9, cx + 6, y - 1));
        g.drawString(texto, (float) (x + 13), (float) (y - 9));
    }

    /** Burbuja redonda con un ícono dentro (lo dibuja quien llama, centrado en cx, cy). */
    static void burbujaIcono(Graphics2D g, double cx, double cy, double r) {
        Ellipse2D c = new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2);
        Path2D cola = new Path2D.Double();
        cola.moveTo(cx - 5, cy + r - 3);
        cola.lineTo(cx - 9, cy + r + 9);
        cola.lineTo(cx + 5, cy + r - 2);
        cola.closePath();
        g.setColor(Color.WHITE);
        g.fill(cola);
        contorno(g, c, Color.WHITE, 2f);
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(cx - 7, cy + r - 6, 12, 8));
    }

    /** Aspecto de un niño o niña. */
    static class Apariencia {
        final Color polo, pelo, piel, short_;
        final boolean peloLargo;

        Apariencia(Color polo, Color pelo, Color piel, Color short_, boolean peloLargo) {
            this.polo = polo;
            this.pelo = pelo;
            this.piel = piel;
            this.short_ = short_;
            this.peloLargo = peloLargo;
        }

        static Apariencia aleatoria(Random azar) {
            return new Apariencia(POLOS[azar.nextInt(POLOS.length)], PELOS[azar.nextInt(PELOS.length)],
                    PIELES[azar.nextInt(PIELES.length)], azar.nextBoolean() ? new Color(0x2E3A59) : new Color(0x3D5A80),
                    azar.nextBoolean());
        }

        static Apariencia protagonista() {
            return new Apariencia(Color.WHITE, new Color(0x5B3A1E), new Color(0xF5C9A0), new Color(0x2E3A59), false);
        }
    }

    /**
     * Niño visto de costado. (x, y) son los pies; mide unos 82 px por s. El brazo delantero
     * termina en (3 + brazoDx, -47 + brazoDy) en coordenadas del niño.
     */
    static void nino(Graphics2D g0, double x, double y, double s, Apariencia a, boolean derecha, double paso,
                     boolean mochila, double brazoDx, double brazoDy) {
        Graphics2D g = (Graphics2D) g0.create();
        g.translate(x, y);
        g.scale(s, s);
        g.setColor(SOMBRA_NINO);
        g.fill(new Ellipse2D.Double(-20, -5, 40, 11));
        if (!derecha) g.scale(-1, 1);
        g.setStroke(STROKE_2_0);

        for (int i = 0; i < 2; i++) {
            double desp = (i == 0 ? -1 : 1) * paso * 6;
            double lx = (i == 0 ? -7 : 2) + desp;
            g.setColor(a.piel);
            g.fill(new Rectangle2D.Double(lx, -22, 7, 16));
            contorno(g, new RoundRectangle2D.Double(lx - 2, -9, 14, 8, 6, 6), AZUL_ZAPATO, 2f);
        }
        g.setColor(a.short_);
        g.fill(new RoundRectangle2D.Double(-11, -31, 22, 12, 5, 5));
        if (mochila) contorno(g, new RoundRectangle2D.Double(-23, -54, 14, 25, 6, 6), AZUL_ZAPATO, 2f);
        if (a.peloLargo) {
            g.setColor(a.pelo);
            g.fill(new RoundRectangle2D.Double(-17, -76, 18, 34, 10, 10));
        }
        contorno(g, new RoundRectangle2D.Double(-11, -52, 22, 23, 8, 8), a.polo, 2f);
        if (mochila) {
            g.setColor(AZUL_CORREA);
            g.setStroke(STROKE_3_0);
            g.draw(new Line2D.Double(-8, -50, -4, -32));
        }
        g.setColor(a.piel);
        g.setStroke(STROKE_5_5);
        g.draw(new Line2D.Double(3, -47, 3 + brazoDx + paso * 6, -47 + brazoDy));

        contorno(g, new Ellipse2D.Double(-14, -80, 29, 29), a.piel, 2f);
        g.setColor(a.pelo);
        g.fill(new Arc2D.Double(-16, -83, 32, 26, 0, 180, Arc2D.PIE));
        g.fill(new Ellipse2D.Double(-16, -76, 14, 18));
        Path2D mechon = new Path2D.Double();
        mechon.moveTo(6, -80);
        mechon.lineTo(17, -73);
        mechon.lineTo(8, -71);
        mechon.closePath();
        g.fill(mechon);
        g.setColor(TRAZO);
        g.fill(new Ellipse2D.Double(5, -68, 3.5, 4.5));
        g.setStroke(STROKE_1_8);
        g.draw(new Arc2D.Double(2, -65, 9, 6, 200, 140, Arc2D.OPEN));
        g.dispose();
    }

    /** Píldora de texto (etiqueta redondeada). */
    static void pildora(Graphics2D g, String texto, double cx, double y, Color fondo, Color tinta, float tam) {
        g.setFont(Tema.fuente(Font.BOLD, tam));
        int w = g.getFontMetrics().stringWidth(texto) + 18;
        RoundRectangle2D r = new RoundRectangle2D.Double(cx - w / 2.0, y, w, tam + 9, tam + 9, tam + 9);
        g.setColor(fondo);
        g.fill(r);
        g.setColor(tinta);
        Tema.centrado(g, texto, cx, y + tam + 1);
    }
}
