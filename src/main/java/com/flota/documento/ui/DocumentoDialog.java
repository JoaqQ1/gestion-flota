package com.flota.documento.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.flota.camion.model.Camion;
import com.flota.camion.service.CamionService;
import com.flota.camionero.model.Camionero;
import com.flota.camionero.service.CamioneroService;
import com.flota.documento.model.Documento;
import com.flota.documento.model.TipoTitular;
import com.flota.documento.service.ArchivoAdjuntoService;
import com.flota.documento.service.DocumentoService;
import com.flota.ui.DialogoEdicion;
import com.flota.ui.Estilos;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

/** Ventana para cargar o modificar un documento con su vencimiento y archivo adjunto. */
public class DocumentoDialog extends DialogoEdicion {

    /** Opcion de los combos de titular: guarda el id y muestra un texto. */
    private record Opcion(Long id, String texto) {
        @Override
        public String toString() {
            return texto;
        }
    }

    private final DocumentoService service;
    private final ArchivoAdjuntoService adjuntos;
    private final Documento original;
    private final List<Opcion> camiones;
    private final List<Opcion> camioneros;

    private final JRadioButton rbCamion = new JRadioButton("Un camión");
    private final JRadioButton rbCamionero = new JRadioButton("Un camionero");
    private final JComboBox<Opcion> titular = new JComboBox<>();
    private final JComboBox<String> tipo = new JComboBox<>();
    private final DatePicker vencimiento;
    private final JTextArea observaciones = new JTextArea(3, 26);
    private final JLabel lblArchivo = new JLabel();
    private final JButton btnVer = new JButton("Abrir");
    private final JButton btnQuitar = Estilos.botonPeligro("Quitar");

    private Path archivoNuevo;
    private boolean quitarArchivo;

    public DocumentoDialog(Component padre, DocumentoService service, ArchivoAdjuntoService adjuntos,
            CamionService camionService, CamioneroService camioneroService, Documento documento) {
        super(padre, documento == null ? "Agregar documento" : "Editar documento",
                documento == null ? "Cargá el documento y su fecha de vencimiento."
                        : "Modificá los datos y tocá Guardar.");
        this.service = service;
        this.adjuntos = adjuntos;
        this.original = documento;
        this.camiones = camionService.findAll().stream()
                .map((Camion c) -> new Opcion(c.id(), c.descripcion())).toList();
        this.camioneros = camioneroService.findAll().stream()
                .map((Camionero c) -> new Opcion(c.id(), c.apellido() + ", " + c.nombre()
                        + (c.dni() == null ? "" : " (DNI " + c.dni() + ")"))).toList();

        // De quien es
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbCamion);
        grupo.add(rbCamionero);
        JPanel panelTipoTitular = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelTipoTitular.add(rbCamion);
        panelTipoTitular.add(javax.swing.Box.createHorizontalStrut(16));
        panelTipoTitular.add(rbCamionero);
        rbCamion.addActionListener(e -> cambiarTipoTitular(TipoTitular.CAMION));
        rbCamionero.addActionListener(e -> cambiarTipoTitular(TipoTitular.CAMIONERO));

        tipo.setEditable(true);

        DatePickerSettings ajustes = new DatePickerSettings(Estilos.ES_AR);
        ajustes.setFormatForDatesCommonEra("dd/MM/yyyy");
        ajustes.setFormatForDatesBeforeCommonEra("dd/MM/uuuu");
        ajustes.setTranslationToday("Hoy");
        ajustes.setTranslationClear("Borrar");
        vencimiento = new DatePicker(ajustes);
        vencimiento.getComponentToggleCalendarButton().setText("Calendario");

        observaciones.setLineWrap(true);
        observaciones.setWrapStyleWord(true);
        JScrollPane scrollObs = new JScrollPane(observaciones);

        formulario.agregar("tipoTitular", "¿De quién es el documento?", panelTipoTitular);
        formulario.agregar("titular", "Camión o camionero *", titular);
        formulario.agregar("tipo", "Tipo de documento *", tipo, "Elegí de la lista o escribí otro.");
        formulario.agregar("vencimiento", "Fecha de vencimiento *", vencimiento,
                "Escribila (dd/mm/aaaa) o elegila en el calendario.");
        formulario.agregar("archivo", "Archivo escaneado (opcional)", panelArchivo(), "PDF o foto del documento.");
        formulario.agregar("observaciones", "Observaciones", scrollObs);

        // Cargar datos
        TipoTitular tt = documento == null ? (camiones.isEmpty() && !camioneros.isEmpty()
                ? TipoTitular.CAMIONERO : TipoTitular.CAMION) : documento.tipoTitular();
        (tt == TipoTitular.CAMION ? rbCamion : rbCamionero).setSelected(true);
        cambiarTipoTitular(tt);
        if (documento != null) {
            seleccionarTitular(documento.titularId());
            tipo.setSelectedItem(documento.tipo());
            vencimiento.setDate(documento.vencimiento());
            observaciones.setText(documento.observaciones());
        } else {
            tipo.setSelectedItem("");
        }
        actualizarArchivo();
    }

    private void cambiarTipoTitular(TipoTitular tt) {
        List<Opcion> opciones = tt == TipoTitular.CAMION ? camiones : camioneros;
        titular.setModel(new DefaultComboBoxModel<>(opciones.toArray(new Opcion[0])));
        Object tipoActual = tipo.getSelectedItem();
        tipo.setModel(new DefaultComboBoxModel<>(tt.tiposSugeridos().toArray(new String[0])));
        tipo.setSelectedItem(tipoActual == null ? "" : tipoActual);
    }

    private void seleccionarTitular(Long id) {
        for (int i = 0; i < titular.getItemCount(); i++) {
            if (Objects.equals(titular.getItemAt(i).id(), id)) {
                titular.setSelectedIndex(i);
                return;
            }
        }
    }

    private JPanel panelArchivo() {
        JButton btnAdjuntar = new JButton("Adjuntar archivo…");
        btnAdjuntar.addActionListener(e -> elegirArchivo());
        btnVer.addActionListener(e -> abrirArchivo());
        btnQuitar.addActionListener(e -> {
            archivoNuevo = null;
            quitarArchivo = true;
            actualizarArchivo();
        });
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        botones.add(btnAdjuntar);
        botones.add(javax.swing.Box.createHorizontalStrut(8));
        botones.add(btnVer);
        botones.add(javax.swing.Box.createHorizontalStrut(8));
        botones.add(btnQuitar);
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.add(lblArchivo, BorderLayout.NORTH);
        p.add(botones, BorderLayout.CENTER);
        return p;
    }

    private void elegirArchivo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Elegí el archivo del documento");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF e imágenes", "pdf", "jpg", "jpeg", "png"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            archivoNuevo = chooser.getSelectedFile().toPath();
            quitarArchivo = false;
            actualizarArchivo();
        }
    }

    private Path archivoActual() {
        if (archivoNuevo != null) {
            return archivoNuevo;
        }
        if (original != null && original.archivo() != null && !quitarArchivo) {
            return adjuntos.resolver(original.archivo());
        }
        return null;
    }

    private void actualizarArchivo() {
        Path actual = archivoActual();
        if (actual == null) {
            lblArchivo.setText("Sin archivo adjunto");
            lblArchivo.setForeground(Estilos.GRIS_TEXTO);
        } else {
            String nombre = archivoNuevo != null ? actual.getFileName().toString()
                    : ArchivoAdjuntoService.nombreVisible(original.archivo());
            lblArchivo.setText("Archivo: " + nombre);
            lblArchivo.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        }
        btnVer.setEnabled(actual != null);
        btnQuitar.setEnabled(actual != null);
    }

    private void abrirArchivo() {
        Path actual = archivoActual();
        if (actual == null) {
            return;
        }
        try {
            File f = actual.toFile();
            if (!Files.exists(actual)) {
                Estilos.info(this, "Archivo no encontrado", "No se encontró el archivo en " + actual);
                return;
            }
            Desktop.getDesktop().open(f);
        } catch (Exception e) {
            Estilos.error(this, e);
        }
    }

    @Override
    protected void guardar() {
        TipoTitular tt = rbCamion.isSelected() ? TipoTitular.CAMION : TipoTitular.CAMIONERO;
        Opcion opcion = (Opcion) titular.getSelectedItem();
        Object tipoElegido = tipo.getEditor().getItem();
        LocalDate fecha = vencimiento.getDate();
        if (fecha == null && !vencimiento.getText().isBlank()) {
            throw com.flota.common.ValidacionException.deCampo("vencimiento",
                    "La fecha no es válida. Escribila como dd/mm/aaaa, por ejemplo 31/12/2026.");
        }
        Documento d = new Documento(original == null ? null : original.id(),
                tipoElegido == null ? null : tipoElegido.toString(), tt,
                opcion == null ? null : opcion.id(), null, fecha,
                original == null ? null : original.archivo(), observaciones.getText());
        service.guardar(d, archivoNuevo, quitarArchivo);
    }
}
