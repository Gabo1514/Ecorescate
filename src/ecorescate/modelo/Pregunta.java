package ecorescate.modelo;

/** Pregunta rápida al final de cada partida para reforzar el concepto aprendido. */
public class Pregunta {

    private final String texto;
    private final String[] opciones;
    private final int correcta;
    private final String explicacion;

    public Pregunta(String texto, String[] opciones, int correcta, String explicacion) {
        this.texto = texto;
        this.opciones = opciones.clone();
        this.correcta = correcta;
        this.explicacion = explicacion;
    }

    public String getTexto() { return texto; }
    public String[] getOpciones() { return opciones.clone(); }
    public int getCorrecta() { return correcta; }
    public String getExplicacion() { return explicacion; }

    public boolean esCorrecta(int opcion) {
        return opcion == correcta;
    }
}
