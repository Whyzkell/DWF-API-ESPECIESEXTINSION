package sv.edu.udb.api_especieextionsion.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import sv.edu.udb.api_especieextionsion.controller.dto.AmenazaRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.AmenazaResponse;
import sv.edu.udb.api_especieextionsion.repository.domain.Amenaza;
import sv.edu.udb.api_especieextionsion.repository.AmenazaRepository;
import sv.edu.udb.api_especieextionsion.service.impl.AmenazaServiceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(AmenazaServiceImpl.class) // usa el Service real dentro del slice JPA
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // fuerza H2 embebida
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AmenazaServiceImplDataJpaTest {

    @Autowired AmenazaRepository repo;
    @Autowired AmenazaServiceImpl service;

    @PersistenceContext
    EntityManager em;

    // ------- helpers -------
    private AmenazaRequest req(String codigo, String tipo, String desc) {
        AmenazaRequest r = new AmenazaRequest();
        r.setCodigo(codigo);
        r.setTipo(tipo);
        r.setDescripcion(desc);
        return r;
    }

    private Amenaza amenaza(String codigo, String tipo, String desc) {
        return Amenaza.builder()
                .codigo(codigo).tipo(tipo).descripcion(desc)
                .build();
    }

    // ------- tests ---------

    @Test
    @DisplayName("crear: persiste y devuelve DTO mapeado")
    void crear_ok() {
        AmenazaResponse res = service.crear(req("IUCN-1.1", "CAZA", "Caza furtiva"));

        assertThat(res.getId()).isNotNull();
        assertThat(res.getCodigo()).isEqualTo("IUCN-1.1");
        assertThat(res.getTipo()).isEqualTo("CAZA");
        assertThat(res.getDescripcion()).isEqualTo("Caza furtiva");

        assertThat(repo.count()).isEqualTo(1);
        assertThat(repo.existsByCodigo("IUCN-1.1")).isTrue();
    }

    @Test
    @DisplayName("crear: lanza IllegalArgumentException cuando el código ya existe")
    void crear_codigoDuplicado() {
        service.crear(req("IUCN-9.9", "OTRA", "desc"));

        assertThatThrownBy(() ->
                service.crear(req("IUCN-9.9", "OTRA2", "desc2"))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya existe");

        // No hagas más operaciones JPA en esta transacción tras la excepción
        // (para evitar flush del EntityManager marcado). Si quieres verificar el
        // count, hazlo en otro test.
    }

    @Test
    @DisplayName("listar: devuelve todas las amenazas mapeadas")
    void listar_ok() {
        repo.saveAll(List.of(
                amenaza("A-1", "T1", "D1"),
                amenaza("A-2", "T2", "D2")
        ));

        List<AmenazaResponse> list = service.listar();

        assertThat(list).hasSize(2);
        assertThat(list).extracting(AmenazaResponse::getCodigo)
                .containsExactlyInAnyOrder("A-1", "A-2");
    }

    @Test
    @DisplayName("buscarPorId: encontrado")
    void buscarPorId_found() {
        Amenaza saved = repo.save(amenaza("B-1", "TT", "DD"));

        AmenazaResponse res = service.buscarPorId(saved.getId());

        assertThat(res.getId()).isEqualTo(saved.getId());
        assertThat(res.getCodigo()).isEqualTo("B-1");
    }

    @Test
    @DisplayName("buscarPorId: 404 si no existe")
    void buscarPorId_notFound() {
        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("no encontrada");
    }

    @Test
    @DisplayName("actualizar: modifica campos y persiste")
    void actualizar_ok() {
        Amenaza saved = repo.save(amenaza("OLD", "T", "D"));

        AmenazaResponse res = service.actualizar(saved.getId(), req("NEW", "NT", "ND"));

        assertThat(res.getId()).isEqualTo(saved.getId());
        assertThat(res.getCodigo()).isEqualTo("NEW");
        assertThat(res.getTipo()).isEqualTo("NT");
        assertThat(res.getDescripcion()).isEqualTo("ND");

        Amenaza inDb = repo.findById(saved.getId()).orElseThrow();
        assertThat(inDb.getCodigo()).isEqualTo("NEW");
    }

    @Test
    @DisplayName("actualizar: lanza IllegalArgumentException por código duplicado y no cambia BD")
    void actualizar_conflict() {
        Amenaza a1 = repo.save(amenaza("C1", "T1", "D1"));
        Amenaza a2 = repo.save(amenaza("C2", "T2", "D2"));

        assertThatThrownBy(() ->
                service.actualizar(a2.getId(), req("C1", "TX", "DX"))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya existe");

        // Evita devolver el estado sucio del primer nivel de caché tras la excepción
        em.clear();

        // Verifica que la BD sigue intacta
        assertThat(repo.findById(a2.getId()).orElseThrow().getCodigo()).isEqualTo("C2");
    }

    @Test
    @DisplayName("eliminar: elimina cuando existe")
    void eliminar_ok() {
        Amenaza saved = repo.save(amenaza("DEL", "T", "D"));

        service.eliminar(saved.getId());

        assertThat(repo.existsById(saved.getId())).isFalse();
        assertThat(repo.count()).isZero();
    }

    @Test
    @DisplayName("eliminar: 404 cuando no existe")
    void eliminar_notFound() {
        assertThatThrownBy(() -> service.eliminar(12345L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("no encontrada");
    }
}


