package ecorescate.ui;

import ecorescate.modelo.Estudiante;
import ecorescate.modelo.ModuloImpactoSocial;
import ecorescate.modelo.SesionJuego;
import ecorescate.modelo.TipoJuego;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Módulo de Impacto Social: muestra lo logrado convertido a métricas reales (RF-06). */
public class PanelImpacto extends JPanel {

    public interface Acciones {
        void volverAlMenu();
    }

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ModuloImpactoSocial modulo;
    private final JLabel titulo = new JLabel();
    private final JLabel nota = new JLabel();
    private final Map<TipoJuego, TarjetaImpacto> tarjetas = new EnumMap<>(TipoJuego.class);
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"Fecha", "Minijuego", "Puntaje", "Nivel", "Aciertos", "Precisión", "Tiempo", "Impacto"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    public PanelImpacto(ModuloImpactoSocial modulo, Acciones acciones) {
        super(new BorderLayout());
        this.modulo = modulo;
        setBackground(Tema.FONDO);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE), new EmptyBorder(10, 10, 10, 10)));
        BotonEco volver = new BotonEco("← Volver al menú", Tema.PRINCIPAL, false);
        volver.addActionListener(e -> acciones.volverAlMenu());
        barra.add(volver);
        add(barra, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 18));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(new EmptyBorder(24, 48, 20, 48));

        JPanel cabecera = new JPanel(new BorderLayout(0, 18));
        cabecera.setOpaque(false);
        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        titulo.setFont(Tema.fuente(Font.BOLD, 36f));
        titulo.setForeground(Tema.TEXTO);
        JLabel sub = new JLabel("Lo que lograste en los minijuegos, convertido a acciones del mundo real.");
        sub.setFont(Tema.fuente(Font.PLAIN, 19f));
        sub.setForeground(Tema.TEXTO_SUAVE);
        textos.add(titulo);
        textos.add(sub);
        cabecera.add(textos, BorderLayout.NORTH);

        JPanel fila = new JPanel(new GridLayout(1, 4, 18, 0));
        fila.setOpaque(false);
        for (TipoJuego t : TipoJuego.values()) {
            TarjetaImpacto tj = new TarjetaImpacto(t);
            tarjetas.put(t, tj);
            fila.add(tj);
        }
        cabecera.add(fila, BorderLayout.CENTER);
        cuerpo.add(cabecera, BorderLayout.NORTH);

        JTable tabla = new JTable(modeloTabla);
        tabla.setFont(Tema.fuente(Font.PLAIN, 16f));
        tabla.setRowHeight(30);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(Tema.BORDE);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionBackground(Tema.alfa(Tema.PRINCIPAL, 40));
        tabla.setSelectionForeground(Tema.TEXTO);
        tabla.getTableHeader().setFont(Tema.fuente(Font.BOLD, 15f));
        tabla.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 2; i < 7; i++) tabla.getColumnModel().getColumn(i).setCellRenderer(centrado);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(160);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(190);
        tabla.getColumnModel().getColumn(7).setPreferredWidth(220);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
        scroll.getViewport().setBackground(Color.WHITE);
        cuerpo.add(scroll, BorderLayout.CENTER);

        nota.setFont(Tema.fuente(Font.PLAIN, 14f));
        nota.setForeground(Tema.ETIQUETA);
        cuerpo.add(nota, BorderLayout.SOUTH);
        add(cuerpo, BorderLayout.CENTER);
    }

    public void actualizar(Estudiante estudiante, List<SesionJuego> sesiones, File archivo) {
        titulo.setText("Tu Impacto Social, " + estudiante.getPrimerNombre());
        for (TarjetaImpacto t : tarjetas.values()) t.actualizar(sesiones);

        modeloTabla.setRowCount(0);
        List<SesionJuego> recientes = new ArrayList<>(sesiones);
        recientes.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));
        for (SesionJuego s : recientes) {
            long seg = s.getDuracionSegundos();
            modeloTabla.addRow(new Object[]{
                s.getFecha().format(FORMATO),
                s.getJuego().getNombre(),
                s.getPuntaje().getPuntos(),
                s.getNivel().getNivel() + "/10",
                s.getPuntaje().getAciertos(),
                s.getPuntaje().getPrecision() + " %",
                String.format("%d:%02d", seg / 60, seg % 60),
                modulo.formatear(s.getImpacto()) + " " + s.getJuego().getUnidadImpacto()
            });
        }
        nota.setText("<html>Partidas guardadas en: " + archivo.getAbsolutePath()
                + " (se abre en Excel).<br>Conversión (RN-02): 1 caño cerrado = 6 L · 1 residuo bien clasificado = 0,05 kg"
                + " · 1 equipo apagado = 0,05 kWh · 1 planta crecida = 0,5 m² · cada nivel superado suma un bono.</html>");
    }

    /** Resumen de un minijuego: total acumulado y su equivalencia. */
    private class TarjetaImpacto extends JComponent {

        private final TipoJuego tipo;
        private double total;
        private int partidas;
        private int mejorNivel;

        TarjetaImpacto(TipoJuego tipo) {
            this.tipo = tipo;
            setPreferredSize(new Dimension(220, 232));
        }

        void actualizar(List<SesionJuego> sesiones) {
            total = modulo.total(tipo, sesiones);
            partidas = 0;
            mejorNivel = 0;
            for (SesionJuego s : sesiones) {
                if (s.getJuego() != tipo) continue;
                partidas++;
                mejorNivel = Math.max(mejorNivel, s.getNivel().getNivel());
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            int w = getWidth(), h = getHeight();
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 18, 18));
            Color color = new Color(tipo.getColor());
            g.setColor(color);
            g.fill(new RoundRectangle2D.Double(0, 0, w - 1, 6, 6, 6));

            Iconos.logo(g, tipo, 46, 50, 52);
            g.setColor(new Color(tipo.getColorTexto()));
            g.setFont(Tema.fuente(Font.BOLD, 18f));
            Tema.parrafo(g, tipo.getNombre(), 82, 46, w - 96, 20, false);

            g.setColor(Tema.TEXTO);
            g.setFont(Tema.fuente(Font.BOLD, 38f));
            String valor = modulo.formatear(total);
            g.drawString(valor, 22, 126);
            g.setColor(Tema.TEXTO_SUAVE);
            g.setFont(Tema.fuente(Font.PLAIN, 15f));
            Tema.parrafo(g, tipo.getUnidadImpacto(), 22, 148, w - 40, 18, false);

            g.setColor(Tema.TEXTO);
            g.setFont(Tema.fuente(Font.PLAIN, 14.5f));
            String eq = partidas == 0 ? "Juega esta misión para sumar impacto." : modulo.equivalencia(tipo, total);
            Tema.parrafo(g, eq, 22, 176, w - 40, 17, false);

            g.setColor(Tema.ETIQUETA);
            g.setFont(Tema.fuente(Font.PLAIN, 13.5f));
            g.drawString(partidas + (partidas == 1 ? " partida" : " partidas")
                    + (partidas > 0 ? " · mejor nivel " + mejorNivel + "/10" : ""), 22, h - 14);
            g.dispose();
        }
    }
}
