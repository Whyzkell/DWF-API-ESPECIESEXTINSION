package sv.edu.udb.api_especieextionsion.service;

import sv.edu.udb.api_especieextionsion.controller.dto.*;

import java.util.List;
public interface FuenteService {
    FuenteResponse crear(FuenteRequest req);
    List<FuenteResponse> listar();
    FuenteResponse obtener(Long id);
    FuenteResponse actualizar(Long id, FuenteRequest req);
    void eliminar(Long id);
}
