package com.flota.ui;

import com.flota.model.Documento;
import com.flota.service.AlertaVencimientoService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainFrame extends JFrame {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final AlertaVencimientoService alertas;

    public MainFrame(AlertaVencimientoService alertas) {
        super("Gestion de Flota");
        this.alertas = alertas;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        var tabs = new JTabbedPane();
        tabs.addTab("Camiones", placeholder("ABM de camiones"));
        tabs.addTab("Camioneros", placeholder("ABM de camioneros"));
        tabs.addTab("Documentos", placeholder("Carga de documentos y vencimientos"));
        add(tabs, BorderLayout.CENTER);
    }

    private JComponent placeholder(String texto) {
        var label = new JLabel(texto + " (pendiente)", SwingConstants.CENTER);
        label.putClientProperty("FlatLaf.styleClass", "h3");
        return label;
    }

    /** Se llama al abrir la aplicacion: muestra los documentos vencidos o por vencer. */
    public void mostrarAlertasAlIniciar() {
        List<Documento> docs = alertas.documentosPorVencer();
        if (docs.isEmpty()) return;

        var hoy = LocalDate.now();
        var sb = new StringBuilder("<html><b>Documentos vencidos o por vencer en los proximos ")
                .append(alertas.getDiasAnticipacion()).append(" dias:</b><br><br>");
        for (var d : docs) {
            long dias = d.diasParaVencer(hoy);
            String estado = d.vencido(hoy) ? "<font color='red'>VENCIDO</font>" : "vence en " + dias + " dias";
            sb.append("&bull; ").append(d.tipo()).append(" - ").append(d.descripcionTitular())
              .append(" (").append(d.vencimiento().format(FECHA)).append(") ").append(estado).append("<br>");
        }
        sb.append("</html>");
        JOptionPane.showMessageDialog(this, sb.toString(), "Avisos de vencimiento", JOptionPane.WARNING_MESSAGE);
    }
}
