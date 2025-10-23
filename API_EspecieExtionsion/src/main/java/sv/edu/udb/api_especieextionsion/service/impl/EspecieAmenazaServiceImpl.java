package sv.edu.udb.api_especieextionsion.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieAmenazaLinkRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieAmenazaResponse;
import sv.edu.udb.api_especieextionsion.domain.Amenaza;
import sv.edu.udb.api_especieextionsion.domain.Especie;
import sv.edu.udb.api_especieextionsion.domain.EspecieAmenaza;
import sv.edu.udb.api_especieextionsion.repository.AmenazaRepository;
import sv.edu.udb.api_especieextionsion.repository.EspecieAmenazaRepository;
import sv.edu.udb.api_especieextionsion.repository.EspecieRepository;
import sv.edu.udb.api_especieextionsion.service.EspecieAmenazaService;
import sv.edu.udb.api_especieextionsion.shared.DuplicateResourceException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EspecieAmenazaServiceImpl implements EspecieAmenazaService {

    private final EspecieRepository especieRepo;
    private final AmenazaRepository amenazaRepo;
    private final EspecieAmenazaRepository linkRepo;

    @Transactional
    @Override
    public EspecieAmenazaResponse asociar(Long especieId, EspecieAmenazaLinkRequest r) {
        Especie especie = especieRepo.findById(especieId)
                .orElseThrow(() -> new EntityNotFoundException("Especie no encontrada"));

        Amenaza amenaza = amenazaRepo.findById(r.getAmenazaId())
                .orElseThrow(() -> new EntityNotFoundException("Amenaza no encontrada"));

        // Prevalidación de unicidad (además de la UNIQUE constraint en DB)
        if (linkRepo.existsByEspecieIdAndAmenazaId(especieId, amenaza.getId())) {
            throw new DuplicateResourceException("La especie ya tiene asociada esta amenaza");
        }

        EspecieAmenaza link = EspecieAmenaza.builder()
                .especie(especie)
                .amenaza(amenaza)
                .severidad(r.getSeveridad())
                .build();

        link = linkRepo.save(link);
        return toResponse(link);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EspecieAmenazaResponse> listarPorEspecie(Long especieId) {
        if (!especieRepo.existsById(especieId)) {
            throw new EntityNotFoundException("Especie no encontrada");
        }
        return linkRepo.findByEspecieId(especieId).stream().map(this::toResponse).toList();
    }

    @Transactional
    @Override
    public EspecieAmenazaResponse actualizarSeveridad(Long especieId, Long amenazaId, String severidad) {
        var link = linkRepo.findByEspecieIdAndAmenazaId(especieId, amenazaId)
                .orElseThrow(() -> new EntityNotFoundException("La especie no tiene asociada esa amenaza"));

        // Validación simple de severidad
        if (!severidad.matches("BAJA|MEDIA|ALTA")) {
            throw new IllegalArgumentException("Severidad inválida (use BAJA, MEDIA o ALTA)");
        }

        link.setSeveridad(severidad);
        link = linkRepo.save(link);
        return toResponse(link);
    }

    @Transactional
    @Override
    public void desasociar(Long especieId, Long amenazaId) {
        var link = linkRepo.findByEspecieIdAndAmenazaId(especieId, amenazaId)
                .orElseThrow(() -> new EntityNotFoundException("La especie no tiene asociada esa amenaza"));
        linkRepo.delete(link);
    }

    private EspecieAmenazaResponse toResponse(EspecieAmenaza l) {
        Amenaza a = l.getAmenaza();
        return EspecieAmenazaResponse.builder()
                .idVinculo(l.getId())
                .amenazaId(a.getId())
                .codigo(a.getCodigo())
                .tipo(a.getTipo())
                .descripcion(a.getDescripcion())
                .severidad(l.getSeveridad())
                .build();
    }
}

