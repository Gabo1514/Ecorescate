package ecorescate.ui;

import ecorescate.modelo.Medalla;
import ecorescate.modelo.Perfil;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

/** Álbum de medallas: las ganadas a color y las que faltan en gris, con lo que hay que hacer. */
public class PanelMedallas extends PantallaSecundaria {

    private final Album album = new Album();
    private Perfil perfil;

    public PanelMedallas(Runnable volver) {
        super("Mis medallas", volver);
        setContenido(album);
    }

    public void mostrar(Perfil perfil) {
        this.perfil = perfil;
        setMonedas(perfil.getMonedas());
        album.repaint();
    }

    private class Album extends JComponent {
        @Override
        protected void paintComponent(Graphics g0) {
            if (perfil == null) return;
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            int w = getWidth(), h = getHeight();
            double margen = Math.max(24, w * 0.04);
            Medalla[] todas = Medalla.values();
            int ganadas = 0;
            for (Medalla m : todas) if (perfil.tiene(m)) ganadas++;

            g.setColor(new Color(0x2D1B69));
            g.setFont(Tema.fuente(Font.BOLD, 26f));
            g.drawString("Tienes " + ganadas + " de " + todas.length + " medallas", (float) margen, (float) (margen + 24));
            double bw = 260, bx = w - margen - bw, by = margen + 6;
            g.setColor(new Color(0xE3E0F5));
            g.fill(new RoundRectangle2D.Double(bx, by, bw, 18, 18, 18));
            g.setColor(new Color(0xFFC83D));
            g.fill(new RoundRectangle2D.Double(bx, by, Math.max(18, bw * ganadas / todas.length), 18, 18, 18));

            int columnas = 4;
            double sep = 16, top = margen + 52;
            double cw = (w - 2 * margen - (columnas - 1) * sep) / columnas;
            int filas = (todas.length + columnas - 1) / columnas;
            double ch = Math.min(170, (h - top - margen - (filas - 1) * sep) / filas);
            for (int i = 0; i < todas.length; i++) {
                Medalla m = todas[i];
                boolean tiene = perfil.tiene(m);
                double x = margen + (i % columnas) * (cw + sep), y = top + (i / columnas) * (ch + sep);
                RoundRectangle2D caja = new RoundRectangle2D.Double(x, y, cw, ch, 20, 20);
                g.setColor(tiene ? Color.WHITE : new Color(0xF4F3FA));
                g.fill(caja);
                g.setColor(tiene ? new Color(0x2A2340) : new Color(0xCFCBE0));
                g.setStroke(new BasicStroke(2f));
                g.draw(caja);
                double r = Math.min(30, ch * 0.2);
                Iconos.medalla(g, m, x + 20 + r, y + ch / 2 + r * 0.4, r, tiene);
                double tx = x + 36 + r * 2;
                g.setColor(tiene ? Tema.TEXTO : Tema.ETIQUETA);
                g.setFont(Tema.fuente(Font.BOLD, 18f));
                Tema.parrafo(g, m.getNombre(), tx, y + ch / 2 - 14, cw - (tx - x) - 12, 20, false);
                g.setColor(Tema.TEXTO_SUAVE);
                g.setFont(Tema.fuente(Font.PLAIN, 14.5f));
                Tema.parrafo(g, m.getDescripcion(), tx, y + ch / 2 + 10, cw - (tx - x) - 12, 17, false);
            }
            g.dispose();
        }
    }
}
