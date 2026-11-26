package com.flota.ui;

import java.util.function.Function;

/** Definicion de una columna de tabla: titulo, tipo, como obtener el valor y ancho preferido. */
public record Columna<T>(String titulo, Class<?> tipo, Function<T, Object> valor, int ancho) {

    public static <T> Columna<T> texto(String titulo, Function<T, Object> valor, int ancho) {
        return new Columna<>(titulo, String.class, valor, ancho);
    }
}
