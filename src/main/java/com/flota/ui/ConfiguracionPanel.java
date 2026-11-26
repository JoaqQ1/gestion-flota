package com.flota.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.flota.alerta.AlertaVencimientoService;
import com.flota.camion.service.CamionService;
import com.flota.camion.ui.CamionesPanel;
import com.flota.camionero.service.CamioneroService;
import com.flota.camionero.ui.CamionerosPanel;
import com.flota.common.ValidacionException;
import com.flota.config.AppConfig;
import com.flota.config.CarpetaDatosService;
import com.flota.config.ConfiguracionService;
import com.flota.config.PreferenciasLocales;
import com.flota.config.TamanoLetra;
import com.flota.documento.service.DocumentoService;
import com.flota.documento.ui.DocumentosPanel;
import com.formdev.flatlaf.FlatClientProperties;

/** Pantalla de configuracion: avisos, tamaño de letra, descargas y carpeta de datos. */
public class ConfiguracionPanel extends JPanel implements Recargable {
    private final ConfiguracionService configuracion;
    private final CarpetaDatosService carpetas = new CarpetaDatosService();
    private final SpinnerNumberModel modeloDias;
    private final JLabel lblEjemplo = Estilos.subtitulo("");
    private final JLabel lblRuta = new JLabel();

    public ConfiguracionPanel(ConfiguracionService configuracion, AlertaVencimientoService alertas,
            CamionService camionService, CamioneroService camioneroService, DocumentoService documentoService) {
        super(new BorderLayout());
        this.configuracion = configuracion;

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(Estilos.margen(20, 24));

        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.add(Estilos.titulo("Configuración"));
        encabezado.add(Estilos.subtitulo("Ajustes de la aplicación y descarga de listados."));
        agregar(contenido, encabezado);

        // ---- Avisos
        modeloDias = new SpinnerNumberModel(configuracion.getDiasAviso(), ConfiguracionService.DIAS_MINIMO,
                ConfiguracionService.DIAS_MAXIMO, 1);
        JSpinner spDias = new JSpinner(modeloDias);
        ((JSpinner.DefaultEditor) spDias.getEditor()).getTextField().setColumns(4);
        spDias.addChangeListener(e -> actualizarEjemplo());
        JButton guardar = Estilos.botonPrimario("Guardar cambios");
        guardar.addActionListener(e -> guardarDias());

        JPanel filaDias = fila();
        filaDias.add(new JLabel("Avisar cuando falten"));
        filaDias.add(spDias);
        filaDias.add(new JLabel("días o menos para el vencimiento."));
        JPanel filaGuardar = fila();
        filaGuardar.add(guardar);
        agregar(contenido, tarjeta("Avisos de vencimiento",
                "Los documentos que vencen dentro de este plazo aparecen como \"Por vencer\" (en amarillo) "
                        + "y se avisan al abrir la aplicación.",
                filaDias, lblEjemplo, filaGuardar));

        // ---- Tamaño de letra
        JPanel filaLetra = fila();
        ButtonGroup grupoLetra = new ButtonGroup();
        TamanoLetra actual = PreferenciasLocales.tamanoLetra();
        for (TamanoLetra t : TamanoLetra.values()) {
            JRadioButton rb = new JRadioButton(t.etiqueta());
            rb.setSelected(t == actual);
            rb.addActionListener(e -> cambiarLetra(t));
            grupoLetra.add(rb);
            filaLetra.add(rb);
            filaLetra.add(Box.createHorizontalStrut(12));
        }
        agregar(contenido, tarjeta("Tamaño de letra",
                "Agrandá los textos si cuesta leerlos. El cambio se aplica al instante en toda la aplicación.",
                filaLetra));

        // ---- Descargas
        JPanel descargas = new JPanel(new GridBagLayout());
        descargas.setOpaque(false);
        filaDescarga(descargas, 0, "Avisos de vencimiento", "Vencidos y por vencer",
                InicioPanel.columnasAvisos(alertas::getDiasAnticipacion), alertas::documentosPorVencer);
        filaDescarga(descargas, 1, "Camiones", "Todos los camiones cargados",
                CamionesPanel.columnas(), camionService::findAll);
        filaDescarga(descargas, 2, "Camioneros", "Todos los choferes cargados",
                CamionerosPanel.columnas(), camioneroService::findAll);
        filaDescarga(descargas, 3, "Documentos", "Todos los documentos con su estado",
                DocumentosPanel.columnas(configuracion::getDiasAviso), documentoService::findAll);
        agregar(contenido, tarjeta("Descargar listados",
                "Guardá un listado completo en PDF (para imprimir o enviar) o en Excel (para trabajarlo).",
                descargas));

        // ---- Carpeta de datos
        lblRuta.putClientProperty(FlatClientProperties.STYLE, "font: $monospaced.font");
        lblRuta.setMinimumSize(new java.awt.Dimension(50, lblRuta.getPreferredSize().height));
        JButton abrirCarpeta = new JButton("Abrir carpeta");
        abrirCarpeta.addActionListener(e -> abrirCarpetaDatos());
        JButton cambiarCarpeta = new JButton("Cambiar carpeta…");
        cambiarCarpeta.addActionListener(e -> cambiarCarpeta());
        JPanel filaBotonesCarpeta = fila();
        filaBotonesCarpeta.add(abrirCarpeta);
        filaBotonesCarpeta.add(cambiarCarpeta);
        agregar(contenido, tarjeta("Dónde se guardan los datos",
                "La base de datos y los archivos adjuntos están en esta carpeta. Podés elegir otra, por "
                        + "ejemplo una carpeta compartida o de respaldo. Para hacer una copia de seguridad, "
                        + "copiá la carpeta completa.",
                lblRuta, filaBotonesCarpeta));

        JPanel envoltorio = new PanelAnchoVisible();
        envoltorio.add(contenido, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(envoltorio);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
        actualizarEjemplo();
        actualizarRuta();
    }

    @Override
    public void recargar() {
        modeloDias.setValue(configuracion.getDiasAviso());
        actualizarEjemplo();
        actualizarRuta();
    }

    // ------------------------------------------------------------- avisos

    private void guardarDias() {
        try {
            int dias = modeloDias.getNumber().intValue();
            configuracion.setDiasAviso(dias);
            Estilos.info(this, "Cambios guardados",
                    "Listo. Ahora se va a avisar con " + dias + (dias == 1 ? " día" : " días") + " de anticipación.");
        } catch (RuntimeException e) {
            Estilos.error(this, e);
        }
    }

    private void actualizarEjemplo() {
        int dias = modeloDias.getNumber().intValue();
        String ejemplo = java.time.LocalDate.now().plusDays(dias).format(Estilos.FECHA);
        lblEjemplo.setText("Hoy se avisaría de todo lo que vence hasta el " + ejemplo + ".");
    }

    // ------------------------------------------------------------- letra

    private void cambiarLetra(TamanoLetra tamano) {
        PreferenciasLocales.setTamanoLetra(tamano);
        Estilos.cambiarTamano(tamano);
    }

    // ------------------------------------------------------------- carpeta

    private void actualizarRuta() {
        String ruta = AppConfig.carpetaDatos().toString();
        lblRuta.setText(ruta);
        lblRuta.setToolTipText(ruta);
    }

    private void abrirCarpetaDatos() {
        try {
            Desktop.getDesktop().open(AppConfig.carpetaDatos().toFile());
        } catch (IOException | RuntimeException e) {
            Estilos.error(this, e instanceof Exception ex ? ex : new IllegalStateException(e));
        }
    }

    private void cambiarCarpeta() {
        JFileChooser chooser = new JFileChooser(AppConfig.carpetaDatos().getParent().toFile());
        chooser.setDialogTitle("Elegí dónde guardar los datos");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setApproveButtonText("Usar esta carpeta");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Path destino = carpetas.carpetaFinal(chooser.getSelectedFile().toPath());
            carpetas.validar(destino);

            if (carpetas.tieneDatos(destino)) {
                String msg = "<html>La carpeta <b>" + destino + "</b> ya tiene datos de la aplicación.<br><br>"
                        + "¿Querés empezar a usar esos datos?<br>"
                        + "<font color='#6B7280'>Los datos de la carpeta actual no se borran.</font></html>";
                if (!Estilos.confirmar(this, "Usar datos existentes", msg, "Sí, usar esos datos")) {
                    return;
                }
            } else {
                String msg = "<html>Los datos se van a guardar en:<br><b>" + destino + "</b><br><br>"
                        + "¿Qué querés hacer con los datos que ya cargaste?<br>"
                        + "<font color='#6B7280'>La carpeta actual queda como está, no se borra nada.</font></html>";
                Object[] opciones = { "Llevar mis datos", "Empezar sin datos", "Cancelar" };
                int r = JOptionPane.showOptionDialog(this, msg, "Cambiar carpeta de datos",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
                if (r == 0) {
                    carpetas.copiarDatosActuales(destino);
                } else if (r != 1) {
                    return;
                }
            }

            carpetas.usar(destino);
            configuracion.recargar();
            recargar();
            Estilos.info(this, "Carpeta cambiada", "Listo. Desde ahora los datos se guardan en " + destino);
        } catch (ValidacionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "No se puede usar esa carpeta",
                    JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException e) {
            Estilos.error(this, e);
        }
    }

    // ------------------------------------------------------------- armado

    private <T> void filaDescarga(JPanel panel, int fila, String nombre, String detalle,
            List<Columna<T>> columnas, Supplier<List<T>> datos) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(fila == 0 ? 0 : 6, 0, 6, 12);
        c.anchor = GridBagConstraints.WEST;

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel lblNombre = new JLabel(nombre);
        lblNombre.putClientProperty(FlatClientProperties.STYLE, "font: bold");
        textos.add(lblNombre);
        textos.add(Estilos.subtitulo(detalle));
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(textos, c);

        JButton pdf = new JButton("PDF");
        JButton excel = new JButton("Excel");
        pdf.addActionListener(e -> BotonExportar.exportar(this, nombre, columnas, datos.get(), false));
        excel.addActionListener(e -> BotonExportar.exportar(this, nombre, columnas, datos.get(), true));
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        panel.add(pdf, c);
        c.gridx = 2;
        c.insets = new Insets(c.insets.top, 0, c.insets.bottom, 0);
        panel.add(excel, c);
    }

    /** Fila de controles que pasa a la linea siguiente si no entra. */
    private static JPanel fila() {
        JPanel p = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 4));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, -8, 0, 0));
        return p;
    }

    /** Recuadro blanco con titulo, descripcion y contenido. */
    private static JPanel tarjeta(String titulo, String descripcion, JComponent... contenido) {
        JPanel t = new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
                setBackground(Color.WHITE);
            }
        };
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.setMaximumSize(new java.awt.Dimension(920, Integer.MAX_VALUE));
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.putClientProperty(FlatClientProperties.STYLE_CLASS, "h3");
        alinear(lblTitulo);
        t.add(lblTitulo);
        t.add(Box.createVerticalStrut(4));
        JComponent desc = Estilos.parrafo(descripcion);
        alinear(desc);
        t.add(desc);
        for (JComponent c : contenido) {
            t.add(Box.createVerticalStrut(10));
            alinear(c);
            t.add(c);
        }
        return t;
    }

    /** Panel que se ajusta al ancho visible, para que nada quede fuera de la pantalla. */
    private static class PanelAnchoVisible extends JPanel implements javax.swing.Scrollable {
        PanelAnchoVisible() {
            super(new BorderLayout());
        }

        public java.awt.Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        public int getScrollableUnitIncrement(java.awt.Rectangle r, int o, int d) {
            return 16;
        }

        public int getScrollableBlockIncrement(java.awt.Rectangle r, int o, int d) {
            return r.height;
        }

        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static void alinear(JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
    }

    private static void agregar(JPanel contenedor, JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        if (contenedor.getComponentCount() > 0) {
            contenedor.add(Box.createVerticalStrut(16));
        }
        contenedor.add(c);
    }
}
