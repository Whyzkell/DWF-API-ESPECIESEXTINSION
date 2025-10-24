package sv.edu.udb.api_especieextionsion.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieAmenazaLinkRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieAmenazaResponse;
import sv.edu.udb.api_especieextionsion.repository.domain.Amenaza;
import sv.edu.udb.api_especieextionsion.repository.domain.Especie;
import sv.edu.udb.api_especieextionsion.repository.domain.EspecieAmenaza;

import java.util.List;

@Mapper(config = MapstructConfig.class)
public interface EspecieAmenazaMapper {

    // Entity -> DTO
    @Mapping(target = "idVinculo", source = "id")
    @Mapping(target = "amenazaId", source = "amenaza.id")
    @Mapping(target = "codigo",    source = "amenaza.codigo")
    @Mapping(target = "tipo",      source = "amenaza.tipo")
    @Mapping(target = "descripcion", source = "amenaza.descripcion")
    @Mapping(target = "severidad", source = "severidad")
    EspecieAmenazaResponse toDto(EspecieAmenaza link);

    List<EspecieAmenazaResponse> toDtoList(List<EspecieAmenaza> links);

    // Composición: parámetros separados
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "especie", source = "especie")
    @Mapping(target = "amenaza", source = "amenaza")
    @Mapping(target = "severidad", source = "req.severidad")
    EspecieAmenaza toEntity(Especie especie, Amenaza amenaza, EspecieAmenazaLinkRequest req);
}




