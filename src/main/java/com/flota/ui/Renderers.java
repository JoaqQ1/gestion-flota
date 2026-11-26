package com.flota.ui;

import java.awt.Component;
import java.time.LocalDate;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.flota.documento.model.EstadoVencimiento;

/** Formas de mostrar valores especiales en las tablas. */
public final class Renderers {
    private Renderers() {
    }

    public static void registrar(JTable tabla) {
        tabla.setDefaultRenderer(LocalDate.class, new FechaRenderer());
        tabla.setDefaultRenderer(EstadoVencimiento.class, new EstadoRenderer());
        DefaultTableCellRenderer conMargen = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int f, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foco, f, c);
                l.setBorder(Estilos.margen(0, 10));
                return l;
            }
        };
        tabla.setDefaultRenderer(String.class, conMargen);
        tabla.setDefaultRenderer(Object.class, conMargen);
        tabla.setDefaultRenderer(Integer.class, conMargen);
    }

    static class FechaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int f, int c) {
            Object texto = v instanceof LocalDate d ? d.format(Estilos.FECHA) : v;
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, texto, sel, foco, f, c);
            l.setBorder(Estilos.margen(0, 10));
            return l;
        }
    }

    /** Muestra el estado como una etiqueta de color: rojo, amarillo o verde. */
    static class EstadoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int f, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foco, f, c);
            l.setBorder(Estilos.margen(0, 10));
            if (v instanceof EstadoVencimiento e) {
                l.setText("● " + e.etiqueta());
                l.setFont(Estilos.negrita(l.getFont()));
                if (!sel) {
                    l.setForeground(Estilos.color(e));
                    l.setBackground(Estilos.fondo(e));
                }
            }
            return l;
        }
    }
}
