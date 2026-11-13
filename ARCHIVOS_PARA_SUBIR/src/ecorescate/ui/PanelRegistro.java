package ecorescate.ui;

import ecorescate.modelo.Estudiante;
import ecorescate.modelo.TipoJuego;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.geom.RoundRectangle2D;

/** Pantalla inicial donde el alumno escribe sus datos (RN-01). */
public class PanelRegistro extends JPanel {

    public interface Acciones {
        void ingresar(Estudiante estudiante);
    }

    private final JTextField nombre = new JTextField(20);
    private final JComboBox<String> grado = new JComboBox<>(new String[]{"1.°", "2.°", "3.°", "4.°", "5.°", "6.°"});
    private final JComboBox<String> seccion = new JComboBox<>(new String[]{"A", "B", "C", "D", "E", "F"});
    private final JLabel error = new JLabel(" ");

    public PanelRegistro(Acciones acciones) {
        super(new GridBagLayout());
        setBackground(Tema.FONDO);

        JPanel tarjeta = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Tema.suavizar(g);
                g.setColor(new Color(20, 60, 50, 14));
                g.fill(new RoundRectangle2D.Double(0, 4, getWidth(), getHeight() - 4, 26, 26));
                g.setColor(Color.WHITE);
                g.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight() - 4, 24, 24));
                g.dispose();
            }
        };
        tarjeta.setOpaque(false);
        tarjeta.setBorder(new EmptyBorder(34, 46, 38, 46));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);

        tarjeta.add(new FilaLogos(), c);

        c.insets = new Insets(18, 0, 0, 0);
        tarjeta.add(etiqueta("Operación EcoRescate", Font.BOLD, 36f, Tema.TEXTO, true), c);
        c.insets = new Insets(4, 0, 22, 0);
        tarjeta.add(etiqueta("Escribe tus datos para guardar tu progreso", Font.PLAIN, 18f, Tema.TEXTO_SUAVE, true), c);

        c.insets = new Insets(0, 0, 6, 0);
        tarjeta.add(etiqueta("Nombre y apellido", Font.BOLD, 16f, Tema.TEXTO, false), c);
        estilizar(nombre);
        c.insets = new Insets(0, 0, 16, 0);
        tarjeta.add(nombre, c);

        c.gridwidth = 1;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 6, 12);
        c.gridy = 6;
        c.gridx = 0;
        tarjeta.add(etiqueta("Grado", Font.BOLD, 16f, Tema.TEXTO, false), c);
        c.gridx = 1;
        c.insets = new Insets(0, 12, 6, 0);
        tarjeta.add(etiqueta("Sección", Font.BOLD, 16f, Tema.TEXTO, false), c);
        c.gridy = 7;
        c.gridx = 0;
        c.insets = new Insets(0, 0, 0, 12);
        estilizar(grado);
        grado.setSelectedIndex(4);
        tarjeta.add(grado, c);
        c.gridx = 1;
        c.insets = new Insets(0, 12, 0, 0);
        estilizar(seccion);
        tarjeta.add(seccion, c);

        c.gridx = 0;
        c.gridy = 8;
        c.gridwidth = 2;
        c.insets = new Insets(10, 0, 6, 0);
        error.setForeground(Tema.ROJO);
        error.setFont(Tema.fuente(Font.PLAIN, 15f));
        tarjeta.add(error, c);

        BotonEco comenzar = new BotonEco("¡Comenzar la misión!", Tema.PRINCIPAL, true);
        comenzar.setFont(Tema.fuente(Font.BOLD, 19f));
        comenzar.setPreferredSize(new Dimension(360, 50));
        c.gridy = 9;
        c.insets = new Insets(4, 0, 0, 0);
        tarjeta.add(comenzar, c);

        Runnable ingresar = () -> {
            String texto = nombre.getText().trim();
            if (texto.length() < 2) {
                error.setText("Escribe tu nombre para guardar tus puntos.");
                nombre.requestFocusInWindow();
                return;
            }
            error.setText(" ");
            acciones.ingresar(new Estudiante(texto, grado.getSelectedIndex() + 1, (String) seccion.getSelectedItem()));
        };
        comenzar.addActionListener(e -> ingresar.run());
        nombre.addActionListener(e -> ingresar.run());

        add(tarjeta);
    }

    /** Limpia el nombre para que entre otro alumno. */
    public void prepararNuevoIngreso() {
        nombre.setText("");
        error.setText(" ");
        SwingUtilities.invokeLater(nombre::requestFocusInWindow);
    }

    private static JLabel etiqueta(String texto, int estilo, float tamano, Color color, boolean centrada) {
        JLabel l = new JLabel(texto, centrada ? JLabel.CENTER : JLabel.LEFT);
        l.setFont(Tema.fuente(estilo, tamano));
        l.setForeground(color);
        return l;
    }

    private static void estilizar(JComponent campo) {
        campo.setFont(Tema.fuente(Font.PLAIN, 18f));
        campo.setPreferredSize(new Dimension(campo.getPreferredSize().width, 42));
        if (campo instanceof JTextField) {
            campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Tema.BORDE.darker(), 1, true),
                    new EmptyBorder(4, 12, 4, 12)));
        }
    }

    /** Los cuatro logos de los minijuegos en fila. */
    private static class FilaLogos extends JComponent {
        FilaLogos() {
            setPreferredSize(new Dimension(300, 64));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            TipoJuego[] tipos = TipoJuego.values();
            double d = 58, sep = 16;
            double inicio = getWidth() / 2.0 - (tipos.length * d + (tipos.length - 1) * sep) / 2 + d / 2;
            for (int i = 0; i < tipos.length; i++) {
                Iconos.logo(g, tipos[i], inicio + i * (d + sep), getHeight() / 2.0, d);
            }
            g.dispose();
        }
    }
}
