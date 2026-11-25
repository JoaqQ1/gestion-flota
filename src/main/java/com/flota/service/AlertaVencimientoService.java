package com.flota.service;

import com.flota.model.Documento;
import com.flota.notification.Notificador;
import com.flota.repository.DocumentoRepository;

import java.time.LocalDate;
import java.util.List;

/** Busca documentos vencidos o proximos a vencer y los envia a los notificadores. */
public class AlertaVencimientoService {
    private final DocumentoRepository repo;
    private final int diasAnticipacion;

    public AlertaVencimientoService(DocumentoRepository repo, int diasAnticipacion) {
        this.repo = repo;
        this.diasAnticipacion = diasAnticipacion;
    }

    public List<Documento> documentosPorVencer() {
        return repo.venceAntesDe(LocalDate.now().plusDays(diasAnticipacion));
    }

    public void notificar(List<Notificador> notificadores) {
        var docs = documentosPorVencer();
        if (docs.isEmpty()) return;
        for (var n : notificadores) {
            n.notificar(docs);
        }
    }

    public int getDiasAnticipacion() {
        return diasAnticipacion;
    }
}
