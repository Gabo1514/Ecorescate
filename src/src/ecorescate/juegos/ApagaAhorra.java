package ecorescate.juegos;

import ecorescate.modelo.Estudiante;
import ecorescate.modelo.ModuloImpactoSocial;
import ecorescate.modelo.TipoJuego;
import ecorescate.ui.Tema;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * RF-03. Es de tarde en el colegio: los alumnos entran y salen y se olvidan luces y equipos
 * prendidos. El niño apaga solo lo que nadie usa y desenchufa los cargadores sin celular.
 */
public class ApagaAhorra extends Minijuego {

    private enum TipoEquipo {
        FOCO("la luz", 0.006), TV("el televisor", 0.010), COMPUTADORA("la computadora", 0.010),
        VENTILADOR("el ventilador", 0.008), CARGADOR("el cargador", 0.004);

        final String nombre;
        final double consumo;

        TipoEquipo(String nombre, double consumo) {
            this.nombre = nombre;
            this.consumo = consumo;
        }
    }

    private static class Equipo {
        final TipoEquipo tipo;
        final Rectangle2D zona;
        boolean encendido;
        double desperdicio, giro;

        Equipo(TipoEquipo tipo, Rectangle2D zona) {
            this.tipo = tipo;
            this.zona = zona;
        }
    }

    private static class Ambiente {
        final String nombre;
        final Rectangle2D rect;
        final Equipo foco, aparato, cargador;
        final Color pared;
        final BufferedImage fondoLuz, fondoOscuro;
        double ocupado;
        final List<Estilo.Apariencia> alumnos = new ArrayList<>();

        Ambiente(String nombre, Rectangle2D rect, Equipo foco, Equipo aparato, Equipo cargador, Color pared) {
            this.nombre = nombre;
            this.rect = rect;
            this.foco = foco;
            this.aparato = aparato;
            this.cargador = cargador;
            this.pared = pared;
            this.fondoLuz = crearFondoAula(pared, true);
            this.fondoOscuro = crearFondoAula(pared, false);
        }

        boolean hayGente() {
            return ocupado > 0;
        }

        Equipo[] equipos() {
            return new Equipo[]{foco, aparato, cargador};
        }
    }

    private static final int W_AULA = 296, H_AULA = 178;
    private static final double ALTO_AULA = H_AULA, ANCHO_AULA = W_AULA;
    private static final BufferedImage HALO_FOCO = crearHaloFoco();

    private final List<Ambiente> ambientes = new ArrayList<>();
    private BufferedImage fondoExteriorCache;
    private int nivelFondoCache = -1;
    private double proximaClase, proximoOlvido, proximoCargador;
    private int apagados;
    private boolean vioCargador;

    public ApagaAhorra(Estudiante estudiante, ModuloImpactoSocial modulo) {
        super(TipoJuego.ENERGIA, estudiante, modulo);
    }

    private static BufferedImage crearFondoAula(Color pared, boolean luz) {
        BufferedImage img = new BufferedImage(W_AULA, H_AULA, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Tema.suavizar(g);
        g.setColor(luz ? Tema.aclarar(pared, 0.25) : Tema.mezclar(pared, new Color(0x2C3350), 0.62));
        g.fillRect(0, 0, W_AULA, H_AULA);
        if (luz) {
            float cx = W_AULA / 2f, cy = 48f;
            g.setPaint(new RadialGradientPaint(cx, cy, 170f, new float[]{0f, 1f},
                    new Color[]{new Color(255, 236, 160, 150), new Color(255, 236, 160, 0)}));
            g.fillRect(0, 0, W_AULA, H_AULA);
            g.setPaint(null);
        }
        g.setColor(luz ? new Color(0xC7A27A) : new Color(0x5C5048));
        g.fillRect(0, H_AULA - 14, W_AULA, 14);

        // ventana
        RoundRectangle2D ventana = new RoundRectangle2D.Double(18, 40, 58, 50, 6, 6);
        g.setPaint(new GradientPaint(0, 40, new Color(0x5B5C9A), 0, 90, new Color(0xF0A66A)));
        g.fill(ventana);
        g.setPaint(null);
        g.setColor(Estilo.TRAZO);
        g.setStroke(Estilo.STROKE_2_5);
        g.draw(ventana);
        g.draw(new Line2D.Double(47, 40, 47, 90));
        g.dispose();
        return img;
    }

    private static BufferedImage crearHaloFoco() {
        BufferedImage img = new BufferedImage(68, 68, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Tema.suavizar(g);
        g.setPaint(new RadialGradientPaint(34f, 34f, 34f, new float[]{0f, 1f},
                new Color[]{new Color(255, 240, 150, 210), new Color(255, 240, 150, 0)}));
        g.fill(new Ellipse2D.Double(0, 0, 68, 68));
        g.dispose();
        return img;
    }

    @Override
    protected String motivoDerrota() {
        return "Se gastó toda la energía del colegio";
    }

    @Override
    protected void reiniciar() {
        if (ambientes.isEmpty()) {
            String[] nombres = {"Aula 1.° A", "Biblioteca", "Sala de cómputo", "Aula 5.° B", "Dirección", "Comedor"};
            TipoEquipo[] aparatos = {TipoEquipo.TV, TipoEquipo.VENTILADOR, TipoEquipo.COMPUTADORA,
                TipoEquipo.VENTILADOR, TipoEquipo.COMPUTADORA, TipoEquipo.TV};
            int[] paredes = {0xFBE7C6, 0xDDF0D5, 0xD7E7F7, 0xF7DCD3, 0xE7DDF4, 0xF6EDC9};
            for (int i = 0; i < 6; i++) {
                double rx = 52 + (i % 3) * 300, ry = i < 3 ? 174 : 360;
                Equipo foco = new Equipo(TipoEquipo.FOCO, new Rectangle2D.Double(rx + 120, ry + 18, 56, 56));
                Equipo aparato = new Equipo(aparatos[i], zonaAparato(aparatos[i], rx, ry));
                Equipo cargador = new Equipo(TipoEquipo.CARGADOR, new Rectangle2D.Double(rx + 14, ry + 112, 40, 46));
                ambientes.add(new Ambiente(nombres[i], new Rectangle2D.Double(rx, ry, ANCHO_AULA, ALTO_AULA),
                        foco, aparato, cargador, new Color(paredes[i])));
            }
        } else {
            for (Ambiente a : ambientes) {
                a.ocupado = 0;
                a.alumnos.clear();
                for (Equipo e : a.equipos()) {
                    e.encendido = false;
                    e.desperdicio = 0;
                    e.giro = 0;
                }
            }
        }
        proximaClase = 0.5;
        proximoOlvido = 2.5;
        proximoCargador = 6;
        apagados = 0;
        vioCargador = false;
        entrarAlumnos(ambientes.get(0));
        entrarAlumnos(ambientes.get(4));
    }

    private static Rectangle2D zonaAparato(TipoEquipo tipo, double rx, double ry) {
        switch (tipo) {
            case TV: return new Rectangle2D.Double(rx + 200, ry + 52, 86, 112);
            case COMPUTADORA: return new Rectangle2D.Double(rx + 196, ry + 70, 92, 94);
            default: return new Rectangle2D.Double(rx + 210, ry + 40, 76, 124);
        }
    }

    // ---------------------------------------------------------------- reglas

    private void entrarAlumnos(Ambiente a) {
        a.ocupado = progresion(10, 7) * aleatorio(0.85, 1.15);
        a.alumnos.clear();
        int n = 1 + azar.nextInt(2);
        for (int i = 0; i < n; i++) a.alumnos.add(Estilo.Apariencia.aleatoria(azar));
        a.foco.encendido = true;
        a.foco.desperdicio = 0;
        if (azar.nextDouble() < 0.7) {
            a.aparato.encendido = true;
            a.aparato.desperdicio = 0;
        }
    }

    @Override
    protected void actualizar(double dt) {
        proximaClase -= dt;
        if (proximaClase <= 0) {
            List<Ambiente> libres = new ArrayList<>();
            for (Ambiente a : ambientes) if (!a.hayGente()) libres.add(a);
            if (!libres.isEmpty()) entrarAlumnos(libres.get(azar.nextInt(libres.size())));
            proximaClase = aleatorio(4.0, 6.0);
        }
        proximoOlvido -= dt;
        if (proximoOlvido <= 0) {
            List<Equipo> apagadosLibres = new ArrayList<>();
            for (Ambiente a : ambientes) {
                if (a.hayGente()) continue;
                if (!a.foco.encendido) apagadosLibres.add(a.foco);
                if (!a.aparato.encendido) apagadosLibres.add(a.aparato);
            }
            if (!apagadosLibres.isEmpty()) {
                // se olvidan más seguido los aparatos que piden los retos
                List<Equipo> necesarios = new ArrayList<>();
                for (Equipo x : apagadosLibres) if (faltaReto(x.tipo.name()) > 0) necesarios.add(x);
                List<Equipo> fuente = !necesarios.isEmpty() && azar.nextDouble() < 0.6 ? necesarios : apagadosLibres;
                Equipo e = fuente.get(azar.nextInt(fuente.size()));
                e.encendido = true;
                e.desperdicio = 0;
            }
            proximoOlvido = progresion(4.0, 1.6) * aleatorio(0.8, 1.2);
        }
        if (nivel >= 3) {
            proximoCargador -= dt;
            if (proximoCargador <= 0) {
                Ambiente a = ambientes.get(azar.nextInt(ambientes.size()));
                if (!a.cargador.encendido) {
                    a.cargador.encendido = true;
                    a.cargador.desperdicio = 0;
                    if (!vioCargador) {
                        vioCargador = true;
                        mostrarMensaje("Nuevo: un cargador enchufado sin celular",
                                "Aunque nadie lo use, sigue gastando energía. ¡Desenchúfalo, aunque haya alumnos!");
                    }
                }
                proximoCargador = progresion(9, 5) * aleatorio(0.8, 1.2) * (faltaReto("CARGADOR") > 0 ? 0.6 : 1);
            }
        }

        double ritmo = progresion(0.75, 1.1);
        for (Ambiente a : ambientes) {
            if (a.hayGente()) a.ocupado -= dt; // al salir, se olvidan todo prendido
            for (Equipo e : a.equipos()) {
                if (e.encendido && e.tipo == TipoEquipo.VENTILADOR) e.giro += dt * 14;
                boolean gasta = e.encendido && (!a.hayGente() || e.tipo == TipoEquipo.CARGADOR);
                if (gasta) {
                    e.desperdicio += dt;
                    medidor -= dt * e.tipo.consumo * ritmo;
                }
            }
        }
    }

    private Ambiente ambienteDe(Equipo e) {
        for (Ambiente a : ambientes) {
            for (Equipo x : a.equipos()) if (x == e) return a;
        }
        return null;
    }

    private Equipo equipoEn(double x, double y) {
        for (Ambiente a : ambientes) {
            if (a.cargador.encendido && a.cargador.zona.contains(x, y)) return a.cargador;
            if (a.foco.zona.contains(x, y)) return a.foco;
            if (a.aparato.zona.contains(x, y)) return a.aparato;
        }
        return null;
    }

    @Override
    protected boolean sobreObjetivo(double x, double y) {
        Equipo e = equipoEn(x, y);
        return e != null && e.encendido;
    }

    @Override
    protected void alPresionar(double x, double y) {
        Equipo e = equipoEn(x, y);
        if (e == null) return;
        Ambiente a = ambienteDe(e);
        double tx = e.zona.getCenterX(), ty = e.zona.getY() - 6;
        if (!e.encendido) {
            aviso("Ya está apagado", tx, ty);
        } else if (e.tipo == TipoEquipo.CARGADOR) {
            e.encendido = false;
            apagados++;
            medidor += 0.02;
            progresoReto("CARGADOR", 1);
            progresoReto("APAGADOS", 1);
            acierto(12, tx, ty);
            mostrarMensaje("¡Cargador desenchufado!", "Un cargador sin celular igual gasta energía.");
        } else if (a != null && a.hayGente()) {
            fallo("¡Lo están usando!", "En " + a.nombre + " hay alumnos: ellos necesitan " + e.tipo.nombre
                    + ". Apaga solo lo que nadie usa.", tx, ty, 0.06);
        } else {
            e.encendido = false;
            apagados++;
            int rapidez = (int) Math.max(0, 6 - e.desperdicio * 1.2);
            medidor += 0.02;
            progresoReto(e.tipo.name(), 1);
            progresoReto("APAGADOS", 1);
            acierto(10 + rapidez, tx, ty);
        }
    }

    // ---------------------------------------------------------------- barras

    @Override
    protected String[] resumenPie() {
        return new String[]{"APAGADOS", String.valueOf(apagados)};
    }

    @Override
    protected String[] pista() {
        if (nivel <= 4) return new String[]{"Apaga lo que nadie usa", "Los botones rojos marcan lo que gasta energía sin que nadie lo use."};
        return new String[]{"Mira bien cada ambiente", "Si está vacío, apaga todo. Si hay alumnos, deja lo que usan."};
    }

    @Override
    protected String textoControles() {
        return "CLIC: APAGAR";
    }

    // ---------------------------------------------------------------- escena

    private BufferedImage obtenerFondoExterior() {
        if (fondoExteriorCache != null && nivelFondoCache == nivel) return fondoExteriorCache;
        nivelFondoCache = nivel;
        BufferedImage img = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Tema.suavizar(g);

        double noche = (nivel - 1) / 9.0;
        Color arriba = Tema.mezclar(new Color(0x5B5EA6), new Color(0x141A3A), noche);
        Color abajo = Tema.mezclar(new Color(0xF6B27A), new Color(0x3B3F7A), noche);
        g.setPaint(new GradientPaint(0, (float) ESC_Y, arriba, 0, 480, abajo));
        g.fill(new Rectangle2D.Double(ESC_X, ESC_Y, ESC_W, ESC_H));
        g.setPaint(null);
        if (noche < 0.5) {
            Estilo.contorno(g, new Ellipse2D.Double(900, 470 + noche * 80, 90, 90), new Color(0xFFD27A));
        } else { // luna
            g.setColor(new Color(0xFFF6D5));
            g.fill(new Ellipse2D.Double(900, 150, 54, 54));
            g.setColor(arriba);
            g.fill(new Ellipse2D.Double(914, 142, 50, 50));
        }
        g.setColor(new Color(0x6DB35A));
        g.fill(new Rectangle2D.Double(ESC_X, 544, ESC_W, ESC_ABA - 544));
        g.setColor(Estilo.TRAZO);
        g.setStroke(Estilo.STROKE_2_2);
        g.draw(new Line2D.Double(ESC_X, 544, ESC_DER, 544));

        Path2D techo = new Path2D.Double();
        techo.moveTo(32, 174);
        techo.lineTo(78, 138);
        techo.lineTo(922, 138);
        techo.lineTo(968, 174);
        techo.closePath();
        Estilo.contorno(g, techo, new Color(0xB5513F));
        g.setColor(Color.WHITE);
        g.setFont(Tema.fuente(Font.BOLD, 19f));
        Tema.centrado(g, "I.E. ECORESCATE · ICA", ANCHO / 2.0, 163);

        Estilo.contorno(g, new Rectangle2D.Double(44, 172, 912, 374), new Color(0xD9C3A0));
        g.dispose();
        fondoExteriorCache = img;
        return fondoExteriorCache;
    }

    @Override
    protected void dibujarEscena(Graphics2D g) {
        g.drawImage(obtenerFondoExterior(), 0, 0, null);

        double noche = (nivel - 1) / 9.0;
        if (noche > 0.3) {
            for (int i = 0; i < 36; i++) { // estrellas que titilan en los márgenes del cielo
                double sx = 30 + (i * 137) % 940, sy = 136 + (i * 53) % 330;
                if (sx >= 44 && sx <= 956 && sy >= 172 && sy <= 546) continue;
                double t = Math.sin(reloj * 2 + i) * 0.5 + 0.5;
                g.setColor(new Color(255, 255, 255, (int) (60 + 150 * t * Math.min(1, (noche - 0.3) * 2))));
                g.fill(new Ellipse2D.Double(sx, sy, 2.5, 2.5));
            }
        }

        for (Ambiente a : ambientes) dibujarAmbiente(g, a);
        g.setColor(new Color(0xB9A27D));
        g.fill(new Rectangle2D.Double(46, 352, 908, 8));
        g.setColor(Estilo.TRAZO);
        g.setStroke(Estilo.STROKE_2_0);
        g.draw(new Line2D.Double(44, 352, 956, 352));
        g.draw(new Line2D.Double(44, 360, 956, 360));
    }

    private void dibujarAmbiente(Graphics2D g0, Ambiente a) {
        Rectangle2D r = a.rect;
        double rx = r.getX(), ry = r.getY(), rw = r.getWidth(), rh = r.getHeight();
        Shape clipAnt = g0.getClip();
        g0.clip(r);

        boolean luz = a.foco.encendido;
        g0.drawImage(luz ? a.fondoLuz : a.fondoOscuro, (int) rx, (int) ry, null);

        // alumnos detrás de sus carpetas
        for (int i = 0; i < 2; i++) {
            double px = rx + 92 + i * 58, py = ry + 132;
            if (a.hayGente() && i < a.alumnos.size()) {
                Estilo.nino(g0, px + 20, ry + rh - 6, 1.0, a.alumnos.get(i), true, 0, false, 10, 6);
            }
            Color madera = luz ? new Color(0x9C6B3F) : new Color(0x4D3B2C);
            Estilo.contorno(g0, new RoundRectangle2D.Double(px, py, 44, 9, 4, 4), madera, 1.6f);
            g0.setColor(madera);
            g0.fill(new Rectangle2D.Double(px + 4, py + 9, 5, rh - 14 - (py - ry) - 9));
            g0.fill(new Rectangle2D.Double(px + 35, py + 9, 5, rh - 14 - (py - ry) - 9));
        }

        dibujarFoco(g0, a.foco, rx + rw / 2, ry);
        dibujarAparato(g0, a.aparato, luz);
        dibujarCargador(g0, a.cargador);

        g0.setFont(Tema.fuente(Font.BOLD, 13.5f));
        int w = g0.getFontMetrics().stringWidth(a.nombre) + 18;
        g0.setColor(new Color(23, 20, 50, 215));
        g0.fill(new RoundRectangle2D.Double(rx + 10, ry + 8, w, 22, 22, 22));
        g0.setColor(Color.WHITE);
        g0.drawString(a.nombre, (float) (rx + 19), (float) (ry + 24));
        String estado = a.hayGente() ? "En clase" : "Vacío";
        g0.setFont(Tema.fuente(Font.BOLD, 13f));
        int we = g0.getFontMetrics().stringWidth(estado) + 16;
        g0.setColor(a.hayGente() ? Tema.VERDE_OK : new Color(0x7D8584));
        g0.fill(new RoundRectangle2D.Double(rx + rw - we - 10, ry + 8, we, 22, 22, 22));
        g0.setColor(Color.WHITE);
        g0.drawString(estado, (float) (rx + rw - we - 2), (float) (ry + 24));

        g0.setClip(clipAnt);

        g0.setColor(Estilo.TRAZO);
        g0.setStroke(Estilo.STROKE_2_5);
        g0.draw(r);

        // ayuda visual en las primeras misiones
        if (nivel <= 4) {
            for (Equipo e : a.equipos()) {
                boolean gasta = e.encendido && (!a.hayGente() || e.tipo == TipoEquipo.CARGADOR);
                if (!gasta) continue;
                double bx = e.tipo == TipoEquipo.FOCO ? e.zona.getMaxX() + 6 : e.zona.getCenterX();
                double by = e.tipo == TipoEquipo.FOCO ? e.zona.getCenterY() : e.zona.getY() - 2;
                dibujarAvisoApagar(g0, bx, by);
            }
        }
    }

    private void dibujarFoco(Graphics2D g, Equipo foco, double cx, double techo) {
        double cy = techo + 52;
        g.setColor(Estilo.TRAZO);
        g.setStroke(Estilo.STROKE_2_0);
        g.draw(new Line2D.Double(cx, techo, cx, cy - 18));
        Estilo.contorno(g, new RoundRectangle2D.Double(cx - 7, cy - 22, 14, 10, 3, 3), new Color(0x6B6F70), 1.6f);
        if (foco.encendido) {
            g.drawImage(HALO_FOCO, (int) Math.round(cx - 34), (int) Math.round(cy - 34), null);
        }
        Estilo.contorno(g, new Ellipse2D.Double(cx - 13, cy - 13, 26, 28), foco.encendido ? new Color(0xFFE873) : new Color(0xD5D9DA), 1.8f);
        if (foco.zona.contains(ratonX, ratonY) && foco.encendido) {
            g.setColor(new Color(255, 255, 255, 140));
            g.setStroke(Estilo.STROKE_3_0);
            g.draw(new Ellipse2D.Double(cx - 22, cy - 22, 44, 46));
        }
    }

    private void dibujarAparato(Graphics2D g, Equipo e, boolean luz) {
        Rectangle2D z = e.zona;
        double x = z.getX(), y = z.getY();
        Color mueble = luz ? new Color(0x8A6440) : new Color(0x45372A);
        switch (e.tipo) {
            case TV: {
                Estilo.contorno(g, new RoundRectangle2D.Double(x + 6, y + 74, 76, 38, 6, 6), mueble, 1.8f);
                g.setColor(new Color(0x222628));
                g.fill(new Rectangle2D.Double(x + 38, y + 58, 12, 16));
                Estilo.contorno(g, new RoundRectangle2D.Double(x, y, 88, 60, 8, 8), new Color(0x222628), 2f);
                Rectangle2D pantalla = new Rectangle2D.Double(x + 6, y + 6, 76, 48);
                if (e.encendido) {
                    Color[] barras = {new Color(0xF25F5C), new Color(0xFFE066), new Color(0x70C1B3), new Color(0x247BA0), new Color(0x9B5DE5)};
                    double desp = (reloj * 30) % 16;
                    Shape clipAnt = g.getClip();
                    g.clip(pantalla);
                    for (int i = -1; i < 6; i++) {
                        g.setColor(barras[(i + 5) % 5]);
                        g.fill(new Rectangle2D.Double(x + 6 + i * 16 + desp, y + 6, 16, 48));
                    }
                    g.setClip(clipAnt);
                } else {
                    g.setColor(new Color(0x101314));
                    g.fill(pantalla);
                }
                break;
            }
            case COMPUTADORA: {
                Estilo.contorno(g, new Rectangle2D.Double(x, y + 58, 92, 9), mueble, 1.6f);
                g.setColor(mueble);
                g.fill(new Rectangle2D.Double(x + 6, y + 67, 6, 27));
                g.fill(new Rectangle2D.Double(x + 80, y + 67, 6, 27));
                g.setColor(new Color(0x2B3033));
                g.fill(new Rectangle2D.Double(x + 40, y + 44, 12, 10));
                Estilo.contorno(g, new RoundRectangle2D.Double(x + 28, y + 52, 36, 6, 3, 3), new Color(0x2B3033), 1.4f);
                Estilo.contorno(g, new RoundRectangle2D.Double(x + 12, y, 68, 46, 6, 6), new Color(0x2B3033), 2f);
                Rectangle2D pantalla = new Rectangle2D.Double(x + 17, y + 5, 58, 36);
                if (e.encendido) {
                    g.setPaint(new GradientPaint(0, (float) y, new Color(0x5BC0EB), 0, (float) (y + 41), new Color(0x2E80A8)));
                    g.fill(pantalla);
                    g.setPaint(null);
                    g.setColor(Color.WHITE);
                    for (int i = 0; i < 3; i++) g.fill(new Rectangle2D.Double(x + 23, y + 11 + i * 8, 30 - i * 6, 4));
                } else {
                    g.setColor(new Color(0x111416));
                    g.fill(pantalla);
                }
                Estilo.contorno(g, new RoundRectangle2D.Double(x + 60, y + 70, 20, 24, 4, 4), new Color(0x3A4044), 1.6f);
                g.setColor(e.encendido ? new Color(0x5CFF7A) : new Color(0x555B5E));
                g.fill(new Ellipse2D.Double(x + 67, y + 75, 6, 6));
                break;
            }
            case VENTILADOR:
            default: {
                double cx = x + 38, cy = y + 34;
                g.setColor(new Color(0x7E8A8F));
                g.fill(new Rectangle2D.Double(cx - 3, cy, 6, 86));
                Estilo.contorno(g, new Ellipse2D.Double(cx - 24, y + 116, 48, 10), new Color(0x7E8A8F), 1.6f);
                Estilo.contorno(g, new Ellipse2D.Double(cx - 32, cy - 32, 64, 64), new Color(0xE9EEF0), 2f);
                Graphics2D gp = (Graphics2D) g.create();
                gp.rotate(e.giro, cx, cy);
                gp.setColor(new Color(0x4C9BC9));
                for (int i = 0; i < 3; i++) {
                    gp.rotate(Math.PI * 2 / 3, cx, cy);
                    gp.fill(new Ellipse2D.Double(cx - 8, cy - 28, 16, 26));
                }
                gp.dispose();
                Estilo.contorno(g, new Ellipse2D.Double(cx - 6, cy - 6, 12, 12), new Color(0x5E6A6F), 1.4f);
                g.setColor(new Color(0x9AA6AB));
                g.setStroke(Estilo.STROKE_1_5);
                g.draw(new Line2D.Double(cx - 32, cy, cx + 32, cy));
                g.draw(new Line2D.Double(cx, cy - 32, cx, cy + 32));
                break;
            }
        }
        if (e.encendido && z.contains(ratonX, ratonY)) {
            g.setColor(new Color(255, 255, 255, 120));
            g.setStroke(Estilo.STROKE_3_0);
            g.draw(new RoundRectangle2D.Double(z.getX() - 4, z.getY() - 4, z.getWidth() + 8, z.getHeight() + 8, 12, 12));
        }
    }

    /** Tomacorriente; si el cargador está enchufado, se ve con su luz azul. */
    private void dibujarCargador(Graphics2D g, Equipo c) {
        double x = c.zona.getX(), y = c.zona.getY();
        Estilo.contorno(g, new RoundRectangle2D.Double(x + 6, y, 26, 30, 6, 6), new Color(0xF2F2EE), 1.6f);
        g.setColor(Estilo.TRAZO);
        g.fill(new Rectangle2D.Double(x + 14, y + 8, 3, 8));
        g.fill(new Rectangle2D.Double(x + 21, y + 8, 3, 8));
        if (!c.encendido) return;
        Estilo.contorno(g, new RoundRectangle2D.Double(x + 9, y + 4, 20, 20, 5, 5), Color.WHITE, 1.8f);
        g.setColor(new Color(0x3DA5FF));
        g.fill(new Ellipse2D.Double(x + 16, y + 9, 6, 6));
        g.setColor(Estilo.TRAZO);
        g.setStroke(Estilo.STROKE_2_0);
        Path2D cable = new Path2D.Double();
        cable.moveTo(x + 19, y + 24);
        cable.quadTo(x + 30, y + 40, x + 12, y + 46);
        g.draw(cable);
        Estilo.contorno(g, new RoundRectangle2D.Double(x + 6, y + 42, 10, 6, 2, 2), new Color(0x555B5E), 1.4f);
        if (c.zona.contains(ratonX, ratonY)) {
            g.setColor(new Color(255, 255, 255, 140));
            g.setStroke(Estilo.STROKE_3_0);
            g.draw(new RoundRectangle2D.Double(x, y - 4, 40, 56, 10, 10));
        }
    }

    /** Botoncito rojo de "apagar" que late sobre lo que gasta energía sin uso. */
    private void dibujarAvisoApagar(Graphics2D g, double cx, double cy) {
        double s = 12 * (1 + Math.sin(reloj * 7) * 0.1);
        Estilo.contorno(g, new Ellipse2D.Double(cx - s, cy - s, s * 2, s * 2), Estilo.ROJO, 1.8f);
        g.setColor(Color.WHITE);
        g.setStroke(Estilo.STROKE_2_5);
        g.draw(new Arc2D.Double(cx - 6, cy - 5, 12, 12, 125, 290, Arc2D.OPEN));
        g.draw(new Line2D.Double(cx, cy - 7.5, cx, cy - 1));
    }
}
