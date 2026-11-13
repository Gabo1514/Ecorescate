package ecorescate.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Colores, fuentes y utilidades de dibujo compartidas por toda la aplicación. */
public final class Tema {

    public static final Color FONDO = new Color(0xF1F6F2);
    public static final Color TARJETA = Color.WHITE;
    public static final Color TEXTO = new Color(0x17332E);
    public static final Color TEXTO_SUAVE = new Color(0x56625E);
    public static final Color ETIQUETA = new Color(0x6E7472);
    public static final Color BORDE = new Color(0xDDE7E1);
    public static final Color ROJO = new Color(0xD64B45);
    public static final Color VERDE_OK = new Color(0x2E9455);
    public static final Color AVISO = new Color(0xC27A12);
    public static final Color PRINCIPAL = new Color(0x23786A);

    private static final String FAMILIA = elegirFamilia("Calibri", "Segoe UI", "Arial");
    private static final Font BASE_PLAIN = new Font(FAMILIA, Font.PLAIN, 12);
    private static final Font BASE_BOLD = new Font(FAMILIA, Font.BOLD, 12);
    private static final Map<Integer, Font> CACHE_FUENTES = new ConcurrentHashMap<>();
    private static final Map<Long, Font> CACHE_ESPACIADAS = new ConcurrentHashMap<>();

    private Tema() {
    }

    private static String elegirFamilia(String... opciones) {
        return Font.SANS_SERIF;
    }

    public static Font fuente(int estilo, float tamano) {
        // Redondear a décimas de punto para permitir caché eficiente incluso en animaciones de escala
        int tamDiez = Math.round(tamano * 10f);
        int clave = (estilo << 24) | (tamDiez & 0xFFFFFF);
        Font f = CACHE_FUENTES.get(clave);
        if (f == null) {
            Font base = (estilo == Font.BOLD) ? BASE_BOLD : (estilo == Font.PLAIN ? BASE_PLAIN : new Font(FAMILIA, estilo, 12));
            f = base.deriveFont(tamDiez / 10f);
            if (CACHE_FUENTES.size() < 256) {
                CACHE_FUENTES.put(clave, f);
            }
        }
        return f;
    }

    /** Fuente con espaciado entre letras, para etiquetas tipo "EN EL JUEGO". */
    public static Font espaciada(int estilo, float tamano, float espaciado) {
        int tamDiez = Math.round(tamano * 10f);
        int espMil = Math.round(espaciado * 1000f);
        long clave = (((long) estilo) << 48) | (((long) (tamDiez & 0xFFFFFF)) << 24) | (espMil & 0xFFFFFFL);
        Font f = CACHE_ESPACIADAS.get(clave);
        if (f == null) {
            Map<TextAttribute, Object> atributos = new HashMap<>(2);
            atributos.put(TextAttribute.TRACKING, espaciado);
            f = fuente(estilo, tamano).deriveFont(atributos);
            if (CACHE_ESPACIADAS.size() < 128) {
                CACHE_ESPACIADAS.put(clave, f);
            }
        }
        return f;
    }

    public static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    }

    /** Dibuja un texto centrado horizontalmente en cx, con su línea base en y. */
    public static void centrado(Graphics2D g, String texto, double cx, double y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(texto, (float) (cx - fm.stringWidth(texto) / 2.0), (float) y);
    }

    private static final Map<String, List<String>> CACHE_PARTIR = new ConcurrentHashMap<>();

    /** Parte un texto en líneas que no superen el ancho indicado. */
    public static List<String> partir(String texto, FontMetrics fm, double ancho) {
        if (texto == null || texto.isEmpty()) return new ArrayList<>();
        int anchoInt = (int) Math.round(ancho);
        String clave = fm.getFont().getName() + "_" + fm.getFont().getSize() + "_" + anchoInt + "_" + texto;
        List<String> enCache = CACHE_PARTIR.get(clave);
        if (enCache != null) return enCache;

        List<String> lineas = new ArrayList<>();
        if (texto == null || texto.isEmpty()) return lineas;
        StringBuilder actual = new StringBuilder();
        int espacioW = fm.charWidth(' ');
        int actualW = 0;
        int inicio = 0;
        int len = texto.length();
        while (inicio < len) {
            int fin = texto.indexOf(' ', inicio);
            if (fin < 0) fin = len;
            String palabra = texto.substring(inicio, fin);
            inicio = fin + 1;
            if (palabra.isEmpty()) continue;
            int palabraW = fm.stringWidth(palabra);
            if (actual.length() == 0) {
                actual.append(palabra);
                actualW = palabraW;
            } else if (actualW + espacioW + palabraW > ancho) {
                lineas.add(actual.toString());
                actual.setLength(0);
                actual.append(palabra);
                actualW = palabraW;
            } else {
                actual.append(' ').append(palabra);
                actualW += espacioW + palabraW;
            }
        }
        if (actual.length() > 0) lineas.add(actual.toString());
        if (CACHE_PARTIR.size() < 256) {
            CACHE_PARTIR.put(clave, lineas);
        }
        return lineas;
    }

    /** Dibuja un párrafo y devuelve la coordenada y después de la última línea. */
    public static double parrafo(Graphics2D g, String texto, double x, double y, double ancho,
                                 double alturaLinea, boolean centrar) {
        for (String linea : partir(texto, g.getFontMetrics(), ancho)) {
            if (centrar) centrado(g, linea, x + ancho / 2, y);
            else g.drawString(linea, (float) x, (float) y);
            y += alturaLinea;
        }
        return y;
    }

    private static final Map<Long, Color> CACHE_ALFA = new ConcurrentHashMap<>();

    public static Color alfa(Color c, int alfa) {
        int a = (int) limitar(alfa, 0, 255);
        if (c.getAlpha() == a) return c;
        // Caché para evitar crear miles de objetos Color en cada frame de pintura
        long clave = ((long) c.getRGB() << 8) | (a & 0xFF);
        Color resultado = CACHE_ALFA.get(clave);
        if (resultado == null) {
            resultado = new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
            if (CACHE_ALFA.size() < 512) {
                CACHE_ALFA.put(clave, resultado);
            }
        }
        return resultado;
    }

    private static final Map<Long, Color> CACHE_COLOR_OP = new ConcurrentHashMap<>();

    public static Color mezclar(Color a, Color b, double t) {
        t = limitar(t, 0, 1);
        int paso = (int) Math.round(t * 100);
        long clave = (((long) a.getRGB()) << 32) ^ (((long) b.getRGB()) << 8) ^ paso;
        Color r = CACHE_COLOR_OP.get(clave);
        if (r == null) {
            r = new Color(
                    (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                    (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                    (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t),
                    (int) Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
            if (CACHE_COLOR_OP.size() < 512) CACHE_COLOR_OP.put(clave, r);
        }
        return r;
    }

    public static Color oscurecer(Color c, double t) {
        t = limitar(t, 0, 1);
        int paso = (int) Math.round(t * 100);
        long clave = (((long) c.getRGB()) << 16) | paso | 0x100000000L;
        Color r = CACHE_COLOR_OP.get(clave);
        if (r == null) {
            double f = 1.0 - t;
            r = new Color(
                    (int) Math.round(c.getRed() * f),
                    (int) Math.round(c.getGreen() * f),
                    (int) Math.round(c.getBlue() * f),
                    c.getAlpha());
            if (CACHE_COLOR_OP.size() < 512) CACHE_COLOR_OP.put(clave, r);
        }
        return r;
    }

    public static Color aclarar(Color c, double t) {
        t = limitar(t, 0, 1);
        int paso = (int) Math.round(t * 100);
        long clave = (((long) c.getRGB()) << 16) | paso | 0x200000000L;
        Color r = CACHE_COLOR_OP.get(clave);
        if (r == null) {
            r = new Color(
                    (int) Math.round(c.getRed() + (255 - c.getRed()) * t),
                    (int) Math.round(c.getGreen() + (255 - c.getGreen()) * t),
                    (int) Math.round(c.getBlue() + (255 - c.getBlue()) * t),
                    c.getAlpha());
            if (CACHE_COLOR_OP.size() < 512) CACHE_COLOR_OP.put(clave, r);
        }
        return r;
    }

    public static double limitar(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
