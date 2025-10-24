package sv.edu.udb.api_especieextionsion.mapping;

import org.springframework.stereotype.Component;
import sv.edu.udb.api_especieextionsion.controller.dto.FuenteRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.FuenteResponse;
import sv.edu.udb.api_especieextionsion.repository.domain.Fuente;

@Component
public class FakeFuenteMapper implements FuenteMapper {

    @Override
    public Fuente toEntity(FuenteRequest dto) {
        if (dto == null) return null;
        return Fuente.builder()
                .id(null) // lo genera la DB
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .tipo(dto.getTipo())
                .enlace(dto.getEnlace())
                .fechaPublicacion(dto.getFechaPublicacion())
                .build();
    }

    @Override
    public FuenteResponse toDto(Fuente entity) {
        if (entity == null) return null;
        return FuenteResponse.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .tipo(entity.getTipo())
                .enlace(entity.getEnlace())
                .fechaPublicacion(entity.getFechaPublicacion())
                .build();
    }

    @Override
    public void updateEntity(Fuente entity, FuenteRequest dto) {
        if (entity == null || dto == null) return;
        // patch: solo copia no-nulos
        if (dto.getNombre() != null) entity.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) entity.setDescripcion(dto.getDescripcion());
        if (dto.getTipo() != null) entity.setTipo(dto.getTipo());
        if (dto.getEnlace() != null) entity.setEnlace(dto.getEnlace());
        if (dto.getFechaPublicacion() != null) entity.setFechaPublicacion(dto.getFechaPublicacion());
    }
}
