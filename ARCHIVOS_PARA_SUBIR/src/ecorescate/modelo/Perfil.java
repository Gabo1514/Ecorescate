package ecorescate.modelo;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Progreso personal del alumno: EcoMonedas, medallas y personaje. */
public class Perfil {

    public static final int COLORES_POLO = 6;

    private final Estudiante estudiante;
    private int monedas;
    private final Set<Medalla> medallas = EnumSet.noneOf(Medalla.class);
    private final Set<Accesorio> comprados = EnumSet.of(Accesorio.NINGUNO);
    private Accesorio accesorio = Accesorio.NINGUNO;
    private int polo;
    /** Mejores estrellas (0 a 3) de cada nivel de cada minijuego. */
    private final Map<TipoJuego, int[]> estrellas = new EnumMap<>(TipoJuego.class);

    public Perfil(Estudiante estudiante) {
        this.estudiante = estudiante;
        for (TipoJuego t : TipoJuego.values()) estrellas.put(t, new int[CatalogoNiveles.NIVELES]);
    }

    public int getEstrellas(TipoJuego juego, int nivel) {
        if (nivel < 1 || nivel > CatalogoNiveles.NIVELES) return 0;
        return estrellas.get(juego)[nivel - 1];
    }

    /** Guarda las estrellas de un nivel si mejoran. Devuelve cuántas estrellas nuevas ganó. */
    public int registrarEstrellas(TipoJuego juego, int nivel, int cantidad) {
        int anterior = getEstrellas(juego, nivel);
        if (cantidad <= anterior || nivel < 1 || nivel > CatalogoNiveles.NIVELES) return 0;
        estrellas.get(juego)[nivel - 1] = Math.min(3, cantidad);
        return Math.min(3, cantidad) - anterior;
    }

    /** El nivel 1 siempre está abierto; los demás se abren al superar el anterior. */
    public boolean desbloqueado(TipoJuego juego, int nivel) {
        return nivel == 1 || getEstrellas(juego, nivel - 1) > 0;
    }

    public int estrellasDe(TipoJuego juego) {
        int total = 0;
        for (int e : estrellas.get(juego)) total += e;
        return total;
    }

    public int totalEstrellas() {
        int total = 0;
        for (TipoJuego t : TipoJuego.values()) total += estrellasDe(t);
        return total;
    }

    public int nivelesSuperados(TipoJuego juego) {
        int n = 0;
        for (int e : estrellas.get(juego)) if (e > 0) n++;
        return n;
    }

    public int nivelesSuperados() {
        int n = 0;
        for (TipoJuego t : TipoJuego.values()) n += nivelesSuperados(t);
        return n;
    }

    /** Primer nivel abierto que aún no tiene 3 estrellas (para sugerir dónde seguir). */
    public int nivelSugerido(TipoJuego juego) {
        for (int n = 1; n <= CatalogoNiveles.NIVELES; n++) {
            if (!desbloqueado(juego, n)) return n - 1;
            if (getEstrellas(juego, n) == 0) return n;
        }
        return CatalogoNiveles.NIVELES;
    }

    int[] estrellasInternas(TipoJuego juego) {
        return estrellas.get(juego);
    }

    public Estudiante getEstudiante() { return estudiante; }
    public int getMonedas() { return monedas; }
    public Set<Medalla> getMedallas() { return EnumSet.copyOf(medallas.isEmpty() ? EnumSet.noneOf(Medalla.class) : medallas); }
    public Accesorio getAccesorio() { return accesorio; }
    public int getPolo() { return polo; }

    public boolean tiene(Medalla m) {
        return medallas.contains(m);
    }

    public boolean tiene(Accesorio a) {
        return comprados.contains(a);
    }

    public void sumarMonedas(int cantidad) {
        monedas = Math.max(0, monedas + cantidad);
    }

    public void otorgar(Medalla m) {
        medallas.add(m);
    }

    /** Compra un accesorio si alcanza el dinero. Devuelve true si se compró. */
    public boolean comprar(Accesorio a) {
        if (tiene(a)) return true;
        if (monedas < a.getPrecio()) return false;
        monedas -= a.getPrecio();
        comprados.add(a);
        return true;
    }

    public void equipar(Accesorio a) {
        if (tiene(a)) accesorio = a;
    }

    public void setPolo(int polo) {
        this.polo = Math.floorMod(polo, COLORES_POLO);
    }

    // usados al leer el archivo del perfil
    void cargarMonedas(int m) { monedas = Math.max(0, m); }
    void cargarComprado(Accesorio a) { comprados.add(a); }
    Set<Accesorio> comprados() { return comprados; }
    Set<Medalla> medallasInternas() { return medallas; }
}
