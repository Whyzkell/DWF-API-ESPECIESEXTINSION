// src/main/java/sv/edu/udb/api_especieextionsion/mapping/AmenazaMapper.java
package sv.edu.udb.api_especieextionsion.mapping;

import org.mapstruct.*;
import sv.edu.udb.api_especieextionsion.controller.dto.AmenazaRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.AmenazaResponse;
import sv.edu.udb.api_especieextionsion.repository.domain.Amenaza;

@Mapper(config = MapperConfig.class)
public interface AmenazaMapper {

    AmenazaResponse toDto(Amenaza a);

    @Mapping(target = "id", ignore = true)
    Amenaza toEntity(AmenazaRequest r);

    @BeanMapping(ignoreByDefault = false) // usa config global; solo recuerda:
    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget Amenaza a, AmenazaRequest r);
}




