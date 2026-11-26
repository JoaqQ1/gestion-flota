package com.flota.documento.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Documento con vencimiento (VTV, seguro, licencia, psicofisico, etc.).
 * Puede pertenecer a un camion o a un camionero.
 *
 * @param descripcionTitular patente o nombre del titular; se completa al leer de la base
 * @param archivo nombre del archivo adjunto dentro de la carpeta de documentos, o null
 */
public record Documento(Long id, String tipo, TipoTitular tipoTitular, Long titularId,
        String descripcionTitular, LocalDate vencimiento, String archivo, String observaciones) {

    public long diasParaVencer(LocalDate hoy) {
        return ChronoUnit.DAYS.between(hoy, vencimiento);
    }

    public boolean vencido(LocalDate hoy) {
        return vencimiento.isBefore(hoy);
    }

    public EstadoVencimiento estado(LocalDate hoy, int diasAviso) {
        return EstadoVencimiento.de(vencimiento, hoy, diasAviso);
    }

    /** Texto amigable: "Venció hace 3 días", "Vence hoy", "Faltan 12 días". */
    public String situacion(LocalDate hoy) {
        long dias = diasParaVencer(hoy);
        if (dias < -1) {
            return "Venció hace " + (-dias) + " días";
        }
        if (dias == -1) {
            return "Venció ayer";
        }
        if (dias == 0) {
            return "Vence hoy";
        }
        if (dias == 1) {
            return "Vence mañana";
        }
        return "Faltan " + dias + " días";
    }

    public Documento conArchivo(String nuevoArchivo) {
        return new Documento(id, tipo, tipoTitular, titularId, descripcionTitular, vencimiento, nuevoArchivo,
                observaciones);
    }

    public Documento conId(Long nuevoId) {
        return new Documento(nuevoId, tipo, tipoTitular, titularId, descripcionTitular, vencimiento, archivo,
                observaciones);
    }
}
