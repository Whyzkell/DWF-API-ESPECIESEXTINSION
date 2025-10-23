package sv.edu.udb.api_especieextionsion.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import sv.edu.udb.api_especieextionsion.domain.Fuente;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class FuenteRepositoryTest {

    @Autowired
    private FuenteRepository repo;

    private Fuente nuevaFuente(String nombre) {
        Fuente f = new Fuente();
        f.setNombre(nombre);
        f.setDescripcion("Referencia de prueba");
        f.setTipo("ARTICULO");
        f.setEnlace("https://ejemplo.test/" + nombre);
        f.setFechaPublicacion(LocalDate.of(2023, 7, 1));
        return f;
    }

    @Test
    @DisplayName("save + findById: persiste y recupera una Fuente")
    void save_and_findById() {
        Fuente f = nuevaFuente("IUCN 2023");
        Fuente saved = repo.save(f);

        assertThat(saved.getId()).isNotNull();

        Optional<Fuente> found = repo.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getNombre()).isEqualTo("IUCN 2023");
        assertThat(found.get().getTipo()).isEqualTo("ARTICULO");
    }

    @Test
    @DisplayName("existsByNombre: true si existe, false si no")
    void existsByNombre_true_false() {
        assertThat(repo.existsByNombre("WHO 2022")).isFalse();

        repo.save(nuevaFuente("WHO 2022"));

        assertThat(repo.existsByNombre("WHO 2022")).isTrue();
        assertThat(repo.existsByNombre("who 2022")).isFalse(); // método es case-sensitive
    }

    @Test
    @DisplayName("Unicidad de nombre: lanza DataIntegrityViolationException en duplicados")
    void unique_nombre_constraint_violation() {
        repo.save(nuevaFuente("UDB Reporte"));

        // Intento de duplicar el 'nombre' único
        Fuente duplicada = nuevaFuente("UDB Reporte");

        assertThatThrownBy(() -> repo.saveAndFlush(duplicada))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("update: modifica campos de una Fuente existente")
    void update_fuente_fields() {
        Fuente f = repo.save(nuevaFuente("MARN 2021"));

        f.setDescripcion("Descripción actualizada");
        f.setTipo("REPORTE");
        f.setEnlace("https://marn.gob.sv/reporte-2021");
        f.setFechaPublicacion(LocalDate.of(2021, 12, 31));

        Fuente updated = repo.saveAndFlush(f);

        assertThat(updated.getDescripcion()).isEqualTo("Descripción actualizada");
        assertThat(updated.getTipo()).isEqualTo("REPORTE");
        assertThat(updated.getEnlace()).isEqualTo("https://marn.gob.sv/reporte-2021");
        assertThat(updated.getFechaPublicacion()).isEqualTo(LocalDate.of(2021, 12, 31));
    }

    @Test
    @DisplayName("delete: elimina una Fuente")
    void delete_fuente() {
        Fuente f = repo.save(nuevaFuente("CONABIO 2020"));
        Long id = f.getId();

        repo.deleteById(id);
        repo.flush();

        assertThat(repo.findById(id)).isNotPresent();
    }
}


