package com.flota.camionero.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.flota.camionero.model.Camionero;
import com.flota.camionero.repository.CamioneroRepository;
import com.flota.common.Textos;
import com.flota.common.ValidacionException;
import com.flota.documento.model.Documento;
import com.flota.documento.model.TipoTitular;
import com.flota.documento.repository.DocumentoRepository;
import com.flota.documento.service.ArchivoAdjuntoService;

public class CamioneroServiceImpl implements CamioneroService {
    private final CamioneroRepository repository;
    private final DocumentoRepository documentoRepository;
    private final ArchivoAdjuntoService adjuntos;

    public CamioneroServiceImpl() {
        this(new CamioneroRepository(), new DocumentoRepository(), new ArchivoAdjuntoService());
    }

    public CamioneroServiceImpl(CamioneroRepository repository, DocumentoRepository documentoRepository,
            ArchivoAdjuntoService adjuntos) {
        this.repository = repository;
        this.documentoRepository = documentoRepository;
        this.adjuntos = adjuntos;
    }

    @Override
    public List<Camionero> findAll() {
        return repository.findAll();
    }

    @Override
    public Camionero findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el camionero con id " + id));
    }

    @Override
    public Camionero create(Camionero camionero) {
        return repository.save(validar(camionero));
    }

    @Override
    public Camionero update(Camionero camionero) {
        if (camionero.id() == null) {
            throw new IllegalArgumentException("Para modificar un camionero se necesita su id");
        }
        return repository.update(validar(camionero));
    }

    @Override
    public void delete(Long id) {
        List<Documento> documentos = documentoRepository.findByTitular(TipoTitular.CAMIONERO, id);
        repository.delete(id);
        for (Documento d : documentos) {
            adjuntos.borrar(d.archivo());
        }
    }

    private Camionero validar(Camionero c) {
        Map<String, String> errores = new LinkedHashMap<>();

        String nombre = Textos.limpiar(c.nombre());
        String apellido = Textos.limpiar(c.apellido());
        if (nombre == null) {
            errores.put("nombre", "Ingresá el nombre.");
        }
        if (apellido == null) {
            errores.put("apellido", "Ingresá el apellido.");
        }

        String dni = Textos.limpiar(c.dni());
        if (dni != null) {
            dni = dni.replaceAll("[\\s.]", "");
            if (!dni.matches("\\d{7,8}")) {
                errores.put("dni", "El DNI debe tener 7 u 8 números.");
            } else {
                Optional<Camionero> existente = repository.findByDni(dni);
                if (existente.isPresent() && !existente.get().id().equals(c.id())) {
                    errores.put("dni", "Ya hay un camionero cargado con el DNI " + dni + ".");
                }
            }
        }

        String telefono = Textos.limpiar(c.telefono());
        if (telefono != null) {
            String soloNumeros = telefono.replaceAll("\\D", "");
            if (!telefono.matches("[+\\d\\s()-]+") || soloNumeros.length() < 8 || soloNumeros.length() > 15) {
                errores.put("telefono", "Revisá el teléfono: usá solo números, con código de área (ej: 11 2345-6789).");
            }
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
        return new Camionero(c.id(), nombre, apellido, dni, telefono);
    }
}
