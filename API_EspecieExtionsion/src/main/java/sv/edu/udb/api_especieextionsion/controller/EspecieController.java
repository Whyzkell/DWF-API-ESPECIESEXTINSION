package sv.edu.udb.api_especieextionsion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import sv.edu.udb.api_especieextionsion.controller.dto.*;
import sv.edu.udb.api_especieextionsion.service.EspecieService;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;

@Tag(name = "Especies", description = "CRUD de especies")
@RestController
@RequestMapping("/api/especies")
@RequiredArgsConstructor
public class EspecieController {
    private final EspecieService service;



    @Operation(summary = "Listar especies")
    @GetMapping
    public List<EspecieResponse> listar() {
        return service.listar();
    }

    @Operation(summary = "Obtener especie por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    @GetMapping("/{id}")
    public EspecieResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @Operation(summary = "Crear especie", description = "Crea una nueva especie (idempotencia por recurso del lado del cliente)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EspecieResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida"),
            @ApiResponse(responseCode = "409", description = "Conflicto (nombre científico duplicado)")
    })
    @PostMapping
    public ResponseEntity<EspecieResponse> crear(@Valid @RequestBody EspecieRequest req,
                                                 UriComponentsBuilder uriBuilder) {
        EspecieResponse creada = service.crear(req);
        var location = uriBuilder.path("/api/especies/{id}")
                .buildAndExpand(creada.getId()).toUri();
        return ResponseEntity.created(location).body(creada);
    }

    @Operation(summary = "Actualizar especie (PUT idempotente)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizada"),
            @ApiResponse(responseCode = "404", description = "No encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflicto")
    })
    @PutMapping("/{id}")
    public EspecieResponse actualizar(@PathVariable Long id, @Valid @RequestBody EspecieRequest req) {
        return service.actualizar(id, req);
    }

    @Operation(summary = "Eliminar especie (DELETE idempotente)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminada"),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
