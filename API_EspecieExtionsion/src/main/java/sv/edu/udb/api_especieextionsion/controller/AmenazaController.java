package sv.edu.udb.api_especieextionsion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.api_especieextionsion.controller.dto.*;
import sv.edu.udb.api_especieextionsion.service.AmenazaService;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;

@Tag(name = "Amenazas", description = "CRUD de amenazas")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/amenazas")
public class AmenazaController {
    private final AmenazaService service;

    @Operation(summary = "Crear nueva amenaza", description = "Crea una nueva amenaza en la base de datos.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AmenazaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida")
    })
    @PostMapping
    public ResponseEntity<AmenazaResponse> crear(@Valid @RequestBody AmenazaRequest req){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(req));
    }

    @Operation(summary = "Listar amenazas", description = "Devuelve una lista de todas las amenazas registradas.")
    @GetMapping
    public List<AmenazaResponse> listar(){
        return service.listar();
    }

    @Operation(summary = "Obtener amenaza por id", description = "Busca y devuelve una amenaza por su identificador único.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    @GetMapping("/{id}")
    public AmenazaResponse obtener(@PathVariable Long id){
        return service.buscarPorId(id); // 200 o 404
    }

    @Operation(summary = "Actualizar amenaza (PUT idempotente)", description = "Actualiza los datos de una amenaza existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizada"),
            @ApiResponse(responseCode = "404", description = "No encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflicto (ej. nombre duplicado)")
    })
    @PutMapping("/{id}")
    public AmenazaResponse actualizar(@PathVariable Long id, @Valid @RequestBody AmenazaRequest req){
        return service.actualizar(id, req); // 200 o 404/409
    }

    @Operation(summary = "Eliminar amenaza (DELETE idempotente)", description = "Elimina una amenaza de la base de datos.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminada"),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        service.eliminar(id);
        return ResponseEntity.noContent().build(); // 204
    }
}
