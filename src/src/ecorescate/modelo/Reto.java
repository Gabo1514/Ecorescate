package ecorescate.modelo;

/**
 * Objetivo de un nivel, por ejemplo "Recicla 4 Botellines".
 * La clave identifica qué acción del juego lo hace avanzar (BOTELLIN, CERRADOS, FOCO, FLORES, COMBO...).
 */
public class Reto {

    /** Clave especial: se cumple al llegar a ese combo. */
    public static final String COMBO = "COMBO";
    /** Clave especial: cuenta cualquier acierto. */
    public static final String ACIERTOS = "ACIERTOS";

    private final String clave;
    private final int meta;
    private final String corto;
    private final String largo;

    public Reto(String clave, int meta, String corto, String largo) {
        this.clave = clave;
        this.meta = meta;
        this.corto = corto;
        this.largo = largo;
    }

    public String getClave() { return clave; }
    public int getMeta() { return meta; }
    /** Texto breve para la barra del juego, por ejemplo "Botellín". */
    public String getCorto() { return corto; }
    /** Texto completo para la pantalla del nivel, por ejemplo "Recicla 4 Botellines (plástico)". */
    public String getLargo() { return largo; }
}
