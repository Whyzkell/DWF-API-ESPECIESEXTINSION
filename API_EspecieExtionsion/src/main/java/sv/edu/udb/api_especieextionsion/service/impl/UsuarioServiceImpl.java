package sv.edu.udb.api_especieextionsion.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import sv.edu.udb.api_especieextionsion.controller.dto.*;
import sv.edu.udb.api_especieextionsion.domain.Usuario;
import sv.edu.udb.api_especieextionsion.repository.UsuarioRepository;
import sv.edu.udb.api_especieextionsion.service.UsuarioService;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService{
    private final UsuarioRepository repo;

    @Override
    public UsuarioResponse crear(UsuarioRequest r) {
        if (repo.existsByUsername(r.getUsername()))
            throw new DataIntegrityViolationException("Username ya existe");
        if (repo.existsByEmail(r.getEmail()))
            throw new DataIntegrityViolationException("Email ya existe");

        Usuario u = Usuario.builder()
                .username(r.getUsername())
                .nombreCompleto(r.getNombreCompleto())
                .email(r.getEmail())
                .rol(r.getRol())
                .activo(r.getActivo() != null ? r.getActivo() : Boolean.TRUE)
                .fechaRegistro(r.getFechaRegistro() != null ? r.getFechaRegistro() : LocalDate.now())
                .build();
        u = repo.save(u);
        return toResponse(u);
    }

    @Override
    public List<UsuarioResponse> listar() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public UsuarioResponse obtener(Long id) {
        Usuario u = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario id " + id + " no existe"));
        return toResponse(u);
    }

    @Override
    public UsuarioResponse actualizar(Long id, UsuarioRequest r) {
        Usuario u = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario id " + id + " no existe"));

        // Si cambian user/email, validar unicidad
        if (!u.getUsername().equals(r.getUsername()) && repo.existsByUsername(r.getUsername()))
            throw new DataIntegrityViolationException("Username ya existe");
        if (!u.getEmail().equals(r.getEmail()) && repo.existsByEmail(r.getEmail()))
            throw new DataIntegrityViolationException("Email ya existe");

        u.setUsername(r.getUsername());
        u.setNombreCompleto(r.getNombreCompleto());
        u.setEmail(r.getEmail());
        u.setRol(r.getRol());
        u.setActivo(r.getActivo());
        u.setFechaRegistro(r.getFechaRegistro() != null ? r.getFechaRegistro() : u.getFechaRegistro());

        repo.save(u);
        return toResponse(u);
    }

    @Override
    public void eliminar(Long id) {
        if (!repo.existsById(id))
            throw new EntityNotFoundException("Usuario id " + id + " no existe");
        repo.deleteById(id);
    }

    private UsuarioResponse toResponse(Usuario u){
        return UsuarioResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .nombreCompleto(u.getNombreCompleto())
                .email(u.getEmail())
                .rol(u.getRol())
                .activo(u.getActivo())
                .fechaRegistro(u.getFechaRegistro())
                .build();
    }
}
