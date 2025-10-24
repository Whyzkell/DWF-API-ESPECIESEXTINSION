package sv.edu.udb.api_especieextionsion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import sv.edu.udb.api_especieextionsion.configuration.web.ApiErrorWrapper;
import sv.edu.udb.api_especieextionsion.controller.dto.*;
import sv.edu.udb.api_especieextionsion.service.FuenteService;

import java.util.List;

@Tag(name = "Fuentes", description = "Gestión de fuentes/bibliografía")
@RestController
@RequestMapping("/api/fuentes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FuenteController {

    private final FuenteService service;

    @Operation(summary = "Crear fuente")
    @ApiResponse(responseCode = "201", description = "Creada")
    @ApiResponse(responseCode = "400", description = "Validación fallida",
            content = @Content(schema = @Schema(implementation = ApiErrorWrapper.class)))
    @ApiResponse(responseCode = "409", description = "Conflicto (nombre duplicado)",
            content = @Content(schema = @Schema(implementation = ApiErrorWrapper.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    public ResponseEntity<FuenteResponse> crear(@Valid @RequestBody FuenteRequest req,
                                                UriComponentsBuilder uriBuilder){
        FuenteResponse creada = service.crear(req);
        var location = uriBuilder.path("/api/fuentes/{id}")
                .buildAndExpand(creada.getId()).toUri();
        return ResponseEntity.created(location).body(creada);
    }

    @Operation(summary = "Listar fuentes")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR','LECTOR')")
    public List<FuenteResponse> listar(){
        return service.listar();
    }

    @Operation(summary = "Obtener fuente por ID")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "No encontrada",
            content = @Content(schema = @Schema(implementation = ApiErrorWrapper.class)))
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR','LECTOR')")
    public FuenteResponse obtener(@PathVariable Long id){
        return service.obtener(id);
    }

    @Operation(summary = "Actualizar fuente")
    @ApiResponse(responseCode = "200", description = "Actualizada")
    @ApiResponse(responseCode = "404", description = "No encontrada",
            content = @Content(schema = @Schema(implementation = ApiErrorWrapper.class)))
    @ApiResponse(responseCode = "409", description = "Conflicto (nombre duplicado)",
            content = @Content(schema = @Schema(implementation = ApiErrorWrapper.class)))
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    public FuenteResponse actualizar(@PathVariable Long id, @Valid @RequestBody FuenteRequest req){
        return service.actualizar(id, req);
    }

    @Operation(summary = "Eliminar fuente")
    @ApiResponse(responseCode = "204", description = "Eliminada")
    @ApiResponse(responseCode = "404", description = "No encontrada",
            content = @Content(schema = @Schema(implementation = ApiErrorWrapper.class)))
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

