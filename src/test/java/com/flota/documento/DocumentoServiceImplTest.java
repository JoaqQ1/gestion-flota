package com.flota.documento;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.flota.BaseDatosTest;
import com.flota.alerta.AlertaVencimientoService;
import com.flota.camion.model.Camion;
import com.flota.camion.service.CamionServiceImpl;
import com.flota.common.ValidacionException;
import com.flota.config.AppConfig;
import com.flota.config.ConfiguracionService;
import com.flota.documento.model.Documento;
import com.flota.documento.model.TipoTitular;
import com.flota.documento.repository.DocumentoRepository;
import com.flota.documento.service.DocumentoService;
import com.flota.documento.service.DocumentoServiceImpl;

class DocumentoServiceImplTest extends BaseDatosTest {
    private DocumentoService service;
    private Camion camion;

    @BeforeEach
    void setUp() {
        service = new DocumentoServiceImpl();
        camion = new CamionServiceImpl().create(new Camion(null, "AB123CD", null, null, null));
    }

    private Documento doc(String tipo, LocalDate venc) {
        return new Documento(null, tipo, TipoTitular.CAMION, camion.id(), null, venc, null, null);
    }

    @Test
    void guardaConArchivoYMuestraPatente() throws Exception {
        Path origen = Files.writeString(carpeta.resolve("vtv.pdf"), "pdf");
        Documento d = service.guardar(doc("VTV", LocalDate.now().plusDays(10)), origen, false);
        Documento leido = service.findById(d.id());
        assertEquals("AB123CD", leido.descripcionTitular());
        assertTrue(Files.exists(AppConfig.carpetaAdjuntos().resolve(leido.archivo())));
    }

    @Test
    void reemplazarYEliminarBorraArchivos() throws Exception {
        Path a = Files.writeString(carpeta.resolve("a.pdf"), "a");
        Path b = Files.writeString(carpeta.resolve("b.pdf"), "b");
        Documento d = service.guardar(doc("Seguro", LocalDate.now()), a, false);
        Path archivoA = AppConfig.carpetaAdjuntos().resolve(d.archivo());

        Documento editado = service.guardar(service.findById(d.id()), b, false);
        Path archivoB = AppConfig.carpetaAdjuntos().resolve(editado.archivo());
        assertFalse(Files.exists(archivoA));
        assertTrue(Files.exists(archivoB));

        service.delete(d.id());
        assertFalse(Files.exists(archivoB));
    }

    @Test
    void validaCamposObligatorios() {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> service.guardar(new Documento(null, " ", TipoTitular.CAMION, null, null, null, null, null),
                        null, false));
        assertEquals(3, e.getErrores().size());
    }

    @Test
    void alertaIncluyeVencidosYProximosPeroNoLosLejanos() {
        service.guardar(doc("VTV", LocalDate.now().minusDays(2)), null, false);
        service.guardar(doc("Seguro", LocalDate.now().plusDays(30)), null, false);
        service.guardar(doc("RUTA", LocalDate.now().plusDays(31)), null, false);
        AlertaVencimientoService alertas = new AlertaVencimientoService(new DocumentoRepository(), new ConfiguracionService());
        assertEquals(2, alertas.documentosPorVencer().size());
    }
}
