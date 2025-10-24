package sv.edu.udb.api_especieextionsion.mapping;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieResponse;
import sv.edu.udb.api_especieextionsion.repository.domain.Especie;

import java.util.List;
import java.util.stream.Collectors;

@Primary
@Component
public class FakeEspecieMapper implements EspecieMapper {

    @Override
    public Especie toEntity(EspecieRequest dto) {
        if (dto == null) return null;
        return Especie.builder()
                .id(null)
                .nombreCientifico(dto.getNombreCientifico())
                .nombreComun(dto.getNombreComun())
                .tipo(dto.getTipo())
                .estadoConservacion(dto.getEstadoConservacion())
                .descripcion(dto.getDescripcion())
                .esEndemica(dto.getEsEndemica())
                .fechaRegistro(dto.getFechaRegistro())
                .build();
    }

    @Override
    public EspecieResponse toDto(Especie e) {
        if (e == null) return null;
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

    @Override
    public List<EspecieResponse> toDtoList(List<Especie> entities) {
        if (entities == null) return null;
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public void updateEntity(Especie entity, EspecieRequest dto) {
        if (entity == null || dto == null) return;
        // “patch”: solo si vienen no-nulos
        if (dto.getNombreCientifico() != null) entity.setNombreCientifico(dto.getNombreCientifico());
        if (dto.getNombreComun() != null) entity.setNombreComun(dto.getNombreComun());
        if (dto.getTipo() != null) entity.setTipo(dto.getTipo());
        if (dto.getEstadoConservacion() != null) entity.setEstadoConservacion(dto.getEstadoConservacion());
        if (dto.getDescripcion() != null) entity.setDescripcion(dto.getDescripcion());
        if (dto.getEsEndemica() != null) entity.setEsEndemica(dto.getEsEndemica());
        if (dto.getFechaRegistro() != null) entity.setFechaRegistro(dto.getFechaRegistro());
    }
}
