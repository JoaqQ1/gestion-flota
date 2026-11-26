package com.flota.camionero.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.flota.camionero.model.Camionero;
import com.flota.common.ValidacionException;
import com.flota.repository.Database;

public class CamioneroRepository {

    private static final String COLUMNAS = "id, nombre, apellido, dni, telefono";

    public List<Camionero> findAll() {
        String query = "SELECT " + COLUMNAS + " FROM camionero ORDER BY apellido, nombre";
        List<Camionero> lista = new ArrayList<>();
        try (Connection conn = Database.connect();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error listando camioneros", e);
        }
        return lista;
    }

    public Optional<Camionero> findById(Long id) {
        return findOne("id = ?", id);
    }

    public Optional<Camionero> findByDni(String dni) {
        return findOne("dni = ?", dni);
    }

    private Optional<Camionero> findOne(String condicion, Object valor) {
        String query = "SELECT " + COLUMNAS + " FROM camionero WHERE " + condicion;
        try (Connection conn = Database.connect(); PreparedStatement pstm = conn.prepareStatement(query)) {
            pstm.setObject(1, valor);
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error buscando camionero", e);
        }
    }

    public Camionero save(Camionero c) {
        String query = "INSERT INTO camionero (nombre, apellido, dni, telefono) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = Database.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            setParametros(pstmt, c);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return new Camionero(rs.getLong(1), c.nombre(), c.apellido(), c.dni(), c.telefono());
            }
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    public Camionero update(Camionero c) {
        String query = "UPDATE camionero SET nombre = ?, apellido = ?, dni = ?, telefono = ? WHERE id = ?";
        try (Connection conn = Database.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            setParametros(pstmt, c);
            pstmt.setLong(5, c.id());
            if (pstmt.executeUpdate() == 0) {
                throw new IllegalStateException("No existe el camionero con id " + c.id());
            }
            return c;
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    /** Borra el camionero y sus documentos en una sola transaccion. */
    public void delete(Long id) {
        try (Connection conn = Database.connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement docs = conn.prepareStatement(
                    "DELETE FROM documento WHERE tipo_titular = 'CAMIONERO' AND titular_id = ?");
                    PreparedStatement cam = conn.prepareStatement("DELETE FROM camionero WHERE id = ?")) {
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
            throw new IllegalStateException("Error eliminando camionero con id " + id, e);
        }
    }

    private void setParametros(PreparedStatement pstmt, Camionero c) throws SQLException {
        pstmt.setString(1, c.nombre());
        pstmt.setString(2, c.apellido());
        pstmt.setString(3, c.dni());
        pstmt.setString(4, c.telefono());
    }

    private RuntimeException traducir(SQLException e) {
        if (Database.esDuplicado(e)) {
            return ValidacionException.deCampo("dni", "Ya hay un camionero cargado con ese DNI.");
        }
        return new IllegalStateException("Error guardando camionero", e);
    }

    private Camionero mapRow(ResultSet rs) throws SQLException {
        return new Camionero(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("dni"),
                rs.getString("telefono"));
    }
}
