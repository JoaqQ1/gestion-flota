package com.flota.documento.ui;

import java.awt.Desktop;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.function.IntSupplier;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.flota.camion.service.CamionService;
import com.flota.camionero.service.CamioneroService;
import com.flota.config.ConfiguracionService;
import com.flota.documento.model.Documento;
import com.flota.documento.model.EstadoVencimiento;
import com.flota.documento.service.ArchivoAdjuntoService;
import com.flota.documento.service.DocumentoService;
import com.flota.ui.Columna;
import com.flota.ui.Estilos;
import com.flota.ui.PanelListado;

/** Listado de todos los documentos con su estado de vencimiento. */
public class DocumentosPanel extends PanelListado<Documento> {
    private static final String TODOS = "Todos";

    private final DocumentoService service;
    private final ArchivoAdjuntoService adjuntos;
    private final CamionService camionService;
    private final CamioneroService camioneroService;
    private final ConfiguracionService configuracion;
    private final JComboBox<Object> filtroEstado = new JComboBox<>(new Object[] {
            TODOS, EstadoVencimiento.VENCIDO, EstadoVencimiento.POR_VENCER, EstadoVencimiento.VIGENTE });
    private final JButton btnAbrir = new JButton("Abrir archivo");

    public DocumentosPanel(DocumentoService service, ArchivoAdjuntoService adjuntos,
            CamionService camionService, CamioneroService camioneroService, ConfiguracionService configuracion) {
        super("Documentos", "VTV, seguros, licencias y demás papeles con vencimiento.",
                "Agregar documento", "Buscar por tipo, patente o nombre",
                "<html><center>Todavía no hay documentos cargados.<br>"
                        + "Tocá <b>Agregar documento</b> para cargar el primero.</center></html>",
                columnas(configuracion::getDiasAviso));
        this.service = service;
        this.adjuntos = adjuntos;
        this.camionService = camionService;
        this.camioneroService = camioneroService;
        this.configuracion = configuracion;

        barra.add(new JLabel("  Mostrar:"));
        barra.add(filtroEstado);
        filtroEstado.addActionListener(e -> aplicarFiltro());
        btnAbrir.setEnabled(false);
        btnAbrir.addActionListener(e -> abrirArchivo());
        barra.add(btnAbrir);
    }

    public static List<Columna<Documento>> columnas(IntSupplier diasAviso) {
        return List.of(
                new Columna<>("Estado", EstadoVencimiento.class,
                        (Documento d) -> d.estado(LocalDate.now(), diasAviso.getAsInt()), 120),
                Columna.texto("Documento", Documento::tipo, 190),
                Columna.texto("De", (Documento d) -> d.tipoTitular().etiqueta(), 100),
                Columna.texto("Camión / Camionero", Documento::descripcionTitular, 200),
                new Columna<>("Vence", LocalDate.class, Documento::vencimiento, 100),
                Columna.texto("Situación", (Documento d) -> d.situacion(LocalDate.now()), 150),
                Columna.texto("Archivo", (Documento d) -> d.archivo() == null ? "" : "Sí", 70));
    }

    /** Muestra solo los documentos con ese estado (o todos si es null). */
    public void filtrarPorEstado(EstadoVencimiento estado) {
        filtroEstado.setSelectedItem(estado == null ? TODOS : estado);
    }

    @Override
    protected boolean filtroExtra(Documento d) {
        Object elegido = filtroEstado.getSelectedItem();
        return !(elegido instanceof EstadoVencimiento e) || d.estado(LocalDate.now(), configuracion.getDiasAviso()) == e;
    }

    @Override
    protected List<Documento> cargar() {
        return service.findAll();
    }

    @Override
    protected void nuevo() {
        if (camionService.findAll().isEmpty() && camioneroService.findAll().isEmpty()) {
            Estilos.info(this, "Primero cargá un camión o camionero",
                    "Para agregar un documento primero tiene que haber al menos un camión o un camionero cargado.");
            return;
        }
        if (dialogo(null).abrir()) {
            recargar();
        }
    }

    @Override
    protected void editar(Documento d) {
        if (dialogo(d).abrir()) {
            recargar();
        }
    }

    public DocumentoDialog dialogo(Documento d) {
        return new DocumentoDialog(this, service, adjuntos, camionService, camioneroService, d);
    }

    @Override
    protected void eliminar(Documento d) {
        String mensaje = "<html>¿Seguro que querés eliminar <b>" + d.tipo() + "</b> de <b>"
                + d.descripcionTitular() + "</b>?"
                + (d.archivo() != null ? "<br>También se borrará el archivo adjunto." : "")
                + "<br><br>Esta acción no se puede deshacer.</html>";
        if (Estilos.confirmar(this, "Eliminar documento", mensaje, "Sí, eliminar")) {
            service.delete(d.id());
            recargar();
        }
    }

    @Override
    protected void alCambiarSeleccion(boolean haySeleccion) {
        Documento d = haySeleccion ? seleccionado() : null;
        btnAbrir.setEnabled(d != null && d.archivo() != null);
    }

    private void abrirArchivo() {
        Documento d = seleccionado();
        if (d == null || d.archivo() == null) {
            return;
        }
        Path p = adjuntos.resolver(d.archivo());
        if (!Files.exists(p)) {
            Estilos.info(this, "Archivo no encontrado", "No se encontró el archivo en " + p);
            return;
        }
        ejecutar(() -> {
            try {
                Desktop.getDesktop().open(p.toFile());
            } catch (java.io.IOException e) {
                throw new IllegalStateException("No se pudo abrir el archivo", e);
            }
        });
    }
}
