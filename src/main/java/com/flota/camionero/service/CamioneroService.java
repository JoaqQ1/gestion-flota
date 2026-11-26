package com.flota.camionero.service;

import java.util.List;

import com.flota.camionero.model.Camionero;

public interface CamioneroService {

    List<Camionero> findAll();

    Camionero findById(Long id);

    Camionero create(Camionero camionero);

    Camionero update(Camionero camionero);

    /** Elimina el camionero junto con sus documentos y archivos adjuntos. */
    void delete(Long id);
}
