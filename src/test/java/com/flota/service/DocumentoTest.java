package com.flota.service;

import com.flota.model.Documento;
import com.flota.model.TipoTitular;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DocumentoTest {
    private Documento doc(LocalDate venc) {
        return new Documento(1L, "VTV", TipoTitular.CAMION, 1, "AB123CD", venc, null);
    }

    @Test
    void calculaDiasParaVencer() {
        var hoy = LocalDate.of(2026, 1, 1);
        assertEquals(10, doc(hoy.plusDays(10)).diasParaVencer(hoy));
        assertFalse(doc(hoy.plusDays(10)).vencido(hoy));
    }

    @Test
    void detectaVencido() {
        var hoy = LocalDate.of(2026, 1, 1);
        assertTrue(doc(hoy.minusDays(1)).vencido(hoy));
    }
}
