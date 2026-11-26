package com.flota.config;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.flota.BaseDatosTest;
import com.flota.alerta.AlertaVencimientoService;
import com.flota.camion.model.Camion;
import com.flota.camion.service.CamionServiceImpl;
import com.flota.common.ValidacionException;
import com.flota.documento.model.Documento;
import com.flota.documento.model.TipoTitular;
import com.flota.documento.repository.DocumentoRepository;

class ConfiguracionServiceTest extends BaseDatosTest {

    @Test
    void usaTreintaDiasPorDefecto() {
        assertEquals(30, new ConfiguracionService().getDiasAviso());
    }

    @Test
    void guardaLosDiasYSeMantienenAlReabrir() {
        new ConfiguracionService().setDiasAviso(45);
        assertEquals(45, new ConfiguracionService().getDiasAviso());
    }

    @Test
    void rechazaValoresFueraDeRango() {
        ConfiguracionService config = new ConfiguracionService();
        assertThrows(ValidacionException.class, () -> config.setDiasAviso(0));
        assertThrows(ValidacionException.class, () -> config.setDiasAviso(366));
        assertEquals(30, config.getDiasAviso());
    }

    @Test
    void lasAlertasUsanLosDiasConfigurados() {
        Camion c = new CamionServiceImpl().create(new Camion(null, "AB123CD", null, null, null));
        DocumentoRepository repo = new DocumentoRepository();
        repo.save(new Documento(null, "VTV", TipoTitular.CAMION, c.id(), null, LocalDate.now().plusDays(20), null, null));
        ConfiguracionService config = new ConfiguracionService();
        AlertaVencimientoService alertas = new AlertaVencimientoService(repo, config);

        assertEquals(1, alertas.documentosPorVencer().size());
        config.setDiasAviso(10);
        assertTrue(alertas.documentosPorVencer().isEmpty());
    }
}
