package sv.edu.udb.api_especieextionsion.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import sv.edu.udb.api_especieextionsion.domain.Amenaza;
import sv.edu.udb.api_especieextionsion.domain.Especie;
import sv.edu.udb.api_especieextionsion.domain.EspecieAmenaza;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class EspecieAmenazaRepositoryTest {

    @Autowired
    private EspecieAmenazaRepository repo;

    @Autowired
    private TestEntityManager em;

    // ---------- Helpers ----------
    private Especie guardarEspecie(String nombreCientifico) {
        Especie e = new Especie();
        e.setNombreCientifico(nombreCientifico);
        e.setNombreComun("Común " + nombreCientifico);
        e.setTipo("MAMIFERO");
        e.setEstadoConservacion("EN_PELIGRO");
        e.setDescripcion("Desc " + nombreCientifico);
        e.setEsEndemica(Boolean.TRUE);
        e.setFechaRegistro(LocalDate.now());
        return em.persistAndFlush(e);
    }

    private Amenaza guardarAmenaza(String codigo) {
        Amenaza a = new Amenaza();
        a.setCodigo(codigo);
        a.setTipo("CAZA");
        a.setDescripcion("Amenaza " + codigo);
        return em.persistAndFlush(a);
    }

    private EspecieAmenaza link(Especie e, Amenaza a, String sev) {
        EspecieAmenaza l = EspecieAmenaza.builder()
                .especie(e).amenaza(a).severidad(sev)
                .build();
        return repo.saveAndFlush(l);
    }

    // ---------- Tests ----------

    @Test
    @DisplayName("exists/find: operaciones básicas sobre especie↔amenaza")
    void exists_and_find_methods_work() {
        Especie e1 = guardarEspecie("Panthera onca");
        Especie e2 = guardarEspecie("Ara macao");
        Amenaza a1 = guardarAmenaza("IUCN-1.1");
        Amenaza a2 = guardarAmenaza("IUCN-3.2");

        link(e1, a1, "ALTA");
        link(e1, a2, "MEDIA");
        link(e2, a1, "BAJA");

        // existsByEspecieIdAndAmenazaId
        assertThat(repo.existsByEspecieIdAndAmenazaId(e1.getId(), a1.getId())).isTrue();
        assertThat(repo.existsByEspecieIdAndAmenazaId(e1.getId(), a2.getId())).isTrue();
        assertThat(repo.existsByEspecieIdAndAmenazaId(e2.getId(), a2.getId())).isFalse();

        // findByEspecieId
        List<EspecieAmenaza> deE1 = repo.findByEspecieId(e1.getId());
        assertThat(deE1).hasSize(2);
        assertThat(deE1).extracting(l -> l.getAmenaza().getCodigo())
                .containsExactlyInAnyOrder("IUCN-1.1", "IUCN-3.2");

        // findByEspecieIdAndAmenazaId
        var opt = repo.findByEspecieIdAndAmenazaId(e1.getId(), a2.getId());
        assertThat(opt).isPresent();
        assertThat(opt.get().getSeveridad()).isEqualTo("MEDIA");
    }

    @Test
    @DisplayName("deleteByEspecieIdAndAmenazaId: elimina el vínculo")
    void delete_by_pair_deletes_link() {
        Especie e = guardarEspecie("Crax rubra");
        Amenaza a = guardarAmenaza("IUCN-2.5");
        link(e, a, "MEDIA");

        repo.deleteByEspecieIdAndAmenazaId(e.getId(), a.getId());
        repo.flush();

        assertThat(repo.existsByEspecieIdAndAmenazaId(e.getId(), a.getId())).isFalse();
        assertThat(repo.findByEspecieId(e.getId())).isEmpty();
    }

    @Test
    @DisplayName("Unicidad (especie_id, amenaza_id): falla al duplicar el mismo par")
    void unique_pair_constraint_violated_on_duplicate() {
        Especie e = guardarEspecie("Puma concolor");
        Amenaza a = guardarAmenaza("IUCN-4.1");
        link(e, a, "ALTA");

        EspecieAmenaza duplicado = EspecieAmenaza.builder()
                .especie(e).amenaza(a).severidad("BAJA")
                .build();

        assertThatThrownBy(() -> repo.saveAndFlush(duplicado))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("severidad NOT NULL: falla si es null")
    void severidad_notnull_violation() {
        Especie e = guardarEspecie("Tayassu pecari");
        Amenaza a = guardarAmenaza("IUCN-7.8");

        EspecieAmenaza l = EspecieAmenaza.builder()
                .especie(e).amenaza(a).severidad(null)
                .build();

        assertThatThrownBy(() -> repo.saveAndFlush(l))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("FK especie NOT NULL: falla si especie es null")
    void especie_fk_notnull_violation() {
        Amenaza a = guardarAmenaza("IUCN-9.9");
        EspecieAmenaza l = EspecieAmenaza.builder()
                .especie(null).amenaza(a).severidad("MEDIA")
                .build();

        assertThatThrownBy(() -> repo.saveAndFlush(l))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("FK amenaza NOT NULL: falla si amenaza es null")
    void amenaza_fk_notnull_violation() {
        Especie e = guardarEspecie("Potos flavus");
        EspecieAmenaza l = EspecieAmenaza.builder()
                .especie(e).amenaza(null).severidad("BAJA")
                .build();

        assertThatThrownBy(() -> repo.saveAndFlush(l))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Actualizar severidad sobre un vínculo existente")
    void update_severity_on_link() {
        Especie e = guardarEspecie("Leopardus pardalis");
        Amenaza a = guardarAmenaza("IUCN-1.3");
        EspecieAmenaza l = link(e, a, "BAJA");

        l.setSeveridad("ALTA");
        repo.saveAndFlush(l);

        var updated = repo.findByEspecieIdAndAmenazaId(e.getId(), a.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getSeveridad()).isEqualTo("ALTA");
    }
}

