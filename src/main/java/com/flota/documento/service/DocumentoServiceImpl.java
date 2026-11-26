package com.flota.documento.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.flota.common.Textos;
import com.flota.common.ValidacionException;
import com.flota.documento.model.Documento;
import com.flota.documento.repository.DocumentoRepository;

public class DocumentoServiceImpl implements DocumentoService {
    private final DocumentoRepository repository;
    private final ArchivoAdjuntoService adjuntos;

    public DocumentoServiceImpl() {
        this(new DocumentoRepository(), new ArchivoAdjuntoService());
    }

    public DocumentoServiceImpl(DocumentoRepository repository, ArchivoAdjuntoService adjuntos) {
        this.repository = repository;
        this.adjuntos = adjuntos;
    }

    @Override
    public List<Documento> findAll() {
        return repository.findAll();
    }

    @Override
    public Documento findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el documento con id " + id));
    }

    @Override
    public Documento guardar(Documento documento, Path archivoNuevo, boolean quitarArchivo) {
        Documento valido = validar(documento, archivoNuevo);

        String anterior = null;
        if (valido.id() != null) {
            anterior = findById(valido.id()).archivo();
        }

        String archivoFinal = quitarArchivo ? null : anterior;
        String copiado = null;
        if (archivoNuevo != null) {
            copiado = adjuntos.guardar(archivoNuevo);
            archivoFinal = copiado;
        }

        Documento guardado;
        try {
            Documento aGuardar = valido.conArchivo(archivoFinal);
            guardado = valido.id() == null ? repository.save(aGuardar) : repository.update(aGuardar);
        } catch (RuntimeException e) {
            adjuntos.borrar(copiado);
            throw e;
        }

        if (anterior != null && !Objects.equals(anterior, archivoFinal)) {
            adjuntos.borrar(anterior);
        }
        return guardado;
    }

    @Override
    public void delete(Long id) {
        Documento d = findById(id);
        repository.delete(id);
        adjuntos.borrar(d.archivo());
    }

    private Documento validar(Documento d, Path archivoNuevo) {
        Map<String, String> errores = new LinkedHashMap<>();
        String tipo = Textos.limpiar(d.tipo());
        if (tipo == null) {
            errores.put("tipo", "Elegí o escribí el tipo de documento.");
        }
        if (d.tipoTitular() == null || d.titularId() == null) {
            errores.put("titular", "Elegí a quién pertenece el documento.");
        }
        if (d.vencimiento() == null) {
            errores.put("vencimiento", "Elegí la fecha de vencimiento.");
        }
        if (archivoNuevo != null && !Files.isRegularFile(archivoNuevo)) {
            errores.put("archivo", "No se encontró el archivo elegido.");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
        return new Documento(d.id(), tipo, d.tipoTitular(), d.titularId(), d.descripcionTitular(),
                d.vencimiento(), d.archivo(), Textos.limpiar(d.observaciones()));
    }
}
