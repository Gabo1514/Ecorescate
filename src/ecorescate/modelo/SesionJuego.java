package ecorescate.modelo;

import java.time.LocalDateTime;

/** Una partida jugada por un estudiante en uno de los minijuegos. */
public class SesionJuego {

    private final Estudiante estudiante;
    private final TipoJuego juego;
    private final LocalDateTime fecha;
    private final long duracionSegundos;
    private final PuntajeObtenido puntaje;
    private final NivelAlcanzado nivel;
    private final double impacto;
    private final Boolean respuestaCorrecta;
    private final int estrellas;

    public SesionJuego(Estudiante estudiante, TipoJuego juego, LocalDateTime fecha, long duracionSegundos,
                       PuntajeObtenido puntaje, NivelAlcanzado nivel, double impacto) {
        this(estudiante, juego, fecha, duracionSegundos, puntaje, nivel, impacto, null, 0);
    }

    public SesionJuego(Estudiante estudiante, TipoJuego juego, LocalDateTime fecha, long duracionSegundos,
                       PuntajeObtenido puntaje, NivelAlcanzado nivel, double impacto, Boolean respuestaCorrecta) {
        this(estudiante, juego, fecha, duracionSegundos, puntaje, nivel, impacto, respuestaCorrecta, 0);
    }

    /**
     * @param nivel número del nivel jugado; "completado" indica si lo superó
     * @param estrellas estrellas obtenidas en ese nivel (0 si no lo superó)
     */
    public SesionJuego(Estudiante estudiante, TipoJuego juego, LocalDateTime fecha, long duracionSegundos,
                       PuntajeObtenido puntaje, NivelAlcanzado nivel, double impacto, Boolean respuestaCorrecta,
                       int estrellas) {
        this.estrellas = estrellas;
        this.estudiante = estudiante;
        this.juego = juego;
        this.fecha = fecha;
        this.duracionSegundos = duracionSegundos;
        this.puntaje = puntaje;
        this.nivel = nivel;
        this.impacto = impacto;
        this.respuestaCorrecta = respuestaCorrecta;
    }

    public Estudiante getEstudiante() { return estudiante; }
    public TipoJuego getJuego() { return juego; }
    public LocalDateTime getFecha() { return fecha; }
    public long getDuracionSegundos() { return duracionSegundos; }
    public PuntajeObtenido getPuntaje() { return puntaje; }
    public NivelAlcanzado getNivel() { return nivel; }

    /** Valor convertido por el módulo de Impacto Social, en la unidad del juego. */
    public double getImpacto() { return impacto; }

    /** Si respondió bien la pregunta final; null si salió antes de responderla. */
    public Boolean getRespuestaCorrecta() { return respuestaCorrecta; }

    public int getEstrellas() { return estrellas; }
}
