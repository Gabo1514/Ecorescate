package ecorescate.ui;

import ecorescate.modelo.TipoJuego;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/** Íconos vectoriales dibujados con Java2D (no dependen de imágenes externas). */
public final class Iconos {

    private Iconos() {
    }

    /** Círculo de color con el ícono blanco del minijuego, como en las tarjetas del menú. */
    public static void logo(Graphics2D g, TipoJuego tipo, double cx, double cy, double diametro) {
        g.setColor(new Color(tipo.getColor()));
        g.fill(new Ellipse2D.Double(cx - diametro / 2, cy - diametro / 2, diametro, diametro));
        glifo(g, tipo, cx, cy, diametro * 0.27, Color.WHITE, new Color(tipo.getColor()));
    }

    /** Solo el dibujo del ícono. s es aproximadamente su radio. */
    public static void glifo(Graphics2D g, TipoJuego tipo, double cx, double cy, double s, Color tinta, Color fondo) {
        switch (tipo) {
            case LIMPIEZA: reciclaje(g, cx, cy, s, tinta); break;
            case AGUA: gota(g, cx, cy, s, tinta, fondo); break;
            case ENERGIA: foco(g, cx, cy, s, tinta, fondo); break;
            default: brote(g, cx, cy, s, tinta); break;
        }
    }

    /** Tres flechas que se persiguen en círculo. */
    public static void reciclaje(Graphics2D g, double cx, double cy, double s, Color tinta) {
        double radio = s * 0.92;
        double grosor = s * 0.30;
        g.setColor(tinta);
        g.setStroke(new BasicStroke((float) grosor, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 3; i++) {
            double inicio = Math.toRadians(-90 + i * 120 + 14);
            double fin = Math.toRadians(-90 + i * 120 + 84);
            Path2D arco = new Path2D.Double();
            for (int k = 0; k <= 16; k++) {
                double t = inicio + (fin - inicio) * k / 16.0;
                double x = cx + radio * Math.cos(t);
                double y = cy + radio * Math.sin(t);
                if (k == 0) arco.moveTo(x, y);
                else arco.lineTo(x, y);
            }
            g.draw(arco);
            // punta de flecha en el sentido del giro
            double px = cx + radio * Math.cos(fin), py = cy + radio * Math.sin(fin);
            double tx = -Math.sin(fin), ty = Math.cos(fin);
            double nx = Math.cos(fin), ny = Math.sin(fin);
            double largo = grosor * 1.35, ancho = grosor * 1.15;
            Path2D punta = new Path2D.Double();
            punta.moveTo(px + tx * largo, py + ty * largo);
            punta.lineTo(px + nx * ancho, py + ny * ancho);
            punta.lineTo(px - nx * ancho, py - ny * ancho);
            punta.closePath();
            g.fill(punta);
        }
    }

    public static Path2D formaGota(double cx, double cy, double s) {
        Path2D p = new Path2D.Double();
        p.moveTo(cx, cy - s * 1.05);
        p.curveTo(cx + s * 0.18, cy - s * 0.65, cx + s * 0.72, cy - s * 0.15, cx + s * 0.72, cy + s * 0.3);
        p.curveTo(cx + s * 0.72, cy + s * 0.72, cx + s * 0.4, cy + s * 1.0, cx, cy + s * 1.0);
        p.curveTo(cx - s * 0.4, cy + s * 1.0, cx - s * 0.72, cy + s * 0.72, cx - s * 0.72, cy + s * 0.3);
        p.curveTo(cx - s * 0.72, cy - s * 0.15, cx - s * 0.18, cy - s * 0.65, cx, cy - s * 1.05);
        p.closePath();
        return p;
    }

    public static void gota(Graphics2D g, double cx, double cy, double s, Color tinta, Color fondo) {
        g.setColor(tinta);
        g.fill(formaGota(cx, cy, s));
        g.setColor(fondo);
        g.setStroke(new BasicStroke((float) (s * 0.13), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D brillo = new Path2D.Double();
        brillo.moveTo(cx - s * 0.38, cy + s * 0.25);
        brillo.quadTo(cx - s * 0.38, cy + s * 0.68, cx - s * 0.02, cy + s * 0.72);
        g.draw(brillo);
    }

    public static void foco(Graphics2D g, double cx, double cy, double s, Color tinta, Color fondo) {
        g.setColor(tinta);
        double r = s * 0.68;
        double cyBulbo = cy - s * 0.22;
        Path2D bulbo = new Path2D.Double();
        bulbo.append(new Ellipse2D.Double(cx - r, cyBulbo - r, r * 2, r * 2), false);
        g.fill(bulbo);
        Path2D cuello = new Path2D.Double();
        cuello.moveTo(cx - r * 0.78, cyBulbo + r * 0.55);
        cuello.lineTo(cx + r * 0.78, cyBulbo + r * 0.55);
        cuello.lineTo(cx + r * 0.5, cy + s * 0.62);
        cuello.lineTo(cx - r * 0.5, cy + s * 0.62);
        cuello.closePath();
        g.fill(cuello);
        // rosca
        g.fill(new RoundRectangle2D.Double(cx - r * 0.5, cy + s * 0.68, r, s * 0.16, s * 0.1, s * 0.1));
        g.fill(new RoundRectangle2D.Double(cx - r * 0.38, cy + s * 0.9, r * 0.76, s * 0.16, s * 0.12, s * 0.12));
        // brillo interior
        g.setColor(fondo);
        g.setStroke(new BasicStroke((float) (s * 0.12), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D brillo = new Path2D.Double();
        brillo.moveTo(cx - r * 0.5, cyBulbo + r * 0.05);
        brillo.quadTo(cx - r * 0.5, cyBulbo - r * 0.5, cx - r * 0.02, cyBulbo - r * 0.55);
        g.draw(brillo);
    }

    public static void brote(Graphics2D g, double cx, double cy, double s, Color tinta) {
        g.setColor(tinta);
        g.setStroke(new BasicStroke((float) (s * 0.24), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(cx + s * 0.02, cy + s * 0.95, cx + s * 0.02, cy - s * 0.05));
        g.fill(hoja(cx, cy - s * 0.05, -s * 1.0, -s * 0.62, s * 0.36));
        g.fill(hoja(cx, cy - s * 0.12, s * 1.05, -s * 0.85, s * 0.42));
    }

    /** Hoja con base en (bx, by) que apunta hacia (bx + dx, by + dy). */
    public static Path2D hoja(double bx, double by, double dx, double dy, double ancho) {
        double largo = Math.hypot(dx, dy);
        double ux = dx / largo, uy = dy / largo;
        double nx = -uy, ny = ux;
        double tx = bx + dx, ty = by + dy;
        double mx = bx + dx * 0.5, my = by + dy * 0.5;
        Path2D p = new Path2D.Double();
        p.moveTo(bx, by);
        p.quadTo(mx + nx * ancho * 1.6, my + ny * ancho * 1.6, tx, ty);
        p.quadTo(mx - nx * ancho * 1.6, my - ny * ancho * 1.6, bx, by);
        p.closePath();
        return p;
    }

    private static final Color TRAZO = new Color(0x2A2340);
    private static final Color DORADO = new Color(0xFFC83D);

    /** EcoMoneda dorada con una hojita. */
    public static void moneda(Graphics2D g, double cx, double cy, double r) {
        Ellipse2D borde = new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2);
        g.setColor(DORADO);
        g.fill(borde);
        g.setColor(new Color(0xFFE38A));
        g.fill(new Ellipse2D.Double(cx - r * 0.72, cy - r * 0.72, r * 1.44, r * 1.44));
        g.setColor(TRAZO);
        g.setStroke(new BasicStroke((float) Math.max(1.2, r * 0.14)));
        g.draw(borde);
        brote(g, cx, cy + r * 0.05, r * 0.5, new Color(0xC98A00));
    }

    private static final Path2D FORMA_ESTRELLA_BASE = crearFormaEstrellaBase();

    private static Path2D crearFormaEstrellaBase() {
        Path2D p = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double radio = i % 2 == 0 ? 1.0 : 0.46;
            double a = -Math.PI / 2 + i * Math.PI / 5;
            double x = Math.cos(a) * radio, y = Math.sin(a) * radio;
            if (i == 0) p.moveTo(x, y);
            else p.lineTo(x, y);
        }
        p.closePath();
        return p;
    }

    public static Path2D formaEstrella(double cx, double cy, double r) {
        Path2D p = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double radio = i % 2 == 0 ? r : r * 0.46;
            double a = -Math.PI / 2 + i * Math.PI / 5;
            double x = cx + Math.cos(a) * radio, y = cy + Math.sin(a) * radio;
            if (i == 0) p.moveTo(x, y);
            else p.lineTo(x, y);
        }
        p.closePath();
        return p;
    }

    /** Estrella de calificación, llena (dorada) o vacía (gris). */
    public static void estrella(Graphics2D g0, double cx, double cy, double r, boolean llena) {
        Graphics2D g = (Graphics2D) g0.create();
        g.translate(cx, cy);
        g.scale(r, r);
        g.setColor(llena ? DORADO : new Color(0xD9D6E8));
        g.fill(FORMA_ESTRELLA_BASE);
        g.setColor(llena ? TRAZO : new Color(0xB9B4CF));
        g.setStroke(new BasicStroke((float) Math.max(1.2 / r, 0.12), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(FORMA_ESTRELLA_BASE);
        g.dispose();
    }

    /** Medalla con cinta. Si no está ganada se ve gris con un signo de pregunta. */
    public static void medalla(Graphics2D g, ecorescate.modelo.Medalla m, double cx, double cy, double r, boolean ganada) {
        Color c = ganada ? new Color(m.getColor()) : new Color(0xC9C6D8);
        Path2D cinta = new Path2D.Double();
        cinta.moveTo(cx - r * 0.7, cy - r * 1.9);
        cinta.lineTo(cx - r * 0.1, cy - r * 0.6);
        cinta.lineTo(cx + r * 0.1, cy - r * 0.6);
        cinta.lineTo(cx + r * 0.7, cy - r * 1.9);
        cinta.lineTo(cx + r * 0.25, cy - r * 1.9);
        cinta.lineTo(cx, cy - r * 1.3);
        cinta.lineTo(cx - r * 0.25, cy - r * 1.9);
        cinta.closePath();
        g.setColor(ganada ? Tema.oscurecer(c, 0.15) : new Color(0xB5B1C8));
        g.fill(cinta);
        Ellipse2D disco = new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2);
        g.setColor(ganada ? DORADO : new Color(0xDDDAE8));
        g.fill(disco);
        g.setColor(c);
        g.fill(new Ellipse2D.Double(cx - r * 0.76, cy - r * 0.76, r * 1.52, r * 1.52));
        g.setColor(TRAZO);
        g.setStroke(new BasicStroke((float) Math.max(1.4, r * 0.08)));
        g.draw(disco);
        g.draw(cinta);
        if (!ganada) {
            g.setColor(Color.WHITE);
            g.setFont(Tema.fuente(java.awt.Font.BOLD, (float) (r * 1.1)));
            Tema.centrado(g, "?", cx, cy + r * 0.38);
            return;
        }
        Color tinta = Color.WHITE;
        double s = r * 0.42;
        switch (m) {
            case RECICLADOR: reciclaje(g, cx, cy, s, tinta); break;
            case GUARDIAN: case CIEN_LITROS: gota(g, cx, cy, s, tinta, c); break;
            case CAZAVATIOS: foco(g, cx, cy, s, tinta, c); break;
            case MANO_VERDE: brote(g, cx, cy, s, tinta); break;
            default:
                g.setColor(tinta);
                g.fill(formaEstrella(cx, cy, r * 0.5));
        }
    }

    /** Ícono de la ventana: hoja verde dentro de un círculo. */
    public static BufferedImage imagenAplicacion(int tam) {
        BufferedImage img = new BufferedImage(tam, tam, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Tema.suavizar(g);
        g.setColor(Tema.PRINCIPAL);
        g.fill(new Ellipse2D.Double(0, 0, tam, tam));
        brote(g, tam / 2.0, tam / 2.0, tam * 0.3, Color.WHITE);
        g.dispose();
        return img;
    }
}
