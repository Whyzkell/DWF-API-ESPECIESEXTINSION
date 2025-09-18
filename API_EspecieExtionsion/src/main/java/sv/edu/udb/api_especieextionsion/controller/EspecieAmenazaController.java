package sv.edu.udb.api_especieextionsion.controller;

import jakarta.validation.Valid;
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

    // Vincular amenaza a especie (no idempotente)
    @PostMapping
    public ResponseEntity<EspecieAmenazaResponse> asociar(@PathVariable Long especieId,
                                                          @Valid @RequestBody EspecieAmenazaLinkRequest req){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.asociar(especieId, req));
    }

    // Listar amenazas de una especie (idempotente)
    @GetMapping
    public List<EspecieAmenazaResponse> listar(@PathVariable Long especieId){
        return service.listarPorEspecie(especieId);
    }
}
