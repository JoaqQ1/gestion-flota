package com.flota.exportacion;

import java.util.List;

/**
 * Datos listos para exportar: titulo, encabezados y filas.
 * Los valores pueden ser texto, numeros, fechas (LocalDate) o estados de vencimiento.
 */
public record TablaExportable(String titulo, String subtitulo, List<String> encabezados, List<List<Object>> filas) {
}
