package ecorescate.ui;

import ecorescate.juegos.Personaje;
import ecorescate.modelo.CatalogoNiveles;
import ecorescate.modelo.Estudiante;
import ecorescate.modelo.Medalla;
import ecorescate.modelo.Perfil;
import ecorescate.modelo.Progreso;
import ecorescate.modelo.SesionJuego;
import ecorescate.modelo.TipoJuego;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Menú central con las cuatro tarjetas de minijuegos (RF-07). */
public class PanelMenu extends JPanel {

    public interface Acciones {
        void jugar(TipoJuego tipo);

        void verImpacto();

        void verTienda();

        void verMedallas();

        void verRanking();

        void cambiarAlumno();
    }

    private final PantallaSecundaria.PildoraMonedas monedas = new PantallaSecundaria.PildoraMonedas();
    private final Map<TipoJuego, Tarjeta> tarjetas = new EnumMap<>(TipoJuego.class);
    private final TarjetaJugador jugador;

    public PanelMenu(Acciones acciones) {
        super(new BorderLayout());
        setBackground(Tema.FONDO);
        add(crearBarra(acciones), BorderLayout.NORTH);

        jugador = new TarjetaJugador(acciones);
        Contenido contenido = new Contenido(jugador);
        for (TipoJuego tipo : TipoJuego.values()) {
            Tarjeta tarjeta = new Tarjeta(tipo, () -> acciones.jugar(tipo));
            tarjetas.put(tipo, tarjeta);
            contenido.agregarTarjeta(tarjeta);
        }
        add(contenido, BorderLayout.CENTER);
    }

    private JPanel crearBarra(Acciones acciones) {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE),
                new EmptyBorder(10, 24, 10, 24)));

        javax.swing.Icon iconoApp = new javax.swing.Icon() {
            private final java.awt.Image img = Iconos.imagenAplicacion(34);
            @Override public void paintIcon(java.awt.Component c, Graphics g, int x, int y) { g.drawImage(img, x, y, null); }
            @Override public int getIconWidth() { return 34; }
            @Override public int getIconHeight() { return 34; }
        };
        JLabel marca = new JLabel("Operación EcoRescate", iconoApp, JLabel.LEFT);
        marca.setIconTextGap(10);
        marca.setFont(Tema.fuente(Font.BOLD, 22f));
        marca.setForeground(Tema.TEXTO);
        barra.add(marca, BorderLayout.WEST);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        derecha.setOpaque(false);
        BotonEco ranking = new BotonEco("Ranking del salón", new Color(0x7C4DFF), false);
        ranking.addActionListener(e -> acciones.verRanking());
        BotonEco impacto = new BotonEco("Mi Impacto Social", Tema.PRINCIPAL, true);
        impacto.addActionListener(e -> acciones.verImpacto());
        BotonEco cambiar = new BotonEco("Cambiar alumno", Tema.PRINCIPAL, false);
        cambiar.addActionListener(e -> acciones.cambiarAlumno());
        derecha.add(monedas);
        derecha.add(ranking);
        derecha.add(impacto);
        derecha.add(cambiar);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    public void actualizar(Estudiante estudiante, List<SesionJuego> sesiones, Perfil perfil) {
        monedas.setCantidad(perfil.getMonedas());
        jugador.actualizar(perfil, sesiones);
        for (Tarjeta t : tarjetas.values()) t.actualizar(perfil);
        repaint();
    }

    /** Zona central: título, tarjeta del jugador y fila de tarjetas que se adapta al ancho. */
    private static class Contenido extends JPanel {

        private static final int ARRIBA = 168;
        private final TarjetaJugador jugador;
        private final List<Tarjeta> juegos = new ArrayList<>();

        Contenido(TarjetaJugador jugador) {
            super(null);
            this.jugador = jugador;
            setBackground(Tema.FONDO);
            add(jugador);
        }

        void agregarTarjeta(Tarjeta t) {
            juegos.add(t);
            add(t);
        }

        private double margen() {
            return Math.max(28, getWidth() * 0.065);
        }

        private float tamanoTitulo = 36f;
        private int ultimoAncho = -1;

        @Override
        public void doLayout() {
            int n = juegos.size();
            double m = margen();
            double separacion = 22;
            double ancho = (getWidth() - 2 * m - separacion * (n - 1)) / n;
            int alto = (int) Math.min(480, Math.max(420, getHeight() - ARRIBA - 24));
            for (int i = 0; i < n; i++) {
                juegos.get(i).setBounds((int) (m + i * (ancho + separacion)), ARRIBA, (int) ancho, alto);
            }
            jugador.setBounds((int) (getWidth() - m - 400), 22, 400, 124);

            int w = getWidth();
            if (w != ultimoAncho) {
                ultimoAncho = w;
                double libre = w - 2 * m - 420;
                String titulo = "Cuatro minijuegos, cuatro hábitos";
                tamanoTitulo = 42f;
                Font f = Tema.fuente(Font.BOLD, tamanoTitulo);
                java.awt.FontMetrics fm = getFontMetrics(f);
                while (tamanoTitulo > 26 && fm.stringWidth(titulo) > libre) {
                    tamanoTitulo -= 2;
                    f = Tema.fuente(Font.BOLD, tamanoTitulo);
                    fm = getFontMetrics(f);
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            float m = (float) margen();
            double libre = getWidth() - 2 * m - 420;
            String titulo = "Cuatro minijuegos, cuatro hábitos";
            g.setFont(Tema.fuente(Font.BOLD, tamanoTitulo));
            g.setColor(Tema.TEXTO);
            g.drawString(titulo, m, 78);
            g.setColor(Tema.TEXTO_SUAVE);
            g.setFont(Tema.fuente(Font.PLAIN, 19f));
            Tema.parrafo(g, "Cada misión entrena una conducta que el niño puede repetir en el colegio y en casa. "
                    + "¡Gana estrellas, EcoMonedas y medallas!", m, 112, libre, 23, false);
            g.dispose();
        }
    }

    /** Tarjeta del jugador: su personaje, estrellas, medallas y accesos a Tienda y Medallas. */
    private static class TarjetaJugador extends JPanel {

        private Perfil perfil;
        private int estrellas;

        TarjetaJugador(Acciones acciones) {
            super(null);
            setOpaque(false);
            BotonEco tienda = new BotonEco("Tienda", new Color(0x7C4DFF), true);
            tienda.setFont(Tema.fuente(Font.BOLD, 15f));
            tienda.addActionListener(e -> acciones.verTienda());
            BotonEco medallas = new BotonEco("Medallas", new Color(0x7C4DFF), false);
            medallas.setFont(Tema.fuente(Font.BOLD, 15f));
            medallas.addActionListener(e -> acciones.verMedallas());
            tienda.setBounds(118, 78, 112, 34);
            medallas.setBounds(240, 78, 124, 34);
            add(tienda);
            add(medallas);
        }

        void actualizar(Perfil perfil, List<SesionJuego> sesiones) {
            this.perfil = perfil;
            this.estrellas = perfil.totalEstrellas();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            if (perfil == null) return;
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            RoundRectangle2D r = new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 22, 22);
            g.setColor(Color.WHITE);
            g.fill(r);
            g.setColor(new Color(0x2A2340));
            g.setStroke(new BasicStroke(2f));
            g.draw(r);
            g.setColor(new Color(0xE9E6FB));
            g.fill(new RoundRectangle2D.Double(10, 10, 92, getHeight() - 20, 16, 16));
            Graphics2D gc = (Graphics2D) g.create();
            gc.clip(new RoundRectangle2D.Double(10, 10, 92, getHeight() - 20, 16, 16));
            Personaje.dibujar(gc, 56, getHeight() - 8, 1.25, perfil.getAccesorio(), perfil.getPolo(), true, 0, false, 12, -42);
            gc.dispose();

            g.setColor(new Color(0x2D1B69));
            g.setFont(Tema.fuente(Font.BOLD, 22f));
            g.drawString("¡Hola, " + perfil.getEstudiante().getPrimerNombre() + "!", 118, 34);
            Iconos.estrella(g, 128, 58, 10, true);
            g.setColor(Tema.TEXTO);
            g.setFont(Tema.fuente(Font.BOLD, 16f));
            g.drawString(estrellas + "/" + (CatalogoNiveles.NIVELES * 3 * TipoJuego.values().length), 144, 64);
            Iconos.medalla(g, Medalla.PRIMERA_MISION, 210, 61, 9, true);
            g.setColor(Tema.TEXTO);
            g.setFont(Tema.fuente(Font.BOLD, 16f));
            g.drawString(perfil.getMedallas().size() + "/" + Medalla.values().length, 226, 64);
            g.setColor(Tema.ETIQUETA);
            g.setFont(Tema.fuente(Font.PLAIN, 15f));
            g.drawString(perfil.getEstudiante().getAula(), 300, 64);
            g.dispose();
        }
    }

    /** Tarjeta de un minijuego, con el mismo diseño de la propuesta. */
    private static class Tarjeta extends JComponent {

        private final TipoJuego tipo;
        private boolean encima;
        private int partidas;
        private int mejorPuntaje;
        private int mejorNivel;
        private int estrellas;
        private int[] porNivel = new int[CatalogoNiveles.NIVELES];

        Tarjeta(TipoJuego tipo, Runnable alPulsar) {
            this.tipo = tipo;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Jugar " + tipo.getNombre());
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                        encima = false;
                        repaint();
                        Sonido.get().tocar(Sonido.Efecto.CLIC);
                        alPulsar.run();
                    }
                }
            });
        }

        void actualizar(Perfil perfil) {
            estrellas = perfil.estrellasDe(tipo);
            mejorNivel = perfil.nivelesSuperados(tipo);
            partidas = perfil.getEstrellas(tipo, 1) > 0 ? 1 : 0;
            mejorPuntaje = 0;
            porNivel = new int[CatalogoNiveles.NIVELES];
            for (int n = 1; n <= CatalogoNiveles.NIVELES; n++) porNivel[n - 1] = perfil.getEstrellas(tipo, n);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            Color color = new Color(tipo.getColor());
            int w = getWidth();
            double y0 = encima ? 2 : 8;
            double alto = getHeight() - 14;

            if (encima) {
                for (int i = 1; i <= 6; i++) {
                    g.setColor(new Color(20, 60, 50, 7));
                    g.fill(new RoundRectangle2D.Double(-i * 0.5 + 1, y0 + i * 1.2, w - 2 + i, alto, 22, 22));
                }
            }
            RoundRectangle2D forma = new RoundRectangle2D.Double(1, y0, w - 2, alto, 18, 18);
            g.setColor(Color.WHITE);
            g.fill(forma);
            if (encima) {
                g.setColor(Tema.alfa(color, 140));
                g.setStroke(new BasicStroke(2f));
                g.draw(forma);
            }

            Iconos.logo(g, tipo, w / 2.0, y0 + 26 + 46, 92);

            double x = 28, ancho = w - 56;
            g.setColor(new Color(tipo.getColorTexto()));
            g.setFont(Tema.fuente(Font.BOLD, 23f));
            Tema.parrafo(g, tipo.getNombre(), 18, y0 + 168, w - 36, 27, true);

            double y = y0 + 232;
            y = seccion(g, "EN EL JUEGO", tipo.getEnElJuego(), false, x, y, ancho);
            seccion(g, "EN LA VIDA REAL", tipo.getEnLaVidaReal(), true, x, Math.max(y, y0 + 322), ancho);

            // pie de la tarjeta: progreso y botón Jugar
            double pie = y0 + alto - 86;
            g.setColor(Tema.BORDE);
            g.fillRect((int) x, (int) pie, (int) ancho, 1);
            Iconos.estrella(g, x + 10, pie + 20, 10, estrellas > 0);
            g.setColor(Tema.TEXTO);
            g.setFont(Tema.fuente(Font.BOLD, 15f));
            g.drawString(estrellas + "/" + CatalogoNiveles.NIVELES * 3, (float) (x + 26), (float) (pie + 26));
            // una bolita por nivel: llena si está superado
            double paso = Math.min(13, (ancho - 92) / CatalogoNiveles.NIVELES);
            for (int i = 0; i < CatalogoNiveles.NIVELES; i++) {
                double bx = x + 86 + i * paso;
                g.setColor(porNivel[i] == 3 ? new Color(0xFFC83D) : porNivel[i] > 0 ? color : new Color(0xE3E0EE));
                g.fill(new java.awt.geom.Ellipse2D.Double(bx, pie + 14, paso - 3, paso - 3));
            }

            RoundRectangle2D boton = new RoundRectangle2D.Double(x, pie + 38, ancho, 36, 36, 36);
            g.setColor(encima ? color : Tema.alfa(color, 30));
            g.fill(boton);
            Color tinta = encima ? Color.WHITE : new Color(tipo.getColorTexto());
            g.setColor(tinta);
            g.setFont(Tema.fuente(Font.BOLD, 17f));
            String jugar = partidas == 0 ? "Empezar" : "Nivel " + Math.min(CatalogoNiveles.NIVELES, mejorNivel + 1) + " de 10";
            if (mejorNivel == CatalogoNiveles.NIVELES) jugar = "Ver niveles";
            int anchoTexto = g.getFontMetrics().stringWidth(jugar);
            double cx = x + ancho / 2 - 8;
            g.drawString(jugar, (float) (cx - anchoTexto / 2.0), (float) (pie + 62));
            Path2D flecha = new Path2D.Double();
            double fx = cx + anchoTexto / 2.0 + 10, fy = pie + 56;
            flecha.moveTo(fx, fy - 6);
            flecha.lineTo(fx + 10, fy);
            flecha.lineTo(fx, fy + 6);
            flecha.closePath();
            g.fill(flecha);
            g.dispose();
        }

        private double seccion(Graphics2D g, String etiqueta, String texto, boolean negrita,
                               double x, double y, double ancho) {
            g.setColor(Tema.ETIQUETA);
            g.setFont(Tema.espaciada(Font.BOLD, 13f, 0.12f));
            g.drawString(etiqueta, (float) x, (float) y);
            g.setColor(Tema.TEXTO);
            g.setFont(Tema.fuente(negrita ? Font.BOLD : Font.PLAIN, 17f));
            return Tema.parrafo(g, texto, x, y + 29, ancho, 21, false) + 22;
        }
    }
}
