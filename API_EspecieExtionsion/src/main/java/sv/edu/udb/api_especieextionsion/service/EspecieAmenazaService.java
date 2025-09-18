package sv.edu.udb.api_especieextionsion.service;

import sv.edu.udb.api_especieextionsion.controller.dto.*;

import java.util.List;

public interface EspecieAmenazaService {
    EspecieAmenazaResponse asociar(Long especieId, EspecieAmenazaLinkRequest req);
    List<EspecieAmenazaResponse> listarPorEspecie(Long especieId);
    EspecieAmenazaResponse actualizarSeveridad(Long especieId, Long amenazaId, String severidad); // PUT
    void desasociar(Long especieId, Long amenazaId);
}
