package com.flota.config;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.flota.BaseDatosTest;
import com.flota.camion.model.Camion;
import com.flota.camion.service.CamionServiceImpl;
import com.flota.common.ValidacionException;
import com.flota.repository.Database;

/** No se llama a usar() porque guarda la preferencia real del usuario. */
class CarpetaDatosServiceTest extends BaseDatosTest {
    private final CarpetaDatosService service = new CarpetaDatosService();

    @Test
    void creaSubcarpetaGestionFlotaSiLaElegidaNoTieneDatos() throws Exception {
        Path elegida = Files.createDirectories(carpeta.resolve("otra/Documentos"));
        assertEquals(elegida.resolve("GestionFlota"), service.carpetaFinal(elegida));
    }

    @Test
    void usaLaCarpetaTalCualSiYaTieneDatosOSeLlamaGestionFlota() throws Exception {
        Path conDatos = Files.createDirectories(carpeta.resolve("respaldo"));
        Files.writeString(conDatos.resolve("flota.db"), "");
        assertEquals(conDatos, service.carpetaFinal(conDatos));
        Path llamada = carpeta.resolve("x/GestionFlota");
        assertEquals(llamada, service.carpetaFinal(llamada));
    }

    @Test
    void noPermiteLaMismaCarpetaNiUnaInterna() {
        assertThrows(ValidacionException.class, () -> service.validar(AppConfig.carpetaDatos()));
        assertThrows(ValidacionException.class, () -> service.validar(AppConfig.carpetaDatos().resolve("sub")));
    }

    @Test
    void copiaBaseYAdjuntosSinBorrarLosOriginales() throws Exception {
        Path origen = carpeta.resolve("origen");
        AppConfig.setCarpetaDatos(origen);
        Database.init();
        new CamionServiceImpl().create(new Camion(null, "AB123CD", null, null, null));
        Files.writeString(AppConfig.carpetaAdjuntos().resolve("abc_vtv.pdf"), "pdf");

        Path destino = carpeta.resolve("nueva/GestionFlota");
        service.validar(destino);
        service.copiarDatosActuales(destino);

        assertTrue(Files.exists(destino.resolve("documentos/abc_vtv.pdf")));
        assertTrue(Files.exists(origen.resolve("flota.db")));
        AppConfig.setCarpetaDatos(destino);
        assertEquals("AB123CD", new CamionServiceImpl().findAll().get(0).patente());
    }
}
