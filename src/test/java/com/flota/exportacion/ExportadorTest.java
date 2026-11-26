package com.flota.exportacion;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.flota.documento.model.EstadoVencimiento;

class ExportadorTest {
    @TempDir
    Path carpeta;

    private final TablaExportable tabla = new TablaExportable("Documentos", "Generado hoy",
            List.of("Estado", "Documento", "Vence", "Año", "Archivo"),
            List.of(
                    List.of(EstadoVencimiento.VENCIDO, "VTV / RTO", LocalDate.of(2026, 11, 22), 2019, "Sí"),
                    java.util.Arrays.asList(EstadoVencimiento.VIGENTE, "Psicofísico", LocalDate.of(2027, 6, 14), null, "")));

    @Test
    void generaExcelConEncabezadosYValores() throws Exception {
        Path archivo = carpeta.resolve("docs.xlsx");
        Exportador.excel(tabla, archivo);
        try (InputStream in = Files.newInputStream(archivo); XSSFWorkbook wb = new XSSFWorkbook(in)) {
            Sheet hoja = wb.getSheetAt(0);
            assertEquals("Documentos", hoja.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Estado", hoja.getRow(3).getCell(0).getStringCellValue());
            assertEquals("Vencido", hoja.getRow(4).getCell(0).getStringCellValue());
            assertEquals(LocalDate.of(2026, 11, 22), hoja.getRow(4).getCell(2).getLocalDateTimeCellValue().toLocalDate());
            assertEquals(2019, hoja.getRow(4).getCell(3).getNumericCellValue());
            assertEquals("Sí", hoja.getRow(4).getCell(4).getStringCellValue());
        }
    }

    @Test
    void generaPdf() throws Exception {
        Path archivo = carpeta.resolve("docs.pdf");
        Exportador.pdf(tabla, archivo);
        byte[] bytes = Files.readAllBytes(archivo);
        assertTrue(bytes.length > 500);
        assertEquals("%PDF", new String(bytes, 0, 4));
    }

    @Test
    void quitaEmojisPeroMantieneAcentos() {
        assertEquals("Sí, Camión", Exportador.texto("📎 Sí, Camión"));
    }
}
