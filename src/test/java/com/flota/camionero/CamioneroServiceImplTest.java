package com.flota.camionero;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.flota.BaseDatosTest;
import com.flota.camionero.model.Camionero;
import com.flota.camionero.service.CamioneroService;
import com.flota.camionero.service.CamioneroServiceImpl;
import com.flota.common.ValidacionException;

class CamioneroServiceImplTest extends BaseDatosTest {
    private CamioneroService service;

    @BeforeEach
    void setUp() {
        service = new CamioneroServiceImpl();
    }

    @Test
    void creaYLimpiaDni() {
        Camionero c = service.create(new Camionero(null, " Juan ", "Pérez", "30.123.456", "11 2345-6789"));
        assertEquals("30123456", c.dni());
        assertEquals("Juan", c.nombre());
    }

    @Test
    void validaCamposObligatoriosYFormato() {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> service.create(new Camionero(null, "", " ", "12", "abc")));
        assertEquals(4, e.getErrores().size());
    }

    @Test
    void rechazaDniDuplicado() {
        service.create(new Camionero(null, "Juan", "Pérez", "30123456", null));
        assertThrows(ValidacionException.class,
                () -> service.create(new Camionero(null, "Pedro", "Gómez", "30.123.456", null)));
    }

    @Test
    void permiteVariosSinDni() {
        service.create(new Camionero(null, "Juan", "Pérez", null, null));
        service.create(new Camionero(null, "Pedro", "Gómez", "", null));
        assertEquals(2, service.findAll().size());
    }
}
