package ecorescate.modelo;

import java.util.Arrays;
import java.util.List;

/**
 * Un nivel de un minijuego: sus retos, el tiempo límite y lo que se pide para cada estrella.
 * ★ completar los retos · ★★ con pocos errores · ★★★ casi sin errores y rápido.
 */
public class Nivel {

    private final TipoJuego juego;
    private final int numero;
    private final String titulo;
    private final List<Reto> retos;
    private final int tiempoLimite;
    private final int erroresDosEstrellas;
    private final int erroresTresEstrellas;
    private final int tiempoTresEstrellas;
    private final String novedad;
    private final boolean conPregunta;

    public Nivel(TipoJuego juego, int numero, String titulo, int tiempoLimite, int erroresDosEstrellas,
                 int erroresTresEstrellas, String novedad, Reto... retos) {
        this.juego = juego;
        this.numero = numero;
        this.titulo = titulo;
        this.retos = Arrays.asList(retos);
        this.tiempoLimite = tiempoLimite;
        this.erroresDosEstrellas = erroresDosEstrellas;
        this.erroresTresEstrellas = erroresTresEstrellas;
        this.tiempoTresEstrellas = (int) Math.round(tiempoLimite * 0.6);
        this.novedad = novedad;
        this.conPregunta = numero % 3 == 0 || numero == NivelAlcanzado.MAXIMO;
    }

    public TipoJuego getJuego() { return juego; }
    public int getNumero() { return numero; }
    public String getTitulo() { return titulo; }
    public List<Reto> getRetos() { return retos; }
    /** Segundos para completar los retos. */
    public int getTiempoLimite() { return tiempoLimite; }
    public int getErroresDosEstrellas() { return erroresDosEstrellas; }
    public int getErroresTresEstrellas() { return erroresTresEstrellas; }
    public int getTiempoTresEstrellas() { return tiempoTresEstrellas; }
    /** Algo nuevo que aparece en este nivel (puede ser null). */
    public String getNovedad() { return novedad; }
    /** Los niveles 3, 6, 9 y 10 terminan con una pregunta rápida. */
    public boolean isConPregunta() { return conPregunta; }

    /** Dato curioso propio de este nivel. */
    public String getDato() {
        String[] datos = juego.getDatos();
        return datos[(numero - 1) % datos.length];
    }

    public int estrellas(boolean superado, int errores, double segundos) {
        if (!superado) return 0;
        if (errores <= erroresTresEstrellas && segundos <= tiempoTresEstrellas) return 3;
        if (errores <= erroresDosEstrellas) return 2;
        return 1;
    }

    /** Descripción de lo que pide cada estrella, para mostrarla antes de jugar. */
    public String[] criterios() {
        return new String[]{
            "Completa los retos",
            "Máximo " + erroresDosEstrellas + (erroresDosEstrellas == 1 ? " error" : " errores"),
            (erroresTresEstrellas == 0 ? "Sin errores" : "Máx. " + erroresTresEstrellas + " error")
                    + " y antes de " + formatoTiempo(tiempoTresEstrellas)
        };
    }

    public static String formatoTiempo(double segundos) {
        int s = (int) Math.max(0, Math.ceil(segundos));
        return String.format("%d:%02d", s / 60, s % 60);
    }
}
