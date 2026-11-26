package com.flota.config;

import java.nio.file.Path;
import java.util.prefs.Preferences;

/**
 * Preferencias de esta computadora que no pueden ir en la base de datos:
 * la carpeta de datos (hay que saberla antes de abrir la base) y el tamaño de letra.
 */
public final class PreferenciasLocales {
    private static final Preferences PREFS = Preferences.userRoot().node("com/flota/gestion-flota");
    private static final String CARPETA_DATOS = "carpetaDatos";
    private static final String TAMANO_LETRA = "tamanoLetra";

    private PreferenciasLocales() {
    }

    /** Carpeta elegida por el usuario, o null si nunca eligio una. */
    public static Path carpetaDatos() {
        String valor = PREFS.get(CARPETA_DATOS, null);
        return valor == null ? null : Path.of(valor);
    }

    public static void setCarpetaDatos(Path carpeta) {
        PREFS.put(CARPETA_DATOS, carpeta.toAbsolutePath().toString());
    }

    public static TamanoLetra tamanoLetra() {
        try {
            return TamanoLetra.valueOf(PREFS.get(TAMANO_LETRA, TamanoLetra.NORMAL.name()));
        } catch (IllegalArgumentException e) {
            return TamanoLetra.NORMAL;
        }
    }

    public static void setTamanoLetra(TamanoLetra tamano) {
        PREFS.put(TAMANO_LETRA, tamano.name());
    }
}
