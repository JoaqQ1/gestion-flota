package com.flota.exportacion;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.flota.documento.model.EstadoVencimiento;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/** Genera archivos Excel (.xlsx) y PDF a partir de una TablaExportable. */
public final class Exportador {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color AZUL = new Color(0x2563EB);
    private static final Color GRIS_BORDE = new Color(0xD1D5DB);
    private static final Color GRIS_TEXTO = new Color(0x6B7280);

    private Exportador() {
    }

    // ------------------------------------------------------------------ Excel

    public static void excel(TablaExportable tabla, Path destino) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); OutputStream out = Files.newOutputStream(destino)) {
            XSSFSheet hoja = wb.createSheet(nombreHoja(tabla.titulo()));
            int columnas = tabla.encabezados().size();

            XSSFFont fuenteTitulo = wb.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 16);
            CellStyle estiloTitulo = wb.createCellStyle();
            estiloTitulo.setFont(fuenteTitulo);

            XSSFFont fuenteSubtitulo = wb.createFont();
            fuenteSubtitulo.setColor(xssf(GRIS_TEXTO));
            CellStyle estiloSubtitulo = wb.createCellStyle();
            estiloSubtitulo.setFont(fuenteSubtitulo);

            XSSFFont fuenteEncabezado = wb.createFont();
            fuenteEncabezado.setBold(true);
            fuenteEncabezado.setColor(xssf(Color.WHITE));
            XSSFCellStyle estiloEncabezado = conBorde(wb.createCellStyle());
            estiloEncabezado.setFont(fuenteEncabezado);
            estiloEncabezado.setFillForegroundColor(xssf(AZUL));
            estiloEncabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle estiloTexto = conBorde(wb.createCellStyle());
            XSSFCellStyle estiloFecha = conBorde(wb.createCellStyle());
            estiloFecha.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy"));
            estiloFecha.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT);

            Map<EstadoVencimiento, XSSFCellStyle> estilosEstado = new EnumMap<>(EstadoVencimiento.class);
            for (EstadoVencimiento e : EstadoVencimiento.values()) {
                XSSFFont f = wb.createFont();
                f.setBold(true);
                f.setColor(xssf(colorTexto(e)));
                XSSFCellStyle st = conBorde(wb.createCellStyle());
                st.setFont(f);
                st.setFillForegroundColor(xssf(colorFondo(e)));
                st.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                estilosEstado.put(e, st);
            }

            Cell titulo = hoja.createRow(0).createCell(0);
            titulo.setCellValue(tabla.titulo());
            titulo.setCellStyle(estiloTitulo);
            Cell subtitulo = hoja.createRow(1).createCell(0);
            subtitulo.setCellValue(tabla.subtitulo());
            subtitulo.setCellStyle(estiloSubtitulo);

            int filaEncabezado = 3;
            Row encabezado = hoja.createRow(filaEncabezado);
            for (int c = 0; c < columnas; c++) {
                Cell celda = encabezado.createCell(c);
                celda.setCellValue(tabla.encabezados().get(c));
                celda.setCellStyle(estiloEncabezado);
            }

            int r = filaEncabezado + 1;
            for (List<Object> fila : tabla.filas()) {
                Row row = hoja.createRow(r++);
                for (int c = 0; c < columnas; c++) {
                    Object v = fila.get(c);
                    Cell celda = row.createCell(c);
                    if (v instanceof LocalDate fecha) {
                        celda.setCellValue(fecha);
                        celda.setCellStyle(estiloFecha);
                    } else if (v instanceof Number n) {
                        celda.setCellValue(n.doubleValue());
                        celda.setCellStyle(estiloTexto);
                    } else if (v instanceof EstadoVencimiento e) {
                        celda.setCellValue(e.etiqueta());
                        celda.setCellStyle(estilosEstado.get(e));
                    } else {
                        celda.setCellValue(texto(v));
                        celda.setCellStyle(estiloTexto);
                    }
                }
            }

            hoja.createFreezePane(0, filaEncabezado + 1);
            if (!tabla.filas().isEmpty()) {
                hoja.setAutoFilter(new CellRangeAddress(filaEncabezado, r - 1, 0, columnas - 1));
            }
            for (int c = 0; c < columnas; c++) {
                hoja.autoSizeColumn(c);
                hoja.setColumnWidth(c, Math.min(hoja.getColumnWidth(c) + 1024, 60 * 256));
            }
            wb.write(out);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el archivo Excel", e);
        }
    }

    private static XSSFCellStyle conBorde(XSSFCellStyle st) {
        st.setBorderBottom(BorderStyle.THIN);
        st.setBorderTop(BorderStyle.THIN);
        st.setBorderLeft(BorderStyle.THIN);
        st.setBorderRight(BorderStyle.THIN);
        XSSFColor gris = xssf(GRIS_BORDE);
        st.setBottomBorderColor(gris);
        st.setTopBorderColor(gris);
        st.setLeftBorderColor(gris);
        st.setRightBorderColor(gris);
        return st;
    }

    private static XSSFColor xssf(Color c) {
        return new XSSFColor(new byte[] { (byte) c.getRed(), (byte) c.getGreen(), (byte) c.getBlue() }, null);
    }

    /** Excel no permite ciertos caracteres en el nombre de la hoja y lo limita a 31 caracteres. */
    private static String nombreHoja(String titulo) {
        String limpio = titulo.replaceAll("[\\\\/?*\\[\\]:]", " ").strip();
        return limpio.length() > 31 ? limpio.substring(0, 31) : limpio;
    }

    // ------------------------------------------------------------------ PDF

    public static void pdf(TablaExportable tabla, Path destino) {
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 32, 32);
        try (OutputStream out = Files.newOutputStream(destino)) {
            PdfWriter.getInstance(doc, out);
            doc.addTitle(tabla.titulo());
            doc.open();

            Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10, GRIS_TEXTO);
            Font fuenteEncabezado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font fuenteCelda = FontFactory.getFont(FontFactory.HELVETICA, 10);

            doc.add(new Paragraph(texto(tabla.titulo()), fuenteTitulo));
            Paragraph sub = new Paragraph(texto(tabla.subtitulo()), fuenteSubtitulo);
            sub.setSpacingAfter(14);
            doc.add(sub);

            int columnas = tabla.encabezados().size();
            PdfPTable t = new PdfPTable(columnas);
            t.setWidthPercentage(100);
            t.setHeaderRows(1);
            for (String h : tabla.encabezados()) {
                PdfPCell celda = celda(new Phrase(texto(h), fuenteEncabezado));
                celda.setBackgroundColor(AZUL);
                t.addCell(celda);
            }
            if (tabla.filas().isEmpty()) {
                PdfPCell vacia = celda(new Phrase("No hay datos para mostrar.", fuenteSubtitulo));
                vacia.setColspan(columnas);
                vacia.setHorizontalAlignment(Element.ALIGN_CENTER);
                t.addCell(vacia);
            }
            for (List<Object> fila : tabla.filas()) {
                for (int c = 0; c < columnas; c++) {
                    Object v = fila.get(c);
                    if (v instanceof EstadoVencimiento e) {
                        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, colorTexto(e));
                        PdfPCell celda = celda(new Phrase(e.etiqueta(), f));
                        celda.setBackgroundColor(colorFondo(e));
                        t.addCell(celda);
                    } else {
                        t.addCell(celda(new Phrase(texto(v), fuenteCelda)));
                    }
                }
            }
            doc.add(t);
            doc.close(); // tiene que cerrarse antes que el archivo para terminar de escribirlo
        } catch (IOException | DocumentException e) {
            throw new IllegalStateException("No se pudo guardar el archivo PDF", e);
        }
    }

    private static PdfPCell celda(Phrase frase) {
        PdfPCell c = new PdfPCell(frase);
        c.setPadding(6);
        c.setBorderColor(GRIS_BORDE);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return c;
    }

    // ------------------------------------------------------------------ comun

    /** Convierte el valor a texto, quitando emojis y simbolos que las fuentes del PDF no soportan. */
    static String texto(Object v) {
        if (v == null) {
            return "";
        }
        if (v instanceof LocalDate fecha) {
            return fecha.format(FECHA);
        }
        if (v instanceof EstadoVencimiento e) {
            return e.etiqueta();
        }
        return v.toString().replaceAll("[^\\x20-\\x7E\\u00A0-\\u00FF]", "").strip();
    }

    private static Color colorTexto(EstadoVencimiento e) {
        return switch (e) {
            case VENCIDO -> new Color(0xB91C1C);
            case POR_VENCER -> new Color(0xA16207);
            case VIGENTE -> new Color(0x15803D);
        };
    }

    private static Color colorFondo(EstadoVencimiento e) {
        return switch (e) {
            case VENCIDO -> new Color(0xFEE2E2);
            case POR_VENCER -> new Color(0xFEF3C7);
            case VIGENTE -> new Color(0xDCFCE7);
        };
    }
}
