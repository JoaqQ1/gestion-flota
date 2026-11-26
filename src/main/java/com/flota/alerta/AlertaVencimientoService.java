package com.flota.alerta;

import java.time.LocalDate;
import java.util.List;

import com.flota.config.ConfiguracionService;
import com.flota.documento.model.Documento;
import com.flota.documento.repository.DocumentoRepository;
import com.flota.notification.Notificador;

/** Busca documentos vencidos o proximos a vencer y los envia a los notificadores. */
public class AlertaVencimientoService {
    private final DocumentoRepository repo;
    private final ConfiguracionService configuracion;

    public AlertaVencimientoService(DocumentoRepository repo, ConfiguracionService configuracion) {
        this.repo = repo;
        this.configuracion = configuracion;
    }

    /** Vencidos y los que vencen dentro de los dias de anticipacion, ordenados por fecha. */
    public List<Documento> documentosPorVencer() {
        return repo.venceAntesDe(LocalDate.now().plusDays(getDiasAnticipacion()));
    }

    public void notificar(List<Notificador> notificadores) {
        List<Documento> docs = documentosPorVencer();
        if (docs.isEmpty()) {
            return;
        }
        for (Notificador n : notificadores) {
            n.notificar(docs);
        }
    }

    /** Dias de anticipacion configurados por el usuario. */
    public int getDiasAnticipacion() {
        return configuracion.getDiasAviso();
    }
}
