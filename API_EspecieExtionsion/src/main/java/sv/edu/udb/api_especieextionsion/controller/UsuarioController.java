package sv.edu.udb.api_especieextionsion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import sv.edu.udb.api_especieextionsion.configuration.web.ApiErrorWrapper;
import sv.edu.udb.api_especieextionsion.controller.dto.*;
import sv.edu.udb.api_especieextionsion.service.UsuarioService;

import java.util.List;

@Tag(name = "Usuarios", description = "Gestión de usuarios")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService service;

    @Operation(summary = "Crear usuario")
    @ApiResponse(responseCode = "201", description = "Creado",
            content = @Content(schema = @Schema(implementation = UsuarioResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validación fallida",
            content = @Content(schema = @Schema(implementation = ApiErrorWrapper.class)))
    @ApiResponse(responseCode = "409", description = "Conflicto (username/email duplicado)",
            content = @Content(schema = @Schema(implementation = ApiErrorWrapper.class)))
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest req,
                                                 UriComponentsBuilder uriBuilder){
        UsuarioResponse creado = service.crear(req);
        var location = uriBuilder.path("/api/usuarios/{id}")
                .buildAndExpand(creado.getId()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @Operation(summary = "Listar usuarios")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping
    public List<UsuarioResponse> listar(){
        return service.listar();
    }

    @Operation(summary = "Obtener usuario por ID")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "No encontrado",
            content = @Content(schema = @Schema(implementation = ApiErrorWrapper.class)))
    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id){
        return service.obtener(id);
    }

    @Operation(summary = "Actualizar usuario")
    @ApiResponse(responseCode = "200", description = "Actualizado")
    @ApiResponse(responseCode = "404", description = "No encontrado",
            content = @Content(schema = @Schema(implementation = ApiErrorWrapper.class)))
    @ApiResponse(responseCode = "409", description = "Conflicto (username/email duplicado)",
            content = @Content(schema = @Schema(implementation = ApiErrorWrapper.class)))
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest req){
        return service.actualizar(id, req);
    }

    @Operation(summary = "Eliminar usuario")
    @ApiResponse(responseCode = "204", description = "Eliminado")
    @ApiResponse(responseCode = "404", description = "No encontrado",
            content = @Content(schema = @Schema(implementation = ApiErrorWrapper.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
