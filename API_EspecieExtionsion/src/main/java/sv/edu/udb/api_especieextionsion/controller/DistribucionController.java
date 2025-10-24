package sv.edu.udb.api_especieextionsion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.api_especieextionsion.controller.dto.*;
import sv.edu.udb.api_especieextionsion.service.DistribucionService;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Tag(name = "Distribucion", description = "Manejo de Distribuciones")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/especies/{especieId}/distribuciones")
@SecurityRequirement(name = "bearerAuth") // Swagger: exige token
public class DistribucionController {

    private final DistribucionService service;

    @Operation(
            summary = "Crear distribución para una especie",
            description = "Asocia una nueva región de distribución geográfica a una especie existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Distribución creada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DistribucionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida o datos incorrectos"),
            @ApiResponse(responseCode = "404", description = "Especie no encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflicto (duplicado/negocio)")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    public ResponseEntity<DistribucionResponse> crear(@PathVariable Long especieId,
                                                      @Valid @RequestBody DistribucionRequest req) {
        DistribucionResponse body = service.crear(especieId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @Operation(
            summary = "Listar distribuciones de una especie",
            description = "Obtiene todas las regiones de distribución asociadas a una especie por su ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de distribuciones exitoso"),
            @ApiResponse(responseCode = "404", description = "Especie no encontrada")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR','LECTOR')")
    public List<DistribucionResponse> listar(@PathVariable Long especieId) {
        return service.listarPorEspecie(especieId);
    }
}
