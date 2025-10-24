package sv.edu.udb.api_especieextionsion.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import sv.edu.udb.api_especieextionsion.controller.dto.UsuarioRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.UsuarioResponse;
import sv.edu.udb.api_especieextionsion.repository.domain.Rol;
import sv.edu.udb.api_especieextionsion.repository.domain.Usuario;
import sv.edu.udb.api_especieextionsion.repository.UsuarioRepository;
import sv.edu.udb.api_especieextionsion.service.impl.UsuarioServiceImpl;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(UsuarioServiceImpl.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UsuarioServiceImplDataJpaTest {

    @Autowired UsuarioRepository repo;
    @Autowired UsuarioService service;
    @Autowired EntityManager em;

    // ===== helpers =====
    private UsuarioRequest req(
            String username,
            String nombreCompleto,
            String email,
            Rol rol,
            Boolean activo,
            LocalDate fecha
    ) {
        UsuarioRequest r = new UsuarioRequest();
        r.setUsername(username);
        r.setNombreCompleto(nombreCompleto);
        r.setEmail(email);
        r.setRol(rol);
        r.setActivo(activo);
        r.setFechaRegistro(fecha);
        return r;
    }

    private Usuario usuario(
            String username,
            String nombreCompleto,
            String email,
            Rol rol,
            boolean activo,
            LocalDate fecha
    ) {
        return Usuario.builder()
                .username(username)
                .nombreCompleto(nombreCompleto)
                .email(email)
                .rol(rol)
                .activo(activo)
                .fechaRegistro(fecha)
                .build();
    }

    // ===== tests =====

    @Test
    @DisplayName("crear: persiste y mapea a UsuarioResponse (defaults activo=true y fecha no nula)")
    void crear_ok() {
        UsuarioResponse res = service.crear(
                req("diego", "Diego Dev", "diego@correo.com", Rol.EDITOR, null, null)
        );

        assertThat(res.getId()).isNotNull();
        assertThat(res.getUsername()).isEqualTo("diego");
        assertThat(res.getActivo()).isTrue();
        assertThat(res.getFechaRegistro()).isNotNull();
        assertThat(repo.count()).isEqualTo(1);
        assertThat(repo.existsByUsername("diego")).isTrue();
        assertThat(repo.existsByEmail("diego@correo.com")).isTrue();
    }

    @Test
    @DisplayName("crear: lanza DataIntegrityViolationException si username ya existe")
    void crear_usernameDuplicado() {
        service.crear(req("user1", "U1", "u1@mail.com", Rol.ADMIN, true, LocalDate.now()));

        assertThatThrownBy(() ->
                service.crear(req("user1", "U1b", "u1b@mail.com", Rol.EDITOR, true, LocalDate.now()))
        )
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Username");

        // limpiar el contexto por si acaso y verificar que no se agregó otro
        em.clear();
        assertThat(repo.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("crear: lanza DataIntegrityViolationException si email ya existe")
    void crear_emailDuplicado() {
        service.crear(req("userA", "A", "a@mail.com", Rol.LECTOR, true, LocalDate.now()));

        assertThatThrownBy(() ->
                service.crear(req("userB", "B", "a@mail.com", Rol.ADMIN, true, LocalDate.now()))
        )
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Email");

        em.clear();
        assertThat(repo.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("listar: devuelve todos los usuarios mapeados")
    void listar_ok() {
        repo.saveAll(List.of(
                usuario("u1", "Uno", "u1@mail.com", Rol.EDITOR, true, LocalDate.now()),
                usuario("u2", "Dos", "u2@mail.com", Rol.ADMIN, false, LocalDate.now())
        ));

        List<UsuarioResponse> list = service.listar();

        assertThat(list).hasSize(2);
        assertThat(list).extracting(UsuarioResponse::getUsername)
                .containsExactlyInAnyOrder("u1", "u2");
    }

    @Test
    @DisplayName("obtener: devuelve el usuario existente")
    void obtener_ok() {
        Usuario saved = repo.save(
                usuario("gbif", "GBIF User", "gbif@mail.com", Rol.LECTOR, true, LocalDate.now())
        );

        UsuarioResponse res = service.obtener(saved.getId());

        assertThat(res.getId()).isEqualTo(saved.getId());
        assertThat(res.getEmail()).isEqualTo("gbif@mail.com");
        assertThat(res.getRol()).isEqualTo(Rol.LECTOR);
    }

    @Test
    @DisplayName("obtener: 404 cuando no existe")
    void obtener_notFound() {
        assertThatThrownBy(() -> service.obtener(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("no existe");
    }

    @Test
    @DisplayName("actualizar: modifica y persiste cambios (mantiene fecha si se manda null)")
    void actualizar_ok() {
        Usuario saved = repo.save(
                usuario("old", "Viejo", "old@mail.com", Rol.EDITOR, true, LocalDate.of(2020,1,1))
        );

        UsuarioResponse res = service.actualizar(saved.getId(),
                req("newuser", "Nuevo Nombre", "new@mail.com", Rol.ADMIN, false, null)
        );

        assertThat(res.getUsername()).isEqualTo("newuser");
        assertThat(res.getNombreCompleto()).isEqualTo("Nuevo Nombre");
        assertThat(res.getEmail()).isEqualTo("new@mail.com");
        assertThat(res.getRol()).isEqualTo(Rol.ADMIN);
        assertThat(res.getActivo()).isFalse();

        Usuario inDb = repo.findById(saved.getId()).orElseThrow();
        assertThat(inDb.getFechaRegistro()).isEqualTo(LocalDate.of(2020,1,1)); // se mantiene
    }

    @Test
    @DisplayName("actualizar: lanza DataIntegrityViolationException si username ya está en uso")
    void actualizar_conflict_username() {
        Usuario u1 = repo.save(usuario("u1", "A", "a@mail.com", Rol.ADMIN, true, LocalDate.now()));
        Usuario u2 = repo.save(usuario("u2", "B", "b@mail.com", Rol.EDITOR, true, LocalDate.now()));

        assertThatThrownBy(() ->
                service.actualizar(u2.getId(),
                        req("u1", "B2", "b2@mail.com", Rol.LECTOR, true, LocalDate.now()))
        )
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Username");

        em.clear();
        assertThat(repo.findById(u2.getId()).orElseThrow().getUsername()).isEqualTo("u2");
    }

    @Test
    @DisplayName("actualizar: lanza DataIntegrityViolationException si email ya está en uso")
    void actualizar_conflict_email() {
        Usuario u1 = repo.save(usuario("x1", "X1", "x1@mail.com", Rol.ADMIN, true, LocalDate.now()));
        Usuario u2 = repo.save(usuario("x2", "X2", "x2@mail.com", Rol.EDITOR, true, LocalDate.now()));

        assertThatThrownBy(() ->
                service.actualizar(u2.getId(),
                        req("x2b", "X2b", "x1@mail.com", Rol.LECTOR, true, LocalDate.now()))
        )
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Email");

        em.clear();
        assertThat(repo.findById(u2.getId()).orElseThrow().getEmail()).isEqualTo("x2@mail.com");
    }

    @Test
    @DisplayName("eliminar: elimina cuando existe")
    void eliminar_ok() {
        Usuario u = repo.save(
                usuario("del", "Para Borrar", "del@mail.com", Rol.EDITOR, true, LocalDate.now())
        );

        service.eliminar(u.getId());

        assertThat(repo.existsById(u.getId())).isFalse();
        assertThat(repo.count()).isZero();
    }

    @Test
    @DisplayName("eliminar: 404 cuando no existe")
    void eliminar_notFound() {
        assertThatThrownBy(() -> service.eliminar(12345L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("no existe");
    }
}

