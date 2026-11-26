package com.flota.camion.model;

public record Camion(Long id, String patente, String marca, String modelo, Integer anio) {

    /** Texto corto para mostrar en listas: "AB123CD - Scania R450". */
    public String descripcion() {
        StringBuilder sb = new StringBuilder(patente);
        String detalle = ((marca == null ? "" : marca) + " " + (modelo == null ? "" : modelo)).strip();
        if (!detalle.isEmpty()) {
            sb.append(" - ").append(detalle);
        }
        return sb.toString();
    }
}
