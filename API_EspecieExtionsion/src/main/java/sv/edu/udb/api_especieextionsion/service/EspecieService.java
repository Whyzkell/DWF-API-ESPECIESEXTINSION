package sv.edu.udb.api_especieextionsion.service;

import sv.edu.udb.api_especieextionsion.controller.dto.*;
import java.util.List;

public interface EspecieService {
    EspecieResponse crear(EspecieRequest req);
    EspecieResponse actualizar(Long id, EspecieRequest req);
    EspecieResponse obtener(Long id);
    List<EspecieResponse> listar();
    void eliminar(Long id);
}