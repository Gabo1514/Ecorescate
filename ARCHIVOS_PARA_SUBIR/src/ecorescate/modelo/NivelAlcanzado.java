package ecorescate.modelo;

/** Nivel al que llegó el estudiante en una partida (escala del 1 al 10). */
public class NivelAlcanzado {

    public static final int MAXIMO = 10;

    private final int nivel;
    private final boolean completado;

    public NivelAlcanzado(int nivel, boolean completado) {
        this.nivel = Math.max(1, Math.min(MAXIMO, nivel));
        this.completado = completado;
    }

    public int getNivel() { return nivel; }

    /** true si superó los 10 niveles. */
    public boolean isCompletado() { return completado; }

    public String getDescripcion() {
        if (completado) return "¡Experto ambiental!";
        if (nivel >= 7) return "Guardián avanzado";
        if (nivel >= 4) return "Guardián en formación";
        return "Aprendiz ecológico";
    }
}
