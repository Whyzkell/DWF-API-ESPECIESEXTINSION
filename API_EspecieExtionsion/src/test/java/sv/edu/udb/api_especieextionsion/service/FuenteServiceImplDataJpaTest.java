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
import sv.edu.udb.api_especieextionsion.controller.dto.FuenteRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.FuenteResponse;
import sv.edu.udb.api_especieextionsion.repository.domain.Fuente;
import sv.edu.udb.api_especieextionsion.repository.FuenteRepository;
import sv.edu.udb.api_especieextionsion.service.impl.FuenteServiceImpl;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(FuenteServiceImpl.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class FuenteServiceImplDataJpaTest {

    @Autowired FuenteRepository repo;
    @Autowired FuenteService service;
    @Autowired EntityManager em;

    // ===== helpers =====
    private FuenteRequest req(
            String nombre,
            String descripcion,
            String tipo,
            String enlace,
            LocalDate fecha
    ){
        FuenteRequest r = new FuenteRequest();
        r.setNombre(nombre);
        r.setDescripcion(descripcion);
        r.setTipo(tipo);
        r.setEnlace(enlace);
        r.setFechaPublicacion(fecha);
        return r;
    }

    private Fuente fuente(
            String nombre,
            String descripcion,
            String tipo,
            String enlace,
            LocalDate fecha
    ){
        return Fuente.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .tipo(tipo)
                .enlace(enlace)
                .fechaPublicacion(fecha)
                .build();
    }

    // ===== tests =====

    @Test
    @DisplayName("crear: persiste y mapea a FuenteResponse")
    void crear_ok() {
        FuenteResponse res = service.crear(
                req("IUCN Red List", "Base global", "WEB", "https://iucn.org", LocalDate.now())
        );

        assertThat(res.getId()).isNotNull();
        assertThat(res.getNombre()).isEqualTo("IUCN Red List");
        assertThat(repo.count()).isEqualTo(1);
        assertThat(repo.existsByNombre("IUCN Red List")).isTrue();
    }

    @Test
    @DisplayName("crear: lanza DataIntegrityViolationException si el nombre ya existe")
    void crear_duplicado() {
        service.crear(req("CONABIO", "Biodiversidad MX", "WEB", "https://conabio.gob.mx", LocalDate.now()));

        assertThatThrownBy(() ->
                service.crear(req("CONABIO", "Otra desc", "WEB", "https://otro", LocalDate.now()))
        )
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ya existe");

        // Por si en algún flujo se intentó persistir y falló la unicidad, limpiamos contexto
        em.clear();

        assertThat(repo.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("listar: devuelve todas las fuentes mapeadas")
    void listar_ok() {
        repo.saveAll(List.of(
                fuente("F1", "d1", "WEB", "http://f1", LocalDate.now()),
                fuente("F2", "d2", "PDF", "http://f2", LocalDate.now())
        ));

        List<FuenteResponse> list = service.listar();

        assertThat(list).hasSize(2);
        assertThat(list).extracting(FuenteResponse::getNombre)
                .containsExactlyInAnyOrder("F1", "F2");
    }

    @Test
    @DisplayName("obtener: devuelve la fuente existente")
    void obtener_ok() {
        Fuente saved = repo.save(
                fuente("GBIF", "Datos de ocurrencia", "WEB", "https://gbif.org", LocalDate.now())
        );

        FuenteResponse res = service.obtener(saved.getId());

        assertThat(res.getId()).isEqualTo(saved.getId());
        assertThat(res.getNombre()).isEqualTo("GBIF");
    }

    @Test
    @DisplayName("obtener: 404 cuando no existe")
    void obtener_notFound() {
        assertThatThrownBy(() -> service.obtener(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("no existe");
    }

    @Test
    @DisplayName("actualizar: modifica y persiste cambios")
    void actualizar_ok() {
        Fuente saved = repo.save(
                fuente("Old", "desc", "WEB", "http://old", LocalDate.now())
        );

        FuenteResponse res = service.actualizar(saved.getId(),
                req("New", "nueva desc", "PDF", "http://new", LocalDate.now())
        );

        assertThat(res.getNombre()).isEqualTo("New");

        Fuente inDb = repo.findById(saved.getId()).orElseThrow();
        assertThat(inDb.getNombre()).isEqualTo("New");
        assertThat(inDb.getDescripcion()).isEqualTo("nueva desc");
        assertThat(inDb.getTipo()).isEqualTo("PDF");
        assertThat(inDb.getEnlace()).isEqualTo("http://new");
    }

    @Test
    @DisplayName("actualizar: lanza DataIntegrityViolationException si el nombre ya está en uso")
    void actualizar_conflict() {
        Fuente f1 = repo.save(fuente("FN-1", "a", "WEB", "http://a", LocalDate.now()));
        Fuente f2 = repo.save(fuente("FN-2", "b", "PDF", "http://b", LocalDate.now()));

        assertThatThrownBy(() ->
                service.actualizar(f2.getId(),
                        req("FN-1", "b2", "PDF", "http://b2", LocalDate.now()))
        )
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ya existe");

        // Asegurar que f2 se mantiene igual tras el intento fallido
        em.clear();
        assertThat(repo.findById(f2.getId()).orElseThrow().getNombre()).isEqualTo("FN-2");
    }

    @Test
    @DisplayName("eliminar: elimina cuando existe")
    void eliminar_ok() {
        Fuente f = repo.save(
                fuente("Del-1", "para borrar", "WEB", "http://del", LocalDate.now())
        );

        service.eliminar(f.getId());

        assertThat(repo.existsById(f.getId())).isFalse();
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

