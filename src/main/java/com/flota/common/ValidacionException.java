package com.flota.common;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Datos ingresados por el usuario que no son validos.
 * Guarda un mensaje por campo para poder marcarlo en el formulario.
 */
public class ValidacionException extends RuntimeException {
    private final Map<String, String> errores;

    public ValidacionException(Map<String, String> errores) {
        super(String.join("\n", errores.values()));
        this.errores = Collections.unmodifiableMap(new LinkedHashMap<>(errores));
    }

    public static ValidacionException deCampo(String campo, String mensaje) {
        return new ValidacionException(Map.of(campo, mensaje));
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}
