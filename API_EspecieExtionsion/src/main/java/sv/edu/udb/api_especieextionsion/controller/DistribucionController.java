package sv.edu.udb.api_especieextionsion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.api_especieextionsion.controller.dto.*;
import sv.edu.udb.api_especieextionsion.service.DistribucionService;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/especies/{especieId}/distribuciones")
public class DistribucionController {
    private final DistribucionService service;

    @PostMapping
    public ResponseEntity<DistribucionResponse> crear(@PathVariable Long especieId,
                                                      @Valid @RequestBody DistribucionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(especieId, req));
    }

    @GetMapping
    public List<DistribucionResponse> listar(@PathVariable Long especieId) {
        return service.listarPorEspecie(especieId);
    }
}