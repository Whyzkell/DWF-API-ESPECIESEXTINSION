// UsuarioServiceImpl
package sv.edu.udb.api_especieextionsion.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sv.edu.udb.api_especieextionsion.controller.dto.UsuarioRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.UsuarioResponse;
import sv.edu.udb.api_especieextionsion.mapping.UsuarioMapper;
import sv.edu.udb.api_especieextionsion.repository.UsuarioRepository;
import sv.edu.udb.api_especieextionsion.repository.domain.Usuario;
import sv.edu.udb.api_especieextionsion.service.UsuarioService;
import sv.edu.udb.api_especieextionsion.shared.DuplicateResourceException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repo;
    private final UsuarioMapper mapper;

    @Override
    public UsuarioResponse crear(UsuarioRequest r) {
        if (repo.existsByUsername(r.getUsername()))
            throw new DuplicateResourceException("Username ya existe");
        if (repo.existsByEmail(r.getEmail()))
            throw new DuplicateResourceException("Email ya existe");

        Usuario u = mapper.toEntity(r);
        if (u.getActivo() == null) u.setActivo(Boolean.TRUE);
        if (u.getFechaRegistro() == null) u.setFechaRegistro(LocalDate.now());

        u = repo.save(u);
        return mapper.toDto(u);
    }

    @Override
    public List<UsuarioResponse> listar() {
        return repo.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public UsuarioResponse obtener(Long id) {
        Usuario u = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario id " + id + " no existe"));
        return mapper.toDto(u);
    }

    @Override
    public UsuarioResponse actualizar(Long id, UsuarioRequest r) {
        Usuario u = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario id " + id + " no existe"));

        if (!u.getUsername().equals(r.getUsername()) && repo.existsByUsername(r.getUsername()))
            throw new DuplicateResourceException("Username ya existe");
        if (!u.getEmail().equals(r.getEmail()) && repo.existsByEmail(r.getEmail()))
            throw new DuplicateResourceException("Email ya existe");

        mapper.updateEntity(u, r);
        u = repo.save(u);
        return mapper.toDto(u);
    }

    @Override
    public void eliminar(Long id) {
        if (!repo.existsById(id))
            throw new EntityNotFoundException("Usuario id " + id + " no existe");
        repo.deleteById(id);
    }
}


