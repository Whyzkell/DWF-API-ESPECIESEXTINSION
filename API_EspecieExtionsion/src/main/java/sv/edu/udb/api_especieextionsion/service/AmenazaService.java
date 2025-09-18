package sv.edu.udb.api_especieextionsion.service;

import sv.edu.udb.api_especieextionsion.controller.dto.AmenazaRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.AmenazaResponse;

import java.util.List;

public interface AmenazaService {
    AmenazaResponse crear(AmenazaRequest req);
    List<AmenazaResponse> listar();
    AmenazaResponse buscarPorId(Long id);
    AmenazaResponse actualizar(Long id, AmenazaRequest req); // idempotente
    void eliminar(Long id);                                   // idempotente
}
