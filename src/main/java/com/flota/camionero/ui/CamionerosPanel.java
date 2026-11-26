package com.flota.camionero.ui;

import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;

import com.flota.camionero.model.Camionero;
import com.flota.camionero.service.CamioneroService;
import com.flota.documento.model.TipoTitular;
import com.flota.documento.repository.DocumentoRepository;
import com.flota.ui.Columna;
import com.flota.ui.Estilos;
import com.flota.ui.PanelListado;

/** Listado de camioneros con alta, edicion y baja. */
public class CamionerosPanel extends PanelListado<Camionero> {
    private final CamioneroService service;
    private final DocumentoRepository documentos;
    private final JButton btnDocumentos = new JButton("Ver documentos");

    public CamionerosPanel(CamioneroService service, DocumentoRepository documentos,
            Consumer<String> verDocumentos) {
        super("Camioneros", "Los choferes de la empresa. Hacé doble clic en uno para editarlo.",
                "Agregar camionero", "Buscar por nombre, apellido o DNI",
                "<html><center>Todavía no hay camioneros cargados.<br>"
                        + "Tocá <b>Agregar camionero</b> para cargar el primero.</center></html>",
                columnas());
        this.service = service;
        this.documentos = documentos;
        btnDocumentos.setEnabled(false);
        btnDocumentos.addActionListener(e -> {
            Camionero c = seleccionado();
            if (c != null) {
                verDocumentos.accept(c.nombreCompleto());
            }
        });
        barra.add(btnDocumentos);
    }

    public static List<Columna<Camionero>> columnas() {
        return List.of(
                Columna.texto("Apellido", Camionero::apellido, 160),
                Columna.texto("Nombre", Camionero::nombre, 160),
                Columna.texto("DNI", Camionero::dni, 120),
                Columna.texto("Teléfono", Camionero::telefono, 150));
    }

    @Override
    protected List<Camionero> cargar() {
        return service.findAll();
    }

    @Override
    protected void nuevo() {
        if (new CamioneroDialog(this, service, null).abrir()) {
            recargar();
        }
    }

    @Override
    protected void editar(Camionero camionero) {
        if (new CamioneroDialog(this, service, camionero).abrir()) {
            recargar();
        }
    }

    @Override
    protected void eliminar(Camionero camionero) {
        int cantidad = documentos.findByTitular(TipoTitular.CAMIONERO, camionero.id()).size();
        String extra = cantidad == 0 ? ""
                : "<br><br><font color='#B91C1C'>También se van a borrar sus " + cantidad
                        + (cantidad == 1 ? " documento." : " documentos.") + "</font>";
        String mensaje = "<html>¿Seguro que querés eliminar a <b>" + camionero.nombreCompleto() + "</b>?"
                + extra + "<br><br>Esta acción no se puede deshacer.</html>";
        if (Estilos.confirmar(this, "Eliminar camionero", mensaje, "Sí, eliminar")) {
            service.delete(camionero.id());
            recargar();
        }
    }

    @Override
    protected void alCambiarSeleccion(boolean haySeleccion) {
        btnDocumentos.setEnabled(haySeleccion);
    }
}
