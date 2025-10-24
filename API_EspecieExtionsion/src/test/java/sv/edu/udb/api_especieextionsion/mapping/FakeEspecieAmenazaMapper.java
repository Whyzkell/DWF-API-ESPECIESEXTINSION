// src/test/java/sv/edu/udb/api_especieextionsion/mapping/FakeEspecieAmenazaMapper.java
package sv.edu.udb.api_especieextionsion.mapping;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieAmenazaLinkRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieAmenazaResponse;
import sv.edu.udb.api_especieextionsion.repository.domain.Amenaza;
import sv.edu.udb.api_especieextionsion.repository.domain.Especie;
import sv.edu.udb.api_especieextionsion.repository.domain.EspecieAmenaza;

import java.util.List;
import java.util.stream.Collectors;

@Primary // si por alguna razón también está el mapper real, este fake tendrá prioridad en tests
@Component
public class FakeEspecieAmenazaMapper implements EspecieAmenazaMapper {

    @Override
    public EspecieAmenazaResponse toDto(EspecieAmenaza l) {
        if (l == null) return null;
        var a = l.getAmenaza();
        return EspecieAmenazaResponse.builder()
                .idVinculo(l.getId())
                .amenazaId(a != null ? a.getId() : null)
                .codigo(a != null ? a.getCodigo() : null)
                .tipo(a != null ? a.getTipo() : null)
                .descripcion(a != null ? a.getDescripcion() : null)
                .severidad(l.getSeveridad())
                .build();
    }

    @Override
    public List<EspecieAmenazaResponse> toDtoList(List<EspecieAmenaza> links) {
        if (links == null) return null;
        return links.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EspecieAmenaza toEntity(Especie especie, Amenaza amenaza, EspecieAmenazaLinkRequest req) {
        return null;
    }
}

