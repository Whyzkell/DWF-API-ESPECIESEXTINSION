package sv.edu.udb.api_especieextionsion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.api_especieextionsion.domain.DistribucionGeografica;
import java.util.List;

public interface DistribucionRepository extends JpaRepository<DistribucionGeografica, Long> {
    List<DistribucionGeografica> findByEspecieId(Long especieId);
}