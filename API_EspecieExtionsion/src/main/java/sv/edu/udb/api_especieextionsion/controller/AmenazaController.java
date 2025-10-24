package sv.edu.udb.api_especieextionsion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.api_especieextionsion.controller.dto.*;
import sv.edu.udb.api_especieextionsion.service.AmenazaService;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Tag(name = "Amenazas", description = "CRUD de amenazas")
@SecurityRequirement(name = "bearerAuth") // <- para que Swagger pida JWT
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/amenazas")
public class AmenazaController {
    private final AmenazaService service;

    @Operation(summary = "Crear nueva amenaza")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AmenazaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Prohibido")
    })
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @PostMapping
    public ResponseEntity<AmenazaResponse> crear(@Valid @RequestBody AmenazaRequest req){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(req));
    }

    @Operation(summary = "Listar amenazas")
    @PreAuthorize("isAuthenticated()") // o: hasAnyRole('ADMIN','EDITOR','LECTOR')
    @GetMapping
    public List<AmenazaResponse> listar(){
        return service.listar();
    }

    @Operation(summary = "Obtener amenaza por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    @PreAuthorize("isAuthenticated()") // o: hasAnyRole('ADMIN','EDITOR','LECTOR')
    @GetMapping("/{id}")
    public AmenazaResponse obtener(@PathVariable Long id){
        return service.buscarPorId(id);
    }

    @Operation(summary = "Actualizar amenaza (PUT idempotente)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizada"),
            @ApiResponse(responseCode = "404", description = "No encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflicto")
    })
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @PutMapping("/{id}")
    public AmenazaResponse actualizar(@PathVariable Long id, @Valid @RequestBody AmenazaRequest req){
        return service.actualizar(id, req);
    }

    @Operation(summary = "Eliminar amenaza (DELETE idempotente)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminada"),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

