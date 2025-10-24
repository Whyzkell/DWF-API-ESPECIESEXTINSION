// FuenteServiceImpl
package sv.edu.udb.api_especieextionsion.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.api_especieextionsion.controller.dto.FuenteRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.FuenteResponse;
import sv.edu.udb.api_especieextionsion.mapping.FuenteMapper;
import sv.edu.udb.api_especieextionsion.repository.FuenteRepository;
import sv.edu.udb.api_especieextionsion.repository.domain.Fuente;
import sv.edu.udb.api_especieextionsion.service.FuenteService;
import sv.edu.udb.api_especieextionsion.shared.DuplicateResourceException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FuenteServiceImpl implements FuenteService {

    private final FuenteRepository repo;
    private final FuenteMapper mapper;

    @Transactional
    @Override
    public FuenteResponse crear(FuenteRequest r) {
        if (repo.existsByNombre(r.getNombre())) {
            throw new DuplicateResourceException("La fuente ya existe con ese nombre");
        }
        Fuente f = mapper.toEntity(r);
        f = repo.save(f);
        return mapper.toDto(f);
    }

    @Transactional(readOnly = true)
    @Override
    public List<FuenteResponse> listar() {
        return repo.findAll().stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public FuenteResponse obtener(Long id) {
        Fuente f = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fuente id " + id + " no existe"));
        return mapper.toDto(f);
    }

    @Transactional
    @Override
    public FuenteResponse actualizar(Long id, FuenteRequest r) {
        Fuente f = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fuente id " + id + " no existe"));

        if (!f.getNombre().equals(r.getNombre()) && repo.existsByNombre(r.getNombre())) {
            throw new DuplicateResourceException("La fuente ya existe con ese nombre");
        }

        mapper.updateEntity(f, r);
        f = repo.save(f);
        return mapper.toDto(f);
    }

    @Transactional
    @Override
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Fuente id " + id + " no existe");
        }
        repo.deleteById(id);
    }
}

