package sv.edu.udb.api_especieextionsion.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import sv.edu.udb.api_especieextionsion.controller.dto.FuenteRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.FuenteResponse;
import sv.edu.udb.api_especieextionsion.mapping.FakeFuenteMapper;
import sv.edu.udb.api_especieextionsion.repository.FuenteRepository;
import sv.edu.udb.api_especieextionsion.repository.domain.Fuente;
import sv.edu.udb.api_especieextionsion.service.impl.FuenteServiceImpl;
import sv.edu.udb.api_especieextionsion.shared.DuplicateResourceException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({FuenteServiceImpl.class, FakeFuenteMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class FuenteServiceImplDataJpaTest {

    @Autowired FuenteRepository repo;
    @Autowired FuenteService service;

    // ===== helpers =====
    private FuenteRequest req(String nombre, String desc, String tipo, String enlace, LocalDate fecha) {
        return FuenteRequest.builder()
                .nombre(nombre)
                .descripcion(desc)
                .tipo(tipo)
                .enlace(enlace)
                .fechaPublicacion(fecha)
                .build();
    }

    private Fuente fuente(String nombre, String desc, String tipo, String enlace, LocalDate fecha) {
        return Fuente.builder()
                .nombre(nombre)
                .descripcion(desc)
                .tipo(tipo)
                .enlace(enlace)
                .fechaPublicacion(fecha)
                .build();
    }

    // ===== tests =====

    @Test
    @DisplayName("crear: persiste y retorna FuenteResponse mapeado")
    void crear_ok() {
        FuenteResponse res = service.crear(
                req("IUCN Panthera onca", "Ficha", "WEB",
                        "https://ejemplo/iucn/jaguar", LocalDate.of(2024, 11, 15))
        );

        assertThat(res.getId()).isNotNull();
        assertThat(res.getNombre()).isEqualTo("IUCN Panthera onca");
        assertThat(repo.count()).isEqualTo(1);
        assertThat(repo.existsByNombre("IUCN Panthera onca")).isTrue();
    }

    @Test
    @DisplayName("crear: lanza DuplicateResourceException si nombre ya existe")
    void crear_duplicado() {
        service.crear(req("Fuente X", "d", "WEB", "http://x", LocalDate.now()));

        assertThatThrownBy(() ->
                service.crear(req("Fuente X", "d2", "WEB", "http://x2", LocalDate.now()))
        )
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ya existe");

        assertThat(repo.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("obtener: devuelve la fuente existente mapeada")
    void obtener_ok() {
        Fuente saved = repo.save(
                fuente("Art. ABC", "paper", "ARTICULO", "http://abc", LocalDate.of(2020,1,1))
        );

        FuenteResponse res = service.obtener(saved.getId());

        assertThat(res.getId()).isEqualTo(saved.getId());
        assertThat(res.getNombre()).isEqualTo("Art. ABC");
        assertThat(res.getTipo()).isEqualTo("ARTICULO");
    }

    @Test
    @DisplayName("obtener: 404 cuando no existe")
    void obtener_notFound() {
        assertThatThrownBy(() -> service.obtener(777L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("no existe");
    }

    @Test
    @DisplayName("listar: devuelve todas las fuentes mapeadas")
    void listar_ok() {
        repo.saveAll(List.of(
                fuente("F-1", "d1", "WEB", "http://1", LocalDate.of(2021,1,1)),
                fuente("F-2", "d2", "LIBRO", "http://2", LocalDate.of(2022,2,2))
        ));

        List<FuenteResponse> list = service.listar();

        assertThat(list).hasSize(2);
        assertThat(list).extracting(FuenteResponse::getNombre)
                .containsExactlyInAnyOrder("F-1", "F-2");
    }

    @Test
    @DisplayName("actualizar: modifica y persiste cambios")
    void actualizar_ok() {
        Fuente saved = repo.save(
                fuente("Old", "desc", "WEB", "http://old", LocalDate.of(2020,1,1))
        );

        FuenteResponse res = service.actualizar(saved.getId(),
                req("New", "nueva desc", "REPORTE", "http://new", LocalDate.of(2024,5,5))
        );

        assertThat(res.getNombre()).isEqualTo("New");
        Fuente inDb = repo.findById(saved.getId()).orElseThrow();
        assertThat(inDb.getDescripcion()).isEqualTo("nueva desc");
        assertThat(inDb.getTipo()).isEqualTo("REPORTE");
        assertThat(inDb.getEnlace()).isEqualTo("http://new");
        assertThat(inDb.getFechaPublicacion()).isEqualTo(LocalDate.of(2024,5,5));
    }

    @Test
    @DisplayName("actualizar: lanza DuplicateResourceException si el nombre nuevo ya existe")
    void actualizar_conflict() {
        Fuente f1 = repo.save(fuente("N1", "d", "WEB", "http://1", LocalDate.now()));
        Fuente f2 = repo.save(fuente("N2", "d", "WEB", "http://2", LocalDate.now()));

        assertThatThrownBy(() ->
                service.actualizar(f2.getId(), req("N1", "d2", "LIBRO", "http://x", LocalDate.now()))
        )
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ya existe");

        // f2 se mantiene con su nombre original
        assertThat(repo.findById(f2.getId()).orElseThrow().getNombre()).isEqualTo("N2");
    }

    @Test
    @DisplayName("eliminar: elimina cuando existe")
    void eliminar_ok() {
        Fuente f = repo.save(fuente("Del", "borrar", "WEB", "http://del", LocalDate.now()));

        service.eliminar(f.getId());

        assertThat(repo.existsById(f.getId())).isFalse();
        assertThat(repo.count()).isZero();
    }

    @Test
    @DisplayName("eliminar: 404 cuando no existe")
    void eliminar_notFound() {
        assertThatThrownBy(() -> service.eliminar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("no existe");
    }
}

