package ecorescate.modelo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Guarda cada partida en un archivo CSV (separado por ";") que se abre directo en Excel
 * y sirve como "Ficha de registro de desempeño" para el análisis en SPSS.
 */
public class RegistroPartidas {

    private static final String CABECERA = "fecha;alumno;grado;seccion;codigo_juego;minijuego;puntaje;"
            + "nivel;completado;aciertos;errores;precision_pct;duracion_seg;impacto;unidad_impacto;pregunta_correcta;estrellas";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final File archivo;

    public RegistroPartidas(File archivo) {
        this.archivo = archivo;
    }

    public File getArchivo() {
        return archivo;
    }

    public synchronized void guardar(SesionJuego s) throws IOException {
        File carpeta = archivo.getAbsoluteFile().getParentFile();
        if (carpeta != null && !carpeta.exists() && !carpeta.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta " + carpeta);
        }
        boolean nuevo = !archivo.exists() || archivo.length() == 0;
        try (Writer w = new OutputStreamWriter(new FileOutputStream(archivo, true), StandardCharsets.UTF_8)) {
            if (nuevo) {
                w.write('﻿'); // BOM para que Excel reconozca las tildes
                w.write(CABECERA);
                w.write("\r\n");
            }
            Estudiante e = s.getEstudiante();
            PuntajeObtenido p = s.getPuntaje();
            String[] campos = {
                s.getFecha().format(FORMATO_FECHA),
                e.getNombre(),
                String.valueOf(e.getGrado()),
                e.getSeccion(),
                s.getJuego().name(),
                s.getJuego().getNombre(),
                String.valueOf(p.getPuntos()),
                String.valueOf(s.getNivel().getNivel()),
                s.getNivel().isCompletado() ? "SI" : "NO",
                String.valueOf(p.getAciertos()),
                String.valueOf(p.getErrores()),
                String.valueOf(p.getPrecision()),
                String.valueOf(s.getDuracionSegundos()),
                String.format(Locale.ROOT, "%.2f", s.getImpacto()).replace('.', ','),
                s.getJuego().getUnidadImpacto(),
                s.getRespuestaCorrecta() == null ? "" : (s.getRespuestaCorrecta() ? "SI" : "NO"),
                String.valueOf(s.getEstrellas())
            };
            w.write(String.join(";", campos));
            w.write("\r\n");
        }
    }

    /** Devuelve las partidas de un estudiante (lista vacía si aún no hay registro). */
    public synchronized List<SesionJuego> cargar(Estudiante estudiante) {
        List<SesionJuego> lista = new ArrayList<>();
        for (SesionJuego s : cargarTodas()) {
            if (s.getEstudiante().esMismo(estudiante)) lista.add(s);
        }
        return lista;
    }

    /** Todas las partidas guardadas (para el ranking del salón). */
    public synchronized List<SesionJuego> cargarTodas() {
        List<SesionJuego> lista = new ArrayList<>();
        if (!archivo.exists()) return lista;
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            String linea;
            boolean primera = true;
            while ((linea = r.readLine()) != null) {
                if (primera) {
                    primera = false;
                    continue; // cabecera
                }
                SesionJuego s = leerLinea(linea);
                if (s != null) lista.add(s);
            }
        } catch (IOException ex) {
            System.err.println("No se pudo leer el registro: " + ex.getMessage());
        }
        return lista;
    }

    private static SesionJuego leerLinea(String linea) {
        String[] c = linea.split(";", -1);
        if (c.length < 15) return null;
        try {
            Estudiante e = new Estudiante(c[1], Integer.parseInt(c[2].trim()), c[3]);
            TipoJuego juego = TipoJuego.valueOf(c[4].trim());
            PuntajeObtenido p = new PuntajeObtenido(Integer.parseInt(c[6].trim()),
                    Integer.parseInt(c[9].trim()), Integer.parseInt(c[10].trim()));
            NivelAlcanzado n = new NivelAlcanzado(Integer.parseInt(c[7].trim()), "SI".equals(c[8].trim()));
            Boolean respuesta = null; // columna agregada después: los registros antiguos no la tienen
            if (c.length > 15 && !c[15].trim().isEmpty()) respuesta = "SI".equals(c[15].trim());
            int estrellas = c.length > 16 && !c[16].trim().isEmpty() ? Integer.parseInt(c[16].trim()) : 0;
            return new SesionJuego(e, juego, LocalDateTime.parse(c[0].trim(), FORMATO_FECHA),
                    Long.parseLong(c[12].trim()), p, n, Double.parseDouble(c[13].trim().replace(',', '.')),
                    respuesta, estrellas);
        } catch (RuntimeException ex) {
            return null; // línea dañada o editada a mano: se ignora
        }
    }
}
