package sv.edu.udb.api_especieextionsion.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import sv.edu.udb.api_especieextionsion.domain.Amenaza;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AmenazaRepositoryTest {

    @Autowired
    private AmenazaRepository repo;

    private Amenaza nuevaAmenaza(String codigo, String tipo) {
        return Amenaza.builder()
                .codigo(codigo)
                .tipo(tipo)
                .descripcion("Descripción de " + tipo)
                .build();
    }

    @Test
    @DisplayName("save + findById: persiste y recupera una Amenaza")
    void save_and_findById() {
        Amenaza a = repo.save(nuevaAmenaza("IUCN-1.1", "Pérdida de hábitat"));

        assertThat(a.getId()).isNotNull();

        Optional<Amenaza> found = repo.findById(a.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCodigo()).isEqualTo("IUCN-1.1");
        assertThat(found.get().getTipo()).isEqualTo("Pérdida de hábitat");
    }

    @Test
    @DisplayName("existsByCodigo: true si existe, false si no (sensible a mayúsculas)")
    void existsByCodigo_true_false() {
        assertThat(repo.existsByCodigo("IUCN-2.3")).isFalse();

        repo.save(nuevaAmenaza("IUCN-2.3", "Sobreexplotación"));

        assertThat(repo.existsByCodigo("IUCN-2.3")).isTrue();
        assertThat(repo.existsByCodigo("iucn-2.3")).isFalse(); // normalmente case-sensitive
    }

    @Test
    @DisplayName("Unicidad en 'codigo': falla al intentar duplicar")
    void unique_codigo_constraint_violation() {
        repo.saveAndFlush(nuevaAmenaza("IUCN-3.1", "Especies invasoras"));

        Amenaza duplicada = nuevaAmenaza("IUCN-3.1", "Otra cosa");

        assertThatThrownBy(() -> repo.saveAndFlush(duplicada))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("update: modifica tipo y descripción")
    void update_fields() {
        Amenaza a = repo.save(nuevaAmenaza("IUCN-4.5", "Contaminación"));

        a.setTipo("Contaminación (actualizada)");
        a.setDescripcion("Descripción actualizada");
        Amenaza updated = repo.saveAndFlush(a);

        assertThat(updated.getTipo()).isEqualTo("Contaminación (actualizada)");
        assertThat(updated.getDescripcion()).isEqualTo("Descripción actualizada");
    }

    @Test
    @DisplayName("delete: elimina por id")
    void delete_by_id() {
        Amenaza a = repo.save(nuevaAmenaza("IUCN-5.7", "Cambio climático"));
        Long id = a.getId();

        repo.deleteById(id);
        repo.flush();

        assertThat(repo.findById(id)).isNotPresent();
    }

    @Test
    @DisplayName("findAll: retorna todas las amenazas insertadas")
    void findAll_returns_inserted() {
        repo.save(nuevaAmenaza("IUCN-6.1", "Incendios"));
        repo.save(nuevaAmenaza("IUCN-6.2", "Sequías"));

        assertThat(repo.findAll())
                .extracting(Amenaza::getCodigo)
                .contains("IUCN-6.1", "IUCN-6.2");
    }
}


