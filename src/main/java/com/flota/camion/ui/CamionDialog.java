package com.flota.camion.ui;

import java.awt.Component;

import javax.swing.JTextField;

import com.flota.camion.model.Camion;
import com.flota.camion.service.CamionService;
import com.flota.common.Textos;
import com.flota.common.ValidacionException;
import com.flota.ui.DialogoEdicion;
import com.formdev.flatlaf.FlatClientProperties;

/** Ventana para agregar o modificar un camion. */
public class CamionDialog extends DialogoEdicion {
    private final CamionService service;
    private final Camion original;
    private final JTextField patente = campo("Ej: AB123CD");
    private final JTextField marca = campo("Ej: Scania");
    private final JTextField modelo = campo("Ej: R450");
    private final JTextField anio = campo("Ej: 2019");

    public CamionDialog(Component padre, CamionService service, Camion camion) {
        super(padre, camion == null ? "Agregar camión" : "Editar camión",
                camion == null ? "Completá los datos del camión nuevo." : "Modificá los datos y tocá Guardar.");
        this.service = service;
        this.original = camion;

        formulario.agregar("patente", "Patente *", patente, "Se guarda en mayúsculas y sin espacios.");
        formulario.agregar("marca", "Marca", marca);
        formulario.agregar("modelo", "Modelo", modelo);
        formulario.agregar("anio", "Año", anio);

        if (camion != null) {
            patente.setText(camion.patente());
            marca.setText(camion.marca());
            modelo.setText(camion.modelo());
            anio.setText(camion.anio() == null ? "" : camion.anio().toString());
        }
    }

    @Override
    protected void guardar() {
        Camion camion = new Camion(original == null ? null : original.id(),
                patente.getText(), marca.getText(), modelo.getText(), leerAnio());
        if (original == null) {
            service.create(camion);
        } else {
            service.update(camion);
        }
    }

    private Integer leerAnio() {
        String texto = Textos.limpiar(anio.getText());
        if (texto == null) {
            return null;
        }
        try {
            return Integer.valueOf(texto);
        } catch (NumberFormatException e) {
            throw ValidacionException.deCampo("anio", "El año tiene que ser un número, por ejemplo 2019.");
        }
    }

    private static JTextField campo(String placeholder) {
        JTextField t = new JTextField(26);
        t.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        return t;
    }
}
