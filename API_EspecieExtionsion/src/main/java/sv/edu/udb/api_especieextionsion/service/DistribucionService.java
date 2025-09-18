package sv.edu.udb.api_especieextionsion.service;

import sv.edu.udb.api_especieextionsion.controller.dto.*;
import java.util.List;

public interface DistribucionService {
    DistribucionResponse crear(Long especieId, DistribucionRequest req);
    List<DistribucionResponse> listarPorEspecie(Long especieId);
}