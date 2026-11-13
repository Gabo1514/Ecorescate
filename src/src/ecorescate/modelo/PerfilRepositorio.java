package ecorescate.modelo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/** Guarda cada perfil en datos/perfiles/&lt;alumno&gt;.properties. */
public class PerfilRepositorio {

    private final File carpeta;

    public PerfilRepositorio(File carpeta) {
        this.carpeta = carpeta;
    }

    private File archivo(Estudiante e) {
        String base = e.getNombre() + "_" + e.getGrado() + "_" + e.getSeccion();
        String limpio = Normalizer.normalize(base, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_");
        return new File(carpeta, limpio + ".properties");
    }

    public Perfil cargar(Estudiante e) {
        Perfil p = new Perfil(e);
        File f = archivo(e);
        if (!f.exists()) return p;
        Properties props = new Properties();
        try (Reader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
            props.load(r);
        } catch (IOException ex) {
            System.err.println("No se pudo leer el perfil: " + ex.getMessage());
            return p;
        }
        try {
            p.cargarMonedas(Integer.parseInt(props.getProperty("monedas", "0").trim()));
        } catch (NumberFormatException ignorado) {
            // archivo editado a mano: se empieza en 0
        }
        for (String m : lista(props.getProperty("medallas"))) {
            try {
                p.otorgar(Medalla.valueOf(m));
            } catch (IllegalArgumentException ignorado) {
                // medalla que ya no existe
            }
        }
        for (String a : lista(props.getProperty("comprados"))) {
            try {
                p.cargarComprado(Accesorio.valueOf(a));
            } catch (IllegalArgumentException ignorado) {
                // accesorio que ya no existe
            }
        }
        for (TipoJuego t : TipoJuego.values()) {
            int[] destino = p.estrellasInternas(t);
            List<String> valores = lista(props.getProperty("estrellas." + t.name()));
            for (int i = 0; i < valores.size() && i < destino.length; i++) {
                try {
                    destino[i] = Math.max(0, Math.min(3, Integer.parseInt(valores.get(i))));
                } catch (NumberFormatException ignorado) {
                    // valor dañado: ese nivel queda en 0
                }
            }
        }
        try {
            p.equipar(Accesorio.valueOf(props.getProperty("accesorio", "NINGUNO")));
            p.setPolo(Integer.parseInt(props.getProperty("polo", "0").trim()));
        } catch (IllegalArgumentException ignorado) {
            // valores dañados: se dejan los de por defecto
        }
        return p;
    }

    public void guardar(Perfil p) throws IOException {
        if (!carpeta.exists() && !carpeta.mkdirs()) throw new IOException("No se pudo crear " + carpeta);
        Properties props = new Properties();
        props.setProperty("alumno", p.getEstudiante().getNombre());
        props.setProperty("aula", p.getEstudiante().getAula());
        props.setProperty("monedas", String.valueOf(p.getMonedas()));
        props.setProperty("medallas", unir(p.medallasInternas()));
        props.setProperty("comprados", unir(p.comprados()));
        props.setProperty("accesorio", p.getAccesorio().name());
        props.setProperty("polo", String.valueOf(p.getPolo()));
        for (TipoJuego t : TipoJuego.values()) {
            StringBuilder sb = new StringBuilder();
            for (int e : p.estrellasInternas(t)) {
                if (sb.length() > 0) sb.append(',');
                sb.append(e);
            }
            props.setProperty("estrellas." + t.name(), sb.toString());
        }
        try (Writer w = new OutputStreamWriter(new FileOutputStream(archivo(p.getEstudiante())), StandardCharsets.UTF_8)) {
            props.store(w, "Perfil de Operación EcoRescate");
        }
    }

    private static List<String> lista(String texto) {
        List<String> r = new ArrayList<>();
        if (texto == null) return r;
        for (String s : texto.split(",")) if (!s.trim().isEmpty()) r.add(s.trim());
        return r;
    }

    private static String unir(Iterable<? extends Enum<?>> valores) {
        StringBuilder sb = new StringBuilder();
        for (Enum<?> v : valores) {
            if (sb.length() > 0) sb.append(',');
            sb.append(v.name());
        }
        return sb.toString();
    }
}
