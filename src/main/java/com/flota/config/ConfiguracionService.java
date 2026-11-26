package com.flota.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.flota.common.ValidacionException;
import com.flota.repository.Database;

/**
 * Preferencias que el usuario cambia desde la pantalla de Configuracion.
 * Se guardan en la tabla "configuracion" y se mantienen en memoria porque se leen muy seguido.
 */
public class ConfiguracionService {
    public static final int DIAS_MINIMO = 1;
    public static final int DIAS_MAXIMO = 365;
    private static final String CLAVE_DIAS_AVISO = "dias_aviso";

    private int diasAviso;

    public ConfiguracionService() {
        recargar();
    }

    /** Vuelve a leer la configuracion de la base (por ejemplo, despues de cambiar de carpeta). */
    public void recargar() {
        diasAviso = leerEntero(CLAVE_DIAS_AVISO, AppConfig.DIAS_AVISO_POR_DEFECTO);
    }

    /** Cuantos dias antes del vencimiento se empieza a avisar. */
    public int getDiasAviso() {
        return diasAviso;
    }

    public void setDiasAviso(int dias) {
        if (dias < DIAS_MINIMO || dias > DIAS_MAXIMO) {
            throw ValidacionException.deCampo("diasAviso",
                    "Los días de aviso deben estar entre " + DIAS_MINIMO + " y " + DIAS_MAXIMO + ".");
        }
        guardar(CLAVE_DIAS_AVISO, String.valueOf(dias));
        diasAviso = dias;
    }

    private int leerEntero(String clave, int porDefecto) {
        try (Connection c = Database.connect();
                PreparedStatement ps = c.prepareStatement("SELECT valor FROM configuracion WHERE clave = ?")) {
            ps.setString(1, clave);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Integer.parseInt(rs.getString(1)) : porDefecto;
            }
        } catch (SQLException | NumberFormatException e) {
            System.err.println("No se pudo leer la configuracion " + clave + ": " + e.getMessage());
            return porDefecto;
        }
    }

    private void guardar(String clave, String valor) {
        String sql = "INSERT INTO configuracion (clave, valor) VALUES (?, ?) "
                + "ON CONFLICT(clave) DO UPDATE SET valor = excluded.valor";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, clave);
            ps.setString(2, valor);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar la configuracion", e);
        }
    }
}
