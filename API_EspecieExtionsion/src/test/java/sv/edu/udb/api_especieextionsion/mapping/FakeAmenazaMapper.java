package sv.edu.udb.api_especieextionsion.mapping;

import org.springframework.stereotype.Component;
import sv.edu.udb.api_especieextionsion.controller.dto.AmenazaRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.AmenazaResponse;
import sv.edu.udb.api_especieextionsion.repository.domain.Amenaza;

@Component
public class FakeAmenazaMapper implements AmenazaMapper {

    @Override
    public AmenazaResponse toDto(Amenaza a) {
        if (a == null) return null;
        return AmenazaResponse.builder()
                .id(a.getId())
                .codigo(a.getCodigo())
                .tipo(a.getTipo())
                .descripcion(a.getDescripcion())
                .build();
    }

    @Override
    public Amenaza toEntity(AmenazaRequest r) {
        if (r == null) return null;
        return Amenaza.builder()
                .codigo(r.getCodigo())
                .tipo(r.getTipo())
                .descripcion(r.getDescripcion())
                .build();
    }

    @Override
    public void updateEntity(Amenaza a, AmenazaRequest r) {
        if (a == null || r == null) return;
        if (r.getCodigo() != null) a.setCodigo(r.getCodigo());
        if (r.getTipo() != null) a.setTipo(r.getTipo());
        if (r.getDescripcion() != null) a.setDescripcion(r.getDescripcion());
    }
}
