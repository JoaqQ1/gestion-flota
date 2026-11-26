package com.flota.common;

import java.text.Normalizer;

public final class Textos {
    private Textos() {
    }

    /** Quita espacios de los extremos; devuelve null si queda vacio. */
    public static String limpiar(String s) {
        if (s == null) {
            return null;
        }
        String t = s.strip();
        return t.isEmpty() ? null : t;
    }

    /** Minusculas y sin acentos, para comparar en busquedas. */
    public static String normalizar(String s) {
        if (s == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase();
    }
}
