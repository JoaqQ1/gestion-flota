package com.flota.documento.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.flota.documento.model.Documento;
import com.flota.documento.model.TipoTitular;
import com.flota.repository.Database;

public class DocumentoRepository {

    /** Trae el documento junto con la patente o el nombre de su titular. */
    private static final String SELECT = """
            SELECT d.*,
                   CASE d.tipo_titular
                     WHEN 'CAMION' THEN (SELECT patente FROM camion WHERE id = d.titular_id)
                     ELSE (SELECT nombre || ' ' || apellido FROM camionero WHERE id = d.titular_id)
                   END AS titular
            FROM documento d
            """;

    public List<Documento> findAll() {
        return lista(SELECT + " ORDER BY d.vencimiento");
    }

    public Optional<Documento> findById(Long id) {
        List<Documento> r = lista(SELECT + " WHERE d.id = ?", id);
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    public List<Documento> findByTitular(TipoTitular tipo, Long titularId) {
        return lista(SELECT + " WHERE d.tipo_titular = ? AND d.titular_id = ? ORDER BY d.vencimiento",
                tipo.name(), titularId);
    }

    /** Documentos que vencen hasta la fecha limite (incluye los ya vencidos). */
    public List<Documento> venceAntesDe(LocalDate limite) {
        return lista(SELECT + " WHERE d.vencimiento <= ? ORDER BY d.vencimiento", limite.toString());
    }

    public Documento save(Documento d) {
        String sql = """
                INSERT INTO documento (tipo, tipo_titular, titular_id, vencimiento, archivo, observaciones)
                VALUES (?, ?, ?, ?, ?, ?) RETURNING id""";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            setParametros(ps, d);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return d.conId(rs.getLong(1));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error guardando documento", e);
        }
    }

    public Documento update(Documento d) {
        String sql = """
                UPDATE documento SET tipo = ?, tipo_titular = ?, titular_id = ?, vencimiento = ?,
                       archivo = ?, observaciones = ?
                WHERE id = ?""";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            setParametros(ps, d);
            ps.setLong(7, d.id());
            if (ps.executeUpdate() == 0) {
                throw new IllegalStateException("No existe el documento con id " + d.id());
            }
            return d;
        } catch (SQLException e) {
            throw new IllegalStateException("Error guardando documento", e);
        }
    }

    public void delete(Long id) {
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(
                "DELETE FROM documento WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Error eliminando documento", e);
        }
    }

    private void setParametros(PreparedStatement ps, Documento d) throws SQLException {
        ps.setString(1, d.tipo());
        ps.setString(2, d.tipoTitular().name());
        ps.setLong(3, d.titularId());
        ps.setString(4, d.vencimiento().toString());
        ps.setString(5, d.archivo());
        ps.setString(6, d.observaciones());
    }

    private List<Documento> lista(String sql, Object... parametros) {
        List<Documento> result = new ArrayList<>();
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error consultando documentos", e);
        }
        return result;
    }

    private Documento mapRow(ResultSet rs) throws SQLException {
        return new Documento(
                rs.getLong("id"),
                rs.getString("tipo"),
                TipoTitular.valueOf(rs.getString("tipo_titular")),
                rs.getLong("titular_id"),
                rs.getString("titular"),
                LocalDate.parse(rs.getString("vencimiento")),
                rs.getString("archivo"),
                rs.getString("observaciones"));
    }
}
