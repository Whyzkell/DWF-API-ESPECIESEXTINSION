package sv.edu.udb.api_especieextionsion.mapping;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import sv.edu.udb.api_especieextionsion.controller.dto.DistribucionRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.DistribucionResponse;
import sv.edu.udb.api_especieextionsion.repository.domain.DistribucionGeografica;

@Mapper(config = MapperConfig.class)
public interface DistribucionMapper {

    // Del request a la entidad (el id lo genera la DB y la especie la pones en el service)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "especie", ignore = true)
    DistribucionGeografica toEntity(DistribucionRequest dto);

    // De entidad a response
    DistribucionResponse toDto(DistribucionGeografica entity);

    // Actualización "patch": ignora nulls y nunca pisa id/especie
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "especie", ignore = true)
    void updateEntity(@MappingTarget DistribucionGeografica entity, DistribucionRequest dto);
}



