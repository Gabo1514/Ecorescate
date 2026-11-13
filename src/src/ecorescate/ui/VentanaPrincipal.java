package ecorescate.ui;

import ecorescate.Main;
import ecorescate.juegos.Minijuego;
import ecorescate.modelo.Estudiante;
import ecorescate.modelo.ModuloImpactoSocial;
import ecorescate.modelo.Perfil;
import ecorescate.modelo.PerfilRepositorio;
import ecorescate.modelo.Progreso;
import ecorescate.modelo.Recompensa;
import ecorescate.modelo.RegistroPartidas;
import ecorescate.modelo.SesionJuego;
import ecorescate.modelo.TipoJuego;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Ventana única de la suite. Cambia entre registro, menú, minijuegos y demás pantallas con un CardLayout. */
public class VentanaPrincipal extends JFrame
        implements PanelRegistro.Acciones, PanelMenu.Acciones, PanelImpacto.Acciones, PanelTienda.Acciones,
        PanelNiveles.Acciones, Minijuego.Oyente {

    private static final String NIVELES = "niveles";

    private static final String REGISTRO = "registro";
    private static final String MENU = "menu";
    private static final String JUEGO = "juego";
    private static final String IMPACTO = "impacto";
    private static final String TIENDA = "tienda";
    private static final String MEDALLAS = "medallas";
    private static final String RANKING = "ranking";

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenedor = new JPanel(cartas);
    private final RegistroPartidas registro;
    private final PerfilRepositorio perfiles;
    private final ModuloImpactoSocial impacto = new ModuloImpactoSocial();
    private final Progreso progreso = new Progreso(impacto);
    private final PanelRegistro panelRegistro;
    private final PanelMenu panelMenu;
    private final PanelImpacto panelImpacto;
    private final PanelTienda panelTienda;
    private final PanelMedallas panelMedallas;
    private final PanelRanking panelRanking;
    private final PanelNiveles panelNiveles;

    private Estudiante estudiante;
    private Perfil perfil;
    private List<SesionJuego> sesiones = new ArrayList<>();
    private Minijuego juegoActual;

    public VentanaPrincipal(RegistroPartidas registro, PerfilRepositorio perfiles) {
        super("Operación EcoRescate");
        this.registro = registro;
        this.perfiles = perfiles;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try { setIconImage(Iconos.imagenAplicacion(64)); } catch (Throwable ignorado) {}
        setMinimumSize(new Dimension(1024, 720));
        setSize(1280, 800);
        try { setLocationRelativeTo(null); } catch (Throwable ignorado) {}

        panelRegistro = new PanelRegistro(this);
        panelMenu = new PanelMenu(this);
        panelImpacto = new PanelImpacto(impacto, this);
        panelTienda = new PanelTienda(this);
        panelMedallas = new PanelMedallas(this::volverAlMenu);
        panelRanking = new PanelRanking(this::volverAlMenu);
        panelNiveles = new PanelNiveles(this);
        contenedor.add(panelNiveles, NIVELES);
        contenedor.add(panelRegistro, REGISTRO);
        contenedor.add(panelMenu, MENU);
        contenedor.add(panelImpacto, IMPACTO);
        contenedor.add(panelTienda, TIENDA);
        contenedor.add(panelMedallas, MEDALLAS);
        contenedor.add(panelRanking, RANKING);
        setContentPane(contenedor);
        cartas.show(contenedor, REGISTRO);
    }

    @Override
    public void ingresar(Estudiante nuevo) {
        estudiante = nuevo;
        sesiones = registro.cargar(nuevo);
        perfil = perfiles.cargar(nuevo);
        Sonido.get().tocar(Sonido.Efecto.NIVEL);
        mostrarMenu();
    }

    private void detenerAnimacionesSecundarias() {
        panelNiveles.detenerAnimacion();
        panelTienda.detenerAnimacion();
    }

    private void mostrarMenu() {
        detenerAnimacionesSecundarias();
        panelMenu.actualizar(estudiante, sesiones, perfil);
        cartas.show(contenedor, MENU);
    }

    /** Desde el menú: abre el mapa de niveles del juego. */
    @Override
    public void jugar(TipoJuego tipo) {
        verNiveles(tipo);
    }

    private void verNiveles(TipoJuego tipo) {
        panelTienda.detenerAnimacion();
        panelNiveles.mostrar(tipo, perfil);
        cartas.show(contenedor, NIVELES);
    }

    @Override
    public void jugarNivel(TipoJuego tipo, int nivel) {
        detenerAnimacionesSecundarias();
        cerrarJuego();
        juegoActual = Minijuego.crear(tipo, estudiante, impacto, nivel);
        juegoActual.usarPersonaje(perfil.getAccesorio(), perfil.getPolo());
        contenedor.add(juegoActual, JUEGO);
        juegoActual.arrancar(this);
        cartas.show(contenedor, JUEGO);
    }

    private void cerrarJuego() {
        if (juegoActual != null) {
            juegoActual.detener();
            contenedor.remove(juegoActual);
            juegoActual = null;
        }
    }

    @Override
    public void verImpacto() {
        detenerAnimacionesSecundarias();
        panelImpacto.actualizar(estudiante, sesiones, registro.getArchivo());
        cartas.show(contenedor, IMPACTO);
    }

    @Override
    public void verTienda() {
        panelNiveles.detenerAnimacion();
        panelTienda.mostrar(perfil);
        cartas.show(contenedor, TIENDA);
    }

    @Override
    public void verMedallas() {
        detenerAnimacionesSecundarias();
        panelMedallas.mostrar(perfil);
        cartas.show(contenedor, MEDALLAS);
    }

    @Override
    public void verRanking() {
        detenerAnimacionesSecundarias();
        // agrupa todas las partidas del mismo grado y sección por alumno
        Map<String, List<SesionJuego>> porAlumno = new LinkedHashMap<>();
        Map<String, Estudiante> alumnos = new LinkedHashMap<>();
        for (SesionJuego s : registro.cargarTodas()) {
            Estudiante e = s.getEstudiante();
            if (e.getGrado() != estudiante.getGrado() || !e.getSeccion().equalsIgnoreCase(estudiante.getSeccion())) continue;
            String clave = e.getNombre().toLowerCase(Locale.ROOT);
            porAlumno.computeIfAbsent(clave, k -> new ArrayList<>()).add(s);
            alumnos.putIfAbsent(clave, e);
        }
        String miClave = estudiante.getNombre().toLowerCase(Locale.ROOT);
        alumnos.putIfAbsent(miClave, estudiante);
        porAlumno.putIfAbsent(miClave, new ArrayList<>());

        List<PanelRanking.Fila> filas = new ArrayList<>();
        for (Map.Entry<String, Estudiante> a : alumnos.entrySet()) {
            List<SesionJuego> suyas = porAlumno.get(a.getKey());
            boolean yo = a.getKey().equals(miClave);
            Perfil p = yo ? perfil : perfiles.cargar(a.getValue());
            filas.add(new PanelRanking.Fila(a.getValue().getNombre(), p.totalEstrellas(),
                    Progreso.puntajeRanking(suyas), p.getAccesorio(), p.getPolo(), yo));
        }
        // primero las estrellas; si empatan, el puntaje
        filas.sort((x, y) -> x.estrellas != y.estrellas ? Integer.compare(y.estrellas, x.estrellas)
                : Integer.compare(y.puntos, x.puntos));
        panelRanking.mostrar(estudiante.getAula(), filas, perfil.getMonedas());
        cartas.show(contenedor, RANKING);
    }

    @Override
    public void cambiarAlumno() {
        detenerAnimacionesSecundarias();
        estudiante = null;
        perfil = null;
        sesiones = new ArrayList<>();
        panelRegistro.prepararNuevoIngreso();
        cartas.show(contenedor, REGISTRO);
    }

    @Override
    public void volverAlMenu() {
        mostrarMenu();
    }

    @Override
    public void perfilCambiado() {
        guardarPerfil();
    }

    @Override
    public Recompensa partidaTerminada(SesionJuego sesion, int mejorCombo) {
        sesiones.add(sesion);
        try {
            registro.guardar(sesion);
        } catch (Throwable ex) {
            System.err.println("No se pudo guardar la partida en " + registro.getArchivo().getAbsolutePath() + ": " + ex.getMessage());
        }
        Recompensa r = progreso.registrar(perfil, sesion, sesiones, mejorCombo);
        guardarPerfil();
        return r;
    }

    private void guardarPerfil() {
        try {
            perfiles.guardar(perfil);
        } catch (Throwable ex) {
            System.err.println("No se pudo guardar el perfil: " + ex.getMessage());
        }
    }

    @Override
    public void salirDelJuego() {
        TipoJuego tipo = panelNiveles.getTipo();
        cerrarJuego();
        verNiveles(tipo);
    }

    /** Carpeta "datos" junto al programa. */
    public static File carpetaDatos() {
        return new File(Main.carpetaPrograma(), "datos");
    }

    /** Ubicación por defecto del archivo de partidas. */
    public static File archivoPorDefecto() {
        return new File(carpetaDatos(), "partidas.csv");
    }
}
