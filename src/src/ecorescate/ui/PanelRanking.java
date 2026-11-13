package ecorescate.ui;

import ecorescate.juegos.Personaje;
import ecorescate.modelo.Accesorio;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/** Ranking del salón: suma del mejor puntaje de cada minijuego. */
public class PanelRanking extends PantallaSecundaria {

    /** Una fila del ranking. */
    public static class Fila {
        final String nombre;
        final int estrellas, puntos, polo;
        final Accesorio accesorio;
        final boolean yo;

        public Fila(String nombre, int estrellas, int puntos, Accesorio accesorio, int polo, boolean yo) {
            this.nombre = nombre;
            this.estrellas = estrellas;
            this.puntos = puntos;
            this.accesorio = accesorio;
            this.polo = polo;
            this.yo = yo;
        }
    }

    private static final Color[] PODIO = {new Color(0xFFC83D), new Color(0xC9CED6), new Color(0xD99058)};

    private final Tabla tabla = new Tabla();
    private final List<Fila> filas = new ArrayList<>();
    private String aula = "";
    private int miPuesto;

    public PanelRanking(Runnable volver) {
        super("Ranking del salón", volver);
        setContenido(tabla);
    }

    /** @param filas ordenadas de mayor a menor */
    public void mostrar(String aula, List<Fila> filas, int monedas) {
        this.aula = aula;
        this.filas.clear();
        this.filas.addAll(filas);
        miPuesto = 0;
        for (int i = 0; i < filas.size(); i++) if (filas.get(i).yo) miPuesto = i + 1;
        setMonedas(monedas);
        tabla.repaint();
    }

    private class Tabla extends JComponent {
        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            int w = getWidth(), h = getHeight();
            double margen = Math.max(24, w * 0.06);
            g.setColor(new Color(0x2D1B69));
            g.setFont(Tema.fuente(Font.BOLD, 26f));
            g.drawString("Los guardianes del " + aula, (float) margen, (float) (margen + 20));
            g.setColor(Tema.TEXTO_SUAVE);
            g.setFont(Tema.fuente(Font.PLAIN, 16f));
            String sub = miPuesto > 0 ? "Vas en el puesto " + miPuesto + " de " + filas.size()
                    + ". Primero cuentan las estrellas; si hay empate, el puntaje." : "";
            g.drawString(sub, (float) margen, (float) (margen + 46));

            if (filas.size() <= 1) {
                g.setColor(Tema.TEXTO);
                g.setFont(Tema.fuente(Font.BOLD, 18f));
                g.drawString("¡Eres el primero de tu salón en jugar! Invita a tus compañeros a superar tu puntaje.",
                        (float) margen, (float) (margen + 90));
            }

            double top = margen + 66;
            double fh = Math.min(58, (h - top - margen) / Math.max(1, Math.min(filas.size(), 10)));
            int mostradas = (int) Math.min(filas.size(), Math.floor((h - top - margen) / fh));
            for (int i = 0; i < mostradas; i++) {
                Fila f = filas.get(i);
                double y = top + i * fh;
                RoundRectangle2D fila = new RoundRectangle2D.Double(margen, y, w - 2 * margen, fh - 8, 18, 18);
                g.setColor(f.yo ? new Color(0xFFF3C4) : Color.WHITE);
                g.fill(fila);
                g.setColor(f.yo ? new Color(0xE0A800) : new Color(0xDDD9EE));
                g.setStroke(new BasicStroke(f.yo ? 2.5f : 1.5f));
                g.draw(fila);
                double cy = y + (fh - 8) / 2;
                Ellipse2D puesto = new Ellipse2D.Double(margen + 14, cy - 17, 34, 34);
                g.setColor(i < 3 ? PODIO[i] : new Color(0xE9E6FB));
                g.fill(puesto);
                g.setColor(new Color(0x2A2340));
                g.setStroke(new BasicStroke(1.8f));
                g.draw(puesto);
                g.setFont(Tema.fuente(Font.BOLD, 17f));
                Tema.centrado(g, String.valueOf(i + 1), margen + 31, cy + 6);

                Graphics2D gc = (Graphics2D) g.create();
                gc.clip(new Rectangle2D.Double(margen + 58, y, 50, fh - 8));
                Personaje.dibujar(gc, margen + 83, y + fh + 22, 0.82, f.accesorio, f.polo, true, 0, false, 6, 13);
                gc.dispose();

                g.setColor(Tema.TEXTO);
                g.setFont(Tema.fuente(Font.BOLD, 19f));
                g.drawString(f.nombre + (f.yo ? "  (tú)" : ""), (float) (margen + 122), (float) (cy + 7));

                double ex = w - margen - 330;
                Iconos.estrella(g, ex, cy, 12, true);
                g.setColor(Tema.TEXTO);
                g.setFont(Tema.fuente(Font.BOLD, 17f));
                g.drawString(f.estrellas + " / 120", (float) (ex + 20), (float) (cy + 6));
                g.setFont(Tema.fuente(Font.BOLD, 22f));
                String pts = f.puntos + " pts";
                g.drawString(pts, (float) (w - margen - 24 - g.getFontMetrics().stringWidth(pts)), (float) (cy + 8));
            }
            g.dispose();
        }
    }
}
