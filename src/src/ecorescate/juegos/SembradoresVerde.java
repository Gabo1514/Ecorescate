package ecorescate.juegos;

import ecorescate.modelo.Estudiante;
import ecorescate.modelo.ModuloImpactoSocial;
import ecorescate.modelo.TipoJuego;
import ecorescate.ui.Iconos;
import ecorescate.ui.Tema;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * RF-04. Biohuerto del colegio: el niño siembra, riega solo cuando la tierra está seca
 * y recoge la basura y las plantas secas para que el área verde crezca sana.
 */
public class SembradoresVerde extends Minijuego {

    private enum Herramienta {
        SEMILLA("Semilla", '1'), REGADERA("Regadera", '2'), RECOGER("Recoger", '3');

        final String nombre;
        final char tecla;

        Herramienta(String nombre, char tecla) {
            this.nombre = nombre;
            this.tecla = tecla;
        }
    }

    private enum EstadoParcela { VACIA, CRECIENDO, FLORECIDA, SECA }

    private static class Parcela {
        final Rectangle2D rect;
        EstadoParcela estado = EstadoParcela.VACIA;
        double agua, crecimiento, sinAgua, florTiempo, riego;
        boolean basura;
        int tipoBasura;
        Color flor;

        Parcela(Rectangle2D rect) {
            this.rect = rect;
        }

        double cx() {
            return rect.getCenterX();
        }

        double base() {
            return rect.getY() + rect.getHeight() * 0.64;
        }
    }

    private static final Color[] FLORES = {
        new Color(0xF25F8C), new Color(0xFFD23F), new Color(0xB57EDC), new Color(0xFF8C42), new Color(0xFFFFFF)
    };
    private static final double SED = 0.35, HUMEDA = 0.6;
    private static final Color TIERRA_SECA = new Color(0xC99C6E), TIERRA_HUMEDA = new Color(0x6E4529);

    private final Parcela[] parcelas = new Parcela[12];
    private final Rectangle2D[] botones = new Rectangle2D[Herramienta.values().length];
    private BufferedImage fondoCache;
    private Herramienta herramienta = Herramienta.SEMILLA;
    private double proximaBasura;
    private int plantas;

    public SembradoresVerde(Estudiante estudiante, ModuloImpactoSocial modulo) {
        super(TipoJuego.VERDE, estudiante, modulo);
        double ancho = 150, alto = 88, sep = 22;
        double x0 = (ANCHO - (4 * ancho + 3 * sep)) / 2;
        for (int i = 0; i < parcelas.length; i++) {
            int fila = i / 4, col = i % 4;
            parcelas[i] = new Parcela(new Rectangle2D.Double(x0 + col * (ancho + sep), 240 + fila * (alto + 18), ancho, alto));
        }
        for (int i = 0; i < botones.length; i++) {
            botones[i] = new Rectangle2D.Double(32 + i * 124, 587, 116, 42);
        }
    }

    @Override
    protected String motivoDerrota() {
        return "El biohuerto se quedó sin cuidado";
    }

    @Override
    protected void reiniciar() {
        for (Parcela p : parcelas) {
            p.estado = EstadoParcela.VACIA;
            p.basura = false;
            p.agua = 0;
            p.crecimiento = 0;
            p.sinAgua = 0;
            p.riego = 0;
        }
        for (int i = 0; i < 3; i++) ponerBasura();
        herramienta = Herramienta.SEMILLA;
        proximaBasura = 7;
        plantas = 0;
    }

    private void ponerBasura() {
        for (int intento = 0; intento < 20; intento++) {
            Parcela p = parcelas[azar.nextInt(parcelas.length)];
            if (!p.basura && p.estado != EstadoParcela.FLORECIDA) {
                p.basura = true;
                p.tipoBasura = azar.nextInt(3);
                return;
            }
        }
    }

    // ---------------------------------------------------------------- reglas

    @Override
    protected void actualizar(double dt) {
        proximaBasura -= dt;
        if (proximaBasura <= 0) {
            ponerBasura();
            proximaBasura = progresion(8, 3.5) * aleatorio(0.8, 1.2) * (faltaReto("BASURA") > 0 ? 0.6 : 1);
        }
        double secado = progresion(0.045, 0.075);
        for (Parcela p : parcelas) {
            p.riego = Math.max(0, p.riego - dt);
            if (p.basura) medidor -= dt * 0.008;
            switch (p.estado) {
                case CRECIENDO:
                    p.agua = Math.max(0, p.agua - dt * secado);
                    if (p.agua > 0 && !p.basura) p.crecimiento += dt * 0.045;
                    if (p.crecimiento >= 1) {
                        p.estado = EstadoParcela.FLORECIDA;
                        p.florTiempo = 3;
                        p.flor = FLORES[azar.nextInt(FLORES.length)];
                        plantas++;
                        medidor += 0.05;
                        progresoReto("FLORES", 1);
                        acierto(20, p.cx(), p.rect.getY() - 6);
                    } else if (p.agua <= 0) {
                        p.sinAgua += dt;
                        if (p.sinAgua > 3) {
                            p.estado = EstadoParcela.SECA;
                            fallo("¡Se secó!", "Esa planta se quedó sin agua. Riega cuando veas la gota roja y la tierra clara.",
                                    p.cx(), p.rect.getY() - 6, 0.1);
                        }
                    }
                    break;
                case FLORECIDA:
                    p.florTiempo -= dt;
                    if (p.florTiempo <= 0) {
                        p.estado = EstadoParcela.VACIA;
                        p.agua = 0;
                        chispas(p.cx(), p.base() - 30, color, 10);
                    }
                    break;
                case SECA:
                    medidor -= dt * 0.006;
                    break;
                default:
                    break;
            }
        }
    }

    private Parcela parcelaEn(double x, double y) {
        for (Parcela p : parcelas) if (p.rect.contains(x, y)) return p;
        return null;
    }

    @Override
    protected boolean sobreObjetivo(double x, double y) {
        if (parcelaEn(x, y) != null) return true;
        for (Rectangle2D b : botones) if (b.contains(x, y)) return true;
        return false;
    }

    @Override
    protected void alTecla(char tecla) {
        for (Herramienta h : Herramienta.values()) if (h.tecla == tecla) herramienta = h;
    }

    @Override
    protected void alPresionar(double x, double y) {
        for (int i = 0; i < botones.length; i++) {
            if (botones[i].contains(x, y)) {
                herramienta = Herramienta.values()[i];
                return;
            }
        }
        Parcela p = parcelaEn(x, y);
        if (p == null) return;
        double tx = p.cx(), ty = p.rect.getY() - 6;
        switch (herramienta) {
            case SEMILLA:
                if (p.basura) aviso("Primero recoge la basura", tx, ty);
                else if (p.estado == EstadoParcela.SECA) aviso("Primero retira la planta seca", tx, ty);
                else if (p.estado != EstadoParcela.VACIA) aviso("Aquí ya hay una planta", tx, ty);
                else {
                    p.estado = EstadoParcela.CRECIENDO;
                    p.agua = 0.7;
                    p.crecimiento = 0;
                    p.sinAgua = 0;
                    progresoReto("SIEMBRA", 1);
                    sumar(2, "+2 ¡Sembrado!", tx, ty);
                }
                break;
            case REGADERA:
                if (p.estado == EstadoParcela.CRECIENDO) {
                    p.riego = 0.8;
                    if (p.agua < HUMEDA) {
                        int puntos = p.agua < SED ? 8 : 5; // regar justo cuando tiene sed vale más
                        p.agua = 1;
                        p.sinAgua = 0;
                        progresoReto("RIEGO", 1);
                        sumar(puntos, "+" + puntos + " ¡Regada!", tx, ty);
                    } else {
                        fallo("¡Demasiada agua!", "La tierra todavía está húmeda (oscura). Regar de más desperdicia agua y pudre las raíces.",
                                tx, ty, 0.02);
                    }
                } else if (p.estado == EstadoParcela.SECA) {
                    aviso("Está seca: recógela", tx, ty);
                } else {
                    aviso("Primero siembra una semilla", tx, ty);
                }
                break;
            case RECOGER:
            default:
                if (p.basura) {
                    p.basura = false;
                    progresoReto("BASURA", 1);
                    sumar(5, "+5 ¡Limpio!", tx, ty);
                } else if (p.estado == EstadoParcela.SECA) {
                    p.estado = EstadoParcela.VACIA;
                    p.agua = 0;
                    sumar(1, "Tierra lista", tx, ty);
                } else {
                    aviso("No hay nada que recoger", tx, ty);
                }
                break;
        }
    }

    // ---------------------------------------------------------------- barras

    @Override
    protected String[] pista() {
        int sed = 0, basura = 0, vacias = 0;
        for (Parcela p : parcelas) {
            if (p.estado == EstadoParcela.CRECIENDO && p.agua < SED) sed++;
            if (p.basura) basura++;
            if (p.estado == EstadoParcela.VACIA && !p.basura) vacias++;
        }
        if (sed > 0) return new String[]{"¡Hay plantas con sed!", "La gota roja y la tierra clara te avisan: usa la regadera (2)."};
        if (basura > 2) return new String[]{"Hay mucha basura en el biohuerto", "La basura no deja crecer las plantas: usa Recoger (3)."};
        if (vacias > 6) return new String[]{"Hay mucha tierra libre", "Siembra semillas (1) para que crezcan nuevas plantas."};
        return new String[]{"Tierra oscura = húmeda", "Riega solo cuando la tierra se vea clara y seca."};
    }

    @Override
    protected void dibujarPie(Graphics2D g) {
        g.setColor(Estilo.MORADO);
        g.fill(new RoundRectangle2D.Double(22, 582, 956, 52, 22, 22));
        for (int i = 0; i < botones.length; i++) {
            Herramienta h = Herramienta.values()[i];
            Rectangle2D b = botones[i];
            boolean activa = h == herramienta;
            boolean encima = b.contains(ratonX, ratonY);
            RoundRectangle2D forma = new RoundRectangle2D.Double(b.getX(), b.getY(), b.getWidth(), b.getHeight(), 14, 14);
            g.setColor(activa ? Estilo.AMARILLO : encima ? new Color(0x5D4FA0) : Estilo.MORADO_MEDIO);
            g.fill(forma);
            dibujarIconoHerramienta(g, h, b.getX() + 22, b.getCenterY(), 0.62);
            g.setColor(activa ? Estilo.MORADO : Color.WHITE);
            g.setFont(Tema.fuente(Font.BOLD, 15.5f));
            g.drawString(h.nombre, (float) (b.getX() + 42), (float) (b.getY() + 20));
            g.setFont(Tema.fuente(Font.PLAIN, 12.5f));
            g.drawString("Tecla " + h.tecla, (float) (b.getX() + 42), (float) (b.getY() + 35));
        }
        dibujarConsejo(g, 412, 958);
    }

    // ---------------------------------------------------------------- escena

    private BufferedImage obtenerFondo() {
        if (fondoCache != null) return fondoCache;
        BufferedImage img = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Tema.suavizar(g);

        g.setPaint(new GradientPaint(0, (float) ESC_Y, new Color(0xA9DDF3), 0, 250, new Color(0xE3F5FB)));
        g.fill(new Rectangle2D.Double(ESC_X, ESC_Y, ESC_W, 120));
        g.setPaint(null);

        Estilo.contorno(g, new Ellipse2D.Double(-120, 196, 520, 140), new Color(0x8CC26F));
        Estilo.contorno(g, new Ellipse2D.Double(560, 204, 560, 130), new Color(0x8CC26F));
        g.setColor(new Color(0x79B85C));
        g.fill(new Rectangle2D.Double(ESC_X, 226, ESC_W, ESC_ABA - 226));
        g.setColor(Estilo.TRAZO);
        g.setStroke(Estilo.STROKE_2_2);
        g.draw(new Line2D.Double(ESC_X, 226, ESC_DER, 226));
        g.setColor(new Color(0x68A54C));
        g.setStroke(Estilo.STROKE_1_6);
        for (int i = 0; i < 50; i++) {
            double hx = 30 + (i * 97) % 940, hy = 236 + (i * 53) % 330;
            g.draw(new Line2D.Double(hx, hy, hx - 3, hy - 8));
            g.draw(new Line2D.Double(hx + 4, hy, hx + 6, hy - 9));
        }
        // letrero
        g.setColor(new Color(0x8A5A34));
        g.fill(new Rectangle2D.Double(86, 196, 8, 40));
        Estilo.contorno(g, new RoundRectangle2D.Double(36, 162, 108, 40, 8, 8), new Color(0xC08A55));
        g.setColor(Color.WHITE);
        g.setFont(Tema.fuente(Font.BOLD, 14f));
        Tema.centrado(g, "BIOHUERTO", 90, 187);

        g.dispose();
        fondoCache = img;
        return fondoCache;
    }

    @Override
    protected void dibujarEscena(Graphics2D g) {
        g.drawImage(obtenerFondo(), 0, 0, null);

        // desde el nivel 5 hace calor: el sol es más grande y anaranjado
        boolean calor = nivel >= 5;
        double sx = 900, sy = 186, rs = calor ? 36 : 28;
        Color sol = calor ? new Color(0xFFA726) : Estilo.AMARILLO;
        g.setColor(sol);
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 10; i++) {
            double a = i * Math.PI / 5 + reloj * 0.3;
            g.draw(new Line2D.Double(sx + Math.cos(a) * (rs + 8), sy + Math.sin(a) * (rs + 8),
                    sx + Math.cos(a) * (rs + 20), sy + Math.sin(a) * (rs + 20)));
        }
        Estilo.contorno(g, new Ellipse2D.Double(sx - rs, sy - rs, rs * 2, rs * 2), sol);
        g.setColor(Estilo.TRAZO);
        g.fill(new Ellipse2D.Double(sx - 9, sy - 6, 5, 6));
        g.fill(new Ellipse2D.Double(sx + 4, sy - 6, 5, 6));
        g.setStroke(Estilo.STROKE_2_0);
        g.draw(new java.awt.geom.Arc2D.Double(sx - 8, sy - 4, 16, 12, 200, 140, java.awt.geom.Arc2D.OPEN));
        if (!calor) {
            for (int i = 0; i < 3; i++) {
                double nx = (i * 330 + reloj * 10) % 1150 - 100;
                g.setColor(new Color(255, 255, 255, 225));
                g.fill(new Ellipse2D.Double(nx, 150 + i * 10, 60, 24));
                g.fill(new Ellipse2D.Double(nx + 22, 140 + i * 10, 50, 34));
                g.fill(new Ellipse2D.Double(nx + 52, 150 + i * 10, 50, 24));
            }
        }

        for (Parcela p : parcelas) dibujarParcela(g, p);
        dibujarBichitos(g);
        dibujarCursor(g);
    }

    /** Mariposas que pasean por el biohuerto y abejas que visitan las flores. */
    private void dibujarBichitos(Graphics2D g0) {
        Color[] alas = {new Color(0xFF8C42), new Color(0x56CCF2), new Color(0xF25F8C)};
        for (int i = 0; i < 3; i++) {
            double x = 500 + Math.sin(reloj * 0.35 + i * 2.1) * 430;
            double y = 330 + Math.sin(reloj * 0.9 + i * 1.7) * 110;
            double aleteo = Math.abs(Math.sin(reloj * 14 + i));
            Graphics2D g = (Graphics2D) g0.create();
            g.translate(x, y);
            g.scale(0.4 + aleteo * 0.6, 1);
            Estilo.contorno(g, new Ellipse2D.Double(-14, -10, 14, 12), alas[i], 1.4f);
            Estilo.contorno(g, new Ellipse2D.Double(0, -10, 14, 12), alas[i], 1.4f);
            Estilo.contorno(g, new Ellipse2D.Double(-11, 0, 11, 9), Tema.aclarar(alas[i], 0.3), 1.4f);
            Estilo.contorno(g, new Ellipse2D.Double(0, 0, 11, 9), Tema.aclarar(alas[i], 0.3), 1.4f);
            g.dispose();
            g0.setColor(Estilo.TRAZO);
            g0.fill(new RoundRectangle2D.Double(x - 2, y - 9, 4, 18, 4, 4));
        }
        for (Parcela p : parcelas) {
            if (p.estado != EstadoParcela.FLORECIDA) continue;
            double bx = p.cx() + Math.cos(reloj * 3 + p.cx()) * 22, by = p.base() - 70 + Math.sin(reloj * 5 + p.cx()) * 8;
            Estilo.contorno(g0, new Ellipse2D.Double(bx - 7, by - 5, 14, 10), new Color(0xFFD23F), 1.4f);
            g0.setColor(Estilo.TRAZO);
            g0.fill(new Rectangle2D.Double(bx - 1, by - 5, 3, 10));
            g0.setColor(new Color(255, 255, 255, 200));
            g0.fill(new Ellipse2D.Double(bx - 4, by - 12, 6, 8));
            g0.fill(new Ellipse2D.Double(bx, by - 12, 6, 8));
        }
    }

    private void dibujarParcela(Graphics2D g, Parcela p) {
        Rectangle2D r = p.rect;
        boolean encima = r.contains(ratonX, ratonY);
        double humedad = p.estado == EstadoParcela.CRECIENDO ? p.agua : p.estado == EstadoParcela.FLORECIDA ? 0.8 : 0.15;
        Color tierra = Tema.mezclar(TIERRA_SECA, TIERRA_HUMEDA, Tema.limitar(humedad / 0.8, 0, 1));
        Estilo.contorno(g, new RoundRectangle2D.Double(r.getX(), r.getY() + 7, r.getWidth(), r.getHeight(), 22, 22), Tema.oscurecer(tierra, 0.35));
        Estilo.contorno(g, new RoundRectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight(), 22, 22),
                encima ? Tema.aclarar(tierra, 0.12) : tierra);
        g.setColor(Tema.oscurecer(tierra, 0.12));
        g.setStroke(Estilo.STROKE_3_0);
        for (int i = 1; i <= 3; i++) {
            double ly = r.getY() + r.getHeight() * i / 4.0;
            g.draw(new Line2D.Double(r.getX() + 16, ly, r.getMaxX() - 16, ly));
        }
        if (humedad < SED) { // grietas de tierra seca
            g.setColor(Tema.oscurecer(tierra, 0.25));
            g.setStroke(Estilo.STROKE_1_5);
            Path2D grieta = new Path2D.Double();
            grieta.moveTo(r.getX() + 24, r.getY() + 14);
            grieta.lineTo(r.getX() + 34, r.getY() + 24);
            grieta.lineTo(r.getX() + 30, r.getY() + 34);
            grieta.moveTo(r.getMaxX() - 30, r.getMaxY() - 16);
            grieta.lineTo(r.getMaxX() - 40, r.getMaxY() - 26);
            grieta.lineTo(r.getMaxX() - 34, r.getMaxY() - 34);
            g.draw(grieta);
        }

        double cx = p.cx(), base = p.base();
        switch (p.estado) {
            case CRECIENDO: dibujarPlanta(g, cx, base, p.crecimiento, false, null); break;
            case FLORECIDA: dibujarPlanta(g, cx, base, 1, false, p.flor); break;
            case SECA: dibujarPlanta(g, cx, base, 0.7, true, null); break;
            default: break;
        }
        if (p.basura) dibujarBasura(g, cx + (p.estado == EstadoParcela.VACIA ? 0 : 42), r.getMaxY() - 24, p.tipoBasura);

        if (p.estado == EstadoParcela.CRECIENDO && p.agua < SED) {
            double pulso = 1 + Math.sin(reloj * 8) * 0.1;
            double bx = cx + 48, by = r.getY() + 20;
            Estilo.burbujaIcono(g, bx, by, 17 * pulso);
            g.setColor(Estilo.ROJO);
            g.fill(Iconos.formaGota(bx, by, 9 * pulso));
        }
        if (p.riego > 0) {
            Graphics2D gr = (Graphics2D) g.create();
            gr.translate(cx - 34, r.getY() - 6);
            gr.rotate(0.5);
            dibujarIconoHerramienta(gr, Herramienta.REGADERA, 0, 0, 0.8);
            gr.dispose();
            g.setColor(new Color(0x4FB3E8));
            for (int i = 0; i < 6; i++) {
                double f = (p.riego * 3 + i * 0.17) % 1;
                g.fill(new Ellipse2D.Double(cx - 16 + i * 7, r.getY() + 10 + f * 40, 4, 7));
            }
        }
    }

    private void dibujarPlanta(Graphics2D g, double cx, double base, double crecimiento, boolean seca, Color flor) {
        Color tallo = seca ? new Color(0x8A6A3A) : new Color(0x2F7D32);
        Color hoja = seca ? new Color(0xB89A5E) : new Color(0x4CAF50);
        g.setColor(new Color(0x5A3820));
        g.fill(new Ellipse2D.Double(cx - 16, base - 5, 32, 12));
        if (crecimiento < 0.2 && !seca) {
            Estilo.contorno(g, new Ellipse2D.Double(cx - 6, base - 9, 12, 10), new Color(0xC8A26B), 1.6f);
            return;
        }
        double altura = 14 + 44 * Math.min(1, crecimiento);
        double caida = seca ? 16 : 0;
        g.setColor(Estilo.TRAZO);
        g.setStroke(new BasicStroke(6.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D t = new Path2D.Double();
        t.moveTo(cx, base);
        t.quadTo(cx + caida * 0.3, base - altura * 0.6, cx + caida, base - altura + caida * 0.4);
        g.draw(t);
        g.setColor(tallo);
        g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(t);
        double tam = 9 + 8 * Math.min(1, crecimiento);
        Estilo.contorno(g, Iconos.hoja(cx, base - altura * 0.35, -tam * 1.6, -tam * (seca ? -0.4 : 0.8), tam * 0.45), hoja, 1.6f);
        Estilo.contorno(g, Iconos.hoja(cx, base - altura * 0.45, tam * 1.6, -tam * (seca ? -0.3 : 0.9), tam * 0.45), hoja, 1.6f);
        if (crecimiento > 0.55) {
            Estilo.contorno(g, Iconos.hoja(cx + caida * 0.5, base - altura * 0.75, -tam * 1.3, -tam * (seca ? -0.5 : 0.9), tam * 0.4), hoja, 1.6f);
            Estilo.contorno(g, Iconos.hoja(cx + caida * 0.5, base - altura * 0.8, tam * 1.3, -tam * (seca ? -0.4 : 1.0), tam * 0.4), hoja, 1.6f);
        }
        if (flor != null) {
            double fx = cx, fy = base - altura - 4;
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI / 3 + reloj;
                Estilo.contorno(g, new Ellipse2D.Double(fx + Math.cos(a) * 9 - 7, fy + Math.sin(a) * 9 - 7, 14, 14), flor, 1.4f);
            }
            Estilo.contorno(g, new Ellipse2D.Double(fx - 6, fy - 6, 12, 12), new Color(0xF4A300), 1.4f);
        }
    }

    private void dibujarBasura(Graphics2D g, double x, double y, int tipo) {
        switch (tipo) {
            case 0: {
                Graphics2D gb = (Graphics2D) g.create();
                gb.rotate(1.25, x, y);
                Estilo.contorno(gb, new RoundRectangle2D.Double(x - 9, y - 18, 18, 34, 9, 9), new Color(170, 215, 240), 1.6f);
                gb.setColor(new Color(0x2F80C0));
                gb.fill(new Rectangle2D.Double(x - 8, y - 6, 16, 9));
                Estilo.contorno(gb, new RoundRectangle2D.Double(x - 4, y - 25, 8, 8, 2, 2), new Color(0x2F80C0), 1.4f);
                gb.dispose();
                break;
            }
            case 1: {
                Estilo.contorno(g, new Ellipse2D.Double(x - 13, y - 12, 26, 24), new Color(0xF2F2EC), 1.6f);
                g.setStroke(Estilo.STROKE_1_5);
                g.draw(new Line2D.Double(x - 7, y - 4, x + 5, y + 3));
                g.draw(new Line2D.Double(x - 2, y - 9, x + 2, y + 1));
                break;
            }
            default: {
                Path2D bolsa = new Path2D.Double();
                bolsa.moveTo(x - 14, y - 6);
                bolsa.quadTo(x - 18, y + 14, x, y + 14);
                bolsa.quadTo(x + 18, y + 14, x + 14, y - 6);
                bolsa.lineTo(x + 8, y - 16);
                bolsa.lineTo(x + 3, y - 8);
                bolsa.lineTo(x - 3, y - 8);
                bolsa.lineTo(x - 8, y - 16);
                bolsa.closePath();
                Estilo.contorno(g, bolsa, new Color(245, 245, 245), 1.6f);
                break;
            }
        }
    }

    private void dibujarCursor(Graphics2D g) {
        if (!jugando() || parcelaEn(ratonX, ratonY) == null) return;
        Estilo.contorno(g, new Ellipse2D.Double(ratonX + 8, ratonY + 8, 40, 40), Color.WHITE, 1.8f);
        dibujarIconoHerramienta(g, herramienta, ratonX + 28, ratonY + 28, 0.62);
    }

    private void dibujarIconoHerramienta(Graphics2D g0, Herramienta h, double cx, double cy, double escala) {
        Graphics2D g = (Graphics2D) g0.create();
        g.translate(cx, cy);
        g.scale(escala, escala);
        switch (h) {
            case SEMILLA: {
                Estilo.contorno(g, new RoundRectangle2D.Double(-20, -26, 40, 52, 6, 6), new Color(0xE9D8A6));
                g.setColor(new Color(0x4E9A45));
                g.fill(new Ellipse2D.Double(-13, -18, 26, 26));
                Iconos.brote(g, 0, -5, 9, Color.WHITE);
                g.setColor(new Color(0x7A5132));
                for (int i = 0; i < 3; i++) g.fill(new Ellipse2D.Double(-12 + i * 9, 13, 6, 8));
                break;
            }
            case REGADERA: {
                Path2D pico = new Path2D.Double();
                pico.moveTo(10, 0);
                pico.lineTo(28, -18);
                pico.lineTo(32, -14);
                pico.lineTo(12, 8);
                pico.closePath();
                Estilo.contorno(g, pico, new Color(0x3C8DC5));
                Estilo.contorno(g, new Ellipse2D.Double(25, -25, 13, 13), new Color(0x3C8DC5));
                g.setColor(Estilo.TRAZO);
                g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Arc2D.Double(-24, -24, 26, 30, 90, 180, Arc2D.OPEN));
                g.setColor(new Color(0x3C8DC5));
                g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Arc2D.Double(-24, -24, 26, 30, 90, 180, Arc2D.OPEN));
                Estilo.contorno(g, new RoundRectangle2D.Double(-18, -12, 30, 30, 8, 8), new Color(0x3C8DC5));
                g.setColor(new Color(0xA8D8F0));
                g.fill(new Rectangle2D.Double(-14, -6, 22, 4));
                break;
            }
            case RECOGER:
            default: {
                Path2D bolsa = new Path2D.Double();
                bolsa.moveTo(-18, -8);
                bolsa.quadTo(-24, 26, 0, 26);
                bolsa.quadTo(24, 26, 18, -8);
                bolsa.lineTo(8, -14);
                bolsa.lineTo(-8, -14);
                bolsa.closePath();
                Estilo.contorno(g, bolsa, new Color(0x2E4A3F));
                g.setColor(new Color(0x2E4A3F));
                g.fill(new Ellipse2D.Double(-10, -24, 9, 12));
                g.fill(new Ellipse2D.Double(1, -24, 9, 12));
                Iconos.reciclaje(g, 0, 7, 9, new Color(0x9BE0A0));
                break;
            }
        }
        g.dispose();
    }
}
