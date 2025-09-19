package sv.edu.udb.api_especieextionsion.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.api_especieextionsion.domain.Fuente;

public interface FuenteRepository extends JpaRepository<Fuente, Long> {
    boolean existsByNombre(String nombre);
}