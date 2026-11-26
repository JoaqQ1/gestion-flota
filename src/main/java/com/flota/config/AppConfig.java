package com.flota.config;

import java.nio.file.Path;

/** Configuracion general de la aplicacion. */
public final class AppConfig {

    /** Dias de aviso si el usuario todavia no los configuro. */
    public static final int DIAS_AVISO_POR_DEFECTO = 30;

    private static Path carpetaDatos = carpetaInicial();

    private AppConfig() {
    }

    /** Carpeta donde se guardan la base de datos y los archivos adjuntos. */
    public static Path carpetaDatos() {
        return carpetaDatos;
    }

    /** Permite cambiar la carpeta de datos (se usa en los tests). */
    public static void setCarpetaDatos(Path carpeta) {
        carpetaDatos = carpeta;
    }

    /**
     * Orden de prioridad: -Dflota.datos (para pruebas), la carpeta que eligio el usuario
     * en Configuracion, o ~/GestionFlota.
     */
    private static Path carpetaInicial() {
        String propiedad = System.getProperty("flota.datos");
        if (propiedad != null) {
            return Path.of(propiedad);
        }
        Path elegida = PreferenciasLocales.carpetaDatos();
        if (elegida != null) {
            return elegida;
        }
        return carpetaPorDefecto();
    }

    public static Path carpetaPorDefecto() {
        return Path.of(System.getProperty("user.home"), "GestionFlota");
    }

    public static Path carpetaAdjuntos() {
        return carpetaDatos.resolve("documentos");
    }
}
