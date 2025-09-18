package sv.edu.udb.api_especieextionsion.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.api_especieextionsion.controller.dto.*;
import sv.edu.udb.api_especieextionsion.domain.*;
import sv.edu.udb.api_especieextionsion.repository.*;
import sv.edu.udb.api_especieextionsion.service.EspecieAmenazaService;

import java.util.List;

@Service @RequiredArgsConstructor
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

        if (linkRepo.existsByEspecieIdAndAmenazaId(especieId, amenaza.getId())) {
            throw new IllegalArgumentException("La especie ya tiene asociada esta amenaza");
        }

        EspecieAmenaza link = EspecieAmenaza.builder()
                .especie(especie)
                .amenaza(amenaza)
                .severidad(r.getSeveridad())
                .build();

        try {
            link = linkRepo.save(link);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Conflicto al asociar especie con amenaza");
        }

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

    @Transactional
    @Override
    public EspecieAmenazaResponse actualizarSeveridad(Long especieId, Long amenazaId, String severidad) {
        var link = linkRepo.findByEspecieIdAndAmenazaId(especieId, amenazaId)
                .orElseThrow(() -> new EntityNotFoundException("La especie no tiene asociada esa amenaza"));
        // validación simple
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
        linkRepo.delete(link); // idempotente por recurso existente
    }
}
