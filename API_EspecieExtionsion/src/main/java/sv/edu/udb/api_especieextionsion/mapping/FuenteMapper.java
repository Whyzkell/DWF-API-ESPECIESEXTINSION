package sv.edu.udb.api_especieextionsion.mapping;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import sv.edu.udb.api_especieextionsion.controller.dto.FuenteRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.FuenteResponse;
import sv.edu.udb.api_especieextionsion.repository.domain.Fuente;

@Mapper(config = MapperConfig.class)
public interface FuenteMapper {

    // Request -> Entity (id lo genera la DB)
    @Mapping(target = "id", ignore = true)
    Fuente toEntity(FuenteRequest dto);

    // Entity -> Response
    FuenteResponse toDto(Fuente entity);

    // Update parcial: ignora nulls y nunca toca el id
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget Fuente entity, FuenteRequest dto);
}





