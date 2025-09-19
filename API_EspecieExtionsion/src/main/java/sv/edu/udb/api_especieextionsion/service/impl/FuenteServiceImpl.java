package sv.edu.udb.api_especieextionsion.service.impl;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import sv.edu.udb.api_especieextionsion.controller.dto.*;
import sv.edu.udb.api_especieextionsion.domain.Fuente;
import sv.edu.udb.api_especieextionsion.repository.FuenteRepository;
import sv.edu.udb.api_especieextionsion.service.FuenteService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FuenteServiceImpl implements FuenteService{
    private final FuenteRepository repo;

    @Override
    public FuenteResponse crear(FuenteRequest r) {
        if (repo.existsByNombre(r.getNombre()))
            throw new DataIntegrityViolationException("La fuente ya existe con ese nombre");

        Fuente f = Fuente.builder()
                .nombre(r.getNombre())
                .descripcion(r.getDescripcion())
                .tipo(r.getTipo())
                .enlace(r.getEnlace())
                .fechaPublicacion(r.getFechaPublicacion())
                .build();
        f = repo.save(f);
        return toResponse(f);
    }

    @Override
    public List<FuenteResponse> listar() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public FuenteResponse obtener(Long id) {
        Fuente f = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fuente id " + id + " no existe"));
        return toResponse(f);
    }

    @Override
    public FuenteResponse actualizar(Long id, FuenteRequest r) {
        Fuente f = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fuente id " + id + " no existe"));

        if (!f.getNombre().equals(r.getNombre()) && repo.existsByNombre(r.getNombre()))
            throw new DataIntegrityViolationException("La fuente ya existe con ese nombre");

        f.setNombre(r.getNombre());
        f.setDescripcion(r.getDescripcion());
        f.setTipo(r.getTipo());
        f.setEnlace(r.getEnlace());
        f.setFechaPublicacion(r.getFechaPublicacion());

        repo.save(f);
        return toResponse(f);
    }

    @Override
    public void eliminar(Long id) {
        if (!repo.existsById(id))
            throw new EntityNotFoundException("Fuente id " + id + " no existe");
        repo.deleteById(id);
    }

    private FuenteResponse toResponse(Fuente f){
        return FuenteResponse.builder()
                .id(f.getId())
                .nombre(f.getNombre())
                .descripcion(f.getDescripcion())
                .tipo(f.getTipo())
                .enlace(f.getEnlace())
                .fechaPublicacion(f.getFechaPublicacion())
                .build();
    }
}
