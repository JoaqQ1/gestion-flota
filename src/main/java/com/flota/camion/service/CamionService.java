package com.flota.camion.service;

import java.util.List;

import com.flota.camion.model.Camion;

public interface CamionService {

    List<Camion> findAll();

    Camion findById(Long id);

    /** Valida y guarda un camion nuevo. Lanza ValidacionException si hay datos incorrectos. */
    Camion create(Camion camion);

    /** Valida y guarda los cambios de un camion existente. */
    Camion update(Camion camion);

    /** Elimina el camion junto con sus documentos y archivos adjuntos. */
    void delete(Long id);
}
