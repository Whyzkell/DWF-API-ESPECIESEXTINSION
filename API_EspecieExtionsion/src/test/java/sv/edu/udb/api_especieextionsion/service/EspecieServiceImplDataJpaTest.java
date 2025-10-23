package sv.edu.udb.api_especieextionsion.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieResponse;
import sv.edu.udb.api_especieextionsion.domain.Especie;
import sv.edu.udb.api_especieextionsion.repository.EspecieRepository;
import sv.edu.udb.api_especieextionsion.service.impl.EspecieServiceImpl;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(EspecieServiceImpl.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class EspecieServiceImplDataJpaTest {

    @Autowired EspecieRepository repo;
    @Autowired EspecieService service;
    @Autowired EntityManager em; // <-- para limpiar el contexto tras excepciones

    // ===== helpers =====
    private EspecieRequest req(
            String nombreCientifico, String nombreComun,
            String tipo, String estado, String descripcion,
            boolean endemica, LocalDate fecha) {

        EspecieRequest r = new EspecieRequest();
        r.setNombreCientifico(nombreCientifico);
        r.setNombreComun(nombreComun);
        r.setTipo(tipo);
        r.setEstadoConservacion(estado);
        r.setDescripcion(descripcion);
        r.setEsEndemica(endemica);
        r.setFechaRegistro(fecha);
        return r;
    }

    private Especie especie(
            String nc, String nombre,
            String tipo, String estado,
            String desc, boolean endemica, LocalDate fecha) {
        return Especie.builder()
                .nombreCientifico(nc)
                .nombreComun(nombre)
                .tipo(tipo)
                .estadoConservacion(estado)
                .descripcion(desc)
                .esEndemica(endemica)
                .fechaRegistro(fecha)
                .build();
    }

    // ===== tests =====

    @Test
    @DisplayName("crear: persiste y mapea a EspecieResponse")
    void crear_ok() {
        EspecieResponse res = service.crear(
                req("Panthera onca", "Jaguar", "FAUNA", "VU", "Felino", false, LocalDate.now())
        );

        assertThat(res.getId()).isNotNull();
        assertThat(res.getNombreCientifico()).isEqualTo("Panthera onca");
        assertThat(repo.count()).isEqualTo(1);
        assertThat(repo.existsByNombreCientifico("Panthera onca")).isTrue();
    }

    @Test
    @DisplayName("crear: lanza IllegalArgumentException si nombre científico ya existe")
    void crear_duplicado() {
        service.crear(req("Quercus robur", "Roble", "FLORA", "LC", "Árbol", false, LocalDate.now()));

        assertThatThrownBy(() ->
                service.crear(req("Quercus robur", "Roble 2", "FLORA", "LC", "Árbol", false, LocalDate.now()))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre científico").hasMessageContaining("existe");

        // Limpia el contexto de persistencia después de la excepción de unicidad
        em.clear();

        assertThat(repo.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("obtener: devuelve la especie existente")
    void obtener_ok() {
        Especie saved = repo.save(
                especie("Ara macao", "Guacamaya roja", "FAUNA", "LC", "Ave", false, LocalDate.now())
        );

        EspecieResponse res = service.obtener(saved.getId());

        assertThat(res.getId()).isEqualTo(saved.getId());
        assertThat(res.getNombreComun()).isEqualTo("Guacamaya roja");
    }

    @Test
    @DisplayName("obtener: 404 cuando no existe")
    void obtener_notFound() {
        assertThatThrownBy(() -> service.obtener(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("no encontrada");
    }

    @Test
    @DisplayName("listar: devuelve todas las especies mapeadas")
    void listar_ok() {
        repo.saveAll(List.of(
                especie("Sp-1", "Uno", "FAUNA", "VU", "d1", false, LocalDate.now()),
                especie("Sp-2", "Dos", "FLORA", "EN", "d2", true, LocalDate.now())
        ));

        List<EspecieResponse> list = service.listar();

        assertThat(list).hasSize(2);
        assertThat(list).extracting(EspecieResponse::getNombreCientifico)
                .containsExactlyInAnyOrder("Sp-1", "Sp-2");
    }

    @Test
    @DisplayName("actualizar: modifica y persiste cambios")
    void actualizar_ok() {
        Especie saved = repo.save(
                especie("Old-nc", "Vieja", "FAUNA", "VU", "desc", false, LocalDate.now())
        );

        EspecieResponse res = service.actualizar(saved.getId(),
                req("New-nc", "Nueva", "FLORA", "EN", "nueva desc", true, LocalDate.now())
        );

        assertThat(res.getNombreCientifico()).isEqualTo("New-nc");
        Especie inDb = repo.findById(saved.getId()).orElseThrow();
        assertThat(inDb.getNombreComun()).isEqualTo("Nueva");
        assertThat(inDb.getTipo()).isEqualTo("FLORA");
        assertThat(inDb.getEstadoConservacion()).isEqualTo("EN");
        assertThat(inDb.getEsEndemica()).isTrue();
    }

    @Test
    @DisplayName("actualizar: lanza IllegalArgumentException si el nombre científico ya está en uso")
    void actualizar_conflict() {
        Especie e1 = repo.save(especie("NC-1", "A", "FAUNA", "VU", "d", false, LocalDate.now()));
        Especie e2 = repo.save(especie("NC-2", "B", "FLORA", "LC", "d", true, LocalDate.now()));

        assertThatThrownBy(() ->
                service.actualizar(e2.getId(),
                        req("NC-1", "B2", "FLORA", "LC", "d", true, LocalDate.now()))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nombre científico");

        // Limpia el contexto tras la excepción para leer desde BD consistente
        em.clear();

        // e2 se mantiene con su nombre original
        assertThat(repo.findById(e2.getId()).orElseThrow().getNombreCientifico()).isEqualTo("NC-2");
    }

    @Test
    @DisplayName("eliminar: elimina cuando existe")
    void eliminar_ok() {
        Especie e = repo.save(
                especie("Del-1", "Para borrar", "FAUNA", "VU", "d", false, LocalDate.now())
        );

        service.eliminar(e.getId());

        assertThat(repo.existsById(e.getId())).isFalse();
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




