package sv.edu.udb.api_especieextionsion.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.api_especieextionsion.controller.dto.*;
import sv.edu.udb.api_especieextionsion.service.EspecieAmenazaService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/especies/{especieId}/amenazas")
public class EspecieAmenazaController {
    private final EspecieAmenazaService service;

    @PostMapping
    public ResponseEntity<EspecieAmenazaResponse> asociar(@PathVariable Long especieId,
                                                          @Valid @RequestBody EspecieAmenazaLinkRequest req){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.asociar(especieId, req));
    }

    @GetMapping
    public List<EspecieAmenazaResponse> listar(@PathVariable Long especieId){
        return service.listarPorEspecie(especieId);
    }

    // PUT: cambiar severidad del vínculo
    @PutMapping("/{amenazaId}")
    public EspecieAmenazaResponse cambiarSeveridad(@PathVariable Long especieId,
                                                   @PathVariable Long amenazaId,
                                                   @RequestParam
                                                   @Pattern(regexp = "BAJA|MEDIA|ALTA") String severidad) {
        return service.actualizarSeveridad(especieId, amenazaId, severidad);
    }

    // DELETE: desasociar
    @DeleteMapping("/{amenazaId}")
    public ResponseEntity<Void> desasociar(@PathVariable Long especieId, @PathVariable Long amenazaId){
        service.desasociar(especieId, amenazaId);
        return ResponseEntity.noContent().build(); // 204
    }
}
