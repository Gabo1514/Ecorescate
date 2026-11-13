package ecorescate.ui;

import ecorescate.juegos.Personaje;
import ecorescate.modelo.CatalogoNiveles;
import ecorescate.modelo.Nivel;
import ecorescate.modelo.Perfil;
import ecorescate.modelo.Reto;
import ecorescate.modelo.TipoJuego;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/** Mapa de los 10 niveles de un minijuego: camino, candados, estrellas y personaje. */
public class PanelNiveles extends PantallaSecundaria {

    public interface Acciones {
        void jugarNivel(TipoJuego tipo, int nivel);

        void volverAlMenu();
    }

    private static final Color TRAZO = new Color(0x2A2340);
    private static final Color MORADO = new Color(0x2D1B69);
    private static final BasicStroke[] STROKES_CAMINO = new BasicStroke[26];
    static {
        for (int i = 0; i < 26; i++) {
            STROKES_CAMINO[i] = new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{14f, 12f}, i);
        }
    }

    private final Acciones acciones;
    private final Mapa mapa = new Mapa();
    private TipoJuego tipo = TipoJuego.LIMPIEZA;
    private Perfil perfil;

    public PanelNiveles(Acciones acciones) {
        super("Niveles", acciones::volverAlMenu);
        this.acciones = acciones;
        setContenido(mapa);
    }

    public void mostrar(TipoJuego tipo, Perfil perfil) {
        this.tipo = tipo;
        this.perfil = perfil;
        setTitulo(tipo.getNombre() + " · Niveles");
        setMonedas(perfil.getMonedas());
        mapa.encima = -1;
        mapa.iniciarAnimacion();
        mapa.repaint();
    }

    public void detenerAnimacion() {
        mapa.detenerAnimacion();
    }

    public TipoJuego getTipo() {
        return tipo;
    }

    private class Mapa extends JComponent {

        private final Point2D[] nodos = new Point2D[CatalogoNiveles.NIVELES];
        private final Timer timerAnim;
        private int encima = -1;
        private double reloj;
        private BufferedImage fondoCache;
        private int ultimoW = -1, ultimoH = -1;
        private TipoJuego ultimoTipoFondo;
        private Path2D caminoCache;

        Mapa() {
            MouseAdapter m = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int n = nodoEn(e.getX(), e.getY());
                    encima = n;
                    if (n != encima) {
                        encima = n;
                        repaint();
                    }
                    boolean abierto = n >= 0 && perfil != null && perfil.desbloqueado(tipo, n + 1);
                    setCursor(Cursor.getPredefinedCursor(abierto ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    int n = nodoEn(e.getX(), e.getY());
                    if (n < 0 || perfil == null) return;
                    if (perfil.desbloqueado(tipo, n + 1)) {
                        detenerAnimacion();
                        Sonido.get().tocar(Sonido.Efecto.CLIC);
                        acciones.jugarNivel(tipo, n + 1);
                    } else {
                        Sonido.get().tocar(Sonido.Efecto.ERROR);
                    }
                }
            };
            addMouseListener(m);
            addMouseMotionListener(m);
            timerAnim = new Timer(45, e -> {
                if (isShowing()) {
                    reloj += 0.045;
                    repaint();
                } else {
                    detenerAnimacion();
                }
            });
            timerAnim.setCoalesce(true);
        }

        void iniciarAnimacion() {
            if (!timerAnim.isRunning()) timerAnim.start();
        }

        void detenerAnimacion() {
            if (timerAnim.isRunning()) timerAnim.stop();
        }

        @Override
        public void removeNotify() {
            detenerAnimacion();
            super.removeNotify();
        }

        private int nodoEn(int x, int y) {
            for (int i = 0; i < nodos.length; i++) {
                if (nodos[i] != null && nodos[i].distance(x, y) < 38) return i;
            }
            return -1;
        }

        private void recalcularGeometria(int w, int h, double margen) {
            if (w == ultimoW && h == ultimoH && tipo == ultimoTipoFondo && caminoCache != null) return;
            ultimoW = w;
            ultimoH = h;
            ultimoTipoFondo = tipo;

            double top = 120, abajo = h - 110;
            double centro = (top + abajo) / 2, amplitud = Math.min(110, (abajo - top) * 0.36);
            for (int i = 0; i < nodos.length; i++) {
                double x = margen + 50 + i * (w - 2 * margen - 100) / (nodos.length - 1.0);
                double y = centro + Math.sin(i * 1.15 + 0.6) * amplitud;
                nodos[i] = new Point2D.Double(x, y);
            }
            caminoCache = new Path2D.Double();
            caminoCache.moveTo(nodos[0].getX(), nodos[0].getY());
            for (int i = 1; i < nodos.length; i++) {
                double mx = (nodos[i - 1].getX() + nodos[i].getX()) / 2;
                caminoCache.curveTo(mx, nodos[i - 1].getY(), mx, nodos[i].getY(), nodos[i].getX(), nodos[i].getY());
            }

            // Renderizar fondo estático con gradiente, círculos y base blanca del camino
            BufferedImage img = new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            Tema.suavizar(g);
            Color color = new Color(tipo.getColor());

            // fondo con el color del juego y círculos decorativos
            g.setPaint(new GradientPaint(0, 0, Tema.aclarar(color, 0.85), 0, h, Tema.aclarar(color, 0.65)));
            g.fillRect(0, 0, w, h);
            g.setPaint(null);
            g.setColor(new Color(255, 255, 255, 40));
            for (int i = 0; i < 14; i++) {
                double cx = (i * 173) % Math.max(1, w), cy = (i * 97) % Math.max(1, h);
                double r = 30 + (i % 4) * 22;
                g.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
            }
            g.setColor(new Color(255, 255, 255, 200));
            g.setStroke(ecorescate.juegos.Estilo.STROKE_22_0);
            g.draw(caminoCache);
            g.dispose();
            fondoCache = img;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            if (perfil == null) return;
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            int w = getWidth(), h = getHeight();
            Color color = new Color(tipo.getColor());
            double margen = Math.max(30, w * 0.05);

            recalcularGeometria(w, h, margen);

            // 1. Fondo pre-renderizado súper rápido
            if (fondoCache != null) {
                g.drawImage(fondoCache, 0, 0, null);
            }

            // Encabezado
            Iconos.logo(g, tipo, margen + 30, 52, 60);
            g.setColor(MORADO);
            g.setFont(Tema.fuente(Font.BOLD, 28f));
            g.drawString(tipo.getNombre(), (float) (margen + 72), 50f);
            g.setFont(Tema.fuente(Font.PLAIN, 16f));
            g.setColor(Tema.TEXTO_SUAVE);
            g.drawString("Supera un nivel para abrir el siguiente. ¡Consigue las 3 estrellas en cada uno!",
                    (float) (margen + 72), 76f);
            double px = w - margen - 230;
            RoundRectangle2D resumen = new RoundRectangle2D.Double(px, 24, 230, 58, 20, 20);
            g.setColor(Color.WHITE);
            g.fill(resumen);
            g.setColor(TRAZO);
            g.setStroke(ecorescate.juegos.Estilo.STROKE_2_0);
            g.draw(resumen);
            Iconos.estrella(g, px + 30, 53, 15, true);
            g.setColor(Tema.TEXTO);
            g.setFont(Tema.fuente(Font.BOLD, 22f));
            g.drawString(perfil.estrellasDe(tipo) + " / " + CatalogoNiveles.NIVELES * 3, (float) (px + 54), 61f);
            g.setFont(Tema.fuente(Font.PLAIN, 14f));
            g.setColor(Tema.TEXTO_SUAVE);
            g.drawString(perfil.nivelesSuperados(tipo) + "/10 niveles", (float) (px + 140), 60f);

            // 2. Línea punteada animada sobre el camino
            if (caminoCache != null) {
                g.setColor(Tema.aclarar(color, 0.3));
                int fase = (int) Math.floor((-reloj * 20) % 26);
                if (fase < 0) fase += 26;
                g.setStroke(STROKES_CAMINO[fase]);
                g.draw(caminoCache);
            }

            int sugerido = perfil.nivelSugerido(tipo);
            Nivel[] niveles = CatalogoNiveles.de(tipo);
            for (int i = 0; i < nodos.length; i++) {
                int n = i + 1;
                boolean abierto = perfil.desbloqueado(tipo, n);
                int estrellas = perfil.getEstrellas(tipo, n);
                double x = nodos[i].getX(), y = nodos[i].getY();
                double r = 34 * (i == encima && abierto ? 1.1 : 1);
                if (n == sugerido && abierto) {
                    double pulso = 6 + Math.sin(reloj * 5) * 4;
                    g.setColor(new Color(255, 216, 77, 160));
                    g.fill(new Ellipse2D.Double(x - r - pulso, y - r - pulso, (r + pulso) * 2, (r + pulso) * 2));
                }
                g.setColor(new Color(0, 0, 0, 40));
                g.fill(new Ellipse2D.Double(x - r, y - r + 5, r * 2, r * 2));
                Ellipse2D circulo = new Ellipse2D.Double(x - r, y - r, r * 2, r * 2);
                g.setColor(abierto ? (estrellas > 0 ? color : Tema.aclarar(color, 0.25)) : new Color(0xC9C6D8));
                g.fill(circulo);
                g.setColor(TRAZO);
                g.setStroke(ecorescate.juegos.Estilo.STROKE_3_0);
                g.draw(circulo);
                if (abierto) {
                    g.setColor(Color.WHITE);
                    g.setFont(Tema.fuente(Font.BOLD, 28f));
                    Tema.centrado(g, String.valueOf(n), x, y + 10);
                } else {
                    candado(g, x, y);
                }
                for (int k = 0; k < 3; k++) {
                    Iconos.estrella(g, x - 22 + k * 22, y + r + 16 - (k == 1 ? 4 : 0), 10, k < estrellas);
                }
                if (n == sugerido && abierto) {
                    double salto = Math.abs(Math.sin(reloj * 4)) * 8;
                    Personaje.dibujar(g, x, y - r - 4 - salto, 0.75, perfil.getAccesorio(), perfil.getPolo(), true, 0, false, 12, -42);
                }
            }

            // 3. Tarjeta con información del nivel señalado
            int info = encima >= 0 ? encima : Math.max(0, sugerido - 1);
            Nivel nv = niveles[info];
            boolean abierto = perfil.desbloqueado(tipo, info + 1);
            double cw = Math.min(760, w - 2 * margen), cx = (w - cw) / 2, cy = h - 92;
            RoundRectangle2D tarjeta = new RoundRectangle2D.Double(cx, cy, cw, 76, 22, 22);
            g.setColor(Color.WHITE);
            g.fill(tarjeta);
            g.setColor(TRAZO);
            g.setStroke(ecorescate.juegos.Estilo.STROKE_2_0);
            g.draw(tarjeta);
            g.setColor(new Color(tipo.getColorTexto()));
            g.setFont(Tema.espaciada(Font.BOLD, 12.5f, 0.1f));
            g.drawString("NIVEL " + nv.getNumero() + (abierto ? "" : " · BLOQUEADO"), (float) (cx + 22), (float) (cy + 26));
            g.setColor(Tema.TEXTO);
            g.setFont(Tema.fuente(Font.BOLD, 20f));
            g.drawString(nv.getTitulo(), (float) (cx + 22), (float) (cy + 52));
            StringBuilder retos = new StringBuilder();
            List<Reto> lista = nv.getRetos();
            for (int i = 0; i < lista.size(); i++) {
                if (i > 0) retos.append(" · ");
                retos.append(lista.get(i).getLargo());
            }
            g.setColor(Tema.TEXTO_SUAVE);
            g.setFont(Tema.fuente(Font.PLAIN, 14.5f));
            String texto = abierto ? "Retos: " + retos : "Supera el nivel " + (info) + " para abrirlo.";
            double tx = cx + 22 + Math.max(220, g.getFontMetrics(Tema.fuente(Font.BOLD, 20f)).stringWidth(nv.getTitulo()) + 30);
            List<String> lineas = Tema.partir(texto, g.getFontMetrics(), cx + cw - 22 - tx);
            double ly = cy + (lineas.size() > 1 ? 32 : 42);
            for (int i = 0; i < Math.min(2, lineas.size()); i++) {
                g.drawString(lineas.get(i), (float) tx, (float) ly);
                ly += 19;
            }
            g.dispose();
        }

        private void candado(Graphics2D g, double x, double y) {
            g.setColor(Color.WHITE);
            g.setStroke(ecorescate.juegos.Estilo.STROKE_4_0);
            g.draw(new java.awt.geom.Arc2D.Double(x - 9, y - 18, 18, 20, 0, 180, java.awt.geom.Arc2D.OPEN));
            g.fill(new RoundRectangle2D.Double(x - 13, y - 8, 26, 20, 5, 5));
            g.setColor(new Color(0xC9C6D8));
            g.fill(new Ellipse2D.Double(x - 3, y - 2, 6, 6));
        }
    }
}
