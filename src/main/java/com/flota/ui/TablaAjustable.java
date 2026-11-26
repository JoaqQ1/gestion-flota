package com.flota.ui;

import java.awt.Container;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.table.TableModel;

/**
 * Tabla que agranda sus columnas junto con la letra y que, si la ventana es angosta,
 * muestra una barra de desplazamiento horizontal en vez de aplastar las columnas.
 */
public class TablaAjustable extends JTable {
    /** Tamaño de letra para el que estan pensados los anchos base. */
    private static final float LETRA_BASE = 14f;
    private int[] anchosBase;

    public TablaAjustable(TableModel modelo, int[] anchosBase) {
        super(modelo);
        this.anchosBase = anchosBase;
        aplicarAnchos();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (anchosBase != null) {
            aplicarAnchos();
        }
    }

    private void aplicarAnchos() {
        Font f = UIManager.getFont("defaultFont");
        float escala = f == null ? 1f : Math.max(1f, f.getSize() / LETRA_BASE);
        for (int i = 0; i < anchosBase.length && i < getColumnModel().getColumnCount(); i++) {
            getColumnModel().getColumn(i).setPreferredWidth(Math.round(anchosBase[i] * escala));
        }
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        Container padre = getParent();
        return padre instanceof JViewport && getPreferredSize().width <= padre.getWidth();
    }
}
