package com.flota.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import com.flota.alerta.AlertaVencimientoService;
import com.flota.documento.model.Documento;
import com.flota.documento.model.EstadoVencimiento;
import com.flota.documento.service.DocumentoService;
import com.formdev.flatlaf.FlatClientProperties;

/** Pantalla de inicio: resumen de vencimientos y lista de lo que requiere atencion. */
public class InicioPanel extends JPanel implements Recargable {
    private final AlertaVencimientoService alertas;
    private final DocumentoService documentos;
    private final Consumer<Documento> editarDocumento;

    private final Tarjeta tarjetaVencidos;
    private final Tarjeta tarjetaPorVencer;
    private final Tarjeta tarjetaAlDia;
    private final ListaTableModel<Documento> modelo;
    private final JTable tabla;
    private final JLabel lblFecha = Estilos.subtitulo("");
    private final java.awt.CardLayout cards = new java.awt.CardLayout();
    private final JPanel centro = new JPanel(cards);
    private final JLabel todoOk = new JLabel();

    public InicioPanel(AlertaVencimientoService alertas, DocumentoService documentos,
            Consumer<EstadoVencimiento> verPorEstado, Consumer<Documento> editarDocumento) {
        super(new BorderLayout(0, 18));
        this.alertas = alertas;
        this.documentos = documentos;
        this.editarDocumento = editarDocumento;
        setBorder(Estilos.margen(20, 24));

        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.add(Estilos.titulo("Resumen de vencimientos"));
        encabezado.add(lblFecha);

        tarjetaVencidos = new Tarjeta("Vencidos", "Hay que renovarlos ya", EstadoVencimiento.VENCIDO, verPorEstado);
        tarjetaPorVencer = new Tarjeta("Por vencer", "",
                EstadoVencimiento.POR_VENCER, verPorEstado);
        tarjetaAlDia = new Tarjeta("Al día", "Todo en regla", EstadoVencimiento.VIGENTE, verPorEstado);
        JPanel tarjetas = new JPanel(new GridLayout(1, 3, 16, 0));
        tarjetas.add(tarjetaVencidos);
        tarjetas.add(tarjetaPorVencer);
        tarjetas.add(tarjetaAlDia);

        JPanel arriba = new JPanel(new BorderLayout(0, 18));
        arriba.add(encabezado, BorderLayout.NORTH);
        arriba.add(tarjetas, BorderLayout.CENTER);
        JLabel lblLista = new JLabel("Requieren atención");
        lblLista.putClientProperty(FlatClientProperties.STYLE_CLASS, "h3");
        JPanel filaLista = new JPanel(new FilaAdaptable(16));
        filaLista.add(lblLista);
        JPanel derechaLista = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 0));
        derechaLista.add(Estilos.subtitulo("Doble clic para actualizar un documento"));
        derechaLista.add(BotonExportar.crear(this, "Avisos de vencimiento",
                columnasAvisos(alertas::getDiasAnticipacion), alertas::documentosPorVencer));
        filaLista.add(derechaLista);
        arriba.add(filaLista, BorderLayout.SOUTH);
        add(arriba, BorderLayout.NORTH);

        modelo = new ListaTableModel<>(columnasAvisos(alertas::getDiasAnticipacion));
        tabla = new TablaAjustable(modelo, modelo.getColumnas().stream().mapToInt(Columna::ancho).toArray());
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        Renderers.registrar(tabla);
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                if (e.getClickCount() == 2 && fila >= 0) {
                    editarDocumento.accept(modelo.get(fila));
                }
            }
        });
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB)));

        todoOk.setHorizontalAlignment(SwingConstants.CENTER);
        todoOk.setForeground(Estilos.GRIS_TEXTO);
        JPanel vacio = new JPanel(new BorderLayout());
        vacio.setBorder(BorderFactory.createDashedBorder(new Color(0xD1D5DB), 6, 4));
        vacio.add(todoOk, BorderLayout.CENTER);

        centro.add(scroll, "tabla");
        centro.add(vacio, "vacio");
        add(centro, BorderLayout.CENTER);
    }

    @Override
    public void recargar() {
        LocalDate hoy = LocalDate.now();
        String fecha = hoy.format(Estilos.FECHA_LARGA);
        lblFecha.setText("Hoy es " + fecha + ".");
        int dias = alertas.getDiasAnticipacion();
        tarjetaPorVencer.setDetalle("En los próximos " + dias + (dias == 1 ? " día" : " días"));
        todoOk.setText("<html><center><font size='+1' color='#15803D'><b>✓ ¡Todo en orden!</b></font>"
                + "<br><br>No hay documentos vencidos ni por vencer en los próximos " + dias
                + (dias == 1 ? " día." : " días.") + "</center></html>");
        try {
            List<Documento> todos = documentos.findAll();
            int vencidos = 0;
            int porVencer = 0;
            for (Documento d : todos) {
                EstadoVencimiento e = d.estado(hoy, alertas.getDiasAnticipacion());
                if (e == EstadoVencimiento.VENCIDO) {
                    vencidos++;
                } else if (e == EstadoVencimiento.POR_VENCER) {
                    porVencer++;
                }
            }
            tarjetaVencidos.setCantidad(vencidos);
            tarjetaPorVencer.setCantidad(porVencer);
            tarjetaAlDia.setCantidad(todos.size() - vencidos - porVencer);

            List<Documento> atencion = alertas.documentosPorVencer();
            modelo.setDatos(atencion);
            cards.show(centro, atencion.isEmpty() ? "vacio" : "tabla");
        } catch (RuntimeException e) {
            Estilos.error(this, e);
        }
    }

    public static List<Columna<Documento>> columnasAvisos(IntSupplier diasAviso) {
        return List.of(
                new Columna<>("Estado", EstadoVencimiento.class,
                        (Documento d) -> d.estado(LocalDate.now(), diasAviso.getAsInt()), 120),
                Columna.texto("Documento", Documento::tipo, 190),
                Columna.texto("De", (Documento d) -> d.tipoTitular().etiqueta(), 100),
                Columna.texto("Camión / Camionero", Documento::descripcionTitular, 220),
                new Columna<>("Vence", LocalDate.class, Documento::vencimiento, 100),
                Columna.texto("Situación", (Documento d) -> d.situacion(LocalDate.now()), 160));
    }

    /** Tarjeta de color con un numero grande. Al hacer clic muestra esos documentos. */
    private static class Tarjeta extends JPanel {
        private final JLabel numero = new JLabel("0");
        private final JLabel lblDetalle = new JLabel();

        Tarjeta(String titulo, String detalle, EstadoVencimiento estado, Consumer<EstadoVencimiento> alClic) {
            super(new BorderLayout(0, 2));
            Color color = Estilos.color(estado);
            setBackground(Estilos.fondo(estado));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 6, 0, 0, color),
                    BorderFactory.createEmptyBorder(14, 18, 14, 18)));
            putClientProperty(FlatClientProperties.STYLE, "arc: 14");
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Ver estos documentos");

            JLabel lblTitulo = new JLabel(titulo);
            lblTitulo.setForeground(color);
            lblTitulo.putClientProperty(FlatClientProperties.STYLE_CLASS, "h4");
            numero.setForeground(color);
            numero.putClientProperty(FlatClientProperties.STYLE, "font: bold +18");
            lblDetalle.setText(detalle);
            lblDetalle.setForeground(color.darker());

            add(lblTitulo, BorderLayout.NORTH);
            add(numero, BorderLayout.CENTER);
            add(lblDetalle, BorderLayout.SOUTH);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    alClic.accept(estado);
                }
            });
        }

        void setDetalle(String texto) {
            lblDetalle.setText(texto);
        }

        void setCantidad(int n) {
            numero.setText(String.valueOf(n));
        }
    }
}
