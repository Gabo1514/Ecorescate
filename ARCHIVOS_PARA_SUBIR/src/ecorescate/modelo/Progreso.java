package ecorescate.modelo;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Reglas de EcoMonedas, estrellas por nivel y medallas. */
public class Progreso {

    private final ModuloImpactoSocial impacto;

    public Progreso(ModuloImpactoSocial impacto) {
        this.impacto = impacto;
    }

    /** Puntaje para el ranking: suma del mejor puntaje de cada nivel de cada minijuego. */
    public static int puntajeRanking(List<SesionJuego> sesiones) {
        Map<String, Integer> mejor = new HashMap<>();
        for (SesionJuego s : sesiones) {
            String clave = s.getJuego().name() + s.getNivel().getNivel();
            mejor.merge(clave, s.getPuntaje().getPuntos(), Math::max);
        }
        int total = 0;
        for (int v : mejor.values()) total += v;
        return total;
    }

    /**
     * Guarda las estrellas del nivel y entrega monedas y medallas.
     * @param historial todas las partidas del alumno, incluida la que acaba de jugar
     */
    public Recompensa registrar(Perfil perfil, SesionJuego sesion, List<SesionJuego> historial, int mejorCombo) {
        int estrellas = sesion.getEstrellas();
        int nuevas = perfil.registrarEstrellas(sesion.getJuego(), sesion.getNivel().getNivel(), estrellas);
        // se premia más la primera vez que se ganan estrellas, para animar a avanzar
        int monedas = sesion.getPuntaje().getPuntos() / 25 + estrellas * 5 + nuevas * 15
                + (Boolean.TRUE.equals(sesion.getRespuestaCorrecta()) ? 10 : 0);
        perfil.sumarMonedas(monedas);

        List<Medalla> medallas = new ArrayList<>();
        for (Medalla m : Medalla.values()) {
            if (!perfil.tiene(m) && cumple(m, perfil, sesion, historial, mejorCombo)) {
                perfil.otorgar(m);
                medallas.add(m);
            }
        }
        return new Recompensa(monedas, estrellas, nuevas, medallas);
    }

    private boolean cumple(Medalla m, Perfil p, SesionJuego s, List<SesionJuego> h, int mejorCombo) {
        switch (m) {
            case PRIMERA_MISION: return p.nivelesSuperados() >= 1;
            case EXPLORADOR: {
                Set<TipoJuego> jugados = EnumSet.noneOf(TipoJuego.class);
                for (SesionJuego x : h) jugados.add(x.getJuego());
                return jugados.size() == TipoJuego.values().length;
            }
            case TRES_ESTRELLAS: return s.getEstrellas() == 3;
            case COMBO: return mejorCombo >= 5;
            case PERFECTO: return s.getNivel().isCompletado() && s.getPuntaje().getErrores() == 0;
            case SABIO: {
                int bien = 0;
                for (SesionJuego x : h) if (Boolean.TRUE.equals(x.getRespuestaCorrecta())) bien++;
                return bien >= 5;
            }
            case RECICLADOR: return p.nivelesSuperados(TipoJuego.LIMPIEZA) == CatalogoNiveles.NIVELES;
            case GUARDIAN: return p.nivelesSuperados(TipoJuego.AGUA) == CatalogoNiveles.NIVELES;
            case CAZAVATIOS: return p.nivelesSuperados(TipoJuego.ENERGIA) == CatalogoNiveles.NIVELES;
            case MANO_VERDE: return p.nivelesSuperados(TipoJuego.VERDE) == CatalogoNiveles.NIVELES;
            case CIEN_LITROS: return impacto.total(TipoJuego.AGUA, h) >= 100;
            case CONSTANTE: return h.size() >= 20;
            case COLECCIONISTA: return p.totalEstrellas() >= 60;
            case ECO_HEROE: return p.nivelesSuperados() == CatalogoNiveles.NIVELES * TipoJuego.values().length;
            default: return false;
        }
    }
}
