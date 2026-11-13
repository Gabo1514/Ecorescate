package ecorescate.modelo;

import java.util.List;
import java.util.Locale;

/**
 * Traduce el rendimiento del juego a métricas del mundo real (RF-06).
 * Los factores de conversión están parametrizados en {@link TipoJuego} (RN-02):
 * impacto = aciertos x factorPorAcierto + niveles superados x factorPorNivel.
 */
public class ModuloImpactoSocial {

    /** Peso aproximado de una botella de plástico, en kg. */
    private static final double KG_POR_BOTELLA = 0.025;
    /** Capacidad de un balde, en litros. */
    private static final double LITROS_POR_BALDE = 10.0;
    /** Consumo de un foco LED de 10 W durante una hora, en kWh. */
    private static final double KWH_FOCO_LED_HORA = 0.01;
    /** Superficie de una pizarra de aula (1,2 m x 3 m), en m². */
    private static final double M2_POR_PIZARRA = 3.6;

    /** RN-02: cada acierto suma su factor y cada nivel superado suma un bono. */
    public double calcular(TipoJuego juego, int aciertos, boolean nivelSuperado) {
        return aciertos * juego.getImpactoPorAcierto() + (nivelSuperado ? juego.getImpactoPorNivel() : 0);
    }

    public double total(TipoJuego juego, List<SesionJuego> sesiones) {
        double suma = 0;
        for (SesionJuego s : sesiones) {
            if (s.getJuego() == juego) suma += s.getImpacto();
        }
        return suma;
    }

    /** Formatea con un decimal si hace falta: 12 / 3,5. */
    public String formatear(double valor) {
        if (Math.abs(valor - Math.rint(valor)) < 0.05) {
            return String.format(Locale.ROOT, "%d", Math.round(valor));
        }
        return String.format(Locale.ROOT, "%.1f", valor).replace('.', ',');
    }

    /** Frase que un niño pueda imaginar. */
    public String equivalencia(TipoJuego juego, double valor) {
        long n;
        switch (juego) {
            case LIMPIEZA:
                n = Math.round(valor / KG_POR_BOTELLA);
                return "Es como reciclar " + n + plural(n, " botella", " botellas")
                        + " de plástico que ya no llegan al río ni al mar.";
            case AGUA:
                n = Math.round(valor / LITROS_POR_BALDE);
                return "Alcanza para llenar " + n + plural(n, " balde", " baldes") + " de 10 litros.";
            case ENERGIA:
                n = Math.round(valor / KWH_FOCO_LED_HORA);
                return "Con esa energía un foco LED puede estar prendido " + n + plural(n, " hora.", " horas.");
            case VERDE:
            default:
                n = Math.max(1, Math.round(valor / M2_POR_PIZARRA));
                return "¡Un jardín tan grande como " + n + plural(n, " pizarra", " pizarras") + " del salón, dando sombra y aire limpio!";
        }
    }

    /** Texto con los factores usados, para mostrar al docente. */
    public String describirFactores(TipoJuego juego) {
        return "1 acierto = " + formatear(juego.getImpactoPorAcierto()) + " · 1 nivel superado = "
                + formatear(juego.getImpactoPorNivel()) + " (" + juego.getUnidadImpacto() + ")";
    }

    private static String plural(long n, String singular, String plural) {
        return n == 1 ? singular : plural;
    }
}
