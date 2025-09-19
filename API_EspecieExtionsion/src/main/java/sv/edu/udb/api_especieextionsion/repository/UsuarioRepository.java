package sv.edu.udb.api_especieextionsion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.api_especieextionsion.domain.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Usuario> findByUsername(String username);
}