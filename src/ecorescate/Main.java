package ecorescate;

import ecorescate.modelo.PerfilRepositorio;
import ecorescate.modelo.RegistroPartidas;
import ecorescate.ui.Sonido;
import ecorescate.ui.VentanaPrincipal;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.File;
import java.net.URISyntaxException;

/**
 * Operación EcoRescate: suite de minijuegos educativos para el cuidado del ambiente.
 * Punto de entrada de la aplicación.
 */
public class Main {

    private static File carpetaBase;

    public static void main(String[] args) {
        System.out.println("[EcoRescate] 1. Main iniciado");
        try {
            System.setProperty("sun.java2d.uiScale.enabled", "true");
        } catch (Throwable ignorado) {
        }

        File datos;
        try {
            datos = VentanaPrincipal.carpetaDatos();
            System.out.println("[EcoRescate] 2. Carpeta datos: " + datos);
        } catch (Throwable t) {
            datos = new File("datos");
            System.out.println("[EcoRescate] 2. Fallback datos: " + t);
        }

        final File datosFinal = datos;
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("[EcoRescate] 3. Creando VentanaPrincipal...");
                RegistroPartidas registro = new RegistroPartidas(new File(datosFinal, "partidas.csv"));
                PerfilRepositorio perfiles = new PerfilRepositorio(new File(datosFinal, "perfiles"));
                VentanaPrincipal v = new VentanaPrincipal(registro, perfiles);
                System.out.println("[EcoRescate] 4. Llamando setVisible(true)...");
                v.setVisible(true);
                System.out.println("[EcoRescate] 5. setVisible(true) completado!");
            } catch (Throwable t) {
                System.err.println("[EcoRescate] ERROR en invokeLater: " + t);
                t.printStackTrace();
            }
        });

        // Iniciar audio en segundo plano tras mostrar la ventana principal
        Thread audioThread = new Thread(() -> {
            try {
                Thread.sleep(800);
                Sonido.get().iniciar(new File(datosFinal, "config.properties"));
                System.out.println("[EcoRescate] 6. Audio iniciado en segundo plano");
            } catch (Throwable ignorado) {
            }
        }, "audio-lazy-init");
        audioThread.setDaemon(true);
        audioThread.start();
        System.out.println("[EcoRescate] 7. Main termino (EDT corriendo)");
    }

    /** Devuelve la carpeta donde reside EcoRescate.jar (o user.dir si se ejecuta desde el IDE o WebAssembly). */
    public static synchronized File carpetaPrograma() {
        if (carpetaBase != null) return carpetaBase;
        try {
            java.net.URL url = Main.class.getProtectionDomain().getCodeSource().getLocation();
            if (url != null) {
                java.net.URI uri = url.toURI();
                if ("file".equalsIgnoreCase(uri.getScheme())) {
                    File ubicacion = new File(uri);
                    File dir = ubicacion.isFile() ? ubicacion.getParentFile() : ubicacion;
                    // Si se corre desde bin/ o target/, subir a la carpeta del proyecto si allí está src/
                    if (dir != null && "bin".equalsIgnoreCase(dir.getName()) && dir.getParentFile() != null) {
                        dir = dir.getParentFile();
                    }
                    if (dir != null && dir.exists() && dir.canWrite()) {
                        carpetaBase = dir;
                        return carpetaBase;
                    }
                }
            }
        } catch (Throwable ignorado) {
            // usar carpeta actual como respaldo
        }
        try {
            File actual = new File(System.getProperty("user.dir", "."));
            if (actual.exists() && actual.canWrite()) {
                carpetaBase = actual;
                return carpetaBase;
            }
        } catch (Throwable ignorado) {
        }
        try {
            carpetaBase = new File(System.getProperty("user.home", "."), "EcoRescate");
        } catch (Throwable ignorado) {
            carpetaBase = new File(".");
        }
        return carpetaBase;
    }
}
