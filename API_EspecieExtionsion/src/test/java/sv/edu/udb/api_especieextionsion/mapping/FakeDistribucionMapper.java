package sv.edu.udb.api_especieextionsion.mapping;

import org.springframework.stereotype.Component;
import sv.edu.udb.api_especieextionsion.controller.dto.DistribucionRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.DistribucionResponse;
import sv.edu.udb.api_especieextionsion.repository.domain.DistribucionGeografica;

@Component
public class FakeDistribucionMapper implements DistribucionMapper {

    @Override
    public DistribucionGeografica toEntity(DistribucionRequest dto) {
        if (dto == null) return null;
        return DistribucionGeografica.builder()
                // id lo genera la DB, especie la setea el service
                .region(dto.getRegion())
                .ecosistema(dto.getEcosistema())
                .latitud(dto.getLatitud())
                .longitud(dto.getLongitud())
                .precisionMetros(dto.getPrecisionMetros())
                .fechaObservacion(dto.getFechaObservacion())
                .build();
    }

    @Override
    public DistribucionResponse toDto(DistribucionGeografica entity) {
        if (entity == null) return null;
        return DistribucionResponse.builder()
                .id(entity.getId())
                .region(entity.getRegion())
                .ecosistema(entity.getEcosistema())
                .latitud(entity.getLatitud())
                .longitud(entity.getLongitud())
                .precisionMetros(entity.getPrecisionMetros())
                .fechaObservacion(entity.getFechaObservacion())
                .build();
    }

    @Override
    public void updateEntity(DistribucionGeografica entity, DistribucionRequest dto) {
        if (entity == null || dto == null) return;
        if (dto.getRegion() != null) entity.setRegion(dto.getRegion());
        if (dto.getEcosistema() != null) entity.setEcosistema(dto.getEcosistema());
        if (dto.getLatitud() != null) entity.setLatitud(dto.getLatitud());
        if (dto.getLongitud() != null) entity.setLongitud(dto.getLongitud());
        if (dto.getPrecisionMetros() != null) entity.setPrecisionMetros(dto.getPrecisionMetros());
        if (dto.getFechaObservacion() != null) entity.setFechaObservacion(dto.getFechaObservacion());
        // especie e id NO se tocan aquí
    }
}
