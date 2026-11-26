package com.flota.documento.service;

import java.nio.file.Path;
import java.util.List;

import com.flota.documento.model.Documento;

public interface DocumentoService {

    List<Documento> findAll();

    Documento findById(Long id);

    /**
     * Crea o modifica un documento (segun tenga id o no).
     *
     * @param archivoNuevo archivo elegido por el usuario para adjuntar, o null para no cambiarlo
     * @param quitarArchivo true si el usuario quito el adjunto actual
     */
    Documento guardar(Documento documento, Path archivoNuevo, boolean quitarArchivo);

    void delete(Long id);
}
