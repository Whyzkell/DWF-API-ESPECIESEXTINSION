package sv.edu.udb.api_especieextionsion.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import sv.edu.udb.api_especieextionsion.controller.dto.AmenazaRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.AmenazaResponse;
import sv.edu.udb.api_especieextionsion.mapping.FakeAmenazaMapper;
import sv.edu.udb.api_especieextionsion.repository.AmenazaRepository;
import sv.edu.udb.api_especieextionsion.repository.domain.Amenaza;
import sv.edu.udb.api_especieextionsion.service.impl.AmenazaServiceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({AmenazaServiceImpl.class, FakeAmenazaMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AmenazaServiceImplDataJpaTest {

    @Autowired AmenazaRepository repo;
    @Autowired sv.edu.udb.api_especieextionsion.service.AmenazaService service;

    // ===== helpers =====
    private AmenazaRequest req(String codigo, String tipo, String desc) {
        return AmenazaRequest.builder()
                .codigo(codigo)
                .tipo(tipo)
                .descripcion(desc)
                .build();
    }

    private Amenaza amenaza(String codigo, String tipo, String desc) {
        return Amenaza.builder()
                .codigo(codigo)
                .tipo(tipo)
                .descripcion(desc)
                .build();
    }

    // ===== tests =====

    @Test
    @DisplayName("crear: persiste y retorna AmenazaResponse mapeado")
    void crear_ok() {
        AmenazaResponse res = service.crear(req("DEF", "HUMANA", "Deforestación"));

        assertThat(res.getId()).isNotNull();
        assertThat(res.getCodigo()).isEqualTo("DEF");
        assertThat(repo.count()).isEqualTo(1);
        assertThat(repo.existsByCodigo("DEF")).isTrue();
    }

    @Test
    @DisplayName("crear: lanza IllegalArgumentException si el código ya existe")
    void crear_duplicado() {
        repo.save(amenaza("DEF", "HUMANA", "d1"));

        assertThatThrownBy(() -> service.crear(req("DEF", "NATURAL", "d2")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("código");

        assertThat(repo.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("listar: devuelve todas las amenazas mapeadas")
    void listar_ok() {
        repo.saveAll(List.of(
                amenaza("A1", "HUMANA", "d1"),
                amenaza("A2", "NATURAL", "d2")
        ));

        List<AmenazaResponse> list = service.listar();

        assertThat(list).hasSize(2);
        assertThat(list).extracting(AmenazaResponse::getCodigo)
                .containsExactlyInAnyOrder("A1", "A2");
    }

    @Test
    @DisplayName("buscarPorId: devuelve la amenaza existente mapeada")
    void buscarPorId_ok() {
        Amenaza saved = repo.save(amenaza("INC", "NATURAL", "Incendios"));

        AmenazaResponse res = service.buscarPorId(saved.getId());

        assertThat(res.getId()).isEqualTo(saved.getId());
        assertThat(res.getCodigo()).isEqualTo("INC");
        assertThat(res.getTipo()).isEqualTo("NATURAL");
    }

    @Test
    @DisplayName("buscarPorId: 404 cuando no existe")
    void buscarPorId_notFound() {
        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("no encontrada");
    }

    @Test
    @DisplayName("actualizar: modifica y persiste cambios")
    void actualizar_ok() {
        Amenaza saved = repo.save(amenaza("OLD", "HUMANA", "desc"));

        AmenazaResponse res = service.actualizar(saved.getId(),
                req("NEW", "NATURAL", "nueva desc"));

        assertThat(res.getCodigo()).isEqualTo("NEW");

        Amenaza inDb = repo.findById(saved.getId()).orElseThrow();
        assertThat(inDb.getTipo()).isEqualTo("NATURAL");
        assertThat(inDb.getDescripcion()).isEqualTo("nueva desc");
    }

    @Test
    @DisplayName("actualizar: lanza IllegalArgumentException si el código nuevo ya existe")
    void actualizar_conflict() {
        Amenaza a1 = repo.save(amenaza("C1", "HUMANA", "d1"));
        Amenaza a2 = repo.save(amenaza("C2", "NATURAL", "d2"));

        assertThatThrownBy(() ->
                service.actualizar(a2.getId(), req("C1", "NATURAL", "d3"))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("código");

        // a2 conserva su código original
        assertThat(repo.findById(a2.getId()).orElseThrow().getCodigo()).isEqualTo("C2");
    }

    @Test
    @DisplayName("eliminar: elimina cuando existe")
    void eliminar_ok() {
        Amenaza a = repo.save(amenaza("DEL", "HUMANA", "d"));

        service.eliminar(a.getId());

        assertThat(repo.existsById(a.getId())).isFalse();
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


