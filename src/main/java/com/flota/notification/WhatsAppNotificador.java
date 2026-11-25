package com.flota.notification;

import com.flota.model.Documento;

import java.util.List;

/**
 * Punto de extension para avisos por WhatsApp.
 * Opciones: WhatsApp Business Cloud API (Meta), Twilio, u otro proveedor.
 * Pendiente de implementar.
 */
public class WhatsAppNotificador implements Notificador {
    @Override
    public void notificar(List<Documento> documentos) {
        throw new UnsupportedOperationException("Notificacion por WhatsApp aun no implementada");
    }
}
