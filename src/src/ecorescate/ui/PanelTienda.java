package ecorescate.ui;

import ecorescate.juegos.Personaje;
import ecorescate.modelo.Accesorio;
import ecorescate.modelo.Perfil;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/** Tienda: el alumno gasta sus EcoMonedas en accesorios y elige el color de su polo. */
public class PanelTienda extends PantallaSecundaria {

    public interface Acciones {
        void volverAlMenu();

        void perfilCambiado();
    }

    private static final Color MORADO = new Color(0x2D1B69);
    private static final Color TRAZO = new Color(0x2A2340);
    private static final BasicStroke STROKE_1_6 = new BasicStroke(1.6f);
    private static final BasicStroke STROKE_2_0 = new BasicStroke(2f);
    private static final BasicStroke STROKE_2_2 = new BasicStroke(2.2f);
    private static final BasicStroke STROKE_4_0 = new BasicStroke(4f);

    private final Acciones acciones;
    private final Vitrina vitrina = new Vitrina();
    private Perfil perfil;

    public PanelTienda(Acciones acciones) {
        super("Tienda de EcoMonedas", acciones::volverAlMenu);
        this.acciones = acciones;
        setContenido(vitrina);
    }

    public void mostrar(Perfil perfil) {
        this.perfil = perfil;
        vitrina.mensaje = "Toca un accesorio para probarlo. ¡Gana EcoMonedas jugando!";
        vitrina.colorMensaje = Tema.TEXTO_SUAVE;
        setMonedas(perfil.getMonedas());
        vitrina.iniciarAnimacion();
        vitrina.repaint();
    }

    public void detenerAnimacion() {
        vitrina.detenerAnimacion();
    }

    private class Vitrina extends JComponent {

        private final Rectangle2D[] cajas = new Rectangle2D[Accesorio.values().length];
        private final Rectangle2D[] colores = new Rectangle2D[Personaje.POLOS.length];
        private final Timer timerAnim;
        private Accesorio encima;
        private String mensaje = "";
        private Color colorMensaje = Tema.TEXTO_SUAVE;
        private double reloj;

        Vitrina() {
            MouseAdapter m = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    Accesorio a = accesorioEn(e.getX(), e.getY());
                    if (a != encima) {
                        encima = a;
                        repaint();
                    }
                    boolean mano = a != null || colorEn(e.getX(), e.getY()) >= 0;
                    setCursor(Cursor.getPredefinedCursor(mano ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = null;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    pulsar(e.getX(), e.getY());
                }
            };
            addMouseListener(m);
            addMouseMotionListener(m);
            timerAnim = new Timer(40, e -> {
                if (isShowing()) {
                    reloj += 0.04;
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

        private Accesorio accesorioEn(int x, int y) {
            for (int i = 0; i < cajas.length; i++) if (cajas[i] != null && cajas[i].contains(x, y)) return Accesorio.values()[i];
            return null;
        }

        private int colorEn(int x, int y) {
            for (int i = 0; i < colores.length; i++) if (colores[i] != null && colores[i].contains(x, y)) return i;
            return -1;
        }

        private void pulsar(int x, int y) {
            if (perfil == null) return;
            int c = colorEn(x, y);
            if (c >= 0) {
                perfil.setPolo(c);
                Sonido.get().tocar(Sonido.Efecto.CLIC);
                acciones.perfilCambiado();
                repaint();
                return;
            }
            Accesorio a = accesorioEn(x, y);
            if (a == null) return;
            if (perfil.tiene(a)) {
                perfil.equipar(a);
                Sonido.get().tocar(Sonido.Efecto.CLIC);
                mensaje = a == Accesorio.NINGUNO ? "Te quitaste el accesorio." : "¡Te pusiste: " + a.getNombre() + "!";
                colorMensaje = Tema.PRINCIPAL;
            } else if (perfil.comprar(a)) {
                perfil.equipar(a);
                Sonido.get().tocar(Sonido.Efecto.MEDALLA);
                mensaje = "¡Compraste " + a.getNombre() + "! Ya lo tienes puesto.";
                colorMensaje = Tema.VERDE_OK;
            } else {
                Sonido.get().tocar(Sonido.Efecto.ERROR);
                mensaje = "Te faltan " + (a.getPrecio() - perfil.getMonedas()) + " EcoMonedas. ¡Sigue jugando para ganarlas!";
                colorMensaje = Tema.ROJO;
            }
            setMonedas(perfil.getMonedas());
            acciones.perfilCambiado();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            if (perfil == null) return;
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            int w = getWidth(), h = getHeight();
            double margen = Math.max(24, w * 0.04);

            // vitrina del personaje
            double pw = Math.min(360, w * 0.32), ph = h - 2 * margen;
            RoundRectangle2D panel = new RoundRectangle2D.Double(margen, margen, pw, ph, 26, 26);
            g.setColor(new Color(0xE9E6FB));
            g.fill(panel);
            g.setColor(TRAZO);
            g.setStroke(STROKE_2_2);
            g.draw(panel);
            Accesorio mostrado = encima != null ? encima : perfil.getAccesorio();
            double cx = margen + pw / 2, pies = margen + ph * 0.66;
            g.setColor(new Color(0xD6D1F5));
            g.fill(new Ellipse2D.Double(cx - 90, pies - 18, 180, 34));
            double escala = Math.min(2.8, ph * 0.0065);
            Personaje.dibujar(g, cx, pies - Math.abs(Math.sin(reloj * 3)) * 6, escala, mostrado, perfil.getPolo(),
                    true, 0, false, 12 + Math.sin(reloj * 6) * 4, -42);
            g.setColor(MORADO);
            g.setFont(Tema.fuente(Font.BOLD, 20f));
            Tema.centrado(g, perfil.getEstudiante().getPrimerNombre(), cx, margen + 36);

            g.setColor(Tema.ETIQUETA);
            g.setFont(Tema.espaciada(Font.BOLD, 12f, 0.1f));
            Tema.centrado(g, "COLOR DE POLO", cx, pies + 46);
            double d = 30, sep = 10, x0 = cx - (Personaje.POLOS.length * d + (Personaje.POLOS.length - 1) * sep) / 2;
            for (int i = 0; i < Personaje.POLOS.length; i++) {
                Ellipse2D c = new Ellipse2D.Double(x0 + i * (d + sep), pies + 58, d, d);
                colores[i] = c.getBounds2D();
                g.setColor(Personaje.POLOS[i]);
                g.fill(c);
                g.setColor(TRAZO);
                g.setStroke(i == perfil.getPolo() ? STROKE_4_0 : STROKE_1_6);
                g.draw(c);
            }

            // artículos
            double gx = margen * 2 + pw, gw = w - gx - margen;
            g.setColor(MORADO);
            g.setFont(Tema.fuente(Font.BOLD, 26f));
            g.drawString("Accesorios", (float) gx, (float) (margen + 26));
            g.setColor(colorMensaje);
            g.setFont(Tema.fuente(Font.BOLD, 16f));
            g.drawString(mensaje, (float) gx, (float) (margen + 52));

            Accesorio[] todos = Accesorio.values();
            int columnas = 4;
            double sepC = 16, cw = (gw - (columnas - 1) * sepC) / columnas;
            double ch = Math.min(200, (h - margen * 2 - 70 - sepC) / 2);
            for (int i = 0; i < todos.length; i++) {
                Accesorio a = todos[i];
                double bx = gx + (i % columnas) * (cw + sepC), by = margen + 70 + (i / columnas) * (ch + sepC);
                cajas[i] = new Rectangle2D.Double(bx, by, cw, ch);
                boolean propio = perfil.tiene(a), puesto = perfil.getAccesorio() == a;
                boolean alcanza = perfil.getMonedas() >= a.getPrecio();
                RoundRectangle2D caja = new RoundRectangle2D.Double(bx, by, cw, ch, 20, 20);
                g.setColor(a == encima ? new Color(0xFFF6D5) : Color.WHITE);
                g.fill(caja);
                g.setColor(puesto ? new Color(0x7C4DFF) : TRAZO);
                g.setStroke(puesto ? STROKE_4_0 : STROKE_2_0);
                g.draw(caja);

                Graphics2D gc = (Graphics2D) g.create();
                gc.clip(new Rectangle2D.Double(bx, by, cw, ch * 0.62));
                Personaje.dibujar(gc, bx + cw / 2, by + ch * 0.62 + 34, Math.min(1.5, ch / 120), a, perfil.getPolo(),
                        true, 0, false, 6, 13);
                gc.dispose();

                g.setColor(Tema.TEXTO);
                g.setFont(Tema.fuente(Font.BOLD, 16f));
                Tema.centrado(g, a.getNombre(), bx + cw / 2, by + ch * 0.62 + 24);
                double py = by + ch - 34;
                RoundRectangle2D boton = new RoundRectangle2D.Double(bx + 14, py, cw - 28, 26, 26, 26);
                if (puesto) {
                    g.setColor(new Color(0x7C4DFF));
                    g.fill(boton);
                    g.setColor(Color.WHITE);
                    g.setFont(Tema.fuente(Font.BOLD, 14f));
                    Tema.centrado(g, "PUESTO", boton.getCenterX(), py + 18);
                } else if (propio) {
                    g.setColor(new Color(0xE9E6FB));
                    g.fill(boton);
                    g.setColor(MORADO);
                    g.setFont(Tema.fuente(Font.BOLD, 14f));
                    Tema.centrado(g, "USAR", boton.getCenterX(), py + 18);
                } else {
                    g.setColor(alcanza ? new Color(0xFFD84D) : new Color(0xEDEAF3));
                    g.fill(boton);
                    Iconos.moneda(g, boton.getCenterX() - 24, py + 13, 9);
                    g.setColor(alcanza ? MORADO : Tema.ETIQUETA);
                    g.setFont(Tema.fuente(Font.BOLD, 15f));
                    g.drawString(String.valueOf(a.getPrecio()), (float) (boton.getCenterX() - 10), (float) (py + 18));
                }
            }
            g.dispose();
        }
    }
}
