package ecorescate.modelo;

import java.util.EnumMap;
import java.util.Map;

/**
 * Los 10 niveles de cada minijuego. Cada nivel pide retos distintos y, a medida que se avanza,
 * aparecen cosas nuevas, el tiempo aprieta y se toleran menos errores para las 3 estrellas.
 */
public final class CatalogoNiveles {

    public static final int NIVELES = NivelAlcanzado.MAXIMO;

    private static final Map<TipoJuego, Nivel[]> NIVELES_POR_JUEGO = new EnumMap<>(TipoJuego.class);

    static {
        NIVELES_POR_JUEGO.put(TipoJuego.LIMPIEZA, limpieza());
        NIVELES_POR_JUEGO.put(TipoJuego.AGUA, agua());
        NIVELES_POR_JUEGO.put(TipoJuego.ENERGIA, energia());
        NIVELES_POR_JUEGO.put(TipoJuego.VERDE, verde());
    }

    private CatalogoNiveles() {
    }

    public static Nivel nivel(TipoJuego juego, int numero) {
        return NIVELES_POR_JUEGO.get(juego)[Math.max(1, Math.min(NIVELES, numero)) - 1];
    }

    public static Nivel[] de(TipoJuego juego) {
        return NIVELES_POR_JUEGO.get(juego).clone();
    }

    private static Reto r(String clave, int meta, String corto, String largo) {
        return new Reto(clave, meta, corto, largo);
    }

    private static Reto combo(int n) {
        return r(Reto.COMBO, n, "Combo ×" + n, "Llega a COMBO ×" + n + " (aciertos seguidos sin errores)");
    }

    private static Nivel[] limpieza() {
        TipoJuego j = TipoJuego.LIMPIEZA;
        return new Nivel[]{
            new Nivel(j, 1, "Primeros pasos", 120, 3, 1, null,
                    r("BOTELLIN", 3, "Botellín", "Recicla 3 Botellines en el tacho de plástico")),
            new Nivel(j, 2, "Papel y metal", 120, 3, 1, "¡Cuidado! Aparece el lodo tóxico: no lo toques.",
                    r("CAJETON", 3, "Cajetón", "Recicla 3 Cajetones en el tacho de papel"),
                    r("LATOX", 2, "Latox", "Recicla 2 Latox en el tacho de metal")),
            new Nivel(j, 3, "Nuevos vecinos", 130, 3, 1, "¡Nuevos monstruos! Vasito es un vaso descartable (plástico) y Periodicón un periódico (papel).",
                    r("VASITO", 3, "Vasito", "Recicla 3 Vasitos (vasos descartables)"),
                    r("PERIODICON", 3, "Periodicón", "Recicla 3 Periodicones (periódicos)")),
            new Nivel(j, 4, "Separación total", 140, 3, 1, null,
                    r("PLASTICO", 5, "Plástico", "Lleva 5 monstruos al tacho de plástico"),
                    r("PAPEL", 4, "Papel", "Lleva 4 monstruos al tacho de papel"),
                    r("METAL", 3, "Metal", "Lleva 3 monstruos al tacho de metal")),
            new Nivel(j, 5, "La lata de atún", 130, 2, 1, "¡Nuevo monstruo! Atunín es una lata de atún: también es metal.",
                    r("ATUNIN", 3, "Atunín", "Recicla 3 Atunines (latas de atún)"),
                    r("METAL", 6, "Metal", "Lleva 6 monstruos al tacho de metal")),
            new Nivel(j, 6, "Racha recicladora", 120, 2, 0, "Recicla seguido y sin errores para subir tu combo.",
                    r(Reto.ACIERTOS, 12, "Reciclar", "Recicla 12 monstruos"),
                    combo(3)),
            new Nivel(j, 7, "Estrella de poder", 120, 2, 0, null,
                    r("ESTRELLA", 1, "Estrella", "Atrapa la estrella de poder"),
                    r("PLASTICO", 8, "Plástico", "Lleva 8 monstruos al tacho de plástico")),
            new Nivel(j, 8, "Plaza invadida", 120, 2, 0, "Los monstruos aparecen más rápido y ahora hay dos lodos tóxicos.",
                    r("PAPEL", 7, "Papel", "Lleva 7 monstruos al tacho de papel"),
                    r("METAL", 6, "Metal", "Lleva 6 monstruos al tacho de metal")),
            new Nivel(j, 9, "Combo maestro", 130, 2, 0, null,
                    combo(4),
                    r(Reto.ACIERTOS, 18, "Reciclar", "Recicla 18 monstruos")),
            new Nivel(j, 10, "La gran limpieza", 160, 2, 0, "¡El desafío final de la plaza!",
                    r("PLASTICO", 10, "Plástico", "Lleva 10 monstruos al tacho de plástico"),
                    r("PAPEL", 8, "Papel", "Lleva 8 monstruos al tacho de papel"),
                    r("METAL", 7, "Metal", "Lleva 7 monstruos al tacho de metal"))
        };
    }

    private static Nivel[] agua() {
        TipoJuego j = TipoJuego.AGUA;
        return new Nivel[]{
            new Nivel(j, 1, "Primer día", 90, 3, 1, null,
                    r("ABANDONADO", 3, "Abandonado", "Cierra 3 caños que quedaron abiertos"),
                    r("CEPILLA", 2, "Cepillado", "Cierra el caño de 2 niños que se cepillan")),
            new Nivel(j, 2, "Manos limpias", 90, 3, 1, "Nuevo: niños que se enjabonan con el caño abierto.",
                    r("ENJABONA", 3, "Enjabonado", "Cierra el caño de 3 niños que se enjabonan"),
                    r("CERRADOS", 7, "Caños", "Cierra 7 caños en total")),
            new Nivel(j, 3, "Gota a gota", 100, 3, 1, "Nuevo: caños que gotean. ¡También desperdician!",
                    r("GOTEO", 3, "Goteos", "Detén 3 caños que gotean"),
                    r("CERRADOS", 9, "Caños", "Cierra 9 caños en total")),
            new Nivel(j, 4, "Espera tu turno", 100, 3, 1, "Nuevo: niños que se enjuagan. ¡A ellos NO se les cierra!",
                    r("RESPETA", 2, "Enjuagues", "Deja terminar a 2 niños que se enjuagan"),
                    r("CERRADOS", 9, "Caños", "Cierra 9 caños en total")),
            new Nivel(j, 5, "Guardián veloz", 80, 2, 1, "Hay menos tiempo: ¡reacciona rápido!",
                    r("CERRADOS", 14, "Caños", "Cierra 14 caños en total")),
            new Nivel(j, 6, "Cero goteos", 110, 2, 0, "¡Ahora pueden abrirse dos caños a la vez!",
                    r("GOTEO", 4, "Goteos", "Detén 4 caños que gotean"),
                    r("CEPILLA", 4, "Cepillado", "Cierra el caño de 4 niños que se cepillan"),
                    r("RESPETA", 3, "Enjuagues", "Deja terminar a 3 niños que se enjuagan")),
            new Nivel(j, 7, "Combo de agua", 110, 2, 0, null,
                    combo(5),
                    r("CERRADOS", 16, "Caños", "Cierra 16 caños en total")),
            new Nivel(j, 8, "Hora punta", 110, 2, 0, "¡Recreo! Todos quieren usar los lavaderos.",
                    r("CERRADOS", 20, "Caños", "Cierra 20 caños en total"),
                    r("RESPETA", 3, "Enjuagues", "Deja terminar a 3 niños que se enjuagan")),
            new Nivel(j, 9, "Ahorro máximo", 120, 2, 0, null,
                    r("LITROS", 150, "Litros", "Ahorra 150 litros de agua")),
            new Nivel(j, 10, "Gran Guardián", 140, 2, 0, "¡El desafío final de los lavaderos!",
                    r("CERRADOS", 28, "Caños", "Cierra 28 caños en total"),
                    r("RESPETA", 4, "Enjuagues", "Deja terminar a 4 niños que se enjuagan"),
                    combo(5))
        };
    }

    private static Nivel[] energia() {
        TipoJuego j = TipoJuego.ENERGIA;
        return new Nivel[]{
            new Nivel(j, 1, "Luces fuera", 75, 3, 1, null,
                    r("FOCO", 6, "Luces", "Apaga 6 luces de ambientes vacíos")),
            new Nivel(j, 2, "Pantallas negras", 90, 3, 1, null,
                    r("TV", 3, "Televisor", "Apaga 3 televisores sin nadie mirando"),
                    r("COMPUTADORA", 3, "Computadora", "Apaga 3 computadoras que nadie usa"),
                    r("FOCO", 5, "Luces", "Apaga 5 luces")),
            new Nivel(j, 3, "Cargadores vampiro", 90, 3, 1, "Nuevo: cargadores enchufados sin celular. ¡Gastan energía!",
                    r("CARGADOR", 3, "Cargadores", "Desenchufa 3 cargadores sin celular"),
                    r("APAGADOS", 14, "Apagados", "Apaga 14 cosas en total")),
            new Nivel(j, 4, "Aire fresco", 90, 3, 1, null,
                    r("VENTILADOR", 4, "Ventiladores", "Apaga 4 ventiladores de ambientes vacíos"),
                    r("APAGADOS", 16, "Apagados", "Apaga 16 cosas en total")),
            new Nivel(j, 5, "Sin ayuda", 90, 2, 1, "Desde aquí ya no hay botones rojos: ¡fíjate tú qué ambiente está vacío!",
                    r("APAGADOS", 20, "Apagados", "Apaga 20 cosas en total")),
            new Nivel(j, 6, "Combo de luz", 90, 2, 0, null,
                    combo(5),
                    r("FOCO", 10, "Luces", "Apaga 10 luces")),
            new Nivel(j, 7, "Recreo largo", 90, 2, 0, "Los alumnos se olvidan las cosas más seguido.",
                    r("APAGADOS", 26, "Apagados", "Apaga 26 cosas en total")),
            new Nivel(j, 8, "Cazavatios", 100, 2, 0, null,
                    r("CARGADOR", 5, "Cargadores", "Desenchufa 5 cargadores"),
                    r("COMPUTADORA", 6, "Computadora", "Apaga 6 computadoras"),
                    r("APAGADOS", 24, "Apagados", "Apaga 24 cosas en total")),
            new Nivel(j, 9, "Turno noche", 100, 2, 0, "Ya es de noche: cada luz olvidada se nota más.",
                    r("APAGADOS", 32, "Apagados", "Apaga 32 cosas en total"),
                    combo(5)),
            new Nivel(j, 10, "Colegio eficiente", 110, 2, 0, "¡El desafío final del colegio!",
                    r("APAGADOS", 38, "Apagados", "Apaga 38 cosas en total"),
                    r("CARGADOR", 6, "Cargadores", "Desenchufa 6 cargadores"),
                    r("FOCO", 14, "Luces", "Apaga 14 luces"))
        };
    }

    private static Nivel[] verde() {
        TipoJuego j = TipoJuego.VERDE;
        return new Nivel[]{
            new Nivel(j, 1, "Primera semilla", 100, 3, 1, null,
                    r("SIEMBRA", 4, "Siembra", "Siembra 4 semillas"),
                    r("FLORES", 3, "Flores", "Haz crecer 3 plantas hasta que florezcan")),
            new Nivel(j, 2, "Agua justa", 110, 3, 1, null,
                    r("RIEGO", 6, "Riego", "Riega 6 veces cuando la planta tenga sed"),
                    r("FLORES", 4, "Flores", "Haz florecer 4 plantas")),
            new Nivel(j, 3, "Biohuerto limpio", 110, 3, 1, "Aparece más basura: recógela para que las plantas crezcan.",
                    r("BASURA", 5, "Basura", "Recoge 5 basuras del biohuerto"),
                    r("FLORES", 4, "Flores", "Haz florecer 4 plantas")),
            new Nivel(j, 4, "Sin desperdicio", 110, 2, 0, "Ojo: regar una planta que no tiene sed es un error.",
                    r("FLORES", 6, "Flores", "Haz florecer 6 plantas")),
            new Nivel(j, 5, "Calor de Ica", 120, 2, 1, "¡Hace calor! La tierra se seca más rápido.",
                    r("FLORES", 6, "Flores", "Haz florecer 6 plantas"),
                    r("RIEGO", 8, "Riego", "Riega 8 veces cuando la planta tenga sed")),
            new Nivel(j, 6, "Racha verde", 120, 2, 0, null,
                    combo(3),
                    r("FLORES", 6, "Flores", "Haz florecer 6 plantas")),
            new Nivel(j, 7, "Viento con basura", 120, 2, 0, "El viento trae basura mucho más seguido.",
                    r("BASURA", 8, "Basura", "Recoge 8 basuras"),
                    r("FLORES", 6, "Flores", "Haz florecer 6 plantas")),
            new Nivel(j, 8, "Biohuerto grande", 130, 2, 0, null,
                    r("FLORES", 9, "Flores", "Haz florecer 9 plantas")),
            new Nivel(j, 9, "Jardinero experto", 140, 2, 0, null,
                    r("FLORES", 10, "Flores", "Haz florecer 10 plantas"),
                    r("BASURA", 6, "Basura", "Recoge 6 basuras"),
                    combo(4)),
            new Nivel(j, 10, "El gran biohuerto", 170, 2, 0, "¡El desafío final del biohuerto!",
                    r("FLORES", 14, "Flores", "Haz florecer 14 plantas"),
                    r("RIEGO", 12, "Riego", "Riega 12 veces cuando la planta tenga sed"))
        };
    }
}
