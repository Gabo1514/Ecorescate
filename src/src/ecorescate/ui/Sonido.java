package ecorescate.ui;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Efectos de sonido y música de fondo. Todo se sintetiza al iniciar (no hay archivos de audio).
 * Todas las operaciones de hardware de audio corren en un hilo dedicado para no bloquear nunca
 * la interfaz gráfica (EDT), incluso en computadoras de laboratorio con drivers lentos o sin parlantes.
 */
public final class Sonido {

    public enum Efecto {
        CLIC, RECOGER, ACIERTO1, ACIERTO2, ACIERTO3, ACIERTO4, ACIERTO5, ERROR, NIVEL, COMBO,
        VICTORIA, DERROTA, MONEDA, MEDALLA, PODER, ESTRELLA
    }

    private enum Onda { SENO, CUADRADA, TRIANGULO }

    private static final float FRECUENCIA = 22050f;
    private static final AudioFormat FORMATO = new AudioFormat(FRECUENCIA, 16, 1, true, false);
    private static final Sonido INSTANCIA = new Sonido();

    private final Map<Efecto, Clip[]> clips = new EnumMap<>(Efecto.class);
    private final Map<Efecto, Integer> turno = new EnumMap<>(Efecto.class);
    private final Map<Efecto, Long> ultimoToque = new EnumMap<>(Efecto.class);
    private final ExecutorService colaAudio = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ecorescate-audio");
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY - 1);
        return t;
    });

    private Clip musica;
    private volatile boolean listo;
    private volatile boolean activo = true;
    private volatile boolean musicaDeseada;
    private File archivoConfig;

    private Sonido() {
    }

    public static Sonido get() {
        return INSTANCIA;
    }

    /** Lee la preferencia guardada y prepara los sonidos en segundo plano sin bloquear el arranque. */
    public void iniciar(File config) {
        archivoConfig = config;
        if (config != null && config.exists()) {
            Properties p = new Properties();
            try (InputStream in = new FileInputStream(config)) {
                p.load(in);
                activo = !"false".equals(p.getProperty("sonido"));
            } catch (IOException ignorado) {
                // se deja el sonido activado
            }
        }
        colaAudio.submit(this::preparar);
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean valor) {
        activo = valor;
        colaAudio.submit(() -> {
            try {
                if (!valor && musica != null) {
                    musica.stop();
                } else if (valor && musicaDeseada && musica != null && !musica.isRunning()) {
                    musica.loop(Clip.LOOP_CONTINUOUSLY);
                }
            } catch (Exception ignorado) {
            }
            if (archivoConfig != null) {
                Properties p = new Properties();
                p.setProperty("sonido", String.valueOf(valor));
                File carpeta = archivoConfig.getAbsoluteFile().getParentFile();
                if (carpeta != null) carpeta.mkdirs();
                try (OutputStream out = new FileOutputStream(archivoConfig)) {
                    p.store(out, "Preferencias de Operación EcoRescate");
                } catch (IOException ignorado) {
                    // no es grave si no se guarda
                }
            }
        });
    }

    public void tocar(Efecto e) {
        if (!activo || !listo || e == null) return;
        long ahora = System.nanoTime();
        Long previo = ultimoToque.get(e);
        // Evita saturar el mixer si el mismo efecto se dispara varias veces en el mismo frame (< 30 ms)
        if (previo != null && (ahora - previo) < 30_000_000L) return;
        ultimoToque.put(e, ahora);

        colaAudio.submit(() -> {
            if (!activo || !listo) return;
            try {
                Clip[] grupo = clips.get(e);
                if (grupo == null || grupo.length == 0) return;
                int i = turno.getOrDefault(e, 0);
                turno.put(e, (i + 1) % grupo.length);
                Clip c = grupo[i];
                if (c != null) {
                    if (c.isRunning()) c.stop();
                    c.setFramePosition(0);
                    c.start();
                }
            } catch (Exception ignorado) {
            }
        });
    }

    /** "Ding" de acierto que sube de tono con el combo (1 a 5). */
    public void acierto(int combo) {
        Efecto[] escala = {Efecto.ACIERTO1, Efecto.ACIERTO2, Efecto.ACIERTO3, Efecto.ACIERTO4, Efecto.ACIERTO5};
        tocar(escala[Math.max(1, Math.min(5, combo)) - 1]);
    }

    public void musica(boolean sonar) {
        musicaDeseada = sonar;
        if (!listo) return;
        colaAudio.submit(this::actualizarMusicaInterno);
    }

    private void actualizarMusicaInterno() {
        if (musica == null) return;
        try {
            if (musicaDeseada && activo) {
                if (!musica.isRunning()) musica.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                if (musica.isRunning()) musica.stop();
            }
        } catch (Exception ignorado) {
        }
    }

    // ---------------------------------------------------------------- síntesis

    private void preparar() {
        try {
            Map<Efecto, double[]> datos = new EnumMap<>(Efecto.class);
            datos.put(Efecto.CLIC, mezcla(0.05, b -> tono(b, 0, 0.04, 900, 650, Onda.SENO, 0.25)));
            datos.put(Efecto.RECOGER, mezcla(0.16, b -> {
                tono(b, 0, 0.08, 500, 1100, Onda.SENO, 0.35);
                tono(b, 0.07, 0.07, 1250, 1250, Onda.TRIANGULO, 0.25);
            }));
            for (int k = 1; k <= 5; k++) {
                final int base = 76 + (k - 1) * 2;
                datos.put(Efecto.valueOf("ACIERTO" + k), mezcla(0.26, b -> {
                    tono(b, 0, 0.07, midi(base), midi(base), Onda.TRIANGULO, 0.32);
                    tono(b, 0.07, 0.17, midi(base + 7), midi(base + 7), Onda.TRIANGULO, 0.32);
                }));
            }
            datos.put(Efecto.ERROR, mezcla(0.36, b -> {
                tono(b, 0, 0.14, 230, 170, Onda.CUADRADA, 0.16);
                tono(b, 0.15, 0.2, 170, 110, Onda.CUADRADA, 0.16);
            }));
            datos.put(Efecto.NIVEL, mezcla(0.62, b -> arpegio(b, 0, new int[]{72, 76, 79, 84}, 0.08, 0.3, 0.28)));
            datos.put(Efecto.COMBO, mezcla(0.34, b -> arpegio(b, 0, new int[]{79, 84, 88}, 0.06, 0.16, 0.25)));
            datos.put(Efecto.VICTORIA, mezcla(1.3, b -> {
                arpegio(b, 0, new int[]{72, 76, 79}, 0.13, 0.13, 0.28);
                tono(b, 0.39, 0.55, midi(84), midi(84), Onda.TRIANGULO, 0.3);
                tono(b, 0.39, 0.55, midi(79), midi(79), Onda.SENO, 0.18);
                tono(b, 0.39, 0.55, midi(76), midi(76), Onda.SENO, 0.14);
            }));
            datos.put(Efecto.DERROTA, mezcla(0.9, b -> arpegio(b, 0, new int[]{72, 70, 67, 64}, 0.17, 0.3, 0.26)));
            datos.put(Efecto.MONEDA, mezcla(0.28, b -> {
                tono(b, 0, 0.06, midi(83), midi(83), Onda.CUADRADA, 0.12);
                tono(b, 0.06, 0.2, midi(88), midi(88), Onda.CUADRADA, 0.12);
            }));
            datos.put(Efecto.MEDALLA, mezcla(0.8, b -> {
                arpegio(b, 0, new int[]{84, 88, 91, 96}, 0.07, 0.4, 0.22);
                tono(b, 0.28, 0.4, midi(91), midi(91), Onda.SENO, 0.12);
            }));
            datos.put(Efecto.PODER, mezcla(0.6, b -> {
                tono(b, 0, 0.35, 300, 1200, Onda.CUADRADA, 0.1);
                arpegio(b, 0.3, new int[]{84, 88, 91}, 0.05, 0.15, 0.22);
            }));
            datos.put(Efecto.ESTRELLA, mezcla(0.32, b -> {
                tono(b, 0, 0.05, midi(88), midi(88), Onda.TRIANGULO, 0.28);
                tono(b, 0.05, 0.22, midi(95), midi(95), Onda.SENO, 0.24);
            }));

            boolean audioDisponible = true;
            for (Map.Entry<Efecto, double[]> e : datos.entrySet()) {
                if (!audioDisponible) break;
                int copias = e.getKey().name().startsWith("ACIERTO") || e.getKey() == Efecto.RECOGER ? 2 : 1;
                byte[] bytes = aBytes(e.getValue());
                Clip primero = abrirSeguro(bytes);
                if (primero == null) {
                    if (clips.isEmpty()) {
                        audioDisponible = false;
                        break;
                    }
                    continue;
                }
                Clip segundo = copias > 1 ? abrirSeguro(bytes) : null;
                Clip[] grupo = segundo != null ? new Clip[]{primero, segundo} : new Clip[]{primero};
                clips.put(e.getKey(), grupo);
            }
            Clip m = audioDisponible ? abrirSeguro(aBytes(cancion())) : null;
            if (m != null) {
                try {
                    FloatControl vol = (FloatControl) m.getControl(FloatControl.Type.MASTER_GAIN);
                    vol.setValue(Math.max(vol.getMinimum(), -6f));
                } catch (IllegalArgumentException ignorado) {
                    // sin control de volumen: ya viene suave
                }
                musica = m;
            }
            listo = !clips.isEmpty() || musica != null;
            if (listo && musicaDeseada) {
                actualizarMusicaInterno();
            }
        } catch (Throwable ex) {
            listo = false; // sin audio: el juego sigue en silencio
        }
    }

    private static Clip abrirSeguro(byte[] bytes) {
        try {
            Clip c = AudioSystem.getClip();
            c.open(FORMATO, bytes, 0, bytes.length);
            return c;
        } catch (Throwable ignorado) {
            return null;
        }
    }

    private interface Dibujo {
        void en(double[] buffer);
    }

    private static double[] mezcla(double segundos, Dibujo d) {
        double[] b = new double[(int) (segundos * FRECUENCIA)];
        d.en(b);
        return b;
    }

    private static double midi(int nota) {
        return 440 * Math.pow(2, (nota - 69) / 12.0);
    }

    private static void arpegio(double[] b, double inicio, int[] notas, double paso, double ultima, double vol) {
        for (int i = 0; i < notas.length; i++) {
            double dur = i == notas.length - 1 ? ultima : paso;
            tono(b, inicio + i * paso, dur, midi(notas[i]), midi(notas[i]), Onda.TRIANGULO, vol);
        }
    }

    /** Suma un tono con barrido de frecuencia y envolvente suave. */
    private static void tono(double[] b, double inicio, double dur, double f0, double f1, Onda onda, double vol) {
        int desde = (int) (inicio * FRECUENCIA);
        int n = (int) (dur * FRECUENCIA);
        double fase = 0;
        for (int i = 0; i < n && desde + i < b.length; i++) {
            double p = i / (double) n;
            double f = f0 + (f1 - f0) * p;
            fase += 2 * Math.PI * f / FRECUENCIA;
            double v;
            switch (onda) {
                case CUADRADA: v = Math.tanh(Math.sin(fase) * 4) * 0.8; break;
                case TRIANGULO: v = 2 / Math.PI * Math.asin(Math.sin(fase)); break;
                default: v = Math.sin(fase);
            }
            double ataque = Math.min(1, i / (FRECUENCIA * 0.004));
            double caida = Math.pow(1 - p, 1.6);
            b[desde + i] += v * vol * ataque * caida;
        }
    }

    /** Melodía alegre de 4 compases que se repite durante el juego. */
    private static double[] cancion() {
        double corchea = 60.0 / 132 / 2;
        int[] melodia = {
            72, 0, 76, 79, 81, 79, 76, 0,
            74, 0, 77, 81, 79, 77, 74, 0,
            72, 76, 79, 84, 83, 79, 76, 74,
            72, 0, 67, 71, 72, 0, 0, 0,
            76, 0, 79, 84, 81, 79, 76, 79,
            77, 0, 74, 77, 81, 79, 77, 74,
            76, 79, 84, 79, 81, 77, 74, 71,
            72, 0, 79, 0, 72, 0, 0, 0
        };
        int[] bajo = {48, 43, 48, 43, 50, 45, 50, 45, 48, 52, 43, 48, 41, 43, 48, 48,
            48, 43, 48, 43, 41, 45, 41, 45, 48, 52, 41, 43, 48, 43, 48, 48};
        double[] b = new double[(int) (melodia.length * corchea * FRECUENCIA)];
        for (int i = 0; i < melodia.length; i++) {
            if (melodia[i] > 0) tono(b, i * corchea, corchea * 0.9, midi(melodia[i]), midi(melodia[i]), Onda.TRIANGULO, 0.11);
        }
        for (int i = 0; i < bajo.length; i++) {
            tono(b, i * corchea * 2, corchea * 1.6, midi(bajo[i]), midi(bajo[i]), Onda.SENO, 0.13);
        }
        for (int i = 0; i < melodia.length; i += 2) { // percusión suave
            tono(b, i * corchea, 0.03, 180, 60, Onda.SENO, i % 4 == 0 ? 0.12 : 0.05);
        }
        return b;
    }

    private static byte[] aBytes(double[] muestras) {
        byte[] r = new byte[muestras.length * 2];
        for (int i = 0; i < muestras.length; i++) {
            int v = (int) (Math.max(-1, Math.min(1, muestras[i])) * 32000);
            r[2 * i] = (byte) v;
            r[2 * i + 1] = (byte) (v >> 8);
        }
        return r;
    }
}
