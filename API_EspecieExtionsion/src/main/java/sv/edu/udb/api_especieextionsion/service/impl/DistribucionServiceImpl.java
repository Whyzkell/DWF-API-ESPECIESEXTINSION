package sv.edu.udb.api_especieextionsion.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.api_especieextionsion.controller.dto.DistribucionRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.DistribucionResponse;
import sv.edu.udb.api_especieextionsion.mapping.DistribucionMapper;
import sv.edu.udb.api_especieextionsion.repository.DistribucionRepository;
import sv.edu.udb.api_especieextionsion.repository.EspecieRepository;
import sv.edu.udb.api_especieextionsion.repository.domain.DistribucionGeografica;
import sv.edu.udb.api_especieextionsion.repository.domain.Especie;
import sv.edu.udb.api_especieextionsion.service.DistribucionService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DistribucionServiceImpl implements DistribucionService {

    private final EspecieRepository especieRepo;
    private final DistribucionRepository distRepo;
    private final DistribucionMapper mapper;

    @Transactional
    @Override
    public DistribucionResponse crear(Long especieId, DistribucionRequest r) {
        Especie especie = especieRepo.findById(especieId)
                .orElseThrow(() -> new EntityNotFoundException("Especie no encontrada"));

        DistribucionGeografica d = mapper.toEntity(r);
        d.setEspecie(especie);

        d = distRepo.save(d);
        return mapper.toDto(d);
    }

    @Transactional(readOnly = true)
    @Override
    public List<DistribucionResponse> listarPorEspecie(Long especieId) {
        if (!especieRepo.existsById(especieId)) {
            throw new EntityNotFoundException("Especie no encontrada");
        }
        return distRepo.findByEspecieId(especieId).stream()
                .map(mapper::toDto)
                .toList();
    }
}
