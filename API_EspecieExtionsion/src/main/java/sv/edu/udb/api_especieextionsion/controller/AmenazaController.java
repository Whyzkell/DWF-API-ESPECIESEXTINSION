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
}
