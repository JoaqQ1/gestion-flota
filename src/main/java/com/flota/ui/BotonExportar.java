package com.flota.ui;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.flota.common.Textos;
import com.flota.exportacion.Exportador;
import com.flota.exportacion.TablaExportable;

/** Boton "Descargar" con las opciones PDF y Excel. Pregunta donde guardar y ofrece abrir el archivo. */
public final class BotonExportar {
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private BotonExportar() {
    }

    public static <T> JButton crear(Component padre, String titulo, List<Columna<T>> columnas,
            Supplier<List<T>> filas) {
        JButton boton = new JButton("Descargar");
        boton.setIcon(new FlechaAbajo());
        boton.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        boton.setIconTextGap(8);
        boton.setToolTipText("Guardar este listado como PDF o Excel");
        boton.addActionListener(e -> {
            // el menu se arma cada vez para que tome el tamaño de letra actual
            JPopupMenu menu = new JPopupMenu();
            JMenuItem pdf = new JMenuItem("Descargar PDF");
            JMenuItem excel = new JMenuItem("Descargar Excel");
            pdf.addActionListener(ev -> exportar(padre, titulo, columnas, filas.get(), false));
            excel.addActionListener(ev -> exportar(padre, titulo, columnas, filas.get(), true));
            menu.add(pdf);
            menu.add(excel);
            menu.show(boton, 0, boton.getHeight());
        });
        return boton;
    }

    /** Flechita hacia abajo dibujada a mano (asi no depende de que la fuente tenga el simbolo). */
    private static class FlechaAbajo implements javax.swing.Icon {
        @Override
        public void paintIcon(Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground());
            int w = getIconWidth();
            int h = getIconHeight();
            g2.fillPolygon(new int[] { x, x + w, x + w / 2 }, new int[] { y + h / 4, y + h / 4, y + h * 3 / 4 }, 3);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return Math.round(javax.swing.UIManager.getFont("defaultFont").getSize() * 0.6f);
        }

        @Override
        public int getIconHeight() {
            return getIconWidth();
        }
    }

    public static <T> void exportar(Component padre, String titulo, List<Columna<T>> columnas, List<T> items,
            boolean comoExcel) {
        try {
            if (items.isEmpty()) {
                Estilos.info(padre, "Nada para descargar", "El listado está vacío, no hay datos para descargar.");
                return;
            }
            String extension = comoExcel ? "xlsx" : "pdf";
            Path destino = elegirDestino(padre, titulo, extension);
            if (destino == null) {
                return;
            }
            TablaExportable tabla = armarTabla(titulo, columnas, items);
            if (comoExcel) {
                Exportador.excel(tabla, destino);
            } else {
                Exportador.pdf(tabla, destino);
            }
            Object[] opciones = { "Abrir archivo", "Cerrar" };
            int r = JOptionPane.showOptionDialog(padre,
                    "<html>Se guardó el archivo:<br><b>" + destino.getFileName() + "</b><br><br>"
                            + "<font color='#6B7280'>en " + destino.getParent() + "</font></html>",
                    "Listo", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opciones,
                    opciones[0]);
            if (r == 0) {
                Desktop.getDesktop().open(destino.toFile());
            }
        } catch (Exception e) {
            Estilos.error(padre, e);
        }
    }

    public static <T> TablaExportable armarTabla(String titulo, List<Columna<T>> columnas, List<T> items) {
        List<String> encabezados = new ArrayList<>();
        for (Columna<T> c : columnas) {
            encabezados.add(c.titulo());
        }
        List<List<Object>> filas = new ArrayList<>();
        for (T item : items) {
            List<Object> fila = new ArrayList<>();
            for (Columna<T> c : columnas) {
                fila.add(c.valor().apply(item));
            }
            filas.add(fila);
        }
        String subtitulo = "Generado el " + LocalDateTime.now().format(FECHA_HORA) + "  ·  "
                + items.size() + (items.size() == 1 ? " registro" : " registros");
        return new TablaExportable(titulo, subtitulo, encabezados, filas);
    }

    private static Path elegirDestino(Component padre, String titulo, String extension) {
        Path descargas = Path.of(System.getProperty("user.home"), "Downloads");
        JFileChooser chooser = new JFileChooser(Files.isDirectory(descargas) ? descargas.toFile() : null);
        chooser.setDialogTitle("Guardar " + titulo + " como " + extension.toUpperCase());
        chooser.setFileFilter(new FileNameExtensionFilter(extension.equals("pdf") ? "PDF" : "Excel", extension));
        String nombre = Textos.normalizar(titulo).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "")
                + "-" + LocalDate.now() + "." + extension;
        chooser.setSelectedFile(new File(chooser.getCurrentDirectory(), nombre));
        chooser.setApproveButtonText("Guardar");
        if (chooser.showSaveDialog(padre) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        Path destino = chooser.getSelectedFile().toPath();
        if (!destino.getFileName().toString().toLowerCase().endsWith("." + extension)) {
            destino = destino.resolveSibling(destino.getFileName() + "." + extension);
        }
        if (Files.exists(destino) && !Estilos.confirmar(padre, "El archivo ya existe",
                "Ya existe " + destino.getFileName() + ". ¿Querés reemplazarlo?", "Sí, reemplazar")) {
            return null;
        }
        return destino;
    }
}
