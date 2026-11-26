package com.flota.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

import com.flota.common.Textos;

/**
 * Pantalla generica de listado: titulo, boton para agregar, buscador, tabla y botones Editar/Eliminar.
 * Cada ABM (camiones, camioneros, documentos) la extiende y completa lo especifico.
 */
public abstract class PanelListado<T> extends JPanel implements Recargable {
    protected final ListaTableModel<T> modelo;
    protected final JTable tabla;
    protected final JPanel barra;
    private final TableRowSorter<ListaTableModel<T>> sorter;
    private final JTextField busqueda;
    private final JButton btnEditar;
    private final JButton btnEliminar;
    private final JLabel contador = new JLabel();
    private final CardLayout cards = new CardLayout();
    private final JPanel centro = new JPanel(cards);
    private final JLabel mensajeVacio;
    private final String textoVacio;

    protected PanelListado(String titulo, String subtitulo, String textoNuevo, String placeholderBusqueda,
            String textoVacio, List<Columna<T>> columnas) {
        super(new BorderLayout(0, 12));
        this.textoVacio = textoVacio;
        setBorder(Estilos.margen(20, 24));

        // Encabezado
        JPanel encabezado = new JPanel(new BorderLayout());
        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Estilos.titulo(titulo));
        textos.add(Estilos.subtitulo(subtitulo));
        encabezado.add(textos, BorderLayout.CENTER);
        JButton btnNuevo = Estilos.botonPrimario("+  " + textoNuevo);
        btnNuevo.addActionListener(e -> ejecutar(this::nuevo));
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        derecha.add(btnNuevo);
        encabezado.add(derecha, BorderLayout.EAST);

        // Barra de busqueda y acciones
        busqueda = Estilos.campoBusqueda(placeholderBusqueda);
        btnEditar = new JButton("Editar");
        btnEliminar = Estilos.botonPeligro("Eliminar");
        btnEditar.addActionListener(e -> conSeleccionado(this::editar));
        btnEliminar.addActionListener(e -> conSeleccionado(this::eliminar));
        barra = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 4));
        barra.add(busqueda);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        contador.setForeground(Estilos.GRIS_TEXTO);
        acciones.add(contador);
        acciones.add(BotonExportar.crear(this, titulo, columnas, this::filasVisibles));
        acciones.add(btnEditar);
        acciones.add(btnEliminar);
        // si la ventana es angosta, los botones de la derecha pasan abajo del buscador
        JPanel filaBarra = new JPanel(new FilaAdaptable(16));
        filaBarra.add(barra);
        filaBarra.add(acciones);

        JPanel arriba = new JPanel(new BorderLayout(0, 14));
        arriba.add(encabezado, BorderLayout.NORTH);
        arriba.add(filaBarra, BorderLayout.SOUTH);
        add(arriba, BorderLayout.NORTH);

        // Tabla
        modelo = new ListaTableModel<>(columnas);
        tabla = new TablaAjustable(modelo, columnas.stream().mapToInt(Columna::ancho).toArray());
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        Renderers.registrar(tabla);
        sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);
        tabla.getSelectionModel().addListSelectionListener(e -> actualizarBotones());
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabla.rowAtPoint(e.getPoint()) >= 0) {
                    conSeleccionado(PanelListado.this::editar);
                }
            }
        });
        tabla.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "editar");
        tabla.getActionMap().put("editar", accion(() -> conSeleccionado(this::editar)));
        tabla.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar");
        tabla.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "eliminar");
        tabla.getActionMap().put("eliminar", accion(() -> conSeleccionado(this::eliminar)));

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0xE5E7EB)));

        mensajeVacio = new JLabel(textoVacio, SwingConstants.CENTER);
        mensajeVacio.setForeground(Estilos.GRIS_TEXTO);
        mensajeVacio.putClientProperty("FlatLaf.styleClass", "h4");
        JPanel vacio = new JPanel(new BorderLayout());
        vacio.setBorder(BorderFactory.createDashedBorder(new java.awt.Color(0xD1D5DB), 6, 4));
        vacio.add(mensajeVacio, BorderLayout.CENTER);

        centro.add(scroll, "tabla");
        centro.add(vacio, "vacio");
        add(centro, BorderLayout.CENTER);

        busqueda.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { aplicarFiltro(); }
            public void removeUpdate(DocumentEvent e) { aplicarFiltro(); }
            public void changedUpdate(DocumentEvent e) { aplicarFiltro(); }
        });
        btnEditar.setEnabled(false);
        btnEliminar.setEnabled(false);
    }

    protected abstract List<T> cargar();

    protected abstract void nuevo();

    protected abstract void editar(T item);

    protected abstract void eliminar(T item);

    /** Filtro adicional (por ejemplo por estado). Por defecto muestra todo. */
    protected boolean filtroExtra(T item) {
        return true;
    }

    @Override
    public void recargar() {
        try {
            modelo.setDatos(cargar());
        } catch (RuntimeException e) {
            Estilos.error(this, e);
        }
        aplicarFiltro();
    }

    public void buscar(String texto) {
        busqueda.setText(texto);
    }

    protected void aplicarFiltro() {
        String texto = Textos.normalizar(busqueda.getText().strip());
        sorter.setRowFilter(new RowFilter<ListaTableModel<T>, Integer>() {
            @Override
            public boolean include(Entry<? extends ListaTableModel<T>, ? extends Integer> entry) {
                T item = modelo.get(entry.getIdentifier());
                return filtroExtra(item) && (texto.isEmpty() || coincide(entry, texto));
            }
        });
        int visibles = tabla.getRowCount();
        int total = modelo.getRowCount();
        boolean filtrando = visibles != total;
        if (total == 0) {
            mensajeVacio.setText(textoVacio);
            cards.show(centro, "vacio");
        } else if (visibles == 0) {
            mensajeVacio.setText("No hay resultados para la búsqueda.");
            cards.show(centro, "vacio");
        } else {
            cards.show(centro, "tabla");
        }
        contador.setText(filtrando ? visibles + " de " + total : total + (total == 1 ? " registro" : " registros"));
        actualizarBotones();
    }

    private boolean coincide(RowFilter.Entry<? extends ListaTableModel<T>, ? extends Integer> entry, String texto) {
        for (int i = 0; i < entry.getValueCount(); i++) {
            Object v = entry.getValue(i);
            if (v != null && Textos.normalizar(v.toString()).contains(texto)) {
                return true;
            }
        }
        return false;
    }

    /** Las filas que se ven en la tabla, en el mismo orden (respeta busqueda, filtros y orden). */
    public List<T> filasVisibles() {
        List<T> visibles = new java.util.ArrayList<>();
        for (int i = 0; i < tabla.getRowCount(); i++) {
            visibles.add(modelo.get(tabla.convertRowIndexToModel(i)));
        }
        return visibles;
    }

    protected T seleccionado() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? null : modelo.get(tabla.convertRowIndexToModel(fila));
    }

    private void conSeleccionado(java.util.function.Consumer<T> accion) {
        T item = seleccionado();
        if (item != null) {
            ejecutar(() -> accion.accept(item));
        }
    }

    /** Ejecuta una accion mostrando un mensaje amigable si algo falla. */
    protected void ejecutar(Runnable accion) {
        try {
            accion.run();
        } catch (RuntimeException e) {
            Estilos.error(this, e);
        }
    }

    private void actualizarBotones() {
        boolean haySeleccion = tabla.getSelectedRow() >= 0;
        btnEditar.setEnabled(haySeleccion);
        btnEliminar.setEnabled(haySeleccion);
        alCambiarSeleccion(haySeleccion);
    }

    /** Para que las subclases habiliten sus propios botones. */
    protected void alCambiarSeleccion(boolean haySeleccion) {
    }

    private static AbstractAction accion(Runnable r) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                r.run();
            }
        };
    }
}
