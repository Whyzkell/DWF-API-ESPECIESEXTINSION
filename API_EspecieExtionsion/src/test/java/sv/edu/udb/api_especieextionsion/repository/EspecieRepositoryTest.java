package sv.edu.udb.api_especieextionsion.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import sv.edu.udb.api_especieextionsion.domain.Especie;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class EspecieRepositoryTest {

    @Autowired
    private EspecieRepository repo;

    @Autowired
    private TestEntityManager em;

    // ---------- Helper para crear una entidad válida ----------
    private Especie nueva(String nombreCientifico) {
        return Especie.builder()
                .nombreCientifico(nombreCientifico)
                .nombreComun("Nombre común de " + nombreCientifico)
                .tipo("FAUNA")
                .estadoConservacion("VU")
                .descripcion("Descripción de " + nombreCientifico)
                .esEndemica(Boolean.TRUE)
                .fechaRegistro(LocalDate.now())
                .build();
    }

    // ---------- Tests ----------

    @Test
    @DisplayName("Guardar y leer por ID")
    void save_and_findById() {
        Especie e = repo.saveAndFlush(nueva("Panthera onca"));
        Optional<Especie> encontrado = repo.findById(e.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNombreCientifico()).isEqualTo("Panthera onca");
        assertThat(encontrado.get().getNombreComun()).startsWith("Nombre común");
    }

    @Test
    @DisplayName("existsByNombreCientifico: true/false")
    void existsByNombreCientifico_returns_expected() {
        repo.saveAndFlush(nueva("Ara macao"));

        assertThat(repo.existsByNombreCientifico("Ara macao")).isTrue();
        assertThat(repo.existsByNombreCientifico("NoExiste")).isFalse();
    }

    @Test
    @DisplayName("Unicidad nombre_cientifico: falla si duplico")
    void unique_nombre_cientifico_violated_on_duplicate() {
        repo.saveAndFlush(nueva("Puma concolor"));

        // Segundo con el mismo nombre científico debe violar la UK
        assertThatThrownBy(() -> repo.saveAndFlush(nueva("Puma concolor")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("NOT NULL (ejemplo con tipo): falla si es null")
    void notnull_violation_on_required_field() {
        Especie e = nueva("Mustela frenata");
        e.setTipo(null); // columna 'tipo' es NOT NULL

        assertThatThrownBy(() -> repo.saveAndFlush(e))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Actualizar campos y persistir cambios")
    void update_entity_fields() {
        Especie e = repo.saveAndFlush(nueva("Leopardus pardalis"));
        e.setNombreComun("Ocelote");
        e.setEstadoConservacion("EN");

        Especie actualizado = repo.saveAndFlush(e);

        assertThat(actualizado.getNombreComun()).isEqualTo("Ocelote");
        assertThat(actualizado.getEstadoConservacion()).isEqualTo("EN");
    }

    @Test
    @DisplayName("Eliminar por ID")
    void delete_by_id() {
        Especie e = repo.saveAndFlush(nueva("Crax rubra"));
        Long id = e.getId();

        repo.deleteById(id);
        repo.flush();

        assertThat(repo.findById(id)).isEmpty();
    }
}


