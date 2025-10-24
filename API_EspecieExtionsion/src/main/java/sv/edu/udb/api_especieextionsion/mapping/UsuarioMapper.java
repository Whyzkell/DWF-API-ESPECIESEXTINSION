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

    // Request -> Entity (id lo genera la DB). Password se maneja aparte (service / register).
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // <-- importante
    Usuario toEntity(UsuarioRequest dto);

    // Entity -> Response (no expone password, así que no hay nada que hacer)
    UsuarioResponse toDto(Usuario entity);

    // Update parcial sobre la entidad, ignorando nulls. Password no se toca aquí.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // <-- importante
    void updateEntity(@MappingTarget Usuario entity, UsuarioRequest dto);
}






