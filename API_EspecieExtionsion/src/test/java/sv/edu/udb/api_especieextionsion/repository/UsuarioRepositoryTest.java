package sv.edu.udb.api_especieextionsion.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import sv.edu.udb.api_especieextionsion.domain.Rol;
import sv.edu.udb.api_especieextionsion.domain.Usuario;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository repo;

    private Usuario nuevoUsuario(String username, String email) {
        return Usuario.builder()
                .username(username)
                .nombreCompleto("Nombre de " + username)
                .email(email)
                .rol(Rol.LECTOR)              // <-- ahora usamos un valor válido de tu enum
                .activo(true)
                .fechaRegistro(LocalDate.of(2024, 1, 2))
                .build();
    }

    @Test
    @DisplayName("save + findById: persiste y recupera un Usuario")
    void save_and_findById() {
        Usuario u = repo.save(nuevoUsuario("diego", "diego@test.com"));

        assertThat(u.getId()).isNotNull();

        Optional<Usuario> found = repo.findById(u.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("diego");
        assertThat(found.get().getEmail()).isEqualTo("diego@test.com");
        assertThat(found.get().getRol()).isEqualTo(Rol.LECTOR);
    }

    @Test
    @DisplayName("existsByUsername: true si existe, false si no")
    void existsByUsername_true_false() {
        assertThat(repo.existsByUsername("ana")).isFalse();

        repo.save(nuevoUsuario("ana", "ana@test.com"));

        assertThat(repo.existsByUsername("ana")).isTrue();
        assertThat(repo.existsByUsername("ANA")).isFalse(); // usualmente case-sensitive
    }

    @Test
    @DisplayName("existsByEmail: true si existe, false si no")
    void existsByEmail_true_false() {
        assertThat(repo.existsByEmail("maria@test.com")).isFalse();

        repo.save(nuevoUsuario("maria", "maria@test.com"));

        assertThat(repo.existsByEmail("maria@test.com")).isTrue();
        assertThat(repo.existsByEmail("MARIA@test.com")).isFalse(); // usualmente case-sensitive
    }

    @Test
    @DisplayName("findByUsername: encuentra por username y es sensible a mayúsculas")
    void findByUsername_present_absent_caseSensitive() {
        repo.save(nuevoUsuario("carlos", "carlos@test.com"));

        assertThat(repo.findByUsername("carlos")).isPresent();
        assertThat(repo.findByUsername("Carlos")).isNotPresent();
        assertThat(repo.findByUsername("otro")).isNotPresent();
    }

    @Test
    @DisplayName("Unicidad de username: falla al duplicar")
    void unique_username_constraint_violation() {
        repo.saveAndFlush(nuevoUsuario("sofia", "sofia@test.com"));

        Usuario duplicado = nuevoUsuario("sofia", "sofia2@test.com");

        assertThatThrownBy(() -> repo.saveAndFlush(duplicado))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Unicidad de email: falla al duplicar")
    void unique_email_constraint_violation() {
        repo.saveAndFlush(nuevoUsuario("luis", "luis@test.com"));

        Usuario duplicado = nuevoUsuario("luis2", "luis@test.com");

        assertThatThrownBy(() -> repo.saveAndFlush(duplicado))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("update: modifica campos de un Usuario existente")
    void update_usuario_fields() {
        Usuario u = repo.save(nuevoUsuario("paula", "paula@test.com"));

        u.setNombreCompleto("Paula Actualizada");
        u.setActivo(false);
        u.setRol(Rol.EDITOR); // probamos otro rol del enum
        u.setEmail("paula.actualizada@test.com");

        Usuario updated = repo.saveAndFlush(u);

        assertThat(updated.getNombreCompleto()).isEqualTo("Paula Actualizada");
        assertThat(updated.getActivo()).isFalse();
        assertThat(updated.getEmail()).isEqualTo("paula.actualizada@test.com");
        assertThat(updated.getRol()).isEqualTo(Rol.EDITOR);
    }

    @Test
    @DisplayName("delete: elimina un Usuario por id")
    void delete_usuario() {
        Usuario u = repo.save(nuevoUsuario("ricardo", "ricardo@test.com"));
        Long id = u.getId();

        repo.deleteById(id);
        repo.flush();

        assertThat(repo.findById(id)).isNotPresent();
    }
}
