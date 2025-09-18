package sv.edu.udb.api_especieextionsion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import sv.edu.udb.api_especieextionsion.controller.dto.*;
import sv.edu.udb.api_especieextionsion.service.EspecieService;

import java.util.List;

@RestController
@RequestMapping("/api/especies")
@RequiredArgsConstructor
public class EspecieController {
    private final EspecieService service;

    @GetMapping
    public List<EspecieResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public EspecieResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<EspecieResponse> crear(@Valid @RequestBody EspecieRequest req,
                                                 UriComponentsBuilder uriBuilder) {
        EspecieResponse creada = service.crear(req);
        var location = uriBuilder.path("/api/especies/{id}")
                .buildAndExpand(creada.getId()).toUri();
        return ResponseEntity.created(location).body(creada);
    }

    @PutMapping("/{id}")
    public EspecieResponse actualizar(@PathVariable Long id, @Valid @RequestBody EspecieRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
