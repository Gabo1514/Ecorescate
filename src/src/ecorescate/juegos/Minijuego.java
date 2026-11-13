package ecorescate.juegos;

import ecorescate.modelo.Accesorio;
import ecorescate.modelo.CatalogoNiveles;
import ecorescate.modelo.Estudiante;
import ecorescate.modelo.Medalla;
import ecorescate.modelo.ModuloImpactoSocial;
import ecorescate.modelo.Nivel;
import ecorescate.modelo.NivelAlcanzado;
import ecorescate.modelo.Pregunta;
import ecorescate.modelo.PuntajeObtenido;
import ecorescate.modelo.Recompensa;
import ecorescate.modelo.Reto;
import ecorescate.modelo.SesionJuego;
import ecorescate.modelo.TipoJuego;
import ecorescate.ui.Iconos;
import ecorescate.ui.Sonido;
import ecorescate.ui.Tema;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * Base de los cuatro minijuegos. Se encarga de lo que todos comparten: el ciclo de juego,
 * los niveles con sus retos y tiempo límite, el puntaje con combo, las estrellas, el medidor,
 * las barras, los datos curiosos, la pregunta rápida y la creación de la {@link SesionJuego}.
 * Cada minijuego solo implementa su escena y sus reglas (herencia y polimorfismo).
 *
 * Todo se dibuja en un lienzo lógico de 1000 x 640 que se escala al tamaño de la ventana.
 */
public abstract class Minijuego extends JPanel {

    public static final int ANCHO = 1000;
    public static final int ALTO = 640;
    /** Alto de la fila del título. */
    public static final int HUD = 64;
    public static final int NIVEL_MAX = NivelAlcanzado.MAXIMO;

    /** Recuadro donde cada juego dibuja su escena. */
    protected static final double ESC_X = 22, ESC_Y = 130, ESC_W = 956, ESC_H = 444;
    protected static final double ESC_DER = ESC_X + ESC_W, ESC_ABA = ESC_Y + ESC_H;

    private static final int BONO_PREGUNTA = 30;
    private static final Color COLOR_VELO = new Color(239, 238, 252, 215);
    private static final Color SOMBRA_TARJETA_1 = new Color(45, 27, 105, 18);
    private static final Color SOMBRA_TARJETA_2 = new Color(45, 27, 105, 32);
    private static final BasicStroke TRAZO_COMBO = new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final GradientPaint GRADIENTE_COMBO = new GradientPaint(0, -24, Estilo.AMARILLO, 0, 18, new Color(0xFF8C42));

    /** Quien muestra el juego recibe aquí el resultado de cada nivel. */
    public interface Oyente {
        /** Guarda la partida y devuelve lo que ganó el alumno (monedas, estrellas y medallas). */
        Recompensa partidaTerminada(SesionJuego sesion, int mejorCombo);

        /** Volver al mapa de niveles. */
        void salirDelJuego();
    }

    /** Palabras que aparecen al subir el combo. */
    private static final String[] PALABRAS_COMBO = {"", "", "¡BIEN!", "¡GENIAL!", "¡SÚPER!", "¡ECO-HÉROE!"};

    protected enum Estado { INTRO, JUGANDO, PAUSA, PREGUNTA, FIN }

    protected final TipoJuego tipo;
    protected final Color color;
    protected final Color colorTexto;
    protected final Random azar = new Random();

    /** Nivel que se está jugando (retos, tiempo y estrellas). */
    protected Nivel def;
    protected Estado estado = Estado.INTRO;
    protected int puntaje;
    protected int aciertos;
    protected int errores;
    /** Número del nivel (1 a 10); los juegos lo usan para su dificultad. */
    protected int nivel = 1;
    /** Aciertos seguidos sin equivocarse; sube el combo. */
    protected int racha;
    /** Recurso que se gasta (vidas, tanque, energía, biohuerto). Si llega a 0 se pierde. */
    protected double medidor = 1;
    protected double tiempoJuego;
    /** Segundos desde que se abrió el juego; sirve para animaciones. */
    protected double reloj;
    protected double ratonX = -100;
    protected double ratonY = -100;
    /** Personaje del alumno (accesorio y color de polo de la tienda). */
    protected Accesorio accesorio = Accesorio.NINGUNO;
    protected int polo;

    private final Estudiante estudiante;
    private final ModuloImpactoSocial modulo;
    private final Efectos efectos = new Efectos();
    private final Timer timer;
    private Oyente oyente;

    private int[] progresoRetos = new int[0];
    private long ultimoTick;
    private AffineTransform transformacion = new AffineTransform();
    private double avisoNivel;
    private double alertaMedidor;
    private LocalDateTime inicio;
    private boolean gano;
    private String motivoFin = "";
    private SesionJuego resultado;
    private int estrellasNivel;

    private String mensaje, mensajeSub;
    private boolean mensajeEsError;
    private double mensajeTiempo;
    private String dato;
    private double datoTiempo, datoPendiente;
    private int ultimoSegundo;

    private Pregunta pregunta;
    private int respuesta = -1;
    private final Rectangle2D[] opciones = new Rectangle2D[3];

    private int mejorCombo = 1;
    private double sacudida;
    private String palabra;
    private String palabraFormaCacheTexto;
    private Shape palabraFormaCache;
    private double palabraTiempo;
    private Recompensa recompensa;
    private double tiempoFin;
    private int estrellasSonadas;
    private boolean cursorManoActual;
    private final Rectangle2D btnSonido = new Rectangle2D.Double();
    private int cacheIntroNivel = -1;
    private List<String> cacheLineasCaja;
    private List<List<String>> cacheLineasPasos;

    /** Visibles en el paquete para que un juego pueda dibujar su propia barra. */
    final BotonLienzo btnMenu = new BotonLienzo("← Niveles", false);
    final BotonLienzo btnPausa = new BotonLienzo("Pausa", false);
    private final BotonLienzo btnPrincipal = new BotonLienzo("¡Jugar!", true);
    private final BotonLienzo btnSecundario = new BotonLienzo("Repetir", false);
    private final BotonLienzo btnTerciario = new BotonLienzo("Niveles", false);

    protected Minijuego(TipoJuego tipo, Estudiante estudiante, ModuloImpactoSocial modulo) {
        this.tipo = tipo;
        this.estudiante = estudiante;
        this.modulo = modulo;
        this.color = new Color(tipo.getColor());
        this.colorTexto = new Color(tipo.getColorTexto());
        btnPrincipal.color = colorTexto;
        btnSecundario.color = colorTexto;
        btnTerciario.color = Estilo.MORADO;
        btnMenu.color = Estilo.MORADO;
        def = CatalogoNiveles.nivel(tipo, 1);
        setBackground(Estilo.LAVANDA);
        setOpaque(true);
        setDoubleBuffered(true);
        setFocusable(true);
        configurarEntrada();
        // ~50 FPS con coalescencia: evita saturar el EDT en PCs de laboratorio manteniendo fluidez total
        timer = new Timer(20, e -> tick());
        timer.setCoalesce(true);
    }

    /** Fábrica: crea el minijuego que corresponde a cada tipo, listo para el nivel indicado. */
    public static Minijuego crear(TipoJuego tipo, Estudiante estudiante, ModuloImpactoSocial modulo, int numeroNivel) {
        Minijuego m;
        switch (tipo) {
            case LIMPIEZA: m = new LimpiezaMonstruosa(estudiante, modulo); break;
            case AGUA: m = new GuardianAgua(estudiante, modulo); break;
            case ENERGIA: m = new ApagaAhorra(estudiante, modulo); break;
            default: m = new SembradoresVerde(estudiante, modulo); break;
        }
        m.cargarNivel(numeroNivel);
        return m;
    }

    private void cargarNivel(int numero) {
        def = CatalogoNiveles.nivel(tipo, numero);
        nivel = def.getNumero();
        progresoRetos = new int[def.getRetos().size()];
    }

    // ---------------------------------------------------------------- reglas de cada juego

    /** Deja la escena lista para jugar el nivel actual. */
    protected abstract void reiniciar();

    /** Avanza la simulación dt segundos. */
    protected abstract void actualizar(double dt);

    /** Dibuja la escena dentro del recuadro ESC_X, ESC_Y, ESC_W, ESC_H. */
    protected abstract void dibujarEscena(Graphics2D g);

    /** Mensaje cuando el medidor llega a cero. */
    protected abstract String motivoDerrota();

    /** Consejo que se muestra en la barra inferior cuando no hay un mensaje reciente: {título, detalle}. */
    protected abstract String[] pista();

    protected void alPresionar(double x, double y) {
    }

    protected void alArrastrar(double x, double y) {
    }

    protected void alSoltar(double x, double y) {
    }

    protected void alTecla(char tecla) {
    }

    /** Teclas mantenidas (por ejemplo, flechas para caminar). */
    protected void alTeclaPresionada(int codigo) {
    }

    protected void alTeclaSoltada(int codigo) {
    }

    /** true si el mouse está sobre algo con lo que se puede interactuar (cambia el cursor). */
    protected boolean sobreObjetivo(double x, double y) {
        return false;
    }

    /** true para mostrar el medidor como 3 corazones. */
    protected boolean usaCorazones() {
        return false;
    }

    /** {etiqueta, valor} que se muestra a la izquierda de la barra inferior. */
    protected String[] resumenPie() {
        return null;
    }

    protected String textoControles() {
        return "CLIC PARA JUGAR";
    }

    // ---------------------------------------------------------------- ciclo de vida

    public void arrancar(Oyente oyente) {
        this.oyente = oyente;
        reiniciarComun();
        reiniciar();
        estado = Estado.INTRO;
        ultimoTick = 0;
        timer.start();
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    public void detener() {
        timer.stop();
        Sonido.get().musica(false);
    }

    /** Usa el personaje que el alumno armó en la tienda. */
    public void usarPersonaje(Accesorio accesorio, int polo) {
        this.accesorio = accesorio == null ? Accesorio.NINGUNO : accesorio;
        this.polo = polo;
    }

    @Override
    public void removeNotify() {
        timer.stop();
        Sonido.get().musica(false);
        super.removeNotify();
    }

    private void reiniciarComun() {
        puntaje = 0;
        aciertos = 0;
        errores = 0;
        racha = 0;
        medidor = 1;
        tiempoJuego = 0;
        avisoNivel = 0;
        alertaMedidor = 0;
        resultado = null;
        mensajeTiempo = 0;
        datoTiempo = 0;
        datoPendiente = 28;
        ultimoSegundo = -1;
        pregunta = null;
        respuesta = -1;
        mejorCombo = 1;
        sacudida = 0;
        palabraTiempo = 0;
        recompensa = null;
        estrellasNivel = 0;
        progresoRetos = new int[def.getRetos().size()];
        efectos.limpiar();
    }

    private void empezar() {
        reiniciarComun();
        reiniciar();
        inicio = LocalDateTime.now();
        estado = Estado.JUGANDO;
        avisoNivel = 1.8;
        ultimoTick = System.nanoTime();
        Sonido.get().tocar(Sonido.Efecto.NIVEL);
        Sonido.get().musica(true);
    }

    private void alternarPausa() {
        if (estado == Estado.JUGANDO) {
            estado = Estado.PAUSA;
            Sonido.get().musica(false);
        } else if (estado == Estado.PAUSA) {
            estado = Estado.JUGANDO;
            ultimoTick = System.nanoTime();
            Sonido.get().musica(true);
        }
    }

    /** Termina el nivel. Si lo superó, calcula las estrellas; algunos niveles tienen pregunta rápida. */
    protected void terminar(boolean ganado, String motivo) {
        if (estado == Estado.PREGUNTA || estado == Estado.FIN) return;
        gano = ganado;
        motivoFin = motivo;
        estrellasNivel = def.estrellas(ganado, errores, tiempoJuego);
        efectos.limpiar(); // que los textos del juego no tapen la pantalla final
        Sonido.get().musica(false);
        Sonido.get().tocar(ganado ? Sonido.Efecto.VICTORIA : Sonido.Efecto.DERROTA);
        if (ganado) efectos.confeti(90);
        if (ganado && def.isConPregunta()) {
            Pregunta[] lista = tipo.getPreguntas();
            pregunta = lista[azar.nextInt(lista.length)];
            respuesta = -1;
            estado = Estado.PREGUNTA;
        } else {
            finalizar(null);
        }
    }

    private void responder(int opcion) {
        if (respuesta >= 0 || opcion < 0 || opcion >= pregunta.getOpciones().length) return;
        respuesta = opcion;
        if (pregunta.esCorrecta(opcion)) {
            puntaje += BONO_PREGUNTA;
            efectos.chispas(ANCHO / 2.0, 330, Tema.VERDE_OK, 30);
            Sonido.get().tocar(Sonido.Efecto.COMBO);
        } else {
            Sonido.get().tocar(Sonido.Efecto.ERROR);
        }
    }

    private void verResultados() {
        if (respuesta < 0) return;
        finalizar(pregunta.esCorrecta(respuesta));
    }

    private void finalizar(Boolean preguntaCorrecta) {
        estado = Estado.FIN;
        resultado = crearSesion(gano, preguntaCorrecta);
        recompensa = oyente != null ? oyente.partidaTerminada(resultado, mejorCombo) : null;
        tiempoFin = 0;
        estrellasSonadas = 0;
    }

    private void siguienteNivel() {
        cargarNivel(Math.min(NIVEL_MAX, def.getNumero() + 1));
        reiniciarComun();
        reiniciar();
        estado = Estado.INTRO;
    }

    private void salir() {
        boolean enCurso = estado == Estado.JUGANDO || estado == Estado.PAUSA;
        if (enCurso && (aciertos > 0 || errores > 0) && oyente != null) {
            gano = false;
            estrellasNivel = 0;
            oyente.partidaTerminada(crearSesion(false, null), mejorCombo); // nivel a medias
        } else if (estado == Estado.PREGUNTA && oyente != null) {
            oyente.partidaTerminada(crearSesion(gano, null), mejorCombo);
        }
        estado = Estado.FIN;
        timer.stop();
        Sonido.get().musica(false);
        if (oyente != null) oyente.salirDelJuego();
    }

    private SesionJuego crearSesion(boolean superado, Boolean preguntaCorrecta) {
        double impacto = modulo.calcular(tipo, aciertos, superado);
        return new SesionJuego(estudiante, tipo, inicio != null ? inicio : LocalDateTime.now(),
                Math.round(tiempoJuego), new PuntajeObtenido(puntaje, aciertos, errores),
                new NivelAlcanzado(def.getNumero(), superado), impacto, preguntaCorrecta, superado ? estrellasNivel : 0);
    }

    private void tick() {
        if (!isShowing()) return;
        long ahora = System.nanoTime();
        double dt = ultimoTick == 0 ? 0.020 : Math.min(0.05, (ahora - ultimoTick) / 1e9);
        ultimoTick = ahora;
        if (estado != Estado.PAUSA) {
            reloj += dt;
            efectos.actualizar(dt);
        }
        if (estado == Estado.JUGANDO) avanzar(dt);
        sacudida = Math.max(0, sacudida - dt);
        if (estado == Estado.FIN && recompensa != null) {
            tiempoFin += dt;
            // las estrellas aparecen una por una, cada una con su sonido
            if (estrellasSonadas < recompensa.getEstrellas() && tiempoFin > 0.5 + estrellasSonadas * 0.45) {
                estrellasSonadas++;
                Sonido.get().tocar(Sonido.Efecto.ESTRELLA);
            } else if (estrellasSonadas == recompensa.getEstrellas() && tiempoFin > 0.7 + estrellasSonadas * 0.45) {
                estrellasSonadas = 99;
                Sonido.get().tocar(recompensa.getMedallasNuevas().isEmpty() ? Sonido.Efecto.MONEDA : Sonido.Efecto.MEDALLA);
            }
        }
        repaint();
    }

    /** Un paso de juego (también lo usan las pruebas automáticas). */
    void avanzar(double dt) {
        tiempoJuego += dt;
        actualizar(dt);
        medidor = Tema.limitar(medidor, 0, 1);
        avisoNivel = Math.max(0, avisoNivel - dt);
        alertaMedidor = Math.max(0, alertaMedidor - dt);
        mensajeTiempo = Math.max(0, mensajeTiempo - dt);
        datoTiempo = Math.max(0, datoTiempo - dt);
        palabraTiempo = Math.max(0, palabraTiempo - dt);
        if (datoPendiente > 0) {
            datoPendiente -= dt;
            if (datoPendiente <= 0) mostrarDato();
        }
        double resta = def.getTiempoLimite() - tiempoJuego;
        int segundo = (int) Math.ceil(resta);
        if (resta <= 10 && segundo != ultimoSegundo && segundo > 0) {
            ultimoSegundo = segundo;
            Sonido.get().tocar(Sonido.Efecto.CLIC); // tic-tac de los últimos segundos
        }
        if (estado != Estado.JUGANDO) return;
        revisarVictoria();
        if (estado != Estado.JUGANDO) return;
        if (medidor <= 0) terminar(false, motivoDerrota());
        else if (resta <= 0) terminar(false, "¡Se acabó el tiempo!");
    }

    // ---------------------------------------------------------------- retos

    /** Suma avance a los retos con esa clave (por ejemplo "BOTELLIN", "CERRADOS", "FLORES"). */
    protected void progresoReto(String clave, int cantidad) {
        List<Reto> retos = def.getRetos();
        for (int i = 0; i < retos.size(); i++) {
            Reto r = retos.get(i);
            if (!r.getClave().equals(clave) || Reto.COMBO.equals(clave)) continue;
            avanzarReto(i, progresoRetos[i] + cantidad);
        }
    }

    private void avanzarReto(int i, int valor) {
        Reto r = def.getRetos().get(i);
        boolean antes = progresoRetos[i] >= r.getMeta();
        progresoRetos[i] = Math.min(r.getMeta(), Math.max(progresoRetos[i], valor));
        if (!antes && progresoRetos[i] >= r.getMeta()) {
            Sonido.get().tocar(Sonido.Efecto.MONEDA);
            efectos.texto("¡Reto cumplido!", Estilo.VIOLETA, ANCHO / 2.0, 170);
            mostrarMensaje("¡Reto cumplido: " + r.getLargo() + "!", restantes() > 0
                    ? "Te falta " + restantes() + (restantes() == 1 ? " reto" : " retos") + " para superar el nivel."
                    : "¡Completaste todos los retos!");
        }
    }

    private int restantes() {
        int n = 0;
        for (int i = 0; i < progresoRetos.length; i++) if (progresoRetos[i] < def.getRetos().get(i).getMeta()) n++;
        return n;
    }

    /** Cuánto falta de los retos con esa clave (0 si no hay o ya se cumplieron). */
    protected int faltaReto(String clave) {
        int falta = 0;
        List<Reto> retos = def.getRetos();
        for (int i = 0; i < retos.size(); i++) {
            int prog = i < progresoRetos.length ? progresoRetos[i] : 0;
            if (retos.get(i).getClave().equals(clave)) falta += retos.get(i).getMeta() - prog;
        }
        return falta;
    }

    private void revisarVictoria() {
        if (estado == Estado.JUGANDO && restantes() == 0) terminar(true, "¡Completaste todos los retos!");
    }

    // ---------------------------------------------------------------- utilidades para los juegos

    protected int combo() {
        return Math.min(5, 1 + racha / 3);
    }

    /** Cuenta un acierto con su combo. Devuelve los puntos ganados. */
    protected int sumarAcierto(int puntosBase) {
        if (estado != Estado.JUGANDO) return 0;
        int comboAntes = combo();
        int puntos = puntosBase * comboAntes;
        racha++;
        aciertos++;
        puntaje += puntos;
        int comboAhora = combo();
        if (comboAhora > comboAntes) { // subió el combo: palabra grande y sonido
            palabra = PALABRAS_COMBO[comboAhora];
            palabraTiempo = 1.1;
            mejorCombo = Math.max(mejorCombo, comboAhora);
            Sonido.get().tocar(Sonido.Efecto.COMBO);
        } else {
            Sonido.get().acierto(comboAhora);
        }
        List<Reto> retos = def.getRetos();
        for (int i = 0; i < retos.size(); i++) {
            if (Reto.COMBO.equals(retos.get(i).getClave())) avanzarReto(i, comboAhora);
        }
        progresoReto(Reto.ACIERTOS, 1);
        return puntos;
    }

    protected void acierto(int puntosBase, double x, double y) {
        int puntos = sumarAcierto(puntosBase);
        if (puntos <= 0) return;
        efectos.texto("+" + puntos, Tema.VERDE_OK, x, y);
        efectos.chispas(x, y, color, 14);
    }

    /** Error: quita parte del medidor, corta el combo y explica qué pasó en la barra inferior. */
    protected void fallo(String corto, String explicacion, double x, double y, double danio) {
        errores++;
        racha = 0;
        medidor -= danio;
        alertaMedidor = 0.8;
        sacudida = 0.3;
        Sonido.get().tocar(Sonido.Efecto.ERROR);
        efectos.texto(corto, Tema.ROJO, x, y);
        if (explicacion != null) mostrar(corto, explicacion, true);
    }

    protected void fallo(String corto, double x, double y, double danio) {
        fallo(corto, null, x, y, danio);
    }

    /** Mensaje neutro (sin castigo) para orientar al niño. */
    protected void aviso(String mensajeCorto, double x, double y) {
        efectos.texto(mensajeCorto, Tema.AVISO, x, y);
    }

    protected void sumar(int puntos, String texto, double x, double y) {
        puntaje += puntos;
        Sonido.get().tocar(Sonido.Efecto.RECOGER);
        efectos.texto(texto, Tema.VERDE_OK, x, y);
    }

    protected void chispas(double x, double y, Color c, int cantidad) {
        efectos.chispas(x, y, c, cantidad);
    }

    protected void sonar(Sonido.Efecto efecto) {
        Sonido.get().tocar(efecto);
    }

    /** Muestra un mensaje en la barra inferior por unos segundos. */
    protected void mostrarMensaje(String titulo, String detalle) {
        mostrar(titulo, detalle, false);
    }

    private void mostrar(String titulo, String detalle, boolean error) {
        mensaje = titulo;
        mensajeSub = detalle;
        mensajeEsError = error;
        mensajeTiempo = 4;
    }

    /** A mitad del nivel aparece un segundo dato curioso. */
    private void mostrarDato() {
        String[] datos = tipo.getDatos();
        dato = datos[(def.getNumero() - 1 + datos.length / 2) % datos.length];
        datoTiempo = 7;
        datoPendiente = 0;
    }

    protected boolean jugando() {
        return estado == Estado.JUGANDO;
    }

    protected double aleatorio(double min, double max) {
        return min + azar.nextDouble() * (max - min);
    }

    /** Escala un valor entre el nivel 1 y el 10 (dificultad progresiva). */
    protected double progresion(double enNivel1, double enNivel10) {
        return enNivel1 + (enNivel10 - enNivel1) * (nivel - 1) / (NIVEL_MAX - 1.0);
    }

    // ---------------------------------------------------------------- entrada

    private Point2D aLogico(MouseEvent e) {
        try {
            return transformacion.inverseTransform(e.getPoint(), null);
        } catch (NoninvertibleTransformException ex) {
            return new Point2D.Double(-100, -100);
        }
    }

    private void configurarEntrada() {
        MouseAdapter raton = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                Point2D p = aLogico(e);
                ratonX = p.getX();
                ratonY = p.getY();
                presionar(ratonX, ratonY);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point2D p = aLogico(e);
                ratonX = p.getX();
                ratonY = p.getY();
                if (jugando()) alArrastrar(ratonX, ratonY);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Point2D p = aLogico(e);
                ratonX = p.getX();
                ratonY = p.getY();
                boolean mano = sobreBoton(ratonX, ratonY) || (jugando() && sobreObjetivo(ratonX, ratonY));
                if (mano != cursorManoActual) {
                    cursorManoActual = mano;
                    setCursor(Cursor.getPredefinedCursor(mano ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Point2D p = aLogico(e);
                if (jugando()) alSoltar(p.getX(), p.getY());
            }
        };
        addMouseListener(raton);
        addMouseMotionListener(raton);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int k = e.getKeyCode();
                boolean aceptar = k == KeyEvent.VK_SPACE || k == KeyEvent.VK_ENTER;
                if (estado == Estado.PREGUNTA) {
                    if (k >= KeyEvent.VK_1 && k <= KeyEvent.VK_3) responder(k - KeyEvent.VK_1);
                    else if (aceptar) verResultados();
                } else if (k == KeyEvent.VK_ESCAPE || k == KeyEvent.VK_P) {
                    alternarPausa();
                } else if (aceptar && estado == Estado.INTRO) {
                    empezar();
                } else if (aceptar && estado == Estado.FIN) {
                    accionPrincipalFin();
                } else if (aceptar && estado == Estado.PAUSA) {
                    alternarPausa();
                } else if (jugando()) {
                    alTeclaPresionada(k);
                    alTecla(e.getKeyChar());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                alTeclaSoltada(e.getKeyCode());
            }
        });
    }

    private int opcionEn(double x, double y) {
        for (int i = 0; i < opciones.length; i++) {
            if (opciones[i] != null && opciones[i].contains(x, y)) return i;
        }
        return -1;
    }

    private boolean hayTercerBoton() {
        return gano && def.getNumero() < NIVEL_MAX;
    }

    private boolean sobreBoton(double x, double y) {
        if (btnMenu.contiene(x, y) || btnSonido.contains(x, y)) return true;
        switch (estado) {
            case JUGANDO: return btnPausa.contiene(x, y);
            case PAUSA: return btnPausa.contiene(x, y) || btnPrincipal.contiene(x, y) || btnSecundario.contiene(x, y);
            case PREGUNTA: return respuesta < 0 ? opcionEn(x, y) >= 0 : btnPrincipal.contiene(x, y);
            case INTRO: return btnPrincipal.contiene(x, y);
            default:
                return btnPrincipal.contiene(x, y) || btnSecundario.contiene(x, y)
                        || (hayTercerBoton() && btnTerciario.contiene(x, y));
        }
    }

    /** En la pantalla final: siguiente nivel si ganó, reintentar si no. */
    private void accionPrincipalFin() {
        if (gano && def.getNumero() < NIVEL_MAX) siguienteNivel();
        else empezar();
    }

    private void presionar(double x, double y) {
        if (btnSonido.contains(x, y)) {
            Sonido s = Sonido.get();
            s.setActivo(!s.isActivo());
            if (s.isActivo()) {
                s.tocar(Sonido.Efecto.CLIC);
                s.musica(estado == Estado.JUGANDO);
            }
            return;
        }
        if (estado != Estado.JUGANDO && sobreBoton(x, y)) Sonido.get().tocar(Sonido.Efecto.CLIC);
        if (btnMenu.contiene(x, y)) {
            salir();
            return;
        }
        if ((estado == Estado.JUGANDO || estado == Estado.PAUSA) && btnPausa.contiene(x, y)) {
            alternarPausa();
            return;
        }
        switch (estado) {
            case INTRO:
                if (btnPrincipal.contiene(x, y)) empezar();
                break;
            case PAUSA:
                if (btnPrincipal.contiene(x, y)) alternarPausa();
                else if (btnSecundario.contiene(x, y)) salir();
                break;
            case PREGUNTA:
                if (respuesta < 0) responder(opcionEn(x, y));
                else if (btnPrincipal.contiene(x, y)) verResultados();
                break;
            case FIN:
                if (btnPrincipal.contiene(x, y)) accionPrincipalFin();
                else if (btnSecundario.contiene(x, y)) {
                    if (hayTercerBoton()) empezar(); // repetir el mismo nivel
                    else salir();
                } else if (hayTercerBoton() && btnTerciario.contiene(x, y)) salir();
                break;
            default:
                if (y > ESC_Y - 4) alPresionar(x, y);
        }
    }

    // ---------------------------------------------------------------- dibujo

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        Tema.suavizar(g);
        double escala = Math.min(getWidth() / (double) ANCHO, getHeight() / (double) ALTO);
        AffineTransform t = new AffineTransform();
        t.translate((getWidth() - ANCHO * escala) / 2, (getHeight() - ALTO * escala) / 2);
        t.scale(escala, escala);
        transformacion = t;
        g.transform(t);
        g.clipRect(0, 0, ANCHO, ALTO);
        g.setColor(Estilo.LAVANDA);
        g.fillRect(0, 0, ANCHO, ALTO);

        dibujarBarra(g);
        dibujarEstado(g);

        RoundRectangle2D marco = new RoundRectangle2D.Double(ESC_X, ESC_Y, ESC_W, ESC_H, 28, 28);
        Graphics2D escena = (Graphics2D) g.create();
        escena.clip(marco);
        if (sacudida > 0) { // la pantalla tiembla un poquito al equivocarse
            double fuerza = sacudida / 0.3 * 7;
            escena.translate(Math.sin(reloj * 90) * fuerza, Math.cos(reloj * 70) * fuerza * 0.5);
        }
        dibujarEscena(escena);
        dibujarDato(escena);
        escena.dispose();
        g.setColor(Estilo.MORADO);
        g.setStroke(Estilo.STROKE_2_5);
        g.draw(marco);

        dibujarPie(g);
        if (palabraTiempo > 0 && jugando()) dibujarPalabraCombo(g);
        if (avisoNivel > 0 && jugando()) dibujarAvisoNivel(g);
        if (estado == Estado.JUGANDO) efectos.dibujar(g);

        switch (estado) {
            case INTRO: dibujarIntro(g); break;
            case PAUSA: dibujarPausa(g); break;
            case PREGUNTA: dibujarPregunta(g); break;
            case FIN: if (resultado != null) dibujarFin(g); break;
            default: break;
        }
        if (estado != Estado.JUGANDO) efectos.dibujar(g); // confeti por encima de las tarjetas
        g.dispose();
    }

    /** Fila del título: botón Niveles, nombre del juego, sonido y nivel con su reloj. */
    protected void dibujarBarra(Graphics2D g) {
        btnMenu.ubicar(14, 14, 112, 36);
        btnMenu.dibujar(g, ratonX, ratonY);
        g.setColor(Estilo.MORADO);
        g.setFont(Tema.fuente(Font.BOLD, 28f));
        g.drawString(tipo.getNombre().toUpperCase(), 140, 43);

        double resta = estado == Estado.INTRO ? def.getTiempoLimite() : def.getTiempoLimite() - tiempoJuego;
        boolean urgente = estado == Estado.JUGANDO && resta <= 15;
        String texto = String.format("NIVEL %02d", def.getNumero());
        String reloj = Nivel.formatoTiempo(resta);
        g.setFont(Tema.fuente(Font.BOLD, 18f));
        double wNivel = g.getFontMetrics().stringWidth(texto);
        double wReloj = g.getFontMetrics().stringWidth(reloj);
        double w = wNivel + wReloj + 72;
        RoundRectangle2D pill = new RoundRectangle2D.Double(984 - w, 14, w, 38, 38, 38);
        g.setColor(urgente && ((int) (this.reloj * 4)) % 2 == 0 ? new Color(0xFFE0E0) : Color.WHITE);
        g.fill(pill);
        g.setColor(Estilo.MORADO);
        g.setStroke(Estilo.STROKE_2_2);
        g.draw(pill);
        double x = pill.getX() + 18;
        g.drawString(texto, (float) x, 39f);
        x += wNivel + 14;
        g.setColor(urgente ? Tema.ROJO : Estilo.MORADO);
        g.setStroke(Estilo.STROKE_2_0);
        g.draw(new Ellipse2D.Double(x, 24, 16, 16));
        g.draw(new Line2D.Double(x + 8, 32, x + 8, 27));
        g.draw(new Line2D.Double(x + 8, 32, x + 12, 34));
        g.drawString(reloj, (float) (x + 22), 39f);
        dibujarBotonSonido(g, pill.getX() - 30, 33);
    }

    protected void etiquetaBarra(Graphics2D g, String texto, double x) {
        g.setColor(Estilo.BLANCO_SUAVE);
        g.setFont(Tema.espaciada(Font.BOLD, 12.5f, 0.06f));
        g.drawString(texto, (float) x, 103f);
    }

    protected void valorBarra(Graphics2D g, String texto, double x) {
        g.setColor(Color.WHITE);
        g.setFont(Tema.fuente(Font.BOLD, 25f));
        g.drawString(texto, (float) x, 106f);
    }

    /** Barra de estado morada: vidas o medidor, puntos, combo, retos y pausa. */
    private void dibujarEstado(Graphics2D g) {
        g.setColor(Estilo.MORADO);
        g.fill(new RoundRectangle2D.Double(22, 72, 956, 50, 22, 22));
        boolean parpadeo = alertaMedidor > 0 && ((int) (reloj * 10)) % 2 == 0;
        if (usaCorazones()) {
            int vidas = (int) Math.round(medidor * 3);
            for (int i = 0; i < 3; i++) {
                g.setColor(i < vidas ? (parpadeo ? Color.WHITE : Estilo.ROSADO) : Estilo.MORADO_MEDIO);
                g.fill(Estilo.corazon(48 + i * 34, 98, 17));
            }
        } else {
            g.setColor(Estilo.BLANCO_SUAVE);
            g.setFont(Tema.espaciada(Font.BOLD, 11.5f, 0.06f));
            g.drawString(tipo.getNombreMedidor().toUpperCase(), 40, 92);
            g.setColor(Estilo.MORADO_MEDIO);
            g.fill(new RoundRectangle2D.Double(40, 99, 120, 13, 13, 13));
            g.setColor(parpadeo ? Color.WHITE : medidor > 0.5 ? Estilo.MENTA : medidor > 0.25 ? Estilo.AMARILLO : Estilo.ROJO);
            g.fill(new RoundRectangle2D.Double(40, 99, Math.max(13, 120 * medidor), 13, 13, 13));
        }
        etiquetaBarra(g, "PUNTOS", 180);
        valorBarra(g, String.format("%05d", Math.min(99999, puntaje)), 238);

        int combo = combo();
        RoundRectangle2D pill = new RoundRectangle2D.Double(332, 82, 114, 30, 30, 30);
        g.setColor(combo > 1 ? Estilo.AMARILLO : Estilo.MORADO_MEDIO);
        g.fill(pill);
        g.setColor(combo > 1 ? Estilo.MORADO : Estilo.BLANCO_SUAVE);
        g.setFont(Tema.fuente(Font.BOLD, 15f));
        Tema.centrado(g, "COMBO ×" + combo, pill.getCenterX(), 102);

        dibujarRetos(g, 460, 912);

        btnPausa.ubicar(924, 78, 44, 38);
        if (btnPausa.contiene(ratonX, ratonY)) {
            g.setColor(Estilo.MORADO_MEDIO);
            g.fill(new RoundRectangle2D.Double(924, 78, 44, 38, 12, 12));
        }
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(937, 87, 6, 20, 3, 3));
        g.fill(new RoundRectangle2D.Double(949, 87, 6, 20, 3, 3));
    }

    /** Chips con el avance de cada reto: "Botellín 2/4". */
    private void dibujarRetos(Graphics2D g, double x0, double x1) {
        List<Reto> retos = def.getRetos();
        etiquetaBarra(g, "RETOS", x0);
        double inicio = x0 + 56, sep = 8;
        double ancho = (x1 - inicio - sep * (retos.size() - 1)) / retos.size();
        for (int i = 0; i < retos.size(); i++) {
            Reto r = retos.get(i);
            int v = i < progresoRetos.length ? progresoRetos[i] : 0;
            boolean listo = v >= r.getMeta();
            double x = inicio + i * (ancho + sep);
            RoundRectangle2D chip = new RoundRectangle2D.Double(x, 82, ancho, 30, 30, 30);
            g.setColor(listo ? Estilo.MENTA : Estilo.MORADO_MEDIO);
            g.fill(chip);
            if (!listo && r.getMeta() > 0 && v > 0) { // barrita de avance dentro del chip
                g.setColor(new Color(0x6A5FB0));
                Shape clipAnt = g.getClip();
                g.clip(chip);
                g.fill(new Rectangle2D.Double(x, 82, ancho * v / (double) r.getMeta(), 30));
                g.setClip(clipAnt);
            }
            String texto = (listo ? "✓ " : "") + r.getCorto() + " " + v + "/" + r.getMeta();
            g.setFont(Tema.fuente(Font.BOLD, 14.5f));
            if (g.getFontMetrics().stringWidth(texto) > ancho - 12) g.setFont(Tema.fuente(Font.BOLD, 12.5f));
            g.setColor(listo ? Estilo.MORADO : Color.WHITE);
            Tema.centrado(g, ajustar(g, texto, ancho - 10), chip.getCenterX(), 102);
        }
    }

    /** Barra inferior: resumen, consejo o mensaje del momento y controles. */
    protected void dibujarPie(Graphics2D g) {
        g.setColor(Estilo.MORADO);
        g.fill(new RoundRectangle2D.Double(22, 582, 956, 52, 22, 22));
        double xTexto = 42;
        String[] resumen = resumenPie();
        if (resumen != null) {
            g.setColor(Estilo.BLANCO_SUAVE);
            g.setFont(Tema.espaciada(Font.BOLD, 11.5f, 0.06f));
            g.drawString(resumen[0], 42, 602);
            g.setColor(Color.WHITE);
            g.setFont(Tema.fuente(Font.BOLD, 21f));
            g.drawString(resumen[1], 42, 625);
            xTexto = 196;
        }
        dibujarConsejo(g, xTexto, 732);
        RoundRectangle2D boton = new RoundRectangle2D.Double(744, 589, 220, 38, 14, 14);
        g.setColor(Estilo.VIOLETA);
        g.fill(boton);
        g.setColor(Color.WHITE);
        g.setFont(Tema.fuente(Font.BOLD, 16f));
        Tema.centrado(g, textoControles(), boton.getCenterX(), 614);
    }

    /** Dibuja el mensaje actual (o la pista) entre x0 y x1 de la barra inferior. */
    protected void dibujarConsejo(Graphics2D g, double x0, double x1) {
        String titulo, sub;
        boolean error = false;
        if (mensajeTiempo > 0 && mensaje != null) {
            titulo = mensaje;
            sub = mensajeSub;
            error = mensajeEsError;
        } else {
            String[] p = pista();
            titulo = p[0];
            sub = p[1];
        }
        g.setColor(error ? new Color(0xFFB3C6) : Color.WHITE);
        g.setFont(Tema.fuente(Font.BOLD, 18f));
        if (g.getFontMetrics().stringWidth(titulo) > x1 - x0) g.setFont(Tema.fuente(Font.BOLD, 15.5f));
        g.drawString(ajustar(g, titulo, x1 - x0), (float) x0, 604f);
        if (sub != null) {
            g.setColor(Estilo.BLANCO_SUAVE);
            g.setFont(Tema.fuente(Font.PLAIN, 15f));
            if (g.getFontMetrics().stringWidth(sub) > x1 - x0) g.setFont(Tema.fuente(Font.PLAIN, 13.5f));
            g.drawString(ajustar(g, sub, x1 - x0), (float) x0, 624f);
        }
    }

    static String ajustar(Graphics2D g, String texto, double ancho) {
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(texto) <= ancho) return texto;
        String t = texto;
        while (t.length() > 1 && fm.stringWidth(t + "…") > ancho) t = t.substring(0, t.length() - 1);
        return t.trim() + "…";
    }

    /** Tarjeta "¿Sabías que…?" arriba de la escena. */
    private void dibujarDato(Graphics2D g0) {
        if (datoTiempo <= 0 || dato == null || estado != Estado.JUGANDO) return;
        Graphics2D g = (Graphics2D) g0.create();
        double aparicion = Math.min(1, Math.min(datoTiempo, 7 - datoTiempo) * 3);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) aparicion));
        double w = 600, x = ANCHO / 2.0 - w / 2;
        g.setFont(Tema.fuente(Font.PLAIN, 16.5f));
        List<String> lineas = Tema.partir(dato, g.getFontMetrics(), w - 100);
        double h = 38 + lineas.size() * 20;
        double y = ESC_Y + 10 - (1 - aparicion) * 20;
        RoundRectangle2D r = new RoundRectangle2D.Double(x, y, w, h, 18, 18);
        g.setColor(new Color(0, 0, 0, 30));
        g.fill(new RoundRectangle2D.Double(x, y + 4, w, h, 18, 18));
        Estilo.contorno(g, r, new Color(0xFFF6D5), 2.2f);
        g.setColor(Estilo.AMARILLO);
        g.fill(new Ellipse2D.Double(x + 16, y + h / 2 - 22, 44, 44));
        Iconos.foco(g, x + 38, y + h / 2, 15, Estilo.TRAZO, Estilo.AMARILLO);
        g.setColor(new Color(0xA86B00));
        g.setFont(Tema.espaciada(Font.BOLD, 12.5f, 0.08f));
        g.drawString("¿SABÍAS QUE…?", (float) (x + 76), (float) (y + 24));
        g.setColor(Tema.TEXTO);
        g.setFont(Tema.fuente(Font.PLAIN, 16.5f));
        double ly = y + 45;
        for (String l : lineas) {
            g.drawString(l, (float) (x + 76), (float) ly);
            ly += 20;
        }
        g.dispose();
    }

    private void dibujarAvisoNivel(Graphics2D g) {
        double t = avisoNivel;
        int a = (int) (255 * Math.min(1, t * 1.5));
        double crecer = t > 1.5 ? 1 + (t - 1.5) * 1.2 : 1;
        String texto = String.format("¡Nivel %d!", def.getNumero());
        float tamAviso = Math.round(34 * crecer * 2f) / 2f;
        g.setFont(Tema.fuente(Font.BOLD, tamAviso));
        int ancho = g.getFontMetrics().stringWidth(texto) + 60;
        double alto = 58 * crecer;
        double x = ANCHO / 2.0 - ancho / 2.0, y = 330 - alto / 2;
        RoundRectangle2D r = new RoundRectangle2D.Double(x, y, ancho, alto, alto, alto);
        g.setColor(Tema.alfa(Estilo.AMARILLO, a));
        g.fill(r);
        g.setColor(Tema.alfa(Estilo.TRAZO, a));
        g.setStroke(Estilo.STROKE_3_0);
        g.draw(r);
        Tema.centrado(g, texto, ANCHO / 2.0, y + alto * 0.68);
    }

    private void dibujarPalabraCombo(Graphics2D g0) {
        if (palabra == null) return;
        Graphics2D g = (Graphics2D) g0.create();
        double t = 1.1 - palabraTiempo;
        double s = t < 0.15 ? 0.5 + t / 0.15 * 0.7 : t < 0.25 ? 1.2 - (t - 0.15) * 2 : 1.0;
        float alfa = (float) Math.min(1, palabraTiempo * 3);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alfa));
        g.translate(ANCHO / 2.0, 250 - t * 20);
        g.rotate(-0.06);
        g.scale(s, s);
        if (!palabra.equals(palabraFormaCacheTexto) || palabraFormaCache == null) {
            Font f = Tema.fuente(Font.BOLD, 54f);
            GlyphVector gv = f.createGlyphVector(g.getFontRenderContext(), palabra);
            palabraFormaCache = gv.getOutline((float) (-gv.getLogicalBounds().getWidth() / 2), 18f);
            palabraFormaCacheTexto = palabra;
        }
        g.setColor(Estilo.TRAZO);
        g.setStroke(TRAZO_COMBO);
        g.draw(palabraFormaCache);
        g.setPaint(GRADIENTE_COMBO);
        g.fill(palabraFormaCache);
        g.dispose();
    }

    /** Botón redondo para prender o apagar el sonido. */
    private void dibujarBotonSonido(Graphics2D g, double cx, double cy) {
        btnSonido.setRect(cx - 19, cy - 19, 38, 38);
        boolean activo = Sonido.get().isActivo();
        Ellipse2D c = new Ellipse2D.Double(cx - 19, cy - 19, 38, 38);
        g.setColor(btnSonido.contains(ratonX, ratonY) ? new Color(0xE4E0FA) : Color.WHITE);
        g.fill(c);
        g.setColor(Estilo.MORADO);
        g.setStroke(Estilo.STROKE_2_2);
        g.draw(c);
        Path2D parlante = new Path2D.Double();
        parlante.moveTo(cx - 10, cy - 4);
        parlante.lineTo(cx - 5, cy - 4);
        parlante.lineTo(cx + 2, cy - 10);
        parlante.lineTo(cx + 2, cy + 10);
        parlante.lineTo(cx - 5, cy + 4);
        parlante.lineTo(cx - 10, cy + 4);
        parlante.closePath();
        g.fill(parlante);
        g.setStroke(Estilo.STROKE_2_2);
        if (activo) {
            g.draw(new Arc2D.Double(cx - 3, cy - 6, 12, 12, -50, 100, Arc2D.OPEN));
            g.draw(new Arc2D.Double(cx - 5, cy - 10, 20, 20, -50, 100, Arc2D.OPEN));
        } else {
            g.setColor(Tema.ROJO);
            g.draw(new Line2D.Double(cx + 6, cy - 6, cx + 13, cy + 6));
            g.draw(new Line2D.Double(cx + 13, cy - 6, cx + 6, cy + 6));
        }
    }

    private void velo(Graphics2D g) {
        g.setColor(COLOR_VELO);
        g.fillRect(0, HUD, ANCHO, ALTO - HUD);
    }

    private RoundRectangle2D tarjeta(Graphics2D g, double w, double h) {
        double x = (ANCHO - w) / 2, y = HUD + (ALTO - HUD - h) / 2;
        g.setColor(SOMBRA_TARJETA_1);
        g.fill(new RoundRectangle2D.Double(x - 5, y - 1, w + 10, h + 10, 32, 32));
        g.setColor(SOMBRA_TARJETA_2);
        g.fill(new RoundRectangle2D.Double(x - 2, y + 2, w + 4, h + 4, 28, 28));
        RoundRectangle2D r = new RoundRectangle2D.Double(x, y, w, h, 26, 26);
        Estilo.contorno(g, r, Color.WHITE, 2.5f);
        return r;
    }

    private void etiqueta(Graphics2D g, String texto, double x, double y, Color c) {
        g.setColor(c);
        g.setFont(Tema.espaciada(Font.BOLD, 13f, 0.12f));
        g.drawString(texto, (float) x, (float) y);
    }

    /** Párrafo con viñeta; devuelve la y siguiente. */
    private double vineta(Graphics2D g, String texto, double x, double y, double ancho) {
        List<String> lineas = Tema.partir(texto, g.getFontMetrics(), ancho);
        for (String l : lineas) {
            g.drawString(l, (float) x, (float) y);
            y += 20;
        }
        return y;
    }

    private void dibujarIntro(Graphics2D g) {
        velo(g);
        RoundRectangle2D r = tarjeta(g, 680, 556);
        double x = r.getX(), y = r.getY(), w = r.getWidth(), cx = x + w / 2;
        double margen = 44;

        Iconos.logo(g, tipo, x + margen + 26, y + 46, 52);
        g.setColor(colorTexto);
        g.setFont(Tema.espaciada(Font.BOLD, 13f, 0.1f));
        g.drawString(String.format("NIVEL %d DE %d", def.getNumero(), NIVEL_MAX), (float) (x + margen + 66), (float) (y + 36));
        g.setColor(Estilo.MORADO);
        g.setFont(Tema.fuente(Font.BOLD, 30f));
        g.drawString(def.getTitulo(), (float) (x + margen + 66), (float) (y + 66));

        // caja informativa: concepto (nivel 1), novedad o dato curioso
        double by = y + 92;
        String etiquetaCaja, textoCaja;
        Color fondoCaja;
        if (def.getNumero() == 1) {
            etiquetaCaja = "¿QUÉ VAS A APRENDER?";
            textoCaja = tipo.getConcepto();
            fondoCaja = Tema.alfa(color, 28);
        } else if (def.getNovedad() != null) {
            etiquetaCaja = "¡NUEVO EN ESTE NIVEL!";
            textoCaja = def.getNovedad();
            fondoCaja = new Color(0xFFE8EE);
        } else {
            etiquetaCaja = "¿SABÍAS QUE…?";
            textoCaja = def.getDato();
            fondoCaja = new Color(0xFFF6D5);
        }

        if (cacheIntroNivel != def.getNumero() || cacheLineasCaja == null) {
            cacheIntroNivel = def.getNumero();
            g.setFont(Tema.fuente(Font.PLAIN, 16.5f));
            cacheLineasCaja = Tema.partir(textoCaja, g.getFontMetrics(), w - 2 * margen - 36);
            if (def.getNumero() == 1) {
                cacheLineasPasos = new java.util.ArrayList<>();
                g.setFont(Tema.fuente(Font.PLAIN, 15.5f));
                for (String paso : tipo.getInstrucciones()) {
                    cacheLineasPasos.add(Tema.partir(paso, g.getFontMetrics(), w - 2 * margen - 24));
                }
            } else {
                cacheLineasPasos = null;
            }
        }

        double hCaja = 42 + cacheLineasCaja.size() * 20;
        g.setColor(fondoCaja);
        g.fill(new RoundRectangle2D.Double(x + margen, by, w - 2 * margen, hCaja, 16, 16));
        etiqueta(g, etiquetaCaja, x + margen + 18, by + 24, def.getNovedad() != null && def.getNumero() > 1 ? new Color(0xC2185B) : colorTexto);
        g.setColor(Tema.TEXTO);
        g.setFont(Tema.fuente(Font.PLAIN, 16.5f));
        double ly = by + 46;
        for (String l : cacheLineasCaja) {
            g.drawString(l, (float) (x + margen + 18), (float) ly);
            ly += 20;
        }

        // cómo se juega: solo en el nivel 1
        double ty = by + hCaja + 30;
        if (def.getNumero() == 1 && cacheLineasPasos != null) {
            etiqueta(g, "CÓMO SE JUEGA", x + margen, ty, Tema.ETIQUETA);
            ty += 22;
            for (int i = 0; i < cacheLineasPasos.size(); i++) {
                g.setColor(colorTexto);
                g.setFont(Tema.fuente(Font.BOLD, 15.5f));
                g.drawString((i + 1) + ".", (float) (x + margen), (float) ty);
                g.setColor(Tema.TEXTO);
                g.setFont(Tema.fuente(Font.PLAIN, 15.5f));
                for (String l : cacheLineasPasos.get(i)) {
                    g.drawString(l, (float) (x + margen + 22), (float) ty);
                    ty += 20;
                }
                ty += 2;
            }
            ty += 16;
        }

        // retos del nivel
        etiqueta(g, "RETOS DEL NIVEL", x + margen, ty, Tema.ETIQUETA);
        ty += 12;
        for (Reto reto : def.getRetos()) {
            RoundRectangle2D fila = new RoundRectangle2D.Double(x + margen, ty, w - 2 * margen, 34, 14, 14);
            g.setColor(Estilo.LAVANDA);
            g.fill(fila);
            g.setColor(Estilo.VIOLETA);
            g.fill(new Ellipse2D.Double(x + margen + 10, ty + 8, 18, 18));
            g.setColor(Color.WHITE);
            g.setStroke(Estilo.STROKE_2_5);
            g.draw(new Line2D.Double(x + margen + 14, ty + 17, x + margen + 18, ty + 21));
            g.draw(new Line2D.Double(x + margen + 18, ty + 21, x + margen + 24, ty + 13));
            g.setColor(Tema.TEXTO);
            g.setFont(Tema.fuente(Font.BOLD, 16.5f));
            g.drawString(ajustar(g, reto.getLargo(), w - 2 * margen - 50), (float) (x + margen + 38), (float) (ty + 23));
            ty += 40;
        }

        // estrellas
        ty += 10;
        etiqueta(g, "ESTRELLAS", x + margen, ty, Tema.ETIQUETA);
        String[] criterios = def.criterios();
        double colW = (w - 2 * margen) / 3;
        for (int i = 0; i < 3; i++) {
            double cxCol = x + margen + i * colW;
            for (int k = 0; k <= i; k++) Iconos.estrella(g, cxCol + 10 + k * 18, ty + 22, 8.5, true);
            g.setColor(Tema.TEXTO_SUAVE);
            g.setFont(Tema.fuente(Font.PLAIN, 13.5f));
            g.drawString(ajustar(g, criterios[i], colW - 8), (float) cxCol, (float) (ty + 48));
        }

        // si sobra espacio y arriba no se mostró el dato curioso, se muestra aquí
        double botonY = y + r.getHeight() - 74;
        if (def.getNumero() > 1 && def.getNovedad() != null && botonY - (ty + 66) > 70) {
            double dy = ty + 66;
            g.setFont(Tema.fuente(Font.PLAIN, 15f));
            List<String> lineas = Tema.partir(def.getDato(), g.getFontMetrics(), w - 2 * margen - 90);
            double hDato = Math.min(botonY - dy - 12, 34 + lineas.size() * 19);
            g.setColor(new Color(0xFFF6D5));
            g.fill(new RoundRectangle2D.Double(x + margen, dy, w - 2 * margen, hDato, 16, 16));
            g.setColor(Estilo.AMARILLO);
            g.fill(new Ellipse2D.Double(x + margen + 14, dy + hDato / 2 - 18, 36, 36));
            Iconos.foco(g, x + margen + 32, dy + hDato / 2, 12, Estilo.TRAZO, Estilo.AMARILLO);
            etiqueta(g, "¿SABÍAS QUE…?", x + margen + 64, dy + 22, new Color(0xA86B00));
            g.setColor(Tema.TEXTO);
            g.setFont(Tema.fuente(Font.PLAIN, 15f));
            double ly2 = dy + 42;
            for (String l : lineas) {
                if (ly2 > dy + hDato - 6) break;
                g.drawString(l, (float) (x + margen + 64), (float) ly2);
                ly2 += 19;
            }
        }

        btnPrincipal.texto = "¡Jugar!";
        btnPrincipal.ubicar(cx - 130, y + r.getHeight() - 74, 260, 50);
        btnPrincipal.dibujar(g, ratonX, ratonY);
        g.setColor(Tema.ETIQUETA);
        g.setFont(Tema.fuente(Font.PLAIN, 13.5f));
        Tema.centrado(g, "Tiempo: " + Nivel.formatoTiempo(def.getTiempoLimite()) + " · Espacio para empezar · Esc para pausar",
                cx, y + r.getHeight() - 10);

        double rebote = Math.abs(Math.sin(reloj * 3)) * 6;
        Personaje.dibujar(g, 82, 600 - rebote, 1.6, accesorio, polo, true, 0, false, 12 + Math.sin(reloj * 8) * 4, -42);
        Estilo.burbuja(g, "¡Vamos, " + estudiante.getPrimerNombre() + "!", 82, 440 - rebote, 4, 158);
    }

    private void dibujarPausa(Graphics2D g) {
        velo(g);
        RoundRectangle2D r = tarjeta(g, 440, 270);
        double cx = r.getCenterX(), y = r.getY();
        g.setColor(Estilo.MORADO);
        g.setFont(Tema.fuente(Font.BOLD, 34f));
        Tema.centrado(g, "Pausa", cx, y + 66);
        g.setColor(Tema.TEXTO_SUAVE);
        g.setFont(Tema.fuente(Font.PLAIN, 18f));
        Tema.centrado(g, "Tómate un respiro, el nivel te espera.", cx, y + 102);
        btnPrincipal.texto = "Continuar";
        btnPrincipal.ubicar(cx - 120, y + 132, 240, 48);
        btnPrincipal.dibujar(g, ratonX, ratonY);
        btnSecundario.texto = "Salir a niveles";
        btnSecundario.ubicar(cx - 120, y + 194, 240, 44);
        btnSecundario.dibujar(g, ratonX, ratonY);
    }

    private void dibujarPregunta(Graphics2D g) {
        velo(g);
        RoundRectangle2D r = tarjeta(g, 660, 500);
        double x = r.getX(), y = r.getY(), w = r.getWidth(), cx = x + w / 2;
        g.setColor(colorTexto);
        g.setFont(Tema.fuente(Font.BOLD, 20f));
        Tema.centrado(g, "¡Nivel superado! Antes de ver tus estrellas…", cx, y + 40);
        Estilo.pildora(g, "DESAFÍO DEL SABER · +" + BONO_PREGUNTA + " PUNTOS", cx, y + 54, Estilo.AMARILLO, Estilo.MORADO, 14f);

        g.setColor(Tema.TEXTO);
        g.setFont(Tema.fuente(Font.BOLD, 25f));
        double qy = Tema.parrafo(g, pregunta.getTexto(), x + 50, y + 120, w - 100, 30, true);

        String[] ops = pregunta.getOpciones();
        double oy = Math.max(qy + 4, y + 160);
        for (int i = 0; i < ops.length; i++) {
            Rectangle2D caja = new Rectangle2D.Double(x + 60, oy + i * 62, w - 120, 50);
            opciones[i] = caja;
            boolean encima = respuesta < 0 && caja.contains(ratonX, ratonY);
            Color fondo = Color.WHITE;
            if (respuesta >= 0 && pregunta.esCorrecta(i)) fondo = new Color(0xD9F5E3);
            else if (respuesta == i) fondo = new Color(0xFDE0E0);
            else if (encima) fondo = Tema.alfa(color, 30);
            RoundRectangle2D forma = new RoundRectangle2D.Double(caja.getX(), caja.getY(), caja.getWidth(), caja.getHeight(), 16, 16);
            Estilo.contorno(g, forma, fondo, respuesta >= 0 && pregunta.esCorrecta(i) ? 3f : 2f);
            g.setColor(Estilo.MORADO);
            g.fill(new Ellipse2D.Double(caja.getX() + 12, caja.getY() + 11, 28, 28));
            g.setColor(Color.WHITE);
            g.setFont(Tema.fuente(Font.BOLD, 16f));
            Tema.centrado(g, String.valueOf(i + 1), caja.getX() + 26, caja.getY() + 31);
            g.setColor(Tema.TEXTO);
            g.setFont(Tema.fuente(Font.PLAIN, 18.5f));
            g.drawString(ops[i], (float) (caja.getX() + 52), (float) (caja.getY() + 32));
        }

        double fy = oy + 3 * 62 + 18;
        if (respuesta < 0) {
            g.setColor(Tema.ETIQUETA);
            g.setFont(Tema.fuente(Font.PLAIN, 15f));
            Tema.centrado(g, "Haz clic en una respuesta (o presiona 1, 2 o 3)", cx, fy + 10);
        } else {
            boolean bien = pregunta.esCorrecta(respuesta);
            g.setColor(bien ? Tema.VERDE_OK : Tema.ROJO);
            g.setFont(Tema.fuente(Font.BOLD, 20f));
            Tema.centrado(g, bien ? "¡Correcto! +" + BONO_PREGUNTA + " puntos" : "¡Casi! Mira la respuesta en verde.", cx, fy + 8);
            g.setColor(Tema.TEXTO_SUAVE);
            g.setFont(Tema.fuente(Font.PLAIN, 16f));
            Tema.parrafo(g, pregunta.getExplicacion(), x + 60, fy + 32, w - 120, 20, true);
            btnPrincipal.texto = "Ver mis estrellas";
            btnPrincipal.ubicar(cx - 130, y + r.getHeight() - 66, 260, 48);
            btnPrincipal.dibujar(g, ratonX, ratonY);
        }
    }

    private void dibujarFin(Graphics2D g) {
        velo(g);
        RoundRectangle2D r = tarjeta(g, 660, 562);
        double x = r.getX(), y = r.getY(), w = r.getWidth(), cx = x + w / 2;
        PuntajeObtenido p = resultado.getPuntaje();
        int estrellas = recompensa != null ? recompensa.getEstrellas() : estrellasNivel;
        int mostradas = recompensa != null ? Math.min(estrellasSonadas, estrellas) : estrellas;

        double salto = estrellas > 0 ? Math.abs(Math.sin(tiempoFin * 6)) * 18 : 0;
        Personaje.dibujar(g, 82, 600 - salto, 1.6, accesorio, polo, true, 0, false, 12 + Math.sin(tiempoFin * 10) * 4, -42);
        Estilo.burbuja(g, gano ? "¡Lo logré!" : "¡Otra vez!", 82, 440 - salto, 4, 158);

        g.setColor(colorTexto);
        g.setFont(Tema.espaciada(Font.BOLD, 13f, 0.1f));
        Tema.centrado(g, String.format("NIVEL %d · %s", def.getNumero(), def.getTitulo().toUpperCase()), cx, y + 30);
        g.setFont(Tema.fuente(Font.BOLD, 32f));
        Tema.centrado(g, gano ? (estrellas == 3 ? "¡Perfecto!" : "¡Nivel superado!") : "¡Casi lo logras!", cx, y + 64);
        for (int i = 0; i < 3; i++) {
            boolean llena = i < mostradas;
            double aparece = tiempoFin - (0.5 + i * 0.45);
            double s = llena ? 1 + Math.max(0, 0.35 - aparece) * 1.6 : 1;
            Iconos.estrella(g, cx + (i - 1) * 62, y + 106 - (i == 1 ? 8 : 0), 24 * s, llena);
        }
        g.setColor(Tema.TEXTO_SUAVE);
        g.setFont(Tema.fuente(Font.PLAIN, 16f));
        String sub = gano ? motivoFin : motivoFin + " Revisa los retos y vuelve a intentarlo.";
        if (gano && estrellas < 3) {
            sub = estrellas == 1 ? "Para 2 estrellas: " + def.criterios()[1].toLowerCase() + "."
                    : "Para 3 estrellas: " + def.criterios()[2].toLowerCase() + ".";
        }
        Tema.centrado(g, ajustar(g, sub, w - 60), cx, y + 154);

        int cumplidos = def.getRetos().size() - restantes();
        String[][] datos = {
            {"PUNTAJE", String.valueOf(p.getPuntos())},
            {"RETOS", cumplidos + "/" + def.getRetos().size()},
            {"ERRORES", String.valueOf(p.getErrores())},
            {"TIEMPO", Nivel.formatoTiempo(resultado.getDuracionSegundos())}
        };
        double margen = 44, sep = 12;
        double bw = (w - 2 * margen - 3 * sep) / 4;
        for (int i = 0; i < datos.length; i++) {
            double bx = x + margen + i * (bw + sep), by = y + 168;
            g.setColor(Estilo.LAVANDA);
            g.fill(new RoundRectangle2D.Double(bx, by, bw, 60, 16, 16));
            g.setColor(Tema.ETIQUETA);
            g.setFont(Tema.espaciada(Font.BOLD, 12f, 0.1f));
            Tema.centrado(g, datos[i][0], bx + bw / 2, by + 21);
            g.setColor(Tema.TEXTO);
            g.setFont(Tema.fuente(Font.BOLD, 22f));
            Tema.centrado(g, datos[i][1], bx + bw / 2, by + 48);
        }

        double iy = y + 240;
        g.setColor(Tema.alfa(color, 28));
        g.fill(new RoundRectangle2D.Double(x + margen, iy, w - 2 * margen, 82, 18, 18));
        Iconos.logo(g, tipo, x + margen + 38, iy + 41, 46);
        double tx = x + margen + 76;
        etiqueta(g, "TU IMPACTO SOCIAL", tx, iy + 24, colorTexto);
        g.setColor(Tema.TEXTO);
        g.setFont(Tema.fuente(Font.BOLD, 22f));
        g.drawString("+" + modulo.formatear(resultado.getImpacto()) + " " + tipo.getUnidadImpacto(),
                (float) tx, (float) (iy + 50));
        g.setColor(Tema.TEXTO_SUAVE);
        g.setFont(Tema.fuente(Font.PLAIN, 14.5f));
        g.drawString(ajustar(g, modulo.equivalencia(tipo, resultado.getImpacto()), w - 2 * margen - 90), (float) tx, (float) (iy + 71));

        double ry = y + 332;
        if (recompensa != null) {
            RoundRectangle2D caja = new RoundRectangle2D.Double(x + margen, ry, w - 2 * margen, 50, 18, 18);
            Estilo.contorno(g, caja, new Color(0xFFF6D5), 2f);
            double brillo = 1 + Math.sin(reloj * 6) * 0.06;
            Iconos.moneda(g, x + margen + 28, ry + 25, 14 * brillo);
            g.setColor(Tema.TEXTO);
            g.setFont(Tema.fuente(Font.BOLD, 19f));
            String monedas = "+" + recompensa.getMonedas() + " EcoMonedas";
            g.drawString(monedas, (float) (x + margen + 50), (float) (ry + 31));
            double mx = x + margen + 62 + g.getFontMetrics().stringWidth(monedas) + 16;
            List<Medalla> nuevas = recompensa.getMedallasNuevas();
            if (!nuevas.isEmpty()) {
                Iconos.medalla(g, nuevas.get(0), mx + 12, ry + 31, 11, true);
                String texto = "Medalla nueva: " + nuevas.get(0).getNombre()
                        + (nuevas.size() > 1 ? " (+" + (nuevas.size() - 1) + ")" : "");
                g.setColor(new Color(0xA86B00));
                g.setFont(Tema.fuente(Font.BOLD, 15.5f));
                g.drawString(ajustar(g, texto, x + w - margen - mx - 40), (float) (mx + 30), (float) (ry + 31));
            } else if (recompensa.getEstrellasNuevas() > 0) {
                g.setColor(new Color(0xA86B00));
                g.setFont(Tema.fuente(Font.BOLD, 15.5f));
                g.drawString(ajustar(g, "¡+" + recompensa.getEstrellasNuevas() + " estrellas nuevas en tu mapa!",
                        x + w - margen - mx - 10), (float) mx, (float) (ry + 31));
            } else {
                g.setColor(Tema.TEXTO_SUAVE);
                g.setFont(Tema.fuente(Font.PLAIN, 15f));
                g.drawString(ajustar(g, "Úsalas en la Tienda para tu personaje", x + w - margen - mx - 10),
                        (float) mx, (float) (ry + 31));
            }
        }

        double cy = y + 406;
        etiqueta(g, "EN LA VIDA REAL", x + margen, cy, Tema.ETIQUETA);
        g.setColor(Tema.TEXTO);
        g.setFont(Tema.fuente(Font.PLAIN, 16f));
        Tema.parrafo(g, tipo.getConsejo(), x + margen, cy + 23, w - 2 * margen, 19, false);

        double by = y + r.getHeight() - 60;
        if (hayTercerBoton()) {
            btnPrincipal.texto = "Siguiente nivel →";
            btnPrincipal.ubicar(x + margen, by, 250, 46);
            btnSecundario.texto = "Repetir";
            btnSecundario.ubicar(x + margen + 262, by, 150, 46);
            btnTerciario.texto = "Niveles";
            btnTerciario.ubicar(x + margen + 424, by, w - 2 * margen - 424, 46);
            btnTerciario.dibujar(g, ratonX, ratonY);
        } else {
            btnPrincipal.texto = gano ? "Jugar otra vez" : "Reintentar";
            btnPrincipal.ubicar(cx - 250, by, 240, 46);
            btnSecundario.texto = "Volver a niveles";
            btnSecundario.ubicar(cx + 10, by, 240, 46);
        }
        btnPrincipal.dibujar(g, ratonX, ratonY);
        btnSecundario.dibujar(g, ratonX, ratonY);
    }
}
