package sv.edu.udb.api_especieextionsion.mapping;

import org.springframework.stereotype.Component;
import sv.edu.udb.api_especieextionsion.controller.dto.UsuarioRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.UsuarioResponse;
import sv.edu.udb.api_especieextionsion.repository.domain.Usuario;

@Component
public class FakeUsuarioMapper implements UsuarioMapper {

    @Override
    public Usuario toEntity(UsuarioRequest dto) {
        if (dto == null) return null;
        return Usuario.builder()
                .id(null)
                .username(dto.getUsername())
                .nombreCompleto(dto.getNombreCompleto())
                .email(dto.getEmail())
                .rol(dto.getRol())
                .activo(dto.getActivo())
                .fechaRegistro(dto.getFechaRegistro())
                .build();
    }

    @Override
    public UsuarioResponse toDto(Usuario entity) {
        if (entity == null) return null;
        return UsuarioResponse.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .nombreCompleto(entity.getNombreCompleto())
                .email(entity.getEmail())
                .rol(entity.getRol())
                .activo(entity.getActivo())
                .fechaRegistro(entity.getFechaRegistro())
                .build();
    }

    @Override
    public void updateEntity(Usuario entity, UsuarioRequest dto) {
        if (entity == null || dto == null) return;
        if (dto.getUsername() != null) entity.setUsername(dto.getUsername());
        if (dto.getNombreCompleto() != null) entity.setNombreCompleto(dto.getNombreCompleto());
        if (dto.getEmail() != null) entity.setEmail(dto.getEmail());
        if (dto.getRol() != null) entity.setRol(dto.getRol());
        if (dto.getActivo() != null) entity.setActivo(dto.getActivo());
        if (dto.getFechaRegistro() != null) entity.setFechaRegistro(dto.getFechaRegistro());
    }
}


