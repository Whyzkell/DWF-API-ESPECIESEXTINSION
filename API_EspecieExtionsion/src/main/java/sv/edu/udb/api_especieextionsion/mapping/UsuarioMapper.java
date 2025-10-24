package sv.edu.udb.api_especieextionsion.mapping;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import sv.edu.udb.api_especieextionsion.controller.dto.UsuarioRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.UsuarioResponse;
import sv.edu.udb.api_especieextionsion.repository.domain.Usuario;

@Mapper(config = MapperConfig.class)
public interface UsuarioMapper {

    // Request -> Entity (id lo genera la DB)
    @Mapping(target = "id", ignore = true)
    Usuario toEntity(UsuarioRequest dto);

    // Entity -> Response
    UsuarioResponse toDto(Usuario entity);

    // Update parcial: ignora nulls y nunca pisa el id
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget Usuario entity, UsuarioRequest dto);
}





