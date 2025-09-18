package sv.edu.udb.api_especieextionsion.service.impl;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sv.edu.udb.api_especieextionsion.controller.dto.*;
import sv.edu.udb.api_especieextionsion.repository.EspecieRepository;
import sv.edu.udb.api_especieextionsion.domain.Especie;
import sv.edu.udb.api_especieextionsion.service.EspecieService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EspecieServiceImpl implements EspecieService {
    private final EspecieRepository repo;

    @Transactional
    @Override
    public EspecieResponse crear(EspecieRequest r) {
        Especie e = mapToEntity(new Especie(), r);
        try {
            e = repo.save(e);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Ya existe una especie con ese nombre científico");
        }
        return mapToResponse(e);
    }

    @Transactional
    @Override
    public EspecieResponse actualizar(Long id, EspecieRequest r) {
        Especie e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Especie no encontrada"));
        mapToEntity(e, r);
        try {
            e = repo.save(e);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Nombre científico ya en uso");
        }
        return mapToResponse(e);
    }

    @Transactional(readOnly = true)
    @Override
    public EspecieResponse obtener(Long id) {
        Especie e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Especie no encontrada"));
        return mapToResponse(e);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EspecieResponse> listar() {
        return repo.findAll().stream().map(this::mapToResponse).toList();
    }

    @Transactional
    @Override
    public void eliminar(Long id) {
        if (!repo.existsById(id)) throw new EntityNotFoundException("Especie no encontrada");
        repo.deleteById(id);
    }

    // ----- mapping manual -----
    private Especie mapToEntity(Especie e, EspecieRequest r){
        e.setNombreCientifico(r.getNombreCientifico());
        e.setNombreComun(r.getNombreComun());
        e.setTipo(r.getTipo());
        e.setEstadoConservacion(r.getEstadoConservacion());
        e.setDescripcion(r.getDescripcion());
        e.setEsEndemica(r.getEsEndemica());
        e.setFechaRegistro(r.getFechaRegistro());
        return e;
    }

    private EspecieResponse mapToResponse(Especie e){
        return EspecieResponse.builder()
                .id(e.getId())
                .nombreCientifico(e.getNombreCientifico())
                .nombreComun(e.getNombreComun())
                .tipo(e.getTipo())
                .estadoConservacion(e.getEstadoConservacion())
                .descripcion(e.getDescripcion())
                .esEndemica(e.getEsEndemica())
                .fechaRegistro(e.getFechaRegistro())
                .build();
    }
}
