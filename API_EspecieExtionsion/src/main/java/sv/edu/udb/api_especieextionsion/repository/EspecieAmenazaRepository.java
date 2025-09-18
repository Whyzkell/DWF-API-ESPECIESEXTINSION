package sv.edu.udb.api_especieextionsion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.api_especieextionsion.domain.EspecieAmenaza;

import java.util.List;

public interface EspecieAmenazaRepository extends JpaRepository<EspecieAmenaza, Long> {
    boolean existsByEspecieIdAndAmenazaId(Long especieId, Long amenazaId);
    List<EspecieAmenaza> findByEspecieId(Long especieId);
}
