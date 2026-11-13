package ecorescate.modelo;

import java.util.Locale;

/** Alumno registrado. Todo progreso guardado se asocia a un estudiante (RN-01). */
public class Estudiante {

    private final String nombre;
    private final int grado;
    private final String seccion;

    public Estudiante(String nombre, int grado, String seccion) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del estudiante es obligatorio");
        }
        this.nombre = limpiar(nombre);
        this.grado = grado;
        this.seccion = limpiar(seccion).toUpperCase(Locale.ROOT);
    }

    /** Quita caracteres que romperían el archivo de registro. */
    private static String limpiar(String texto) {
        return texto == null ? "" : texto.replace(';', ' ').replace('\n', ' ').replace('\r', ' ').trim();
    }

    public String getNombre() { return nombre; }
    public int getGrado() { return grado; }
    public String getSeccion() { return seccion; }

    /** Ejemplo: "5.° A". */
    public String getAula() {
        return grado + ".° " + seccion;
    }

    public String getPrimerNombre() {
        String[] partes = nombre.split("\\s+");
        return partes[0];
    }

    /** Dos estudiantes son el mismo si coinciden nombre, grado y sección (sin importar mayúsculas). */
    public boolean esMismo(Estudiante otro) {
        return otro != null
                && nombre.equalsIgnoreCase(otro.nombre)
                && grado == otro.grado
                && seccion.equalsIgnoreCase(otro.seccion);
    }

    @Override
    public String toString() {
        return nombre + " (" + getAula() + ")";
    }
}
