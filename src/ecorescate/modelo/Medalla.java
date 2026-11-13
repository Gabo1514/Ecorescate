package ecorescate.modelo;

/** Logros coleccionables. Las condiciones se revisan en {@link Progreso}. */
public enum Medalla {
    PRIMERA_MISION("¡Primer nivel!", "Supera tu primer nivel.", 0x23786A),
    EXPLORADOR("Explorador", "Juega los 4 minijuegos.", 0x7C4DFF),
    TRES_ESTRELLAS("¡Perfecto!", "Gana 3 estrellas en un nivel.", 0xFFB300),
    COMBO("Combo máximo", "Llega a COMBO ×5 en un nivel.", 0xF2A93B),
    PERFECTO("Sin errores", "Supera un nivel sin equivocarte ni una vez.", 0x2E9455),
    SABIO("Sabelotodo", "Responde bien 5 preguntas rápidas.", 0x3C8DC5),
    RECICLADOR("Reciclador estrella", "Supera los 10 niveles de Limpieza Monstruosa.", 0x23786A),
    GUARDIAN("Guardián del agua", "Supera los 10 niveles de Guardián del Agua.", 0x2E80A8),
    CAZAVATIOS("Cazavatios", "Supera los 10 niveles de Apaga y Ahorra.", 0xDFA855),
    MANO_VERDE("Mano verde", "Supera los 10 niveles de Sembradores del Verde.", 0x4E9A45),
    CIEN_LITROS("100 litros", "Ahorra 100 litros de agua en total.", 0x2479A8),
    CONSTANTE("Constante", "Juega 20 niveles (ganados o no).", 0xC0453F),
    COLECCIONISTA("Coleccionista", "Junta 60 estrellas en total.", 0xE0A800),
    ECO_HEROE("Eco-héroe", "Supera los 40 niveles de la suite.", 0xFF7043);

    private final String nombre;
    private final String descripcion;
    private final int color;

    Medalla(String nombre, String descripcion, int color) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.color = color;
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getColor() { return color; }
}
