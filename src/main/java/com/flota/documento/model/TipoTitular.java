package com.flota.documento.model;

import java.util.List;

/** A quien pertenece un documento, con los tipos de documento mas comunes de cada uno. */
public enum TipoTitular {
    CAMION("Camión", List.of("VTV / RTO", "Seguro", "Cédula verde", "RUTA", "SENASA", "Patente (impuesto)")),
    CAMIONERO("Camionero", List.of("Licencia de conducir", "LINTI", "Psicofísico", "ART",
            "Curso de cargas peligrosas", "Libreta sanitaria"));

    private final String etiqueta;
    private final List<String> tiposSugeridos;

    TipoTitular(String etiqueta, List<String> tiposSugeridos) {
        this.etiqueta = etiqueta;
        this.tiposSugeridos = tiposSugeridos;
    }

    public String etiqueta() {
        return etiqueta;
    }

    public List<String> tiposSugeridos() {
        return tiposSugeridos;
    }
}
