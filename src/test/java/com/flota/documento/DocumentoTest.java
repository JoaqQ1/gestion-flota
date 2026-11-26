package com.flota.documento;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.flota.documento.model.Documento;
import com.flota.documento.model.EstadoVencimiento;
import com.flota.documento.model.TipoTitular;

class DocumentoTest {
    private static final LocalDate HOY = LocalDate.of(2026, 1, 1);

    private Documento doc(LocalDate venc) {
        return new Documento(1L, "VTV", TipoTitular.CAMION, 1L, "AB123CD", venc, null, null);
    }

    @Test
    void calculaEstado() {
        assertEquals(EstadoVencimiento.VENCIDO, doc(HOY.minusDays(1)).estado(HOY, 30));
        assertEquals(EstadoVencimiento.POR_VENCER, doc(HOY).estado(HOY, 30));
        assertEquals(EstadoVencimiento.POR_VENCER, doc(HOY.plusDays(30)).estado(HOY, 30));
        assertEquals(EstadoVencimiento.VIGENTE, doc(HOY.plusDays(31)).estado(HOY, 30));
    }

    @Test
    void textoDeSituacion() {
        assertEquals("Venció hace 3 días", doc(HOY.minusDays(3)).situacion(HOY));
        assertEquals("Venció ayer", doc(HOY.minusDays(1)).situacion(HOY));
        assertEquals("Vence hoy", doc(HOY).situacion(HOY));
        assertEquals("Vence mañana", doc(HOY.plusDays(1)).situacion(HOY));
        assertEquals("Faltan 12 días", doc(HOY.plusDays(12)).situacion(HOY));
    }
}
