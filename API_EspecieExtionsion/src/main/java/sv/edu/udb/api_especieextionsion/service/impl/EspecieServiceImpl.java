// EspecieServiceImpl
package sv.edu.udb.api_especieextionsion.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieResponse;
import sv.edu.udb.api_especieextionsion.mapping.EspecieMapper;
import sv.edu.udb.api_especieextionsion.repository.EspecieRepository;
import sv.edu.udb.api_especieextionsion.repository.domain.Especie;
import sv.edu.udb.api_especieextionsion.service.EspecieService;
import sv.edu.udb.api_especieextionsion.shared.DuplicateResourceException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EspecieServiceImpl implements EspecieService {

    private final EspecieRepository repo;
    private final EspecieMapper mapper;

    @Transactional
    @Override
    public EspecieResponse crear(EspecieRequest r) {
        if (repo.existsByNombreCientifico(r.getNombreCientifico())) {
            throw new DuplicateResourceException("Ya existe una especie con ese nombre científico");
        }
        Especie e = mapper.toEntity(r);
        e = repo.save(e);
        return mapper.toDto(e);
    }

    @Transactional
    @Override
    public EspecieResponse actualizar(Long id, EspecieRequest r) {
        Especie e = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Especie no encontrada"));

        if (!e.getNombreCientifico().equals(r.getNombreCientifico())
                && repo.existsByNombreCientifico(r.getNombreCientifico())) {
            throw new DuplicateResourceException("Nombre científico ya en uso");
        }

        mapper.updateEntity(e, r);
        e = repo.save(e);
        return mapper.toDto(e);
    }

    @Transactional(readOnly = true)
    @Override
    public EspecieResponse obtener(Long id) {
        Especie e = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Especie no encontrada"));
        return mapper.toDto(e);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EspecieResponse> listar() {
        return repo.findAll().stream().map(mapper::toDto).toList();
    }

    @Transactional
    @Override
    public void eliminar(Long id) {
        if (!repo.existsById(id)) throw new EntityNotFoundException("Especie no encontrada");
        repo.deleteById(id);
    }
}



