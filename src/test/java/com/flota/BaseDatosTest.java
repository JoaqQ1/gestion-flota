package com.flota;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import com.flota.config.AppConfig;
import com.flota.repository.Database;

/** Cada test usa una base nueva en una carpeta temporal. */
public abstract class BaseDatosTest {
    @TempDir
    protected Path carpeta;

    @BeforeEach
    void prepararBase() {
        AppConfig.setCarpetaDatos(carpeta);
        Database.init();
    }
}
