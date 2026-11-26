package com.flota.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.flota.common.ValidacionException;

/**
 * Ventana modal para cargar o editar un registro, con botones Guardar y Cancelar.
 * Si los datos no son validos, marca los campos con error sin cerrar la ventana.
 */
public abstract class DialogoEdicion extends JDialog {
    protected final Formulario formulario = new Formulario();
    private boolean guardado = false;

    protected DialogoEdicion(Component padre, String titulo, String subtitulo) {
        super(SwingUtilities.getWindowAncestor(padre), titulo, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.add(Estilos.titulo(titulo));
        if (subtitulo != null) {
            encabezado.add(Estilos.subtitulo(subtitulo));
        }
        encabezado.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));

        JScrollPane scroll = new JScrollPane(formulario);
        scroll.setBorder(BorderFactory.createEmptyBorder(4, 24, 8, 24));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JButton cancelar = new JButton("Cancelar");
        JButton guardar = Estilos.botonPrimario("Guardar");
        cancelar.addActionListener(e -> dispose());
        guardar.addActionListener(e -> intentarGuardar());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(0xE5E7EB)),
                BorderFactory.createEmptyBorder(12, 24, 14, 24)));
        botones.add(cancelar);
        botones.add(guardar);

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(scroll, BorderLayout.CENTER);
        contenido.add(botones, BorderLayout.SOUTH);
        setContentPane(contenido);
        getRootPane().setDefaultButton(guardar);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /** Lee los campos, valida y guarda. Lanza ValidacionException si algo esta mal. */
    protected abstract void guardar();

    private void intentarGuardar() {
        try {
            formulario.limpiarErrores();
            guardar();
            guardado = true;
            dispose();
        } catch (ValidacionException e) {
            formulario.mostrarErrores(e.getErrores());
            ajustarTamano();
            String otros = formulario.erroresSinCampo(e.getErrores());
            if (!otros.isEmpty()) {
                JOptionPane.showMessageDialog(this, otros, "Revisá los datos", JOptionPane.WARNING_MESSAGE);
            }
        } catch (RuntimeException e) {
            Estilos.error(this, e);
        }
    }

    /** Muestra la ventana y devuelve true si el usuario guardo. */
    public boolean abrir() {
        ajustarTamano();
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return guardado;
    }

    private void ajustarTamano() {
        int ancho = Math.max(getWidth(), 0);
        pack();
        java.awt.Rectangle pantalla = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        int maxAlto = pantalla.height - 60;
        int maxAncho = pantalla.width - 60;
        setSize(Math.min(Math.max(Math.max(getWidth(), ancho), 500), maxAncho), Math.min(getHeight(), maxAlto));
    }
}
