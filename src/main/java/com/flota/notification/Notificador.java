package com.flota.notification;

import com.flota.model.Documento;

import java.util.List;

/**
 * Canal de aviso de vencimientos. Hoy existe el aviso en pantalla;
 * para WhatsApp se agrega una implementacion nueva (ver WhatsAppNotificador).
 */
public interface Notificador {
    void notificar(List<Documento> documentos);
}
