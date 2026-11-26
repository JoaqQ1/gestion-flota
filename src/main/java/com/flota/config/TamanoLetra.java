package com.flota.config;

/** Tamaños de letra que el usuario puede elegir. "aumento" son puntos sobre la letra del sistema. */
public enum TamanoLetra {
    NORMAL("Normal", 1),
    GRANDE("Grande", 3),
    MUY_GRANDE("Muy grande", 5);

    private final String etiqueta;
    private final int aumento;

    TamanoLetra(String etiqueta, int aumento) {
        this.etiqueta = etiqueta;
        this.aumento = aumento;
    }

    public String etiqueta() {
        return etiqueta;
    }

    public int aumento() {
        return aumento;
    }
}
