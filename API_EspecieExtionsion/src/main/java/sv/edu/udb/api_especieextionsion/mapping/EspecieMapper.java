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

@Mapper(config = MapstructConfig.class)
public interface EspecieMapper {

    @Mapping(target = "id", ignore = true)
    Especie toEntity(EspecieRequest dto);

    EspecieResponse toDto(Especie entity);

    List<EspecieResponse> toDtoList(List<Especie> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget Especie entity, EspecieRequest dto);
}





