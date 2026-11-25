package com.flota.repository;

import com.flota.model.Documento;
import com.flota.model.TipoTitular;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DocumentoRepository {

    /** Documentos que vencen hasta la fecha limite (incluye los ya vencidos). */
    public List<Documento> venceAntesDe(LocalDate limite) {
        String sql = """
            SELECT d.*,
                   CASE d.tipo_titular
                     WHEN 'CAMION' THEN (SELECT patente FROM camion WHERE id = d.titular_id)
                     ELSE (SELECT nombre || ' ' || apellido FROM camionero WHERE id = d.titular_id)
                   END AS titular
            FROM documento d
            WHERE d.vencimiento <= ?
            ORDER BY d.vencimiento""";
        var result = new ArrayList<Documento>();
        try (var c = Database.connect(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, limite.toString());
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Documento(
                            rs.getLong("id"),
                            rs.getString("tipo"),
                            TipoTitular.valueOf(rs.getString("tipo_titular")),
                            rs.getLong("titular_id"),
                            rs.getString("titular"),
                            LocalDate.parse(rs.getString("vencimiento")),
                            rs.getString("ruta_archivo")));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error consultando documentos", e);
        }
        return result;
    }
}
