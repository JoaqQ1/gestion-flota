package com.flota.camion.ui;

import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;

import com.flota.camion.model.Camion;
import com.flota.camion.service.CamionService;
import com.flota.documento.model.TipoTitular;
import com.flota.documento.repository.DocumentoRepository;
import com.flota.ui.Columna;
import com.flota.ui.Estilos;
import com.flota.ui.PanelListado;

/** Listado de camiones con alta, edicion y baja. */
public class CamionesPanel extends PanelListado<Camion> {
    private final CamionService service;
    private final DocumentoRepository documentos;
    private final JButton btnDocumentos = new JButton("Ver documentos");

    public CamionesPanel(CamionService service, DocumentoRepository documentos, Consumer<String> verDocumentos) {
        super("Camiones", "Los camiones de la empresa. Hacé doble clic en uno para editarlo.",
                "Agregar camión", "Buscar por patente, marca o modelo",
                "<html><center>Todavía no hay camiones cargados.<br>"
                        + "Tocá <b>Agregar camión</b> para cargar el primero.</center></html>",
                columnas());
        this.service = service;
        this.documentos = documentos;
        btnDocumentos.setEnabled(false);
        btnDocumentos.addActionListener(e -> {
            Camion c = seleccionado();
            if (c != null) {
                verDocumentos.accept(c.patente());
            }
        });
        barra.add(btnDocumentos);
    }

    public static List<Columna<Camion>> columnas() {
        return List.of(
                Columna.texto("Patente", Camion::patente, 120),
                Columna.texto("Marca", Camion::marca, 180),
                Columna.texto("Modelo", Camion::modelo, 180),
                new Columna<>("Año", Integer.class, Camion::anio, 80));
    }

    @Override
    protected List<Camion> cargar() {
        return service.findAll();
    }

    @Override
    protected void nuevo() {
        if (new CamionDialog(this, service, null).abrir()) {
            recargar();
        }
    }

    @Override
    protected void editar(Camion camion) {
        if (new CamionDialog(this, service, camion).abrir()) {
            recargar();
        }
    }

    @Override
    protected void eliminar(Camion camion) {
        int cantidad = documentos.findByTitular(TipoTitular.CAMION, camion.id()).size();
        String extra = cantidad == 0 ? ""
                : "<br><br><font color='#B91C1C'>También se van a borrar sus " + cantidad
                        + (cantidad == 1 ? " documento." : " documentos.") + "</font>";
        String mensaje = "<html>¿Seguro que querés eliminar el camión <b>" + camion.patente() + "</b>?"
                + extra + "<br><br>Esta acción no se puede deshacer.</html>";
        if (Estilos.confirmar(this, "Eliminar camión", mensaje, "Sí, eliminar")) {
            service.delete(camion.id());
            recargar();
        }
    }

    @Override
    protected void alCambiarSeleccion(boolean haySeleccion) {
        btnDocumentos.setEnabled(haySeleccion);
    }
}
