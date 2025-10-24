// src/test/java/sv/edu/udb/api_especieextionsion/service/AmenazaServiceImplDataJpaTest.java
package sv.edu.udb.api_especieextionsion.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import sv.edu.udb.api_especieextionsion.controller.dto.AmenazaRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.AmenazaResponse;
import sv.edu.udb.api_especieextionsion.mapping.AmenazaMapper;
import sv.edu.udb.api_especieextionsion.repository.AmenazaRepository;
import sv.edu.udb.api_especieextionsion.repository.domain.Amenaza;
import sv.edu.udb.api_especieextionsion.service.impl.AmenazaServiceImpl;


import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({AmenazaServiceImpl.class, AmenazaMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
@ActiveProfiles("test")
class AmenazaServiceImplDataJpaTest {

    @Autowired AmenazaRepository repo;
    @Autowired sv.edu.udb.api_especieextionsion.service.AmenazaService service;

    // ===== helpers =====
    private AmenazaRequest req(String codigo, String tipo, String descripcion){
        return AmenazaRequest.builder()
                .codigo(codigo)
                .tipo(tipo)
                .descripcion(descripcion)
                .build();
    }

    private Amenaza amenaza(String codigo, String tipo, String descripcion){
        return Amenaza.builder()
                .codigo(codigo)
                .tipo(tipo)
                .descripcion(descripcion)
                .build();
    }

    // ===== tests =====

    @Test
    @DisplayName("crear: persiste y retorna AmenazaResponse mapeado")
    void crear_ok() {
        AmenazaResponse res = service.crear(req("DEF-01", "ACTIVIDADES_HUMANAS", "Deforestación"));

        assertThat(res.getId()).isNotNull();
        assertThat(res.getCodigo()).isEqualTo("DEF-01");
        assertThat(repo.count()).isEqualTo(1);
        assertThat(repo.existsByCodigo("DEF-01")).isTrue();
    }

    @Test
    @DisplayName("crear: lanza IllegalArgumentException si el código ya existe")
    void crear_duplicado() {
        service.crear(req("INC-01", "ACTIVIDADES_HUMANAS", "Incendios"));

        assertThatThrownBy(() ->
                service.crear(req("INC-01", "OTRO", "desc"))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("código")
                .hasMessageContaining("existe");

        assertThat(repo.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("buscarPorId: devuelve la amenaza existente mapeada")
    void buscarPorId_ok() {
        Amenaza saved = repo.save(amenaza("EROS-01", "NATURALES", "Erosión"));

        AmenazaResponse res = service.buscarPorId(saved.getId());

        assertThat(res.getId()).isEqualTo(saved.getId());
        assertThat(res.getCodigo()).isEqualTo("EROS-01");
        assertThat(res.getTipo()).isEqualTo("NATURALES");
    }

    @Test
    @DisplayName("buscarPorId: 404 cuando no existe")
    void buscarPorId_notFound() {
        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Amenaza no encontrada");
    }

    @Test
    @DisplayName("listar: devuelve todas las amenazas mapeadas")
    void listar_ok() {
        repo.saveAll(List.of(
                amenaza("COD-1", "NATURALES", "d1"),
                amenaza("COD-2", "ACTIVIDADES_HUMANAS", "d2")
        ));

        List<AmenazaResponse> list = service.listar();

        assertThat(list).hasSize(2);
        assertThat(list).extracting(AmenazaResponse::getCodigo)
                .containsExactlyInAnyOrder("COD-1", "COD-2");
    }

    @Test
    @DisplayName("actualizar: modifica y persiste cambios")
    void actualizar_ok() {
        Amenaza saved = repo.save(amenaza("OLD", "NATURALES", "vieja"));

        AmenazaResponse res = service.actualizar(saved.getId(),
                req("NEW", "ACTIVIDADES_HUMANAS", "nueva")
        );

        assertThat(res.getCodigo()).isEqualTo("NEW");
        Amenaza inDb = repo.findById(saved.getId()).orElseThrow();
        assertThat(inDb.getTipo()).isEqualTo("ACTIVIDADES_HUMANAS");
        assertThat(inDb.getDescripcion()).isEqualTo("nueva");
    }

    @Test
    @DisplayName("actualizar: lanza IllegalArgumentException si el código nuevo ya existe")
    void actualizar_conflict() {
        Amenaza a1 = repo.save(amenaza("A-1", "NATURALES", "d"));
        Amenaza a2 = repo.save(amenaza("A-2", "HUMANAS", "d"));

        assertThatThrownBy(() ->
                service.actualizar(a2.getId(), req("A-1", "HUMANAS", "d2"))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("código")
                .hasMessageContaining("existe");

        // a2 mantiene su código original
        assertThat(repo.findById(a2.getId()).orElseThrow().getCodigo())
                .isEqualTo("A-2");
    }

    @Test
    @DisplayName("eliminar: elimina cuando existe")
    void eliminar_ok() {
        Amenaza a = repo.save(amenaza("DEL-1", "HUMANAS", "x"));

        service.eliminar(a.getId());

        assertThat(repo.existsById(a.getId())).isFalse();
        assertThat(repo.count()).isZero();
    }

    @Test
    @DisplayName("eliminar: 404 cuando no existe")
    void eliminar_notFound() {
        assertThatThrownBy(() -> service.eliminar(12345L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Amenaza no encontrada");
    }
}


