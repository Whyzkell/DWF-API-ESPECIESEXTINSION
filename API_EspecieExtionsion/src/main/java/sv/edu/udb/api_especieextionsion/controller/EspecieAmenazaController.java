package sv.edu.udb.api_especieextionsion.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.api_especieextionsion.controller.dto.*;
import sv.edu.udb.api_especieextionsion.service.EspecieAmenazaService;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Tag(name = "Especie - Amenazas", description = "Asociación especie↔amenazas")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/especies/{especieId}/amenazas")
@SecurityRequirement(name = "bearerAuth") // Swagger: requiere Bearer token
public class EspecieAmenazaController {

    private final EspecieAmenazaService service;

    @Operation(
            summary = "Asociar amenaza a especie",
            description = "Asocia una amenaza a una especie con un nivel de severidad."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Asociación creada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EspecieAmenazaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida o datos incorrectos"),
            @ApiResponse(responseCode = "404", description = "Especie o amenaza no encontrada"),
            @ApiResponse(responseCode = "409", description = "La asociación ya existe")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    public ResponseEntity<EspecieAmenazaResponse> asociar(@PathVariable Long especieId,
                                                          @Valid @RequestBody EspecieAmenazaLinkRequest req){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.asociar(especieId, req));
    }

    @Operation(
            summary = "Listar amenazas de una especie",
            description = "Obtiene todas las amenazas asociadas a una especie por su ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de amenazas exitoso"),
            @ApiResponse(responseCode = "404", description = "Especie no encontrada")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR','LECTOR')")
    public List<EspecieAmenazaResponse> listar(@PathVariable Long especieId){
        return service.listarPorEspecie(especieId);
    }

    @Operation(
            summary = "Actualizar severidad del vínculo",
            description = "Actualiza el nivel de severidad de la amenaza asociada a una especie."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Severidad actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Valor de severidad inválido"),
            @ApiResponse(responseCode = "404", description = "Especie o amenaza no encontrada")
    })
    @PutMapping(value = "/{amenazaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    public EspecieAmenazaResponse cambiarSeveridad(@PathVariable Long especieId,
                                                   @PathVariable Long amenazaId,
                                                   @RequestParam
                                                   @Pattern(regexp = "BAJA|MEDIA|ALTA") String severidad) {
        return service.actualizarSeveridad(especieId, amenazaId, severidad);
    }

    @Operation(
            summary = "Desasociar amenaza de especie",
            description = "Elimina la asociación entre una especie y una amenaza por sus IDs."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Desasociación exitosa"),
            @ApiResponse(responseCode = "404", description = "Especie o amenaza no encontrada")
    })
    @DeleteMapping("/{amenazaId}")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    public ResponseEntity<Void> desasociar(@PathVariable Long especieId, @PathVariable Long amenazaId){
        service.desasociar(especieId, amenazaId);
        return ResponseEntity.noContent().build();
    }
}

