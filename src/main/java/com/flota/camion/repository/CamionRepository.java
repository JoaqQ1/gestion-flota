package com.flota.camion.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.flota.camion.exception.PatenteDuplicadaException;
import com.flota.camion.model.Camion;
import com.flota.repository.Database;

public class CamionRepository {

    private static final String COLUMNAS = "id, patente, marca, modelo, anio";

    public List<Camion> findAll() {
        String query = "SELECT " + COLUMNAS + " FROM camion ORDER BY patente";
        List<Camion> camiones = new ArrayList<>();
        try (Connection conn = Database.connect();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                camiones.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error listando camiones", e);
        }
        return camiones;
    }

    public Optional<Camion> findByPatente(String patente) {
        String query = "SELECT " + COLUMNAS + " FROM camion WHERE patente = ?";
        try (Connection conn = Database.connect(); PreparedStatement pstm = conn.prepareStatement(query)) {
            pstm.setString(1, patente);
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error buscando camion " + patente, e);
        }
    }

    public Optional<Camion> findById(Long id) {
        String query = "SELECT " + COLUMNAS + " FROM camion WHERE id = ?";
        try (Connection conn = Database.connect(); PreparedStatement pstm = conn.prepareStatement(query)) {
            pstm.setLong(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error buscando camion con id " + id, e);
        }
    }

    public Camion save(Camion camion) {
        String query = "INSERT INTO camion (patente, marca, modelo, anio) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = Database.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            setParametros(pstmt, camion);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return new Camion(rs.getLong(1), camion.patente(), camion.marca(), camion.modelo(), camion.anio());
            }
        } catch (SQLException e) {
            throw traducir(e, camion);
        }
    }

    public Camion update(Camion camion) {
        String query = "UPDATE camion SET patente = ?, marca = ?, modelo = ?, anio = ? WHERE id = ?";
        try (Connection conn = Database.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            setParametros(pstmt, camion);
            pstmt.setLong(5, camion.id());
            if (pstmt.executeUpdate() == 0) {
                throw new IllegalStateException("No existe el camion con id " + camion.id());
            }
            return camion;
        } catch (SQLException e) {
            throw traducir(e, camion);
        }
    }

    /** Borra el camion y sus documentos en una sola transaccion. */
    public void delete(Long id) {
        try (Connection conn = Database.connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement docs = conn.prepareStatement(
                    "DELETE FROM documento WHERE tipo_titular = 'CAMION' AND titular_id = ?");
                    PreparedStatement cam = conn.prepareStatement("DELETE FROM camion WHERE id = ?")) {
                docs.setLong(1, id);
                docs.executeUpdate();
                cam.setLong(1, id);
                cam.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error eliminando camion con id " + id, e);
        }
    }

    private void setParametros(PreparedStatement pstmt, Camion camion) throws SQLException {
        pstmt.setString(1, camion.patente());
        pstmt.setString(2, camion.marca());
        pstmt.setString(3, camion.modelo());
        pstmt.setObject(4, camion.anio());
    }

    private RuntimeException traducir(SQLException e, Camion camion) {
        if (Database.esDuplicado(e)) {
            return new PatenteDuplicadaException("Ya existe un camion con la patente " + camion.patente(), e);
        }
        return new IllegalStateException("Error guardando camion", e);
    }

    private Camion mapRow(ResultSet rs) throws SQLException {
        // getObject(..., Integer.class) falla con NULL en sqlite-jdbc; por eso se usa getInt + wasNull
        int anio = rs.getInt("anio");
        Integer anioONull = rs.wasNull() ? null : anio;
        return new Camion(
                rs.getLong("id"),
                rs.getString("patente"),
                rs.getString("marca"),
                rs.getString("modelo"),
                anioONull);
    }
}
