package sv.edu.udb.api_especieextionsion.service.impl;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.api_especieextionsion.controller.dto.DistribucionRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.DistribucionResponse;
import sv.edu.udb.api_especieextionsion.domain.DistribucionGeografica;
import sv.edu.udb.api_especieextionsion.domain.Especie;
import sv.edu.udb.api_especieextionsion.repository.DistribucionRepository;
import sv.edu.udb.api_especieextionsion.repository.EspecieRepository;
import sv.edu.udb.api_especieextionsion.service.DistribucionService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DistribucionServiceImpl implements DistribucionService {
    private final EspecieRepository especieRepo;
    private final DistribucionRepository distRepo;

    @Transactional
    @Override
    public DistribucionResponse crear(Long especieId, DistribucionRequest r) {
        Especie especie = especieRepo.findById(especieId)
                .orElseThrow(() -> new EntityNotFoundException("Especie no encontrada"));

        DistribucionGeografica d = DistribucionGeografica.builder()
                .especie(especie)
                .region(r.getRegion())
                .ecosistema(r.getEcosistema())
                .latitud(r.getLatitud())
                .longitud(r.getLongitud())
                .precisionMetros(r.getPrecisionMetros())
                .fechaObservacion(r.getFechaObservacion())
                .build();

        d = distRepo.save(d);
        return toResponse(d);
    }

    @Transactional(readOnly = true)
    @Override
    public List<DistribucionResponse> listarPorEspecie(Long especieId) {
        if (!especieRepo.existsById(especieId)) {
            throw new EntityNotFoundException("Especie no encontrada");
        }
        return distRepo.findByEspecieId(especieId).stream().map(this::toResponse).toList();
    }

    // mapeo manual
    private DistribucionResponse toResponse(DistribucionGeografica d) {
        return DistribucionResponse.builder()
                .id(d.getId())
                .region(d.getRegion())
                .ecosistema(d.getEcosistema())
                .latitud(d.getLatitud())
                .longitud(d.getLongitud())
                .precisionMetros(d.getPrecisionMetros())
                .fechaObservacion(d.getFechaObservacion())
                .build();
    }
}