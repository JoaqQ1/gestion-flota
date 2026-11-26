package com.flota;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.flota.alerta.AlertaVencimientoService;
import com.flota.camion.repository.CamionRepository;
import com.flota.camion.service.CamionService;
import com.flota.camion.service.CamionServiceImpl;
import com.flota.camionero.repository.CamioneroRepository;
import com.flota.camionero.service.CamioneroService;
import com.flota.camionero.service.CamioneroServiceImpl;
import com.flota.config.AppConfig;
import com.flota.config.ConfiguracionService;
import com.flota.config.PreferenciasLocales;
import com.flota.documento.repository.DocumentoRepository;
import com.flota.documento.service.ArchivoAdjuntoService;
import com.flota.documento.service.DocumentoService;
import com.flota.documento.service.DocumentoServiceImpl;
import com.flota.repository.Database;
import com.flota.ui.Estilos;
import com.flota.ui.MainFrame;

public class App {
    public static void main(String[] args) {
        Estilos.aplicar(PreferenciasLocales.tamanoLetra());
        try {
            Database.init();
        } catch (RuntimeException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "No se pudo abrir la base de datos en " + AppConfig.carpetaDatos() + "\n" + e.getMessage(),
                    "Error al iniciar", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        DocumentoRepository documentoRepository = new DocumentoRepository();
        ArchivoAdjuntoService adjuntos = new ArchivoAdjuntoService();
        CamionService camionService = new CamionServiceImpl(new CamionRepository(), documentoRepository, adjuntos);
        CamioneroService camioneroService = new CamioneroServiceImpl(new CamioneroRepository(),
                documentoRepository, adjuntos);
        DocumentoService documentoService = new DocumentoServiceImpl(documentoRepository, adjuntos);
        ConfiguracionService configuracion = new ConfiguracionService();
        AlertaVencimientoService alertas = new AlertaVencimientoService(documentoRepository, configuracion);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(alertas, camionService, camioneroService, documentoService,
                    documentoRepository, adjuntos, configuracion);
            frame.setVisible(true);
            frame.mostrarAlertasAlIniciar();
        });
    }
}
