package com.flota.camion.service;

import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;

import com.flota.camion.exception.PatenteDuplicadaException;
import com.flota.camion.model.Camion;
import com.flota.camion.repository.CamionRepository;
import com.flota.common.Textos;
import com.flota.common.ValidacionException;
import com.flota.documento.model.Documento;
import com.flota.documento.model.TipoTitular;
import com.flota.documento.repository.DocumentoRepository;
import com.flota.documento.service.ArchivoAdjuntoService;

public class CamionServiceImpl implements CamionService {
    /** Formato viejo (ABC123) o Mercosur (AB123CD). */
    private static final Pattern PATENTE = Pattern.compile("^[A-Z]{3}\\d{3}$|^[A-Z]{2}\\d{3}[A-Z]{2}$");

    private final CamionRepository repository;
    private final DocumentoRepository documentoRepository;
    private final ArchivoAdjuntoService adjuntos;

    public CamionServiceImpl() {
        this(new CamionRepository(), new DocumentoRepository(), new ArchivoAdjuntoService());
    }

    public CamionServiceImpl(CamionRepository repository, DocumentoRepository documentoRepository,
            ArchivoAdjuntoService adjuntos) {
        this.repository = repository;
        this.documentoRepository = documentoRepository;
        this.adjuntos = adjuntos;
    }

    @Override
    public List<Camion> findAll() {
        return repository.findAll();
    }

    @Override
    public Camion findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el camion con id " + id));
    }

    @Override
    public Camion create(Camion camion) {
        Camion valido = validar(camion);
        try {
            return repository.save(valido);
        } catch (PatenteDuplicadaException e) {
            throw patenteRepetida();
        }
    }

    @Override
    public Camion update(Camion camion) {
        if (camion.id() == null) {
            throw new IllegalArgumentException("Para modificar un camion se necesita su id");
        }
        Camion valido = validar(camion);
        try {
            return repository.update(valido);
        } catch (PatenteDuplicadaException e) {
            throw patenteRepetida();
        }
    }

    @Override
    public void delete(Long id) {
        List<Documento> documentos = documentoRepository.findByTitular(TipoTitular.CAMION, id);
        repository.delete(id);
        for (Documento d : documentos) {
            adjuntos.borrar(d.archivo());
        }
    }

    /** Normaliza los datos y verifica que sean correctos. */
    private Camion validar(Camion camion) {
        Map<String, String> errores = new LinkedHashMap<>();

        String patente = normalizarPatente(camion.patente());
        if (patente == null) {
            errores.put("patente", "Ingresá la patente.");
        } else if (!PATENTE.matcher(patente).matches()) {
            errores.put("patente", "La patente debe tener el formato ABC123 o AB123CD.");
        } else {
            Optional<Camion> existente = repository.findByPatente(patente);
            if (existente.isPresent() && !existente.get().id().equals(camion.id())) {
                errores.put("patente", "Ya hay un camión cargado con la patente " + patente + ".");
            }
        }

        Integer anio = camion.anio();
        int maximo = Year.now().getValue() + 1;
        if (anio != null && (anio < 1950 || anio > maximo)) {
            errores.put("anio", "El año debe estar entre 1950 y " + maximo + ".");
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
        return new Camion(camion.id(), patente, Textos.limpiar(camion.marca()), Textos.limpiar(camion.modelo()), anio);
    }

    static String normalizarPatente(String patente) {
        String limpia = Textos.limpiar(patente);
        if (limpia == null) {
            return null;
        }
        return limpia.replaceAll("[\\s.-]", "").toUpperCase();
    }

    private ValidacionException patenteRepetida() {
        return ValidacionException.deCampo("patente", "Ya hay un camión cargado con esa patente.");
    }
}
