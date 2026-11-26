package com.flota.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.formdev.flatlaf.FlatClientProperties;
import com.github.lgooddatepicker.components.DatePicker;

/**
 * Formulario en una columna: etiqueta arriba, campo, texto de ayuda y mensaje de error debajo.
 * Los errores se muestran junto al campo correspondiente (marcado en rojo).
 */
public class Formulario extends JPanel {
    private final Map<String, JComponent> campos = new LinkedHashMap<>();
    private final Map<String, JLabel> errores = new LinkedHashMap<>();
    private final Map<String, JLabel> ayudas = new LinkedHashMap<>();
    private int fila = 0;

    public Formulario() {
        super(new GridBagLayout());
    }

    public void agregar(String clave, String etiqueta, JComponent campo) {
        agregar(clave, etiqueta, campo, null);
    }

    public void agregar(String clave, String etiqueta, JComponent campo, String ayuda) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(Estilos.negrita(lbl.getFont()));
        lbl.setLabelFor(campo);
        c.gridy = fila++;
        c.insets = new Insets(fila == 1 ? 0 : 12, 0, 4, 0);
        add(lbl, c);

        c.gridy = fila++;
        c.insets = new Insets(0, 0, 0, 0);
        add(campo, c);

        JLabel lblAyuda = new JLabel(ayuda == null ? "" : ayuda);
        lblAyuda.setForeground(Estilos.GRIS_TEXTO);
        lblAyuda.putClientProperty(FlatClientProperties.STYLE_CLASS, "small");
        lblAyuda.setVisible(ayuda != null);
        c.gridy = fila++;
        c.insets = new Insets(3, 2, 0, 0);
        add(lblAyuda, c);

        JLabel lblError = new JLabel();
        lblError.setForeground(Estilos.ROJO);
        lblError.setVisible(false);
        c.gridy = fila++;
        add(lblError, c);

        campos.put(clave, campo);
        errores.put(clave, lblError);
        ayudas.put(clave, lblAyuda);
    }

    public void mostrarErrores(Map<String, String> mensajes) {
        limpiarErrores();
        JComponent primero = null;
        for (Map.Entry<String, String> e : mensajes.entrySet()) {
            JLabel lbl = errores.get(e.getKey());
            if (lbl == null) {
                continue;
            }
            lbl.setText(e.getValue());
            lbl.setVisible(true);
            JComponent campo = destino(campos.get(e.getKey()));
            campo.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
            if (primero == null) {
                primero = campo;
            }
        }
        if (primero != null) {
            primero.requestFocusInWindow();
        }
        revalidate();
        repaint();
    }

    public void limpiarErrores() {
        for (String clave : campos.keySet()) {
            errores.get(clave).setVisible(false);
            destino(campos.get(clave)).putClientProperty(FlatClientProperties.OUTLINE, null);
        }
    }

    /** Si hay errores en campos que no estan en el formulario, devuelve su texto. */
    public String erroresSinCampo(Map<String, String> mensajes) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : mensajes.entrySet()) {
            if (!errores.containsKey(e.getKey())) {
                sb.append(e.getValue()).append('\n');
            }
        }
        return sb.toString().strip();
    }

    private static JComponent destino(JComponent campo) {
        if (campo instanceof DatePicker dp) {
            return dp.getComponentDateTextField();
        }
        return campo;
    }
}
