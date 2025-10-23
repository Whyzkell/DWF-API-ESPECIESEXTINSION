package sv.edu.udb.api_especieextionsion.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.api_especieextionsion.controller.dto.AmenazaRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.AmenazaResponse;
import sv.edu.udb.api_especieextionsion.domain.Amenaza;
import sv.edu.udb.api_especieextionsion.repository.AmenazaRepository;
import sv.edu.udb.api_especieextionsion.service.AmenazaService;
import sv.edu.udb.api_especieextionsion.shared.DuplicateResourceException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AmenazaServiceImpl implements AmenazaService {
    private final AmenazaRepository repo;

    @Transactional
    @Override
    public AmenazaResponse crear(AmenazaRequest r) {
        if (repo.existsByCodigo(r.getCodigo())) {
            throw new DuplicateResourceException("El código de amenaza ya existe");
        }
        Amenaza a = Amenaza.builder()
                .codigo(r.getCodigo())
                .tipo(r.getTipo())
                .descripcion(r.getDescripcion())
                .build();
        a = repo.save(a);
        return toResponse(a);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AmenazaResponse> listar() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public AmenazaResponse buscarPorId(Long id) {
        Amenaza a = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Amenaza no encontrada"));
        return toResponse(a);
    }

    @Transactional
    @Override
    public AmenazaResponse actualizar(Long id, AmenazaRequest r) {
        Amenaza a = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Amenaza no encontrada"));

        // Si cambia el código, validar unicidad antes de guardar
        if (!a.getCodigo().equals(r.getCodigo()) && repo.existsByCodigo(r.getCodigo())) {
            throw new DuplicateResourceException("El código de amenaza ya existe");
        }

        a.setCodigo(r.getCodigo());
        a.setTipo(r.getTipo());
        a.setDescripcion(r.getDescripcion());

        a = repo.save(a);
        return toResponse(a);
    }

    @Transactional
    @Override
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Amenaza no encontrada");
        }
        repo.deleteById(id);
    }

    private AmenazaResponse toResponse(Amenaza a) {
        return AmenazaResponse.builder()
                .id(a.getId())
                .codigo(a.getCodigo())
                .tipo(a.getTipo())
                .descripcion(a.getDescripcion())
                .build();
    }
}

