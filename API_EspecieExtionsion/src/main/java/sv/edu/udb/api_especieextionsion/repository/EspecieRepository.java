package sv.edu.udb.api_especieextionsion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sv.edu.udb.api_especieextionsion.domain.Especie;

@Repository
public interface EspecieRepository extends JpaRepository<Especie, Long> {
    boolean existsByNombreCientifico(String nombreCientifico);
}
