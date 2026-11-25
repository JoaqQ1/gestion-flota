package com.flota.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Documento con vencimiento (VTV, seguro, licencia, psicofisico, etc.).
 * Puede pertenecer a un camion o a un camionero.
 */
public record Documento(Long id, String tipo, TipoTitular tipoTitular, long titularId,
                        String descripcionTitular, LocalDate vencimiento, String rutaArchivo) {

    public long diasParaVencer(LocalDate hoy) {
        return ChronoUnit.DAYS.between(hoy, vencimiento);
    }

    public boolean vencido(LocalDate hoy) {
        return vencimiento.isBefore(hoy);
    }
}
