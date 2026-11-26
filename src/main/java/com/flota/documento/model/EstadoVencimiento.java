package com.flota.documento.model;

import java.time.LocalDate;

/** Situacion de un documento segun su fecha de vencimiento. El orden se usa para ordenar la tabla. */
public enum EstadoVencimiento {
    VENCIDO("Vencido"),
    POR_VENCER("Por vencer"),
    VIGENTE("Al día");

    private final String etiqueta;

    EstadoVencimiento(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }

    public static EstadoVencimiento de(LocalDate vencimiento, LocalDate hoy, int diasAviso) {
        if (vencimiento.isBefore(hoy)) {
            return VENCIDO;
        }
        if (!vencimiento.isAfter(hoy.plusDays(diasAviso))) {
            return POR_VENCER;
        }
        return VIGENTE;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
