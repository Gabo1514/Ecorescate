package ecorescate.modelo;

/** Resultado numérico de una partida (RF-05). */
public class PuntajeObtenido {

    private final int puntos;
    private final int aciertos;
    private final int errores;

    public PuntajeObtenido(int puntos, int aciertos, int errores) {
        this.puntos = Math.max(0, puntos);
        this.aciertos = Math.max(0, aciertos);
        this.errores = Math.max(0, errores);
    }

    public int getPuntos() { return puntos; }
    public int getAciertos() { return aciertos; }
    public int getErrores() { return errores; }

    /** Porcentaje de acciones correctas, de 0 a 100. */
    public int getPrecision() {
        int total = aciertos + errores;
        return total == 0 ? 0 : Math.round(aciertos * 100f / total);
    }
}
