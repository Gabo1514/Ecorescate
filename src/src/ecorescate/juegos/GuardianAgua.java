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
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * RF-02. Lavaderos del colegio. Los niños se cepillan, se enjabonan o se enjuagan.
 * El jugador cierra el agua que se desperdicia (caños abandonados, goteos, o abiertos
 * mientras alguien se cepilla o se enjabona) y deja abierto el caño de quien se enjuaga.
 */
public class GuardianAgua extends Minijuego {

    private enum Actividad {
        NADA, CEPILLA, ENJABONA, ENJUAGA
    }

    private static class Lavadero {
        final double cx;
        final Rectangle2D zona;
        boolean abierto, goteo, avisado, interrumpido;
        Actividad actividad = Actividad.NADA;
        Estilo.Apariencia nino;
        double tiempoActividad, tiempoAbierto, aparicion, salida, gracias, cerroSolo, giroManija, agua, proximaGota;

        Lavadero(double cx) {
            this.cx = cx;
            this.zona = new Rectangle2D.Double(cx - 96, 330, 166, 230);
        }

        boolean hayNino() {
            return actividad != Actividad.NADA;
        }

        double ninoX() {
            return cx - 46;
        }

        boolean libre() {
            return !abierto && !hayNino() && salida <= 0;
        }

        /** El agua sale sin que nadie la aproveche. */
        boolean desperdicia() {
            return abierto && actividad != Actividad.ENJUAGA;
        }
    }

    private static class Gota {
        double x, y, vy, limite;
    }

    private static final Color AGUA = new Color(0x3BA3D9);
    private static final Color AGUA_OSCURA = new Color(0x2479A8);
    private static final double MESA = 418;
    private static final double PISO_NINO = 556;
    private static final double ESCALA_NINO = 2.0;

    private final List<Lavadero> lavaderos = new ArrayList<>();
    private final List<Gota> gotas = new ArrayList<>();
    private BufferedImage fondoCache;
    private double proximo;
    private double litrosAhorrados;
    private boolean vioEnjabona, vioGoteo, vioEnjuaga;

    public GuardianAgua(Estudiante estudiante, ModuloImpactoSocial modulo) {
        super(TipoJuego.AGUA, estudiante, modulo);
    }

    @Override
    protected String motivoDerrota() {
        return "El tanque del colegio se quedó vacío";
    }

    @Override
    protected void reiniciar() {
        lavaderos.clear();
        gotas.clear();
        for (int i = 0; i < 4; i++) lavaderos.add(new Lavadero(150 + i * 186));
        proximo = 1.0;
        litrosAhorrados = 0;
        vioEnjabona = vioGoteo = vioEnjuaga = false;
    }

    // ---------------------------------------------------------------- reglas

    private void nuevoEvento() {
        List<Lavadero> libres = new ArrayList<>();
        for (Lavadero l : lavaderos) if (l.libre()) libres.add(l);
        if (libres.isEmpty()) return;
        Lavadero l = libres.get(azar.nextInt(libres.size()));

        List<String> opciones = new ArrayList<>();
        opciones.add("abandonado");
        opciones.add("cepilla");
        if (nivel >= 2) opciones.add("enjabona");
        if (nivel >= 3) opciones.add("goteo");
        if (nivel >= 4) {
            opciones.add("enjuaga");
            opciones.add("enjuaga");
        }
        // lo que piden los retos aparece más seguido
        if (faltaReto("ABANDONADO") > 0) opciones.add("abandonado");
        if (faltaReto("CEPILLA") > 0) opciones.add("cepilla");
        if (faltaReto("ENJABONA") > 0) opciones.add("enjabona");
        if (faltaReto("GOTEO") > 0) opciones.add("goteo");
        if (faltaReto("RESPETA") > 0) opciones.add("enjuaga");
        String evento = opciones.get(azar.nextInt(opciones.size()));
        l.abierto = true;
        l.goteo = false;
        l.tiempoAbierto = 0;
        l.avisado = false;
        switch (evento) {
            case "cepilla":
                ponerNino(l, Actividad.CEPILLA, progresion(8, 5));
                break;
            case "enjabona":
                ponerNino(l, Actividad.ENJABONA, progresion(8, 5));
                if (!vioEnjabona) {
                    vioEnjabona = true;
                    mostrarMensaje("Nuevo: alguien se enjabona con el caño abierto",
                            "Mientras te enjabonas no necesitas agua: ¡ciérralo!");
                }
                break;
            case "goteo":
                l.goteo = true;
                if (!vioGoteo) {
                    vioGoteo = true;
                    mostrarMensaje("Nuevo: ¡un caño que gotea!", "Gota a gota también se pierde mucha agua. Ciérralo.");
                }
                break;
            case "enjuaga":
                ponerNino(l, Actividad.ENJUAGA, aleatorio(2.8, 3.8));
                if (!vioEnjuaga) {
                    vioEnjuaga = true;
                    mostrarMensaje("Nuevo: alguien se enjuaga las manos",
                            "Para enjuagarse sí necesita el agua. ¡No lo cierres, espera a que termine!");
                }
                break;
            default:
                break;
        }
    }

    private void ponerNino(Lavadero l, Actividad a, double duracion) {
        l.actividad = a;
        l.tiempoActividad = duracion;
        l.nino = Estilo.Apariencia.aleatoria(azar);
        l.aparicion = 0;
        l.gracias = 0;
        l.interrumpido = false;
    }

    private void retirarNino(Lavadero l) {
        l.actividad = Actividad.NADA;
        l.salida = 0.45;
    }

    @Override
    protected void actualizar(double dt) {
        proximo -= dt;
        if (proximo <= 0) {
            nuevoEvento();
            if (nivel >= 6 && azar.nextDouble() < 0.4) nuevoEvento(); // hora punta: dos a la vez
            proximo = progresion(3.4, 1.4) * aleatorio(0.8, 1.2);
        }
        double ritmo = progresion(0.8, 1.5);
        for (Lavadero l : lavaderos) {
            l.giroManija = Math.max(0, l.giroManija - dt * 3);
            l.aparicion = Math.min(1, l.aparicion + dt * 3);
            l.salida = Math.max(0, l.salida - dt);
            l.cerroSolo = Math.max(0, l.cerroSolo - dt);
            if (l.gracias > 0) {
                l.gracias -= dt;
                if (l.gracias <= 0) retirarNino(l);
            }

            if (l.hayNino() && l.gracias <= 0) {
                l.tiempoActividad -= dt;
                if (l.tiempoActividad <= 0) terminarActividad(l);
            }

            if (l.abierto) {
                l.tiempoAbierto += dt;
                if (l.desperdicia()) medidor -= dt * (l.goteo ? 0.007 : 0.02) * ritmo;
                else medidor -= dt * 0.003;
                l.agua = Math.min(1, l.agua + dt * (l.goteo ? 0.05 : 0.18));
                if (l.goteo) {
                    l.proximaGota -= dt;
                    if (l.proximaGota <= 0) {
                        Gota g = new Gota();
                        g.x = l.cx;
                        g.y = 380;
                        g.limite = MESA;
                        gotas.add(g);
                        l.proximaGota = 0.45;
                    }
                }
                if (!l.avisado && !l.hayNino() && l.tiempoAbierto > progresion(8, 5)) {
                    l.avisado = true;
                    fallo("¡Se pierde agua!", l.cx, 320, 0.03);
                }
            } else {
                l.agua = Math.max(0, l.agua - dt * 0.3);
            }
        }
        for (Iterator<Gota> it = gotas.iterator(); it.hasNext(); ) {
            Gota g = it.next();
            g.vy += 900 * dt;
            g.y += g.vy * dt;
            if (g.y >= g.limite) it.remove();
        }
    }

    private void terminarActividad(Lavadero l) {
        if (l.actividad == Actividad.ENJUAGA) {
            if (!l.interrumpido) { // el jugador respetó que necesitaba el agua
                progresoReto("RESPETA", 1);
                sumar(5, "+5 ¡Bien, esperaste!", l.cx, 320);
            }
            if (azar.nextDouble() < 0.6) { // da el buen ejemplo: cierra el caño al terminar
                l.abierto = false;
                l.giroManija = 1;
                l.cerroSolo = 1.6;
            }
            l.tiempoAbierto = 0;
            retirarNino(l);
        } else {
            // se cepilló o se enjabonó con el caño abierto todo el tiempo
            fallo("¡Desperdicio!", "Se " + (l.actividad == Actividad.CEPILLA ? "cepilló" : "enjabonó")
                    + " con el caño abierto todo el tiempo. ¡Ciérralo mientras lo hace!", l.cx, 320, 0.05);
            l.tiempoAbierto = 0;
            l.avisado = true;
            retirarNino(l);
        }
    }

    private Lavadero lavaderoEn(double x, double y) {
        for (Lavadero l : lavaderos) if (l.zona.contains(x, y)) return l;
        return null;
    }

    @Override
    protected boolean sobreObjetivo(double x, double y) {
        Lavadero l = lavaderoEn(x, y);
        return l != null && l.abierto;
    }

    @Override
    protected void alPresionar(double x, double y) {
        Lavadero l = lavaderoEn(x, y);
        if (l == null) return;
        if (!l.abierto) {
            aviso("Ya está cerrado", l.cx, 330);
            return;
        }
        if (l.actividad == Actividad.ENJUAGA && l.gracias <= 0) {
            l.interrumpido = true;
            fallo("¡Se está enjuagando!", "Para enjuagarse sí necesita agua. Espera a que termine y se vaya.", l.cx, 330, 0.05);
            return;
        }
        String situacion = l.actividad == Actividad.CEPILLA ? "CEPILLA" : l.actividad == Actividad.ENJABONA ? "ENJABONA"
                : l.goteo ? "GOTEO" : "ABANDONADO";
        progresoReto(situacion, 1);
        progresoReto("CERRADOS", 1);
        progresoReto("LITROS", 6);
        int rapidez = (int) Math.max(0, 6 - l.tiempoAbierto * 1.5);
        l.abierto = false;
        l.giroManija = 1;
        medidor += 0.035;
        litrosAhorrados += 6;
        acierto(10 + rapidez, l.cx + 24, 330);
        switch (l.actividad) {
            case CEPILLA:
                l.gracias = 1.2;
                mostrarMensaje("¡Bien! Mientras se cepilla, el caño va cerrado.",
                        "Así se ahorran hasta 12 litros cada vez que te lavas los dientes.");
                break;
            case ENJABONA:
                l.gracias = 1.2;
                mostrarMensaje("¡Bien! Mientras se enjabona, no necesita agua.",
                        "Se abre solo un momento para enjuagarse.");
                break;
            default:
                if (l.goteo) mostrarMensaje("¡Goteo detenido!", "Un caño que gotea pierde más de 30 litros al día.");
                break;
        }
        l.goteo = false;
    }

    // ---------------------------------------------------------------- barras

    @Override
    protected String[] resumenPie() {
        return new String[]{"AHORRADO", Math.round(litrosAhorrados) + " litros"};
    }

    @Override
    protected String[] pista() {
        for (Lavadero l : lavaderos) {
            if (l.actividad == Actividad.ENJUAGA && l.abierto) {
                return new String[]{"Alguien se está enjuagando", "Para enjuagarse sí necesita el agua: espera a que termine."};
            }
        }
        if (nivel == 1) return new String[]{"¡Cierra el agua que nadie aprovecha!", "Haz clic en el caño. Si alguien se cepilla, también va cerrado."};
        return new String[]{"Observa qué hace cada niño", "Cepillarse o enjabonarse no necesita el caño abierto."};
    }

    @Override
    protected String textoControles() {
        return "CLIC: CERRAR CAÑO";
    }

    // ---------------------------------------------------------------- escena

    private BufferedImage obtenerFondo() {
        if (fondoCache != null) return fondoCache;
        BufferedImage img = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Tema.suavizar(g);

        // pared de mayólicas
        g.setColor(new Color(0xD9EEF8));
        g.fill(new Rectangle2D.Double(ESC_X, ESC_Y, ESC_W, ESC_H));
        g.setColor(new Color(0xC4E2F0));
        g.setStroke(Estilo.STROKE_1_5);
        for (double x = ESC_X; x < 812; x += 38) g.draw(new Line2D.Double(x, ESC_Y, x, 500));
        for (double y = ESC_Y + 10; y < 500; y += 38) g.draw(new Line2D.Double(ESC_X, y, 812, y));
        g.setColor(new Color(0x8FC3DD));
        g.fill(new Rectangle2D.Double(ESC_X, 160, 790, 8));
        // piso
        g.setColor(new Color(0xBFCFD8));
        g.fill(new Rectangle2D.Double(ESC_X, 500, ESC_W, ESC_ABA - 500));
        g.setColor(new Color(0xAFC0CA));
        for (double x = ESC_X; x < ESC_DER; x += 70) g.draw(new Line2D.Double(x, 500, x - 30, ESC_ABA));

        for (int i = 0; i < 4; i++) {
            double cx = 150 + i * 186;
            RoundRectangle2D espejo = new RoundRectangle2D.Double(cx - 44, 192, 88, 108, 16, 16);
            Estilo.contorno(g, espejo, new Color(0xEAF6FC));
            g.setColor(new Color(255, 255, 255, 200));
            g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(cx - 26, 226, cx - 8, 208));
            g.draw(new Line2D.Double(cx - 26, 248, cx + 8, 214));
            if (cx < 700) {
                double jx = cx + 93;
                Estilo.contorno(g, new RoundRectangle2D.Double(jx - 12, 300, 24, 36, 8, 8), new Color(0xF8BBD0), 1.8f);
                Estilo.contorno(g, new RoundRectangle2D.Double(jx - 4, 290, 8, 12, 3, 3), new Color(0xB0BEC5), 1.6f);
                g.setColor(new Color(255, 255, 255, 180));
                g.fill(new Ellipse2D.Double(jx - 6, 308, 6, 14));
            }
        }

        // letrero
        RoundRectangle2D letrero = new RoundRectangle2D.Double(300, 136, 236, 34, 34, 34);
        Estilo.contorno(g, letrero, Color.WHITE, 2f);
        g.setColor(AGUA);
        g.fill(Iconos.formaGota(326, 153, 9));
        g.setColor(AGUA_OSCURA);
        g.setFont(Tema.fuente(Font.BOLD, 16f));
        g.drawString("¡Cada gota cuenta!", 344, 159);

        // mesada y muebles
        Estilo.contorno(g, new RoundRectangle2D.Double(36, 436, 772, 66, 8, 8), new Color(0xA9BCC6));
        for (int i = 0; i < 4; i++) {
            double cx = 150 + i * 186;
            Estilo.contorno(g, new RoundRectangle2D.Double(cx - 40, 446, 80, 48, 6, 6), new Color(0xB9CAD3), 1.8f);
            g.setColor(Estilo.TRAZO);
            g.fill(new Ellipse2D.Double(cx - 4, 466, 8, 8));
        }
        Estilo.contorno(g, new RoundRectangle2D.Double(28, MESA, 788, 20, 8, 8), new Color(0xEEF3F5));

        // estructura fija del tanque
        double tx = 838, ty = 200, tw = 116, th = 290;
        g.setColor(Estilo.TRAZO);
        g.setFont(Tema.fuente(Font.BOLD, 17f));
        Tema.centrado(g, "TANQUE", tx + tw / 2, ty - 14);
        g.setColor(new Color(0x9FB3BC));
        g.fill(new Rectangle2D.Double(812, ty + th - 40, 30, 14));
        Estilo.contorno(g, new Rectangle2D.Double(820, 170, 10, ty + th - 156), new Color(0x9FB3BC), 1.6f);

        g.dispose();
        fondoCache = img;
        return fondoCache;
    }

    @Override
    protected void dibujarEscena(Graphics2D g) {
        g.drawImage(obtenerFondo(), 0, 0, null);

        for (Lavadero l : lavaderos) {
            // destello que recorre el espejo
            double brillo = (reloj * 0.4 + l.cx * 0.01) % 3;
            if (brillo < 1) {
                double bx = l.cx - 30 + brillo * 60, by = 210 + brillo * 70;
                g.setColor(new Color(255, 255, 255, (int) (200 * Math.sin(brillo * Math.PI))));
                g.fill(Estilo.estrella(bx, by, 7, 0.3, 4, 0));
            }
        }

        dibujarTanque(g);

        // plantita en la esquina de la mesada
        double viento = Math.sin(reloj * 2) * 2;
        Color hoja = new Color(0x4CAF50);
        Estilo.contorno(g, Iconos.hoja(792, MESA - 22, -16 + viento, -22, 6), hoja, 1.6f);
        Estilo.contorno(g, Iconos.hoja(792, MESA - 22, 16 + viento, -24, 6), hoja, 1.6f);
        Estilo.contorno(g, Iconos.hoja(792, MESA - 22, viento, -30, 6), hoja, 1.6f);
        Path2D maceta = new Path2D.Double();
        maceta.moveTo(778, MESA - 24);
        maceta.lineTo(806, MESA - 24);
        maceta.lineTo(802, MESA);
        maceta.lineTo(782, MESA);
        maceta.closePath();
        Estilo.contorno(g, maceta, new Color(0xD9825B), 1.8f);

        for (Lavadero l : lavaderos) dibujarLavadero(g, l);
        g.setColor(AGUA);
        for (Gota gota : gotas) g.fill(Iconos.formaGota(gota.x, gota.y, 5));
        for (Lavadero l : lavaderos) dibujarNino(g, l);
    }

    private void dibujarLavadero(Graphics2D g, Lavadero l) {
        double cx = l.cx;
        boolean encima = l.abierto && l.zona.contains(ratonX, ratonY);

        // taza (encajada en la mesada)
        Ellipse2D borde = new Ellipse2D.Double(cx - 50, MESA - 8, 100, 22);
        Estilo.contorno(g, borde, Color.WHITE, 2f);
        g.setColor(new Color(0xD7E1E6));
        g.fill(new Ellipse2D.Double(cx - 40, MESA - 4, 80, 14));
        if (l.agua > 0.02) {
            double a = 0.4 + 0.6 * l.agua;
            g.setColor(Tema.alfa(AGUA, (int) (130 + 100 * l.agua)));
            g.fill(new Ellipse2D.Double(cx - 40 * a, MESA + 3 - 7 * a, 80 * a, 14 * a));
        }

        // caño
        Color metal = new Color(0xB4C0C6);
        Estilo.contorno(g, new RoundRectangle2D.Double(cx + 18, 358, 14, 64, 6, 6), metal, 2f);
        Path2D brazo = new Path2D.Double();
        brazo.moveTo(cx + 25, 364);
        brazo.quadTo(cx + 25, 352, cx + 12, 352);
        brazo.lineTo(cx + 6, 352);
        brazo.quadTo(cx, 352, cx, 362);
        brazo.lineTo(cx, 374);
        g.setColor(Estilo.TRAZO);
        g.setStroke(new BasicStroke(14f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g.draw(brazo);
        g.setColor(metal);
        g.setStroke(new BasicStroke(10f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g.draw(brazo);
        g.setColor(new Color(0xE3E9EC));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g.draw(brazo);

        // manija en cruz: roja si está abierta
        double mx = cx + 25, my = 344;
        if (encima) {
            g.setColor(new Color(255, 255, 255, 200));
            g.fill(new Ellipse2D.Double(mx - 26, my - 26, 52, 52));
        }
        Graphics2D gm = (Graphics2D) g.create();
        gm.rotate(l.giroManija * Math.PI * 0.75 + (l.abierto ? 0.6 : 0), mx, my);
        Color manija = l.abierto ? new Color(0xEF5A5A) : new Color(0x4C9BC9);
        gm.setColor(Estilo.TRAZO);
        gm.setStroke(new BasicStroke(11f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gm.draw(new Line2D.Double(mx - 16, my, mx + 16, my));
        gm.draw(new Line2D.Double(mx, my - 10, mx, my + 10));
        gm.setColor(manija);
        gm.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gm.draw(new Line2D.Double(mx - 16, my, mx + 16, my));
        gm.draw(new Line2D.Double(mx, my - 10, mx, my + 10));
        gm.dispose();
        Estilo.contorno(g, new Ellipse2D.Double(mx - 5, my - 5, 10, 10), Color.WHITE, 1.6f);

        if (l.abierto && !l.goteo) {
            double fondo = l.actividad == Actividad.ENJUAGA ? 400 : MESA + 2;
            Path2D chorro = new Path2D.Double();
            chorro.moveTo(cx - 5, 376);
            for (double yy = 376; yy <= fondo; yy += 6) chorro.lineTo(cx - 5 + Math.sin(reloj * 18 + yy * 0.25) * 1.3, yy);
            for (double yy = fondo; yy >= 376; yy -= 6) chorro.lineTo(cx + 5 + Math.sin(reloj * 18 + yy * 0.25 + 1) * 1.3, yy);
            chorro.closePath();
            g.setPaint(new GradientPaint((float) (cx - 5), 0, AGUA, (float) (cx + 5), 0, new Color(0x9ADAF6)));
            g.fill(chorro);
            g.setPaint(null);
            g.setColor(Tema.alfa(AGUA, 210));
            for (int i = 0; i < 5; i++) {
                double f = (reloj * 2.5 + i * 0.2) % 1;
                double sx = cx + Math.cos(i * 1.7) * 22 * f, sy = fondo - Math.sin(f * Math.PI) * 12;
                g.fill(new Ellipse2D.Double(sx - 2.5, sy - 2.5, 5, 5));
            }
        }

        if (l.abierto && !l.hayNino() && l.salida <= 0) {
            // aviso de caño abandonado o goteando
            double pulso = 1 + Math.sin(reloj * 8) * 0.08;
            double bx = cx - 36, by = 336;
            Estilo.contorno(g, new Ellipse2D.Double(bx - 15 * pulso, by - 15 * pulso, 30 * pulso, 30 * pulso),
                    l.tiempoAbierto > 4 ? Estilo.ROJO : Estilo.AMARILLO, 2f);
            g.setColor(Estilo.TRAZO);
            g.setFont(Tema.fuente(Font.BOLD, 20f));
            Tema.centrado(g, "!", bx, by + 7);
            if (l.goteo) Estilo.pildora(g, "GOTEA", cx, 302, AGUA_OSCURA, Color.WHITE, 12f);
        }
        if (l.cerroSolo > 0) Estilo.pildora(g, "✓ ¡Cerró el caño!", cx, 302, Tema.VERDE_OK, Color.WHITE, 13f);
    }

    private void dibujarNino(Graphics2D g0, Lavadero l) {
        if (!l.hayNino() && l.salida <= 0) return;
        if (l.nino == null) return;
        Graphics2D g = (Graphics2D) g0.create();
        float alfa = (float) (l.hayNino() ? l.aparicion : l.salida / 0.45);
        g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, Math.max(0, Math.min(1, alfa))));
        // los niños entran y salen caminando por la izquierda
        double recorrido = l.hayNino() ? 1 - l.aparicion : 1 - l.salida / 0.45;
        double nx = l.ninoX() - recorrido * 70, s = ESCALA_NINO;
        double pasoNino = recorrido > 0.02 ? Math.sin(reloj * 14) : 0;
        double bdx = 6, bdy = 13;
        Actividad a = l.hayNino() ? l.actividad : Actividad.NADA;
        double mueve = Math.sin(reloj * 14);
        switch (a) {
            case CEPILLA: bdx = 8 + mueve * 1.5; bdy = -15; break;
            case ENJABONA: bdx = 13 + mueve * 2; bdy = -1; break;
            case ENJUAGA: bdx = 17; bdy = -29 + mueve; break;
            default: break;
        }
        Estilo.nino(g, nx, PISO_NINO, s, l.nino, l.hayNino(), pasoNino, false, bdx, bdy);
        double manoX = nx + s * (3 + bdx), manoY = PISO_NINO + s * (-47 + bdy);

        if (a == Actividad.CEPILLA) {
            Graphics2D gc = (Graphics2D) g.create();
            gc.translate(manoX, manoY);
            gc.rotate(-0.5);
            Estilo.contorno(gc, new RoundRectangle2D.Double(-3, -4, 26, 7, 4, 4), new Color(0x4FC3F7), 1.6f);
            Estilo.contorno(gc, new RoundRectangle2D.Double(20, -9, 8, 9, 2, 2), Color.WHITE, 1.4f);
            gc.dispose();
            g.setColor(Color.WHITE);
            double bocaX = nx + s * 14, bocaY = PISO_NINO + s * -62;
            for (int i = 0; i < 3; i++) {
                double r = 3 + i;
                Estilo.contorno(g, new Ellipse2D.Double(bocaX + i * 5 - r, bocaY + Math.sin(reloj * 6 + i) * 2 - r, r * 2, r * 2), Color.WHITE, 1.2f);
            }
        } else if (a == Actividad.ENJABONA) {
            for (int i = 0; i < 5; i++) {
                double ang = reloj * 2 + i * 1.3;
                double r = 4 + (i % 3) * 2;
                double bx = manoX + Math.cos(ang) * 14, by = manoY - 6 + Math.sin(ang) * 10;
                Estilo.contorno(g, new Ellipse2D.Double(bx - r, by - r, r * 2, r * 2), new Color(255, 255, 255, 230), 1.3f);
            }
        }

        // burbuja con lo que está haciendo
        if (a != Actividad.NADA) {
            double bx = nx + 4, by = 360;
            Estilo.burbujaIcono(g, bx, by, 21);
            if (l.gracias > 0) {
                g.setColor(Estilo.ROSADO);
                g.fill(Estilo.corazon(bx, by + 2, 12));
            } else if (a == Actividad.CEPILLA) {
                Graphics2D gi = (Graphics2D) g.create();
                gi.translate(bx, by);
                gi.rotate(-0.6);
                Estilo.contorno(gi, new RoundRectangle2D.Double(-13, -3, 22, 6, 3, 3), new Color(0x4FC3F7), 1.4f);
                Estilo.contorno(gi, new RoundRectangle2D.Double(6, -8, 8, 8, 2, 2), Color.WHITE, 1.2f);
                gi.dispose();
            } else if (a == Actividad.ENJABONA) {
                Estilo.contorno(g, new RoundRectangle2D.Double(bx - 11, by - 3, 22, 12, 6, 6), new Color(0xF8BBD0), 1.4f);
                Estilo.contorno(g, new Ellipse2D.Double(bx - 2, by - 12, 8, 8), Color.WHITE, 1.2f);
                Estilo.contorno(g, new Ellipse2D.Double(bx - 10, by - 10, 6, 6), Color.WHITE, 1.2f);
            } else {
                g.setColor(AGUA);
                g.fill(Iconos.formaGota(bx - 5, by, 7));
                g.setColor(new Color(0xF5C9A0));
                g.fill(new RoundRectangle2D.Double(bx, by - 4, 11, 14, 6, 6));
            }
        }
        g.dispose();
    }

    private void dibujarTanque(Graphics2D g) {
        double x = 838, y = 200, w = 116, h = 290;
        RoundRectangle2D cuerpo = new RoundRectangle2D.Double(x, y, w, h, 30, 30);
        g.setColor(Color.WHITE);
        g.fill(cuerpo);
        double superficie = y + h - h * medidor;
        Path2D ola = new Path2D.Double();
        ola.moveTo(x, y + h);
        for (double xx = x; xx <= x + w; xx += 6) ola.lineTo(xx, superficie + Math.sin(reloj * 3 + xx * 0.08) * 4);
        ola.lineTo(x + w, y + h);
        ola.closePath();

        Shape clipAnterior = g.getClip();
        g.clip(cuerpo);
        g.setPaint(new GradientPaint(0, (float) superficie, new Color(0x6CC3EC), 0, (float) (y + h), AGUA_OSCURA));
        g.fill(ola);
        g.setPaint(null);
        g.setClip(clipAnterior);

        g.setColor(Estilo.TRAZO);
        g.setStroke(Estilo.STROKE_2_5);
        g.draw(cuerpo);
        g.setFont(Tema.fuente(Font.BOLD, 28f));
        double ty = Tema.limitar(superficie + 40, y + 46, y + h - 14);
        g.setColor(medidor > 0.15 ? Color.WHITE : Tema.ROJO);
        Tema.centrado(g, Math.round(medidor * 100) + "%", x + w / 2, ty);
    }
}
