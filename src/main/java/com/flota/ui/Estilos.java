package com.flota.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import com.flota.common.ValidacionException;
import com.flota.config.TamanoLetra;
import com.flota.documento.model.EstadoVencimiento;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.fonts.inter.FlatInterFont;
import com.formdev.flatlaf.icons.FlatSearchIcon;

/** Tema visual y componentes comunes para que todas las pantallas se vean igual. */
public final class Estilos {
    public static final Locale ES_AR = Locale.of("es", "AR");
    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FECHA_LARGA = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", ES_AR);

    public static final Color AZUL = new Color(0x2563EB);
    public static final Color ROJO = new Color(0xB91C1C);
    public static final Color ROJO_FONDO = new Color(0xFEE2E2);
    public static final Color AMARILLO = new Color(0xA16207);
    public static final Color AMARILLO_FONDO = new Color(0xFEF3C7);
    public static final Color VERDE = new Color(0x15803D);
    public static final Color VERDE_FONDO = new Color(0xDCFCE7);
    public static final Color GRIS_TEXTO = new Color(0x6B7280);

    private Estilos() {
    }

    public static void aplicar(TamanoLetra tamano) {
        Locale.setDefault(ES_AR);
        int extra = tamano.aumento();
        // Fuente Inter incluida en la app, igual en Mac y Windows
        FlatInterFont.installLazy();
        FlatLaf.setPreferredFontFamily(FlatInterFont.FAMILY);
        FlatLaf.setPreferredLightFontFamily(FlatInterFont.FAMILY_LIGHT);
        FlatLaf.setPreferredSemiboldFontFamily(FlatInterFont.FAMILY_SEMIBOLD);
        FlatLaf.setGlobalExtraDefaults(Map.of(
                "@accentColor", "#2563EB",
                "defaultFont", "+" + extra));
        FlatLightLaf.setup();
        // alturas que crecen junto con la letra
        UIManager.put("Table.rowHeight", 30 + extra * 2);
        UIManager.put("TableHeader.height", 32 + extra * 2);
        UIManager.put("TabbedPane.tabHeight", 42 + extra * 2);
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.intercellSpacing", new java.awt.Dimension(0, 1));
        UIManager.put("TabbedPane.selectedBackground", Color.WHITE);
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("OptionPane.yesButtonText", "Sí");
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");
        UIManager.put("OptionPane.okButtonText", "Aceptar");
        UIManager.put("FileChooser.cancelButtonText", "Cancelar");
        UIManager.put("FileChooser.openButtonText", "Elegir");
    }

    /** Cambia el tamaño de letra con la aplicacion abierta y redibuja todas las ventanas. */
    public static void cambiarTamano(TamanoLetra tamano) {
        aplicar(tamano);
        FlatLaf.updateUI();
    }

    /** Texto de varias lineas que se acomoda al ancho disponible (los JLabel no lo hacen bien). */
    public static JTextArea parrafo(String texto) {
        JTextArea t = new JTextArea(texto) {
            @Override
            public java.awt.Dimension getPreferredSize() {
                // calcula la altura segun el ancho disponible para que el texto baje de linea
                java.awt.Container padre = getParent();
                if (padre != null) {
                    java.awt.Insets in = padre.getInsets();
                    int ancho = padre.getWidth() - in.left - in.right;
                    if (ancho > 0 && ancho != getWidth()) {
                        setSize(ancho, Short.MAX_VALUE);
                    }
                }
                return super.getPreferredSize();
            }

            @Override
            public void updateUI() {
                super.updateUI();
                // se repite al cambiar el tamaño de letra, que vuelve a aplicar el tema
                setFont(UIManager.getFont("Label.font"));
                setForeground(GRIS_TEXTO);
                setOpaque(false);
                setBorder(null);
            }
        };
        // al cambiar el ancho hay que volver a calcular cuantas lineas ocupa
        t.addComponentListener(new java.awt.event.ComponentAdapter() {
            private int anchoAnterior = -1;

            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (t.getWidth() != anchoAnterior) {
                    anchoAnterior = t.getWidth();
                    javax.swing.SwingUtilities.invokeLater(t::revalidate);
                }
            }
        });
        t.setEditable(false);
        t.setFocusable(false);
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        return t;
    }

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.putClientProperty(FlatClientProperties.STYLE_CLASS, "h2");
        return l;
    }

    public static JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(GRIS_TEXTO);
        return l;
    }

    /** Boton azul para la accion principal de la pantalla. */
    public static JButton botonPrimario(String texto) {
        JButton b = new JButton(texto);
        b.putClientProperty(FlatClientProperties.STYLE,
                "background: #2563EB; foreground: #FFFFFF; borderWidth: 0; focusWidth: 0; font: bold;"
                        + " hoverBackground: #1D4ED8; pressedBackground: #1E40AF; margin: 6,16,6,16");
        return b;
    }

    /** Boton rojo para acciones que borran datos. */
    public static JButton botonPeligro(String texto) {
        JButton b = new JButton(texto);
        b.putClientProperty(FlatClientProperties.STYLE, "foreground: #B91C1C");
        return b;
    }

    public static JTextField campoBusqueda(String placeholder) {
        JTextField t = new JTextField(24);
        t.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        t.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        t.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSearchIcon());
        return t;
    }

    public static Color color(EstadoVencimiento e) {
        return switch (e) {
            case VENCIDO -> ROJO;
            case POR_VENCER -> AMARILLO;
            case VIGENTE -> VERDE;
        };
    }

    public static Color fondo(EstadoVencimiento e) {
        return switch (e) {
            case VENCIDO -> ROJO_FONDO;
            case POR_VENCER -> AMARILLO_FONDO;
            case VIGENTE -> VERDE_FONDO;
        };
    }

    public static Font negrita(Font f) {
        return f.deriveFont(Font.BOLD);
    }

    public static javax.swing.border.Border margen(int v, int h) {
        return BorderFactory.createEmptyBorder(v, h, v, h);
    }

    /** Pregunta antes de una accion peligrosa. Devuelve true si el usuario confirma. */
    public static boolean confirmar(Component padre, String titulo, String mensaje, String textoSi) {
        Object[] opciones = { textoSi, "Cancelar" };
        int r = JOptionPane.showOptionDialog(padre, mensaje, titulo, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE, null, opciones, opciones[1]);
        return r == 0;
    }

    public static void info(Component padre, String titulo, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    /** Muestra un error entendible para el usuario y deja el detalle tecnico en la consola. */
    public static void error(Component padre, Exception e) {
        if (e instanceof ValidacionException ve) {
            JOptionPane.showMessageDialog(padre, ve.getMessage(), "Revisá los datos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        e.printStackTrace();
        JOptionPane.showMessageDialog(padre,
                "<html><b>Ocurrió un problema y no se pudo completar la acción.</b><br><br>"
                        + "Si se repite, avisale al encargado del sistema.<br>"
                        + "<font color='#6B7280'>Detalle: " + e.getMessage() + "</font></html>",
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
