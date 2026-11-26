package com.flota.documento.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import com.flota.config.AppConfig;

/**
 * Guarda una copia de los archivos adjuntos (PDF, fotos) dentro de la carpeta de datos,
 * asi no se pierden si el usuario mueve o borra el original.
 */
public class ArchivoAdjuntoService {

    /** Copia el archivo y devuelve el nombre con el que quedo guardado. */
    public String guardar(Path origen) {
        String nombreOriginal = origen.getFileName().toString().replaceAll("[^\\w.\\- ]", "_");
        String nombre = UUID.randomUUID().toString().substring(0, 8) + "_" + nombreOriginal;
        try {
            Files.createDirectories(AppConfig.carpetaAdjuntos());
            Files.copy(origen, resolver(nombre));
            return nombre;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo copiar el archivo " + origen.getFileName(), e);
        }
    }

    public Path resolver(String nombre) {
        return AppConfig.carpetaAdjuntos().resolve(nombre);
    }

    public void borrar(String nombre) {
        if (nombre == null) {
            return;
        }
        try {
            Files.deleteIfExists(resolver(nombre));
        } catch (IOException e) {
            System.err.println("No se pudo borrar el adjunto " + nombre + ": " + e.getMessage());
        }
    }

    /** Nombre para mostrar al usuario, sin el prefijo interno. */
    public static String nombreVisible(String nombre) {
        if (nombre == null) {
            return null;
        }
        int i = nombre.indexOf('_');
        return i >= 0 ? nombre.substring(i + 1) : nombre;
    }
}
