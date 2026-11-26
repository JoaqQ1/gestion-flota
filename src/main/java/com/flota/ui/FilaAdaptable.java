package com.flota.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * Dos grupos en una fila: uno a la izquierda y otro a la derecha.
 * Si no entran en el ancho disponible, el de la derecha pasa abajo (alineado a la izquierda).
 */
public class FilaAdaptable implements LayoutManager {
    private final int separacion;

    public FilaAdaptable(int separacion) {
        this.separacion = separacion;
    }

    private boolean entraEnUnaFila(Container c, Component izq, Component der) {
        Insets in = c.getInsets();
        int ancho = c.getWidth() - in.left - in.right;
        return ancho <= 0 || izq.getPreferredSize().width + separacion + der.getPreferredSize().width <= ancho;
    }

    @Override
    public void layoutContainer(Container c) {
        if (c.getComponentCount() < 2) {
            return;
        }
        Component izq = c.getComponent(0);
        Component der = c.getComponent(1);
        Insets in = c.getInsets();
        int ancho = c.getWidth() - in.left - in.right;
        Dimension di = izq.getPreferredSize();
        Dimension dd = der.getPreferredSize();
        if (entraEnUnaFila(c, izq, der)) {
            int alto = Math.max(di.height, dd.height);
            izq.setBounds(in.left, in.top + (alto - di.height) / 2, ancho - dd.width - separacion, di.height);
            der.setBounds(in.left + ancho - dd.width, in.top + (alto - dd.height) / 2, dd.width, dd.height);
        } else {
            izq.setBounds(in.left, in.top, Math.min(di.width, ancho), di.height);
            der.setBounds(in.left, in.top + di.height + separacion / 2, Math.min(dd.width, ancho), dd.height);
        }
    }

    @Override
    public Dimension preferredLayoutSize(Container c) {
        if (c.getComponentCount() < 2) {
            return new Dimension();
        }
        Component izq = c.getComponent(0);
        Component der = c.getComponent(1);
        Insets in = c.getInsets();
        Dimension di = izq.getPreferredSize();
        Dimension dd = der.getPreferredSize();
        if (entraEnUnaFila(c, izq, der)) {
            return new Dimension(di.width + separacion + dd.width + in.left + in.right,
                    Math.max(di.height, dd.height) + in.top + in.bottom);
        }
        return new Dimension(Math.max(di.width, dd.width) + in.left + in.right,
                di.height + separacion / 2 + dd.height + in.top + in.bottom);
    }

    @Override
    public Dimension minimumLayoutSize(Container c) {
        Dimension d = preferredLayoutSize(c);
        d.width = 0;
        return d;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }
}
