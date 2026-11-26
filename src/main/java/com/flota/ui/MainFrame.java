package com.flota.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalDate;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import com.flota.alerta.AlertaVencimientoService;
import com.flota.camion.service.CamionService;
import com.flota.camion.ui.CamionesPanel;
import com.flota.camionero.service.CamioneroService;
import com.flota.camionero.ui.CamionerosPanel;
import com.flota.config.ConfiguracionService;
import com.flota.documento.model.Documento;
import com.flota.documento.model.EstadoVencimiento;
import com.flota.documento.repository.DocumentoRepository;
import com.flota.documento.service.ArchivoAdjuntoService;
import com.flota.documento.service.DocumentoService;
import com.flota.documento.ui.DocumentosPanel;
import com.formdev.flatlaf.FlatClientProperties;

public class MainFrame extends JFrame {
    private static final int TAB_INICIO = 0;
    private static final int TAB_DOCUMENTOS = 3;

    private final AlertaVencimientoService alertas;
    private final JTabbedPane tabs = new JTabbedPane();
    private final InicioPanel inicio;
    private final DocumentosPanel documentosPanel;

    public MainFrame(AlertaVencimientoService alertas, CamionService camionService,
            CamioneroService camioneroService, DocumentoService documentoService,
            DocumentoRepository documentoRepository, ArchivoAdjuntoService adjuntos,
            ConfiguracionService configuracion) {
        super("Gestión de Flota");
        this.alertas = alertas;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 540));
        // tamaño inicial: 1200x780 o lo que entre en la pantalla
        java.awt.Rectangle pantalla = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        setSize(Math.min(1200, pantalla.width - 40), Math.min(780, pantalla.height - 40));
        setLocationRelativeTo(null);

        documentosPanel = new DocumentosPanel(documentoService, adjuntos, camionService, camioneroService,
                configuracion);
        inicio = new InicioPanel(alertas, documentoService, this::verDocumentosPorEstado, this::editarDocumento);
        CamionesPanel camiones = new CamionesPanel(camionService, documentoRepository, this::buscarDocumentos);
        CamionerosPanel camioneros = new CamionerosPanel(camioneroService, documentoRepository,
                this::buscarDocumentos);

        tabs.addTab("Inicio", inicio);
        tabs.addTab("Camiones", camiones);
        tabs.addTab("Camioneros", camioneros);
        tabs.addTab("Documentos", documentosPanel);
        tabs.addTab("Configuración", new ConfiguracionPanel(configuracion, alertas, camionService,
                camioneroService, documentoService));
        tabs.setToolTipTextAt(0, "Resumen de vencimientos");

        JLabel marca = new JLabel("Gestión de Flota");
        marca.putClientProperty(FlatClientProperties.STYLE, "font: bold +2; border: 0,16,0,24");
        tabs.putClientProperty(FlatClientProperties.TABBED_PANE_LEADING_COMPONENT, marca);
        tabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_CARD);
        // con letra grande o ventana angosta, las pestañas se desplazan en vez de partirse en dos filas
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.addChangeListener(e -> recargarActual());
        add(tabs, BorderLayout.CENTER);
        recargarActual();
    }

    private void recargarActual() {
        if (tabs.getSelectedComponent() instanceof Recargable r) {
            r.recargar();
        }
    }

    private void buscarDocumentos(String texto) {
        documentosPanel.filtrarPorEstado(null);
        documentosPanel.buscar(texto);
        tabs.setSelectedIndex(TAB_DOCUMENTOS);
    }

    private void verDocumentosPorEstado(EstadoVencimiento estado) {
        documentosPanel.buscar("");
        documentosPanel.filtrarPorEstado(estado);
        tabs.setSelectedIndex(TAB_DOCUMENTOS);
    }

    private void editarDocumento(Documento d) {
        try {
            if (documentosPanel.dialogo(d).abrir()) {
                inicio.recargar();
            }
        } catch (RuntimeException e) {
            Estilos.error(this, e);
        }
    }

    /** Se llama al abrir la aplicacion: avisa si hay documentos vencidos o por vencer. */
    public void mostrarAlertasAlIniciar() {
        List<Documento> docs = alertas.documentosPorVencer();
        if (docs.isEmpty()) {
            return;
        }
        LocalDate hoy = LocalDate.now();
        long vencidos = docs.stream().filter(d -> d.vencido(hoy)).count();
        long porVencer = docs.size() - vencidos;

        StringBuilder sb = new StringBuilder("<html><div style='width:380px'>");
        if (vencidos > 0) {
            sb.append("<font color='#B91C1C'><b>").append(vencidos)
                    .append(vencidos == 1 ? " documento vencido" : " documentos vencidos").append("</b></font><br>");
        }
        if (porVencer > 0) {
            sb.append("<font color='#A16207'><b>").append(porVencer)
                    .append(porVencer == 1 ? " documento vence" : " documentos vencen")
                    .append(" en los próximos ").append(alertas.getDiasAnticipacion()).append(" días</b></font><br>");
        }
        sb.append("<br>");
        int mostrados = 0;
        for (Documento d : docs) {
            if (mostrados++ == 8) {
                sb.append("… y ").append(docs.size() - 8).append(" más.<br>");
                break;
            }
            sb.append("• <b>").append(d.tipo()).append("</b> de ").append(d.descripcionTitular())
                    .append(": ").append(d.situacion(hoy).toLowerCase()).append("<br>");
        }
        sb.append("<br>Podés ver el detalle en la pantalla de Inicio.</div></html>");
        tabs.setSelectedIndex(TAB_INICIO);
        JOptionPane.showMessageDialog(this, sb.toString(), "Atención: vencimientos", JOptionPane.WARNING_MESSAGE);
    }
}
