// src/main/java/sv/edu/udb/api_especieextionsion/mapping/EspecieAmenazaMapper.java
package sv.edu.udb.api_especieextionsion.mapping;

import org.mapstruct.*;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieAmenazaResponse;
import sv.edu.udb.api_especieextionsion.repository.domain.EspecieAmenaza;

@Mapper(config = MapperConfig.class)
public interface EspecieAmenazaMapper {

    @Mapping(target = "idVinculo", source = "id")
    @Mapping(target = "amenazaId", source = "amenaza.id")
    @Mapping(target = "codigo",    source = "amenaza.codigo")
    @Mapping(target = "tipo",      source = "amenaza.tipo")
    @Mapping(target = "descripcion", source = "amenaza.descripcion")
    EspecieAmenazaResponse toDto(EspecieAmenaza link);
}




