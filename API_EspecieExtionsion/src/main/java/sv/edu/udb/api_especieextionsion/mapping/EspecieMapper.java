package sv.edu.udb.api_especieextionsion.mapping;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import sv.edu.udb.api_especieextionsion.controller.dto.EspecieRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieResponse;
import sv.edu.udb.api_especieextionsion.repository.domain.Especie;

import java.util.List;

@Mapper(config = MapperConfig.class)
public interface EspecieMapper {

    // Request -> Entity (id lo genera la DB)
    @Mapping(target = "id", ignore = true)
    Especie toEntity(EspecieRequest dto);

    // Entity -> Response
    EspecieResponse toDto(Especie entity);

    // List<Entity> -> List<Response>
    List<EspecieResponse> toDtoList(List<Especie> entities);

    // Update parcial: ignora nulls y nunca toca el id
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget Especie entity, EspecieRequest dto);
}





