package com.flota;

import com.flota.repository.Database;
import com.flota.repository.DocumentoRepository;
import com.flota.service.AlertaVencimientoService;
import com.flota.ui.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        Database.init();

        var documentos = new DocumentoRepository();
        var alertas = new AlertaVencimientoService(documentos, 30);

        SwingUtilities.invokeLater(() -> {
            var frame = new MainFrame(alertas);
            frame.setVisible(true);
            frame.mostrarAlertasAlIniciar();
        });
    }
}
