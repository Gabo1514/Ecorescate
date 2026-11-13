package ecorescate.modelo;

import java.util.ArrayList;
import java.util.List;

/** Lo que gana el alumno al terminar un nivel. */
public class Recompensa {

    private final int monedas;
    private final int estrellas;
    private final int estrellasNuevas;
    private final List<Medalla> medallasNuevas;

    public Recompensa(int monedas, int estrellas, int estrellasNuevas, List<Medalla> medallasNuevas) {
        this.monedas = monedas;
        this.estrellas = estrellas;
        this.estrellasNuevas = estrellasNuevas;
        this.medallasNuevas = new ArrayList<>(medallasNuevas);
    }

    public int getMonedas() { return monedas; }
    public int getEstrellas() { return estrellas; }
    /** Estrellas que mejoró respecto a su mejor intento en ese nivel. */
    public int getEstrellasNuevas() { return estrellasNuevas; }
    public List<Medalla> getMedallasNuevas() { return new ArrayList<>(medallasNuevas); }
}
