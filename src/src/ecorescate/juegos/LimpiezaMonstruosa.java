package ecorescate.juegos;

import ecorescate.modelo.Estudiante;
import ecorescate.modelo.ModuloImpactoSocial;
import ecorescate.modelo.TipoJuego;
import ecorescate.ui.Sonido;
import ecorescate.ui.Tema;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * RF-01. Misión en la plaza: el niño camina con las flechas, atrapa a los monstruos de
 * basura, los guarda en su mochila y los lleva al tacho de su material.
 * En cada misión aparecen residuos nuevos, para que aprenda a reconocer el material.
 */
public class LimpiezaMonstruosa extends Minijuego {

    private enum Material {
        PLASTICO("PLÁSTICO", "plástico", "botellas · vasos", 0x4DA3E8),
        PAPEL("PAPEL", "papel", "cajas · periódicos", 0xA8775A),
        METAL("METAL", "metal", "latas", 0xFFD54F);

        final String titulo, nombre, ejemplos;
        final Color color;

        Material(String titulo, String nombre, String ejemplos, int rgb) {
            this.titulo = titulo;
            this.nombre = nombre;
            this.ejemplos = ejemplos;
            this.color = new Color(rgb);
        }
    }

    /** Cada monstruo es un residuo real; aparecen poco a poco según la misión. */
    private enum TipoMonstruo {
        BOTELLIN("Botellín", "una botella de plástico", Material.PLASTICO, 1, 0x4C7C9B, new String[]{"GRRRR...", "¡GLUP GLUP!"}),
        CAJETON("Cajetón", "una caja de cartón", Material.PAPEL, 1, 0x8B6542, new String[]{"CRAAACK...", "¡CRUNCH!"}),
        LATOX("Latox", "una lata de gaseosa", Material.METAL, 1, 0xC0453F, new String[]{"¡CLANK!", "CLONK..."}),
        VASITO("Vasito", "un vaso descartable", Material.PLASTICO, 3, 0xF4F4F0, new String[]{"¡SLURP!", "GRRR..."}),
        PERIODICON("Periodicón", "un periódico", Material.PAPEL, 3, 0xE6E3D8, new String[]{"¡RAAASS!", "¡NOTICIA!"}),
        ATUNIN("Atunín", "una lata de atún", Material.METAL, 5, 0x9FB3C8, new String[]{"¡CLINK!", "GLUB..."});

        final String nombre, descripcion;
        final Material material;
        final int mision;
        final Color cuerpo;
        final String[] frases;

        TipoMonstruo(String nombre, String descripcion, Material material, int mision, int cuerpo, String[] frases) {
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.material = material;
            this.mision = mision;
            this.cuerpo = new Color(cuerpo);
            this.frases = frases;
        }
    }

    private static class Monstruo {
        TipoMonstruo tipo;
        double x, y, angulo, velocidad, cambio, anim, aparicion, burbuja;
        String frase;
    }

    private static class Lodo {
        double x, y, angulo, cambio, anim;
    }

    private static class Tacho {
        final Material material;
        final Rectangle2D rect;
        double brillo, temblor;

        Tacho(Material material, Rectangle2D rect) {
            this.material = material;
            this.rect = rect;
        }
    }

    /** Estrella amarilla con "+10" al reciclar. */
    private static class Estallido {
        final double x, y;
        final String texto;
        double vida = 0.9;

        Estallido(double x, double y, String texto) {
            this.x = x;
            this.y = y;
            this.texto = texto;
        }
    }

    private static final Color ARENA = new Color(0xF5DEB8);
    private static final double MIN_X = 84, MAX_X = 916, MIN_Y = 258, MAX_Y = 552;
    private static final double MON_MIN_X = 92, MON_MAX_X = 908, MON_MIN_Y = 272, MON_MAX_Y = 550;
    private static final int CAPACIDAD = 5;
    private static final int MAX_INVASION = 10;
    private static final int VIDAS = 3;
    private static final double ESPERA_TACHO = 0.35;
    private static final double[][] LUGARES_ESTRELLA = {{870, 300}, {130, 300}, {500, 500}, {220, 470}, {780, 470}};

    private final List<Monstruo> monstruos = new ArrayList<>();
    private final List<Lodo> lodos = new ArrayList<>();
    private final List<Tacho> tachos = new ArrayList<>();
    private final List<TipoMonstruo> mochila = new ArrayList<>();
    private final List<Estallido> estallidos = new ArrayList<>();
    private final List<Object> ordenProfundidad = new ArrayList<>(16);
    private final Set<Integer> teclas = new HashSet<>();
    private final List<double[]> pajaros = new ArrayList<>();
    private double polvo;

    private BufferedImage fondoCache;
    private int faseFondoCache = -1;

    private double jx, jy, animJugador;
    private boolean mirandoDerecha = true, caminando;
    private double[] destino;
    private int vidas;
    private double invulnerable;
    private double proximoMonstruo;
    private double estrellaEn, estrellaVida, poder;
    private double[] estrella;
    private double avisoLleno;
    private Tacho frente;
    private double espera;
    private boolean entregado;

    public LimpiezaMonstruosa(Estudiante estudiante, ModuloImpactoSocial modulo) {
        super(TipoJuego.LIMPIEZA, estudiante, modulo);
        double ancho = 118, sep = 12, x0 = 311;
        for (int i = 0; i < 3; i++) {
            tachos.add(new Tacho(Material.values()[i], new Rectangle2D.Double(x0 + i * (ancho + sep), 226, ancho, 86)));
        }
    }

    @Override
    protected String motivoDerrota() {
        return "Los monstruos de basura ganaron esta vez";
    }

    @Override
    protected boolean usaCorazones() {
        return true;
    }

    @Override
    protected void reiniciar() {
        monstruos.clear();
        lodos.clear();
        mochila.clear();
        estallidos.clear();
        teclas.clear();
        jx = 500;
        jy = 430;
        destino = null;
        vidas = VIDAS;
        invulnerable = 0;
        proximoMonstruo = 1.5;
        estrellaEn = 14;
        estrella = null;
        poder = 0;
        frente = null;
        polvo = 0;
        for (int i = 0; i < 3; i++) crearMonstruo(true);
        int lodosNivel = nivel >= 8 ? 2 : nivel >= 2 ? 1 : 0; // el peligro crece con los niveles
        for (int i = 0; i < lodosNivel; i++) crearLodo();
        // pájaros que cruzan el cielo
        pajaros.clear();
        for (int i = 0; i < 3; i++) pajaros.add(new double[]{aleatorio(0, ANCHO), aleatorio(140, 175), aleatorio(25, 45)});
    }

    // ---------------------------------------------------------------- entrada

    @Override
    protected void alTeclaPresionada(int codigo) {
        teclas.add(codigo);
    }

    @Override
    protected void alTeclaSoltada(int codigo) {
        teclas.remove(codigo);
    }

    private boolean tecla(int a, int b) {
        return teclas.contains(a) || teclas.contains(b);
    }

    @Override
    protected void alPresionar(double x, double y) {
        if (x > 64 && x < 936 && y > 214 && y < 562) {
            // clic sobre un tacho: caminar hasta quedar frente a él
            for (Tacho t : tachos) {
                if (t.rect.contains(x, y)) {
                    destino = new double[]{t.rect.getCenterX(), 352};
                    return;
                }
            }
            destino = new double[]{x, y};
        }
    }

    @Override
    protected void alArrastrar(double x, double y) {
        alPresionar(x, y);
    }

    // ---------------------------------------------------------------- reglas

    private void crearMonstruo(boolean adentro) {
        List<TipoMonstruo> posibles = new ArrayList<>();
        for (TipoMonstruo t : TipoMonstruo.values()) if (t.mision <= nivel) posibles.add(t);
        Monstruo m = new Monstruo();
        // aparecen más seguido los monstruos que piden los retos del nivel
        List<TipoMonstruo> necesarios = new ArrayList<>();
        for (TipoMonstruo t : posibles) {
            if (faltaReto(t.name()) > 0 || faltaReto(t.material.name()) > 0) necesarios.add(t);
        }
        List<TipoMonstruo> fuente = !necesarios.isEmpty() && azar.nextDouble() < 0.6 ? necesarios : posibles;
        m.tipo = fuente.get(azar.nextInt(fuente.size()));
        if (adentro) {
            m.x = aleatorio(MON_MIN_X + 40, MON_MAX_X - 40);
            m.y = aleatorio(380, MON_MAX_Y);
            if (Math.hypot(m.x - jx, m.y - jy) < 150) m.x = m.x < 500 ? MON_MIN_X + 30 : MON_MAX_X - 30;
        } else {
            switch (azar.nextInt(3)) {
                case 0: m.x = MON_MIN_X; m.y = aleatorio(380, MON_MAX_Y); break;
                case 1: m.x = MON_MAX_X; m.y = aleatorio(380, MON_MAX_Y); break;
                default: m.x = aleatorio(MON_MIN_X, MON_MAX_X); m.y = MON_MAX_Y; break;
            }
        }
        m.angulo = aleatorio(0, Math.PI * 2);
        m.velocidad = progresion(30, 72) * aleatorio(0.85, 1.15);
        m.cambio = aleatorio(1, 2.5);
        m.anim = aleatorio(0, 6);
        monstruos.add(m);
    }

    private void crearLodo() {
        Lodo l = new Lodo();
        do {
            l.x = aleatorio(MON_MIN_X + 60, MON_MAX_X - 60);
            l.y = aleatorio(400, MON_MAX_Y);
        } while (Math.hypot(l.x - jx, l.y - jy) < 240);
        l.angulo = aleatorio(0, Math.PI * 2);
        l.cambio = aleatorio(2, 4);
        lodos.add(l);
    }

    @Override
    protected void actualizar(double dt) {
        moverJugador(dt);
        moverMonstruos(dt);
        moverLodos(dt);
        actualizarEstrella(dt);
        revisarTachos(dt);

        proximoMonstruo -= dt;
        if (proximoMonstruo <= 0) {
            crearMonstruo(false);
            proximoMonstruo = progresion(3.4, 1.4) * aleatorio(0.8, 1.2);
        }
        if (monstruos.size() >= MAX_INVASION) {
            for (int i = 0; i < 4 && !monstruos.isEmpty(); i++) monstruos.remove(0);
            perderVida("¡Invasión!", "La plaza se llenó de basura. Atrapa a los monstruos antes de que sean demasiados.", 500, 400);
        }

        invulnerable = Math.max(0, invulnerable - dt);
        poder = Math.max(0, poder - dt);
        avisoLleno = Math.max(0, avisoLleno - dt);
        for (Tacho t : tachos) {
            t.brillo = Math.max(0, t.brillo - dt);
            t.temblor = Math.max(0, t.temblor - dt);
        }
        for (Iterator<Estallido> it = estallidos.iterator(); it.hasNext(); ) {
            Estallido e = it.next();
            e.vida -= dt;
            if (e.vida <= 0) it.remove();
        }
        for (double[] p : pajaros) {
            p[0] += p[2] * dt;
            if (p[0] > ANCHO + 30) {
                p[0] = -30;
                p[1] = aleatorio(140, 175);
            }
        }
        medidor = vidas / (double) VIDAS;
    }

    private static boolean enPlataforma(double x, double y) {
        return x > 288 && x < 712 && y < 346;
    }

    private void moverJugador(double dt) {
        double dx = 0, dy = 0;
        if (tecla(KeyEvent.VK_LEFT, KeyEvent.VK_A)) dx -= 1;
        if (tecla(KeyEvent.VK_RIGHT, KeyEvent.VK_D)) dx += 1;
        if (tecla(KeyEvent.VK_UP, KeyEvent.VK_W)) dy -= 1;
        if (tecla(KeyEvent.VK_DOWN, KeyEvent.VK_S)) dy += 1;
        if (dx != 0 || dy != 0) {
            destino = null;
        } else if (destino != null) {
            double ddx = destino[0] - jx, ddy = destino[1] - jy, d = Math.hypot(ddx, ddy);
            if (d < 6) destino = null;
            else {
                dx = ddx / d;
                dy = ddy / d;
            }
        }
        double largo = Math.hypot(dx, dy);
        caminando = largo > 0;
        if (!caminando) return;
        double velocidad = poder > 0 ? 320 : 230;
        double nx = Tema.limitar(jx + dx / largo * velocidad * dt, MIN_X, MAX_X);
        double ny = Tema.limitar(jy + dy / largo * velocidad * dt, MIN_Y, MAX_Y);
        if (enPlataforma(nx, ny)) {
            if (!enPlataforma(nx, jy)) ny = jy;
            else if (!enPlataforma(jx, ny)) nx = jx;
            else {
                nx = jx;
                ny = jy;
                destino = null;
            }
        }
        jx = nx;
        jy = ny;
        if (dx != 0) mirandoDerecha = dx > 0;
        animJugador += dt * 12;
    }

    private boolean prohibidoParaMonstruo(double x, double y) {
        return x > 270 && x < 730 && y < 368;
    }

    private void moverMonstruos(double dt) {
        int hablando = 0;
        for (Monstruo m : monstruos) if (m.burbuja > 0) hablando++;
        for (Iterator<Monstruo> it = monstruos.iterator(); it.hasNext(); ) {
            Monstruo m = it.next();
            m.aparicion = Math.min(1, m.aparicion + dt * 2.5);
            m.anim += dt * 10;
            m.cambio -= dt;
            if (m.cambio <= 0) {
                m.angulo = aleatorio(0, Math.PI * 2);
                m.cambio = aleatorio(1.2, 2.8);
            }
            double vel = m.velocidad;
            if (poder > 0 && Math.hypot(jx - m.x, jy - m.y) < 220) { // imán de la estrella
                m.angulo = Math.atan2(jy - m.y, jx - m.x);
                vel *= 1.8;
            }
            double nx = m.x + Math.cos(m.angulo) * vel * dt;
            double ny = m.y + Math.sin(m.angulo) * vel * dt;
            if (prohibidoParaMonstruo(nx, ny)) {
                m.angulo += Math.PI;
            } else {
                m.x = nx;
                m.y = ny;
            }
            if (m.x < MON_MIN_X || m.x > MON_MAX_X) m.angulo = Math.PI - m.angulo;
            if (m.y < MON_MIN_Y || m.y > MON_MAX_Y) m.angulo = -m.angulo;
            m.x = Tema.limitar(m.x, MON_MIN_X, MON_MAX_X);
            m.y = Tema.limitar(m.y, MON_MIN_Y, MON_MAX_Y);

            m.burbuja = Math.max(0, m.burbuja - dt);
            if (m.burbuja <= 0 && hablando < 2 && azar.nextDouble() < dt * 0.12) {
                m.frase = m.tipo.frases[azar.nextInt(m.tipo.frases.length)];
                m.burbuja = 1.8;
                hablando++;
            }

            if (m.aparicion >= 1 && Math.hypot(m.x - jx, (m.y - 25) - (jy - 35)) < 40) {
                if (mochila.size() < CAPACIDAD) {
                    mochila.add(m.tipo);
                    it.remove();
                    chispas(m.x, m.y - 30, m.tipo.material.color, 10);
                    aviso(m.tipo.nombre, m.x, m.y - 70);
                    sonar(Sonido.Efecto.RECOGER);
                    if (mochila.size() == CAPACIDAD) {
                        mostrarMensaje("¡Mochila llena!", "Lleva cada monstruo al tacho de su material.");
                    }
                } else if (avisoLleno <= 0) {
                    aviso("¡Mochila llena!", jx, jy - 90);
                    avisoLleno = 1.5;
                }
            }
        }
    }

    private void moverLodos(double dt) {
        double vel = progresion(14, 30);
        for (Lodo l : lodos) {
            l.anim += dt * 4;
            l.cambio -= dt;
            if (l.cambio <= 0) {
                l.angulo = aleatorio(0, Math.PI * 2);
                l.cambio = aleatorio(2, 4);
            }
            double nx = l.x + Math.cos(l.angulo) * vel * dt;
            double ny = l.y + Math.sin(l.angulo) * vel * dt;
            if (prohibidoParaMonstruo(nx - 40, ny) || prohibidoParaMonstruo(nx + 40, ny)) {
                l.angulo += Math.PI;
            } else {
                l.x = nx;
                l.y = ny;
            }
            if (l.x < MON_MIN_X + 40 || l.x > MON_MAX_X - 40) l.angulo = Math.PI - l.angulo;
            if (l.y < MON_MIN_Y + 30 || l.y > MON_MAX_Y) l.angulo = -l.angulo;
            l.x = Tema.limitar(l.x, MON_MIN_X + 40, MON_MAX_X - 40);
            l.y = Tema.limitar(l.y, MON_MIN_Y + 30, MON_MAX_Y);

            if (invulnerable <= 0 && poder <= 0 && Math.hypot(l.x - jx, (l.y - 22) - (jy - 30)) < 40) {
                double ang = Math.atan2(jy - l.y, jx - l.x);
                jx = Tema.limitar(jx + Math.cos(ang) * 50, MIN_X, MAX_X);
                jy = Tema.limitar(jy + Math.sin(ang) * 50, MIN_Y, MAX_Y);
                if (enPlataforma(jx, jy)) jy = 348;
                perderVida("¡Lodo tóxico!", "El lodo tóxico es basura peligrosa: no se toca. Rodéalo.", jx, jy - 90);
            }
        }
    }

    private void perderVida(String texto, String explicacion, double x, double y) {
        vidas--;
        invulnerable = 1.6;
        destino = null;
        fallo(texto, explicacion, x, y, 0);
        medidor = vidas / (double) VIDAS;
    }

    private void actualizarEstrella(double dt) {
        if (estrella == null) {
            if (poder > 0) return;
            estrellaEn -= dt;
            if (estrellaEn <= 0) {
                estrella = LUGARES_ESTRELLA[azar.nextInt(LUGARES_ESTRELLA.length)];
                estrellaVida = 8;
            }
            return;
        }
        estrellaVida -= dt;
        if (Math.hypot(estrella[0] - jx, estrella[1] - (jy - 30)) < 40) {
            poder = 6;
            chispas(estrella[0], estrella[1], Estilo.AMARILLO, 24);
            sonar(Sonido.Efecto.PODER);
            progresoReto("ESTRELLA", 1);
            String extra = "";
            if (vidas < VIDAS) {
                vidas++;
                extra = " ¡Y recuperas una vida!";
            }
            sumar(15, "+15 ¡PODER!", estrella[0], estrella[1] - 30);
            mostrarMensaje("¡Poder de estrella!" + extra, "Corres más rápido, atraes monstruos y el lodo no te daña.");
            estrella = null;
            estrellaEn = 20;
        } else if (estrellaVida <= 0) {
            estrella = null;
            estrellaEn = 15;
        }
    }

    /** Si el niño se queda frente a un tacho, entrega los monstruos de ese material. */
    private void revisarTachos(double dt) {
        Tacho actual = null;
        if (jy < 362) {
            for (Tacho t : tachos) {
                if (jx >= t.rect.getX() - 4 && jx <= t.rect.getMaxX() + 4) actual = t;
            }
        }
        if (actual != frente) {
            frente = actual;
            espera = 0;
            entregado = false;
        }
        if (frente == null || entregado || mochila.isEmpty()) return;
        espera += dt;
        if (espera < ESPERA_TACHO) return;
        entregado = true;

        Tacho t = frente;
        Rectangle2D r = t.rect;
        int depositados = 0;
        TipoMonstruo ultimo = null;
        for (Iterator<TipoMonstruo> it = mochila.iterator(); it.hasNext(); ) {
            TipoMonstruo m = it.next();
            if (m.material != t.material) continue;
            it.remove();
            progresoReto(m.name(), 1);
            progresoReto(m.material.name(), 1);
            int puntos = sumarAcierto(10);
            if (puntos == 0) break; // la partida ya terminó
            estallidos.add(new Estallido(r.getCenterX() + depositados * 26 - 10, r.getY() - 6 - depositados * 14, "+" + puntos));
            depositados++;
            ultimo = m;
        }
        if (depositados > 0) {
            t.brillo = 0.6;
            chispas(r.getCenterX(), r.getY() + 20, t.material.color, 16);
            mostrarMensaje("¡Bien! " + ultimo.nombre + " es " + ultimo.descripcion + ".",
                    "Va en el tacho de " + t.material.nombre + ". ¡Sigue así y sube tu combo!");
        } else {
            // ninguno de los que lleva es de este material: tacho equivocado
            TipoMonstruo m = mochila.get(0);
            t.temblor = 0.5;
            fallo("¡Tacho equivocado!", m.nombre + " es " + m.descripcion + ": va en el tacho de "
                    + m.material.nombre + ", no en el de " + t.material.nombre + ".", r.getCenterX(), r.getMaxY() + 40, 0);
        }
    }

    // ---------------------------------------------------------------- barras

    /** Barra de invasión y aviso de la estrella, en la parte derecha de la barra inferior. */
    private void dibujarInvasion(Graphics2D g) {
        g.setColor(Estilo.BLANCO_SUAVE);
        g.setFont(Tema.espaciada(Font.BOLD, 11.5f, 0.06f));
        g.drawString("INVASIÓN", 660, 602);
        double invasion = monstruos.size() / (double) MAX_INVASION;
        g.setColor(Estilo.MORADO_MEDIO);
        g.fill(new RoundRectangle2D.Double(740, 592, 220, 13, 13, 13));
        g.setColor(invasion > 0.7 ? Estilo.ROJO : Estilo.MENTA);
        g.fill(new RoundRectangle2D.Double(740, 592, Math.max(13, 220 * Math.min(1, invasion)), 13, 13, 13));

        g.setFont(Tema.fuente(Font.BOLD, 15f));
        String texto;
        if (poder > 0) {
            g.setColor(Estilo.AMARILLO);
            texto = String.format("PODER 00:%02d", (int) Math.ceil(poder));
        } else if (estrella != null) {
            g.setColor(Estilo.AMARILLO);
            texto = "¡ATRAPA LA ESTRELLA!";
        } else {
            g.setColor(Color.WHITE);
            texto = String.format("ESTRELLA EN 00:%02d", (int) Math.ceil(Math.max(0, estrellaEn)));
        }
        g.drawString(texto, 660, 625);
        g.setColor(Estilo.BLANCO_SUAVE);
        g.setFont(Tema.fuente(Font.PLAIN, 13f));
        g.drawString("Mover: ↑ ↓ ← → o clic", 828, 625);
    }

    @Override
    protected String[] pista() {
        if (poder > 0) return new String[]{"¡Poder de estrella!", "Corres más rápido y los monstruos vienen hacia ti."};
        if (mochila.size() == CAPACIDAD) return new String[]{"¡Mochila llena!", "Lleva cada monstruo al tacho de su material."};
        if (mochila.isEmpty()) return new String[]{"¡Atrapa a los monstruos!", "Tócalos para guardarlos en tu mochila (máximo 5)."};
        if (nivel <= 2) return new String[]{"Ahora llévalos a su tacho", "Botellín → plástico · Cajetón → papel · Latox → metal"};
        return new String[]{"Ahora llévalos a su tacho", "Párate un momento frente al tacho de su material."};
    }

    @Override
    protected String textoControles() {
        return "↑ ↓ ← →  MOVER";
    }

    @Override
    protected void dibujarPie(Graphics2D g) {
        g.setColor(Estilo.MORADO);
        g.fill(new RoundRectangle2D.Double(22, 582, 956, 52, 22, 22));
        g.setColor(Estilo.BLANCO_SUAVE);
        g.setFont(Tema.espaciada(Font.BOLD, 11.5f, 0.06f));
        g.drawString("MOCHILA", 42, 602);
        g.setColor(Color.WHITE);
        g.setFont(Tema.fuente(Font.BOLD, 21f));
        g.drawString(mochila.size() + " / " + CAPACIDAD, 42, 625);
        for (int i = 0; i < CAPACIDAD; i++) {
            double sx = 118 + i * 36;
            RoundRectangle2D celda = new RoundRectangle2D.Double(sx, 590, 32, 36, 9, 9);
            g.setColor(i < mochila.size() ? new Color(0xF4F1EA) : Estilo.MORADO_MEDIO);
            g.fill(celda);
            if (i < mochila.size()) {
                Graphics2D gm = (Graphics2D) g.create();
                gm.clip(celda);
                dibujarCuerpoMonstruo(gm, mochila.get(i), sx + 16, 623, 0.5, 0);
                gm.dispose();
            }
        }
        dibujarConsejo(g, 312, 644);
        dibujarInvasion(g);
    }

    // ---------------------------------------------------------------- escena

    private BufferedImage obtenerFondo(int fase) {
        if (fondoCache != null && faseFondoCache == fase) return fondoCache;
        faseFondoCache = fase;
        BufferedImage img = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Tema.suavizar(g);
        int[][] cielos = {{0xBDE6F7, 0xEAF7FC}, {0xFFD9A0, 0xFFF3DC}, {0xF6A27A, 0xFBD3B0}, {0x7E6FC8, 0xF0B3A0}};
        g.setPaint(new GradientPaint(0, (float) ESC_Y, new Color(cielos[fase][0]), 0, 214, new Color(cielos[fase][1])));
        g.fill(new Rectangle2D.Double(ESC_X, ESC_Y, ESC_W, 90));
        g.setPaint(null);
        g.setColor(new Color(0xCFEBC2));
        g.fill(new Rectangle2D.Double(ESC_X, 210, ESC_W, ESC_ABA - 210));
        Color sol = fase == 3 ? new Color(0xFFF1C1) : fase == 2 ? new Color(0xFFB347) : new Color(0xFFD84D);
        g.setColor(Tema.alfa(sol, 90));
        g.fill(new Ellipse2D.Double(828, 126, 84, 84));
        g.setColor(sol);
        g.fill(new Ellipse2D.Double(842, 140, 56, 56));

        Color[] colores = {new Color(0xEE8FC4), new Color(0xFFD86B), new Color(0x93C0EE), new Color(0xEE8FC4),
            new Color(0xF5A86B), new Color(0x93C0EE), new Color(0xEE8FC4)};
        for (int i = 0; i < 7; i++) casa(g, 118 + i * 127, colores[i]);

        RoundRectangle2D arena = new RoundRectangle2D.Double(64, 216, 872, 346, 24, 24);
        g.setColor(ARENA);
        g.fill(arena);
        Graphics2D gl = (Graphics2D) g.create();
        gl.clip(arena);
        gl.setColor(new Color(0xE6CBA0));
        gl.setStroke(Estilo.STROKE_1_5);
        for (int x = 64 + 145; x < 936; x += 145) gl.draw(new Line2D.Double(x, 216, x, 562));
        for (int y = 320; y < 562; y += 100) gl.draw(new Line2D.Double(64, y, 936, y));
        gl.dispose();
        g.setColor(Estilo.TRAZO);
        g.setStroke(Estilo.STROKE_2_5);
        g.draw(arena);

        g.setColor(new Color(0xE8CFA8));
        g.fill(new RoundRectangle2D.Double(298, 218, 404, 104, 16, 16));
        g.dispose();
        fondoCache = img;
        return fondoCache;
    }

    @Override
    protected void dibujarEscena(Graphics2D g) {
        // el cielo cambia con los niveles: mañana, tarde, atardecer y anochecer
        int fase = nivel <= 3 ? 0 : nivel <= 6 ? 1 : nivel <= 9 ? 2 : 3;
        for (int i = 0; i < 4; i++) {
            // Las nubes y aves se dibujan detrás de las casas o sobre el cielo cacheado
        }
        g.drawImage(obtenerFondo(fase), 0, 0, null);

        for (int i = 0; i < 4; i++) {
            double nx = (i * 290 + reloj * 12) % 1180 - 100;
            nube(g, nx, 136 + (i % 2) * 14, 0.6 + (i % 3) * 0.12);
        }
        g.setColor(Estilo.TRAZO);
        g.setStroke(Estilo.STROKE_2_0);
        for (double[] p : pajaros) {
            double aleteo = Math.sin(reloj * 12 + p[0] * 0.1) * 3;
            Path2D ave = new Path2D.Double();
            ave.moveTo(p[0] - 7, p[1]);
            ave.quadTo(p[0] - 3, p[1] - 5 - aleteo, p[0], p[1]);
            ave.quadTo(p[0] + 3, p[1] - 5 - aleteo, p[0] + 7, p[1]);
            g.draw(ave);
        }
        arbol(g, 46, 306);
        arbol(g, 954, 306);

        for (Tacho t : tachos) dibujarTacho(g, t);

        if (estrella != null) {
            boolean parpadea = estrellaVida < 2 && ((int) (reloj * 8)) % 2 == 0;
            if (!parpadea) {
                double pulso = 1 + Math.sin(reloj * 6) * 0.08;
                Estilo.contorno(g, Estilo.estrella(estrella[0], estrella[1], 26 * pulso, 0.45, 5, reloj * 0.6), Estilo.AMARILLO);
                g.setFont(Tema.fuente(Font.BOLD, 13f));
                Tema.centrado(g, "¡PODER!", estrella[0], estrella[1] + 44);
            }
        }

        // personajes ordenados por profundidad (los de abajo se dibujan encima)
        ordenProfundidad.clear();
        ordenProfundidad.addAll(lodos);
        ordenProfundidad.addAll(monstruos);
        ordenProfundidad.add(this);
        ordenProfundidad.sort((a, b) -> Double.compare(profundidad(a), profundidad(b)));
        for (int i = 0; i < ordenProfundidad.size(); i++) {
            Object o = ordenProfundidad.get(i);
            if (o instanceof Lodo) dibujarLodo(g, (Lodo) o);
            else if (o instanceof Monstruo) dibujarMonstruo(g, (Monstruo) o);
            else dibujarJugador(g);
        }
        for (Monstruo m : monstruos) {
            if (m.burbuja > 0) Estilo.burbuja(g, m.frase, m.x, m.y - 74, 70, 930);
            else if (Math.hypot(m.x - jx, m.y - jy) < 140) {
                Estilo.pildora(g, m.tipo.nombre, m.x, m.y - 92, new Color(45, 27, 105, 210), Color.WHITE, 12f);
            }
        }

        for (Estallido e : estallidos) {
            double t = 1 - e.vida / 0.9;
            double s = t < 0.2 ? 0.6 + t * 2 : 1.0;
            int alfa = (int) (255 * Math.min(1, e.vida * 2.5));
            double y = e.y - t * 30;
            Path2D forma = Estilo.estrella(e.x, y, 34 * s, 0.72, 12, 0);
            g.setColor(Tema.alfa(Estilo.AMARILLO, alfa));
            g.fill(forma);
            g.setColor(Tema.alfa(Estilo.TRAZO, alfa));
            g.setStroke(Estilo.STROKE_2_0);
            g.draw(forma);
            g.setFont(Tema.fuente(Font.BOLD, (float) (17 * s)));
            Tema.centrado(g, e.texto, e.x, y + 6 * s);
        }
    }

    private double profundidad(Object o) {
        if (o instanceof Lodo) return ((Lodo) o).y;
        if (o instanceof Monstruo) return ((Monstruo) o).y;
        return jy;
    }

    private void casa(Graphics2D g, double cx, Color c) {
        Estilo.contorno(g, new Rectangle2D.Double(cx - 50, 168, 100, 46), c);
        Path2D techo = new Path2D.Double();
        techo.moveTo(cx - 60, 169);
        techo.lineTo(cx, 136);
        techo.lineTo(cx + 60, 169);
        techo.closePath();
        Estilo.contorno(g, techo, Tema.oscurecer(c, 0.12));
        for (int i = -1; i <= 1; i += 2) {
            Estilo.contorno(g, new RoundRectangle2D.Double(cx + i * 20 - 11, 178, 22, 20, 4, 4), new Color(0xFFF4D6));
        }
    }

    private void arbol(Graphics2D g, double x, double y) {
        double viento = Math.sin(reloj * 1.6 + x) * 3; // la copa se mece con el viento
        g.setColor(new Color(0x9C6B3F));
        g.fill(new Rectangle2D.Double(x - 5, y - 70, 10, 76));
        Estilo.contorno(g, new Ellipse2D.Double(x - 32 + viento, y - 122, 64, 64), new Color(0x5DAE6B));
        Estilo.contorno(g, new Ellipse2D.Double(x - 8 + viento * 1.3, y - 130, 40, 40), new Color(0x86CF8A));
        g.setColor(new Color(0xF25F8C));
        g.fill(new Ellipse2D.Double(x - 14 + viento, y - 100, 7, 7));
        g.fill(new Ellipse2D.Double(x + 12 + viento, y - 86, 7, 7));
    }

    private void nube(Graphics2D g, double x, double y, double s) {
        g.setColor(new Color(255, 255, 255, 225));
        g.fill(new Ellipse2D.Double(x, y + 10 * s, 70 * s, 30 * s));
        g.fill(new Ellipse2D.Double(x + 26 * s, y, 56 * s, 40 * s));
        g.fill(new Ellipse2D.Double(x + 62 * s, y + 10 * s, 58 * s, 30 * s));
    }

    private void dibujarTacho(Graphics2D g0, Tacho t) {
        Graphics2D g = (Graphics2D) g0.create();
        if (t.temblor > 0) g.translate(Math.sin(t.temblor * 60) * 4, 0);
        Rectangle2D r = t.rect;
        double x = r.getX(), y = r.getY(), w = r.getWidth(), h = r.getHeight();
        if (t.brillo > 0) {
            g.setColor(Tema.alfa(Estilo.AMARILLO, (int) (200 * t.brillo / 0.6)));
            g.fill(new RoundRectangle2D.Double(x - 8, y - 8, w + 16, h + 16, 20, 20));
        }
        if (t == frente && !mochila.isEmpty() && !entregado) { // anillo de espera
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Arc2D.Double(r.getCenterX() - 16, y + h + 8, 32, 32, 90, -360 * Math.min(1, espera / ESPERA_TACHO), Arc2D.OPEN));
        }
        Estilo.contorno(g, new RoundRectangle2D.Double(x, y + 6, w, h - 6, 10, 10), t.material.color);
        Estilo.contorno(g, new RoundRectangle2D.Double(x - 4, y, w + 8, 12, 6, 6), Tema.oscurecer(t.material.color, 0.15));
        Estilo.contorno(g, new RoundRectangle2D.Double(x + 10, y + 20, w - 20, 32, 8, 8), Color.WHITE);
        g.setColor(Estilo.TRAZO);
        g.setFont(Tema.fuente(Font.BOLD, 16f));
        Tema.centrado(g, t.material.titulo, r.getCenterX(), y + 42);
        g.setFont(Tema.fuente(Font.BOLD, 11.5f));
        Tema.centrado(g, t.material.ejemplos.toUpperCase(), r.getCenterX(), y + 72);
        g.dispose();
    }

    private void dibujarJugador(Graphics2D g) {
        if (invulnerable > 0 && ((int) (reloj * 10)) % 2 == 0) return;
        if (poder > 0) {
            double s = 46 + Math.sin(reloj * 10) * 4;
            g.setColor(new Color(255, 216, 77, 90));
            g.fill(new Ellipse2D.Double(jx - s, jy - 40 - s, s * 2, s * 2));
        }
        Personaje.dibujar(g, jx, jy, 1.0, accesorio, polo, mirandoDerecha, caminando ? Math.sin(animJugador) : 0, true, 6, 13);
    }

    private void dibujarMonstruo(Graphics2D g0, Monstruo m) {
        double s = m.aparicion < 1 ? 0.3 + 0.7 * m.aparicion + Math.sin(m.aparicion * Math.PI) * 0.2 : 1;
        g0.setColor(new Color(0, 0, 0, 38));
        g0.fill(new Ellipse2D.Double(m.x - 20 * s, m.y - 5, 40 * s, 10));
        dibujarCuerpoMonstruo(g0, m.tipo, m.x, m.y, s, Math.sin(m.anim));
    }

    /** Dibuja el monstruo con sus pies en (x, y). También se usa en la mochila. */
    private void dibujarCuerpoMonstruo(Graphics2D g0, TipoMonstruo tipo, double x, double y, double s, double paso) {
        Graphics2D g = (Graphics2D) g0.create();
        g.translate(x, y);
        g.scale(s, s);
        g.setColor(Estilo.TRAZO);
        g.setStroke(Estilo.STROKE_3_0);
        g.draw(new Line2D.Double(-7, -10, -10 + paso * 4, 0));
        g.draw(new Line2D.Double(7, -10, 10 - paso * 4, 0));
        g.draw(new Line2D.Double(-15, -30, -25, -22 + paso * 4));
        g.draw(new Line2D.Double(15, -30, 25, -22 - paso * 4));

        Color c = tipo.cuerpo;
        double ojos = -38, boca = -27;
        switch (tipo) {
            case BOTELLIN: {
                Path2D hombro = new Path2D.Double();
                hombro.moveTo(-15, -46);
                hombro.lineTo(-8, -58);
                hombro.lineTo(8, -58);
                hombro.lineTo(15, -46);
                hombro.closePath();
                Estilo.contorno(g, hombro, c);
                Estilo.contorno(g, new RoundRectangle2D.Double(-15, -50, 30, 42, 10, 10), c);
                Estilo.contorno(g, new RoundRectangle2D.Double(-8, -66, 16, 9, 3, 3), new Color(0x2F5873));
                g.setColor(Tema.aclarar(c, 0.3));
                g.fill(new Rectangle2D.Double(-14, -17, 28, 5));
                break;
            }
            case VASITO: {
                Path2D vaso = new Path2D.Double();
                vaso.moveTo(-18, -56);
                vaso.lineTo(18, -56);
                vaso.lineTo(12, -8);
                vaso.lineTo(-12, -8);
                vaso.closePath();
                Estilo.contorno(g, vaso, c);
                g.setColor(new Color(0xE0574F));
                g.fill(new Rectangle2D.Double(-15, -18, 30, 5));
                g.fill(new Rectangle2D.Double(-14, -13, 28, 2));
                Estilo.contorno(g, new Ellipse2D.Double(-18, -59, 36, 7), new Color(0xDADAD4), 2f);
                break;
            }
            case CAJETON: {
                Path2D caja = new Path2D.Double();
                caja.moveTo(-20, -8);
                caja.lineTo(-22, -30);
                caja.lineTo(-18, -46);
                caja.lineTo(-8, -50);
                caja.lineTo(0, -44);
                caja.lineTo(9, -52);
                caja.lineTo(21, -45);
                caja.lineTo(22, -26);
                caja.lineTo(19, -8);
                caja.closePath();
                Estilo.contorno(g, caja, c);
                g.setColor(new Color(0xB48A5E));
                Path2D solapa = new Path2D.Double();
                solapa.moveTo(-18, -46);
                solapa.lineTo(0, -40);
                solapa.lineTo(21, -45);
                g.draw(solapa);
                break;
            }
            case PERIODICON: {
                Estilo.contorno(g, new Rectangle2D.Double(-19, -54, 38, 46), c);
                g.setColor(Estilo.TRAZO);
                g.fill(new Rectangle2D.Double(-15, -50, 30, 5));
                g.setColor(new Color(0x9A968A));
                g.setStroke(Estilo.STROKE_1_5);
                for (int i = 0; i < 3; i++) g.draw(new Line2D.Double(-15, -18 + i * 4, 15, -18 + i * 4));
                break;
            }
            case LATOX: {
                Estilo.contorno(g, new RoundRectangle2D.Double(-15, -50, 30, 42, 6, 6), c);
                Estilo.contorno(g, new Ellipse2D.Double(-15, -56, 30, 11), new Color(0xD8DCDF));
                g.setColor(Color.WHITE);
                g.setStroke(Estilo.STROKE_2_5);
                Path2D onda = new Path2D.Double();
                onda.moveTo(-14, -15);
                onda.quadTo(-4, -22, 4, -15);
                onda.quadTo(10, -11, 14, -16);
                g.draw(onda);
                break;
            }
            case ATUNIN:
            default: {
                Estilo.contorno(g, new RoundRectangle2D.Double(-21, -38, 42, 30, 8, 8), c);
                Estilo.contorno(g, new Ellipse2D.Double(-21, -43, 42, 11), new Color(0xD8DCDF));
                g.setColor(new Color(0x2E80A8));
                g.fill(new Rectangle2D.Double(-20, -16, 40, 6));
                ojos = -29;
                boca = -19;
                break;
            }
        }
        // cara de monstruo
        for (int lado = -1; lado <= 1; lado += 2) {
            double ox = lado * 7;
            g.setColor(new Color(0x1B1530));
            g.fill(new Ellipse2D.Double(ox - 5, ojos - 2, 10, 8));
            g.setColor(new Color(0x7CFF8A));
            g.fill(new Ellipse2D.Double(ox - 2 + lado, ojos, 4.5, 4.5));
            g.setColor(Estilo.TRAZO);
            g.setStroke(Estilo.STROKE_2_5);
            g.draw(new Line2D.Double(lado * 13, ojos - 8, lado * 3, ojos - 4));
        }
        g.setColor(new Color(0x1B1530));
        g.fill(new RoundRectangle2D.Double(-9, boca - 2, 18, 9, 4, 4));
        g.setColor(Color.WHITE);
        for (int i = 0; i < 3; i++) {
            Path2D diente = new Path2D.Double();
            diente.moveTo(-8 + i * 6, boca - 2);
            diente.lineTo(-5 + i * 6, boca + 3);
            diente.lineTo(-2 + i * 6, boca - 2);
            diente.closePath();
            g.fill(diente);
        }
        g.dispose();
    }

    private void dibujarLodo(Graphics2D g0, Lodo l) {
        Graphics2D g = (Graphics2D) g0.create();
        g.translate(l.x, l.y);
        g.setColor(new Color(0, 0, 0, 40));
        g.fill(new Ellipse2D.Double(-50, -6, 100, 13));
        Graphics2D gc = (Graphics2D) g.create();
        gc.scale(1 + Math.sin(l.anim) * 0.05, 1 - Math.sin(l.anim) * 0.05);
        Path2D blob = new Path2D.Double();
        blob.moveTo(-48, 0);
        blob.quadTo(-56, -18, -40, -26);
        blob.lineTo(-34, -46);
        blob.lineTo(-22, -32);
        blob.lineTo(-10, -60);
        blob.lineTo(3, -35);
        blob.lineTo(15, -52);
        blob.lineTo(24, -31);
        blob.quadTo(48, -30, 50, -10);
        blob.quadTo(52, 0, 40, 0);
        blob.closePath();
        Estilo.contorno(gc, blob, new Color(0x8E6BB5), 2.4f);
        gc.setColor(new Color(0xA88BD0));
        gc.fill(new Ellipse2D.Double(-36, -22, 14, 8));
        double[][] ojos = {{-17, -21}, {3, -25}, {22, -18}};
        for (double[] o : ojos) {
            gc.setColor(new Color(0x1B1530));
            gc.fill(new Ellipse2D.Double(o[0] - 6, o[1] - 4, 12, 8));
            gc.setColor(new Color(0x7CFF8A));
            gc.fill(new Ellipse2D.Double(o[0] - 2, o[1] - 2.5, 4.5, 4.5));
        }
        Path2D boca = new Path2D.Double();
        boca.moveTo(-16, -11);
        for (int i = 1; i <= 8; i++) boca.lineTo(-16 + i * 4, i % 2 == 0 ? -11 : -5);
        gc.setColor(new Color(0x1B1530));
        gc.setStroke(Estilo.STROKE_3_0);
        gc.draw(boca);
        gc.dispose();
        g.setColor(Estilo.TRAZO);
        g.setFont(Tema.fuente(Font.BOLD, 11.5f));
        Tema.centrado(g, "LODO TÓXICO · PELIGRO", 0, 22);
        g.dispose();
    }
}
