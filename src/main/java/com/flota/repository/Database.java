package com.flota.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Conexion a la base SQLite local y creacion del esquema. */
public final class Database {
    private static final String URL = "jdbc:sqlite:flota.db";

    private Database() {}

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void init() {
        try (var c = connect(); var st = c.createStatement()) {
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
                    ruta_archivo TEXT)""");
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo inicializar la base de datos", e);
        }
    }
}
