package com.flota.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import com.flota.config.AppConfig;

/** Conexion a la base SQLite local y creacion del esquema. */
public final class Database {

    private Database() {
    }

    public static Connection connect() throws SQLException {
        String url = "jdbc:sqlite:" + AppConfig.carpetaDatos().resolve("flota.db");
        return DriverManager.getConnection(url);
    }

    public static void init() {
        try {
            Files.createDirectories(AppConfig.carpetaDatos());
            Files.createDirectories(AppConfig.carpetaAdjuntos());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear la carpeta de datos " + AppConfig.carpetaDatos(), e);
        }
        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS camion (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        patente TEXT NOT NULL UNIQUE,
                        marca TEXT, modelo TEXT, anio INTEGER)""");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS camionero (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nombre TEXT NOT NULL, apellido TEXT NOT NULL,
                        dni TEXT UNIQUE, telefono TEXT)""");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS documento (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        tipo TEXT NOT NULL,
                        tipo_titular TEXT NOT NULL CHECK (tipo_titular IN ('CAMION','CAMIONERO')),
                        titular_id INTEGER NOT NULL,
                        vencimiento TEXT NOT NULL,
                        archivo TEXT,
                        observaciones TEXT)""");
            st.execute("CREATE TABLE IF NOT EXISTS configuracion (clave TEXT PRIMARY KEY, valor TEXT)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_documento_titular ON documento (tipo_titular, titular_id)");
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo inicializar la base de datos", e);
        }
    }

    /** Indica si el error es por violar una restriccion UNIQUE (dato repetido). */
    public static boolean esDuplicado(SQLException e) {
        if (e instanceof SQLiteException se && se.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE) {
            return true;
        }
        return e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed");
    }
}
