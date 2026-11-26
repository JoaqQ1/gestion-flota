package com.flota.camionero.model;

public record Camionero(Long id, String nombre, String apellido, String dni, String telefono) {
    public String nombreCompleto() {
        return nombre + " " + apellido;
    }
}
