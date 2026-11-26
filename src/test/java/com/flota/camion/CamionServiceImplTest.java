package com.flota.camion;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.Year;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.flota.BaseDatosTest;
import com.flota.camion.model.Camion;
import com.flota.camion.service.CamionService;
import com.flota.camion.service.CamionServiceImpl;
import com.flota.common.ValidacionException;
import com.flota.documento.model.Documento;
import com.flota.documento.model.TipoTitular;
import com.flota.documento.repository.DocumentoRepository;

class CamionServiceImplTest extends BaseDatosTest {
    private CamionService service;

    @BeforeEach
    void setUp() {
        service = new CamionServiceImpl();
    }

    @Test
    void creaYNormalizaPatente() {
        Camion c = service.create(new Camion(null, " ab 123 cd ", " Scania ", "", null));
        assertNotNull(c.id());
        assertEquals("AB123CD", c.patente());
        assertEquals("Scania", c.marca());
        assertNull(c.modelo());
        assertEquals(1, service.findAll().size());
    }

    @Test
    void aceptaPatenteVieja() {
        assertEquals("ABC123", service.create(new Camion(null, "abc-123", null, null, 2010)).patente());
    }

    @Test
    void rechazaPatenteInvalidaYAnioFueraDeRango() {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> service.create(new Camion(null, "123", null, null, Year.now().getValue() + 5)));
        assertTrue(e.getErrores().containsKey("patente"));
        assertTrue(e.getErrores().containsKey("anio"));
    }

    @Test
    void rechazaPatenteVacia() {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> service.create(new Camion(null, "  ", null, null, null)));
        assertEquals("Ingresá la patente.", e.getErrores().get("patente"));
    }

    @Test
    void rechazaPatenteDuplicada() {
        service.create(new Camion(null, "AB123CD", null, null, null));
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> service.create(new Camion(null, "ab123cd", null, null, null)));
        assertTrue(e.getErrores().containsKey("patente"));
    }

    @Test
    void modificaSinChocarConsigoMismo() {
        Camion c = service.create(new Camion(null, "AB123CD", "Scania", null, 2015));
        Camion editado = service.update(new Camion(c.id(), "AB123CD", "Volvo", "FH", 2016));
        assertEquals("Volvo", service.findById(c.id()).marca());
        assertEquals(2016, editado.anio());
    }

    @Test
    void eliminarBorraSusDocumentos() {
        Camion c = service.create(new Camion(null, "AB123CD", null, null, null));
        DocumentoRepository docs = new DocumentoRepository();
        docs.save(new Documento(null, "VTV", TipoTitular.CAMION, c.id(), null, LocalDate.now(), null, null));
        service.delete(c.id());
        assertTrue(service.findAll().isEmpty());
        assertTrue(docs.findAll().isEmpty());
    }
}
