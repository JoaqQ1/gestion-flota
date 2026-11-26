package com.flota.camionero.ui;

import java.awt.Component;

import javax.swing.JTextField;

import com.flota.camionero.model.Camionero;
import com.flota.camionero.service.CamioneroService;
import com.flota.ui.DialogoEdicion;
import com.formdev.flatlaf.FlatClientProperties;

/** Ventana para agregar o modificar un camionero. */
public class CamioneroDialog extends DialogoEdicion {
    private final CamioneroService service;
    private final Camionero original;
    private final JTextField nombre = campo("Ej: Juan");
    private final JTextField apellido = campo("Ej: Pérez");
    private final JTextField dni = campo("Ej: 30123456");
    private final JTextField telefono = campo("Ej: 11 2345-6789");

    public CamioneroDialog(Component padre, CamioneroService service, Camionero camionero) {
        super(padre, camionero == null ? "Agregar camionero" : "Editar camionero",
                camionero == null ? "Completá los datos del chofer." : "Modificá los datos y tocá Guardar.");
        this.service = service;
        this.original = camionero;

        formulario.agregar("nombre", "Nombre *", nombre);
        formulario.agregar("apellido", "Apellido *", apellido);
        formulario.agregar("dni", "DNI", dni, "Sin puntos.");
        formulario.agregar("telefono", "Teléfono celular", telefono,
                "Con código de área. Más adelante se usará para avisos por WhatsApp.");

        if (camionero != null) {
            nombre.setText(camionero.nombre());
            apellido.setText(camionero.apellido());
            dni.setText(camionero.dni());
            telefono.setText(camionero.telefono());
        }
    }

    @Override
    protected void guardar() {
        Camionero c = new Camionero(original == null ? null : original.id(),
                nombre.getText(), apellido.getText(), dni.getText(), telefono.getText());
        if (original == null) {
            service.create(c);
        } else {
            service.update(c);
        }
    }

    private static JTextField campo(String placeholder) {
        JTextField t = new JTextField(26);
        t.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        return t;
    }
}
