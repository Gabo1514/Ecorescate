package ecorescate.modelo;

/** Accesorios del personaje que se compran con EcoMonedas en la tienda. */
public enum Accesorio {
    NINGUNO("Sin accesorio", 0),
    GORRA("Gorra roja", 80),
    LENTES("Lentes de sol", 120),
    AUDIFONOS("Audífonos", 160),
    SOMBRERO("Sombrero explorador", 220),
    CAPA("Capa de eco-héroe", 320),
    CORONA("Corona del reciclaje", 500);

    private final String nombre;
    private final int precio;

    Accesorio(String nombre, int precio) {
        this.nombre = nombre;
        this.precio = precio;
    }

    public String getNombre() { return nombre; }
    public int getPrecio() { return precio; }
}
