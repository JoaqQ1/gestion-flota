package com.flota.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * FlowLayout que pasa los componentes a la linea siguiente cuando no entran,
 * y calcula bien la altura para que la fila crezca. Basado en la idea de Rob Camick.
 */
public class WrapLayout extends FlowLayout {

    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return calcular(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension min = calcular(target, false);
        min.width -= getHgap() + 1;
        return min;
    }

    private Dimension calcular(Container target, boolean preferido) {
        synchronized (target.getTreeLock()) {
            int anchoDisponible = target.getSize().width;
            Container contenedor = target;
            while (anchoDisponible == 0 && contenedor.getParent() != null) {
                contenedor = contenedor.getParent();
                anchoDisponible = contenedor.getSize().width;
            }
            if (anchoDisponible == 0) {
                anchoDisponible = Integer.MAX_VALUE;
            }
            Insets insets = target.getInsets();
            int maximo = anchoDisponible - (insets.left + insets.right + getHgap() * 2);

            Dimension total = new Dimension(0, 0);
            int anchoFila = 0;
            int altoFila = 0;
            for (Component c : target.getComponents()) {
                if (!c.isVisible()) {
                    continue;
                }
                Dimension d = preferido ? c.getPreferredSize() : c.getMinimumSize();
                if (anchoFila + d.width > maximo && anchoFila > 0) {
                    agregarFila(total, anchoFila, altoFila);
                    anchoFila = 0;
                    altoFila = 0;
                }
                if (anchoFila != 0) {
                    anchoFila += getHgap();
                }
                anchoFila += d.width;
                altoFila = Math.max(altoFila, d.height);
            }
            agregarFila(total, anchoFila, altoFila);
            total.width += insets.left + insets.right + getHgap() * 2;
            total.height += insets.top + insets.bottom + getVgap() * 2;

            // dentro de un scroll, no pedir mas ancho que el visible
            Container scroll = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
            if (scroll != null && target.isValid()) {
                total.width -= getHgap() + 1;
            }
            return total;
        }
    }

    private void agregarFila(Dimension total, int ancho, int alto) {
        total.width = Math.max(total.width, ancho);
        if (total.height > 0) {
            total.height += getVgap();
        }
        total.height += alto;
    }
}
