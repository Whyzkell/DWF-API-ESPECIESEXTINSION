package sv.edu.udb.api_especieextionsion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.api_especieextionsion.domain.Amenaza;

public interface AmenazaRepository extends JpaRepository<Amenaza, Long> {
    boolean existsByCodigo(String codigo);
}
