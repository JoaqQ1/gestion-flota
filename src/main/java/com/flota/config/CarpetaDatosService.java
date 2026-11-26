package com.flota.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.flota.common.ValidacionException;
import com.flota.repository.Database;

/** Cambia la carpeta donde se guardan la base de datos y los adjuntos. */
public class CarpetaDatosService {
    public static final String NOMBRE_CARPETA = "GestionFlota";
    private static final String BASE = "flota.db";

    /**
     * Carpeta final a usar a partir de la que eligio el usuario: si ya tiene datos o ya se llama
     * GestionFlota se usa tal cual; si no, se crea GestionFlota adentro para no desparramar archivos.
     */
    public Path carpetaFinal(Path elegida) {
        Path absoluta = elegida.toAbsolutePath().normalize();
        if (tieneDatos(absoluta) || absoluta.getFileName() != null
                && absoluta.getFileName().toString().equals(NOMBRE_CARPETA)) {
            return absoluta;
        }
        return absoluta.resolve(NOMBRE_CARPETA);
    }

    public boolean tieneDatos(Path carpeta) {
        return Files.isRegularFile(carpeta.resolve(BASE));
    }

    /** Verifica que se pueda usar la carpeta. Lanza ValidacionException con un mensaje claro si no. */
    public void validar(Path destino) {
        Path actual = AppConfig.carpetaDatos().toAbsolutePath().normalize();
        Path nueva = destino.toAbsolutePath().normalize();
        if (nueva.equals(actual)) {
            throw ValidacionException.deCampo("carpeta", "Esa ya es la carpeta que se está usando.");
        }
        if (nueva.startsWith(actual)) {
            throw ValidacionException.deCampo("carpeta",
                    "Elegí una carpeta que no esté dentro de la carpeta de datos actual.");
        }
        Path existente = nueva;
        while (existente != null && !Files.exists(existente)) {
            existente = existente.getParent();
        }
        if (existente == null || !Files.isWritable(existente)) {
            throw ValidacionException.deCampo("carpeta",
                    "No se puede guardar en esa carpeta (no hay permiso de escritura). Elegí otra.");
        }
    }

    /** Copia la base y los adjuntos de la carpeta actual a la nueva. La carpeta actual queda intacta. */
    public void copiarDatosActuales(Path destino) {
        Path origen = AppConfig.carpetaDatos();
        try {
            Files.createDirectories(destino);
            if (Files.exists(origen.resolve(BASE))) {
                Files.copy(origen.resolve(BASE), destino.resolve(BASE));
            }
            Path adjuntos = origen.resolve("documentos");
            if (Files.isDirectory(adjuntos)) {
                try (Stream<Path> archivos = Files.walk(adjuntos)) {
                    for (Path p : (Iterable<Path>) archivos::iterator) {
                        Path copia = destino.resolve(origen.relativize(p).toString());
                        if (Files.isDirectory(p)) {
                            Files.createDirectories(copia);
                        } else {
                            Files.copy(p, copia);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron copiar los datos a " + destino, e);
        }
    }

    /** Empieza a usar la carpeta y la recuerda para las proximas veces que se abra la app. */
    public void usar(Path carpeta) {
        Path anterior = AppConfig.carpetaDatos();
        AppConfig.setCarpetaDatos(carpeta);
        try {
            Database.init();
        } catch (RuntimeException e) {
            AppConfig.setCarpetaDatos(anterior);
            throw e;
        }
        PreferenciasLocales.setCarpetaDatos(carpeta);
    }
}
