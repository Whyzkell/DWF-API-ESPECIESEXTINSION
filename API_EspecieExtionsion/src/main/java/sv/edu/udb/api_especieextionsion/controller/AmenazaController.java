package sv.edu.udb.api_especieextionsion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.api_especieextionsion.controller.dto.*;
import sv.edu.udb.api_especieextionsion.service.AmenazaService;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/amenazas")
public class AmenazaController {
    private final AmenazaService service;

    @PostMapping
    public ResponseEntity<AmenazaResponse> crear(@Valid @RequestBody AmenazaRequest req){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(req));
    }

    @GetMapping
    public List<AmenazaResponse> listar(){
        return service.listar();
    }

    @GetMapping("/{id}")
    public AmenazaResponse obtener(@PathVariable Long id){
        return service.buscarPorId(id); // 200 o 404
    }

    @PutMapping("/{id}")
    public AmenazaResponse actualizar(@PathVariable Long id, @Valid @RequestBody AmenazaRequest req){
        return service.actualizar(id, req); // 200 o 404/409
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        service.eliminar(id);
        return ResponseEntity.noContent().build(); // 204
    }
}
