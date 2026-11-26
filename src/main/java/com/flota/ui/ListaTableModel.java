package com.flota.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class ListaTableModel<T> extends AbstractTableModel {
    private final List<Columna<T>> columnas;
    private List<T> datos = new ArrayList<>();

    public ListaTableModel(List<Columna<T>> columnas) {
        this.columnas = columnas;
    }

    public void setDatos(List<T> datos) {
        this.datos = new ArrayList<>(datos);
        fireTableDataChanged();
    }

    public T get(int fila) {
        return datos.get(fila);
    }

    public List<Columna<T>> getColumnas() {
        return columnas;
    }

    @Override
    public int getRowCount() {
        return datos.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnas.get(col).titulo();
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return columnas.get(col).tipo();
    }

    @Override
    public Object getValueAt(int fila, int col) {
        return columnas.get(col).valor().apply(datos.get(fila));
    }
}
