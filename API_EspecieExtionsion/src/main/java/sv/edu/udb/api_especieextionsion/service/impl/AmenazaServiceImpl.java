// AmenazaServiceImpl
package sv.edu.udb.api_especieextionsion.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.api_especieextionsion.controller.dto.AmenazaRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.AmenazaResponse;
import sv.edu.udb.api_especieextionsion.mapping.AmenazaMapper;
import sv.edu.udb.api_especieextionsion.repository.AmenazaRepository;
import sv.edu.udb.api_especieextionsion.repository.domain.Amenaza;
import sv.edu.udb.api_especieextionsion.service.AmenazaService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AmenazaServiceImpl implements AmenazaService {

    private final AmenazaRepository repo;
    private final AmenazaMapper mapper;

    @Transactional
    @Override
    public AmenazaResponse crear(AmenazaRequest r) {
        if (repo.existsByCodigo(r.getCodigo())) {
            throw new IllegalArgumentException("El código de amenaza ya existe");
        }
        Amenaza a = mapper.toEntity(r);
        a = repo.save(a);
        return mapper.toDto(a);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AmenazaResponse> listar() {
        return repo.findAll().stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public AmenazaResponse buscarPorId(Long id) {
        Amenaza a = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Amenaza no encontrada"));
        return mapper.toDto(a);
    }

    @Transactional
    @Override
    public AmenazaResponse actualizar(Long id, AmenazaRequest r) {
        Amenaza a = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Amenaza no encontrada"));

        if (!a.getCodigo().equals(r.getCodigo()) && repo.existsByCodigo(r.getCodigo())) {
            throw new IllegalArgumentException("El código de amenaza ya existe");
        }

        // ⚠️ MapStruct: primero la entidad, luego el request
        mapper.updateEntity(a, r);
        a = repo.save(a);
        return mapper.toDto(a);
    }

    @Transactional
    @Override
    public void eliminar(Long id) {
        if (!repo.existsById(id)) throw new EntityNotFoundException("Amenaza no encontrada");
        repo.deleteById(id);
    }
}



