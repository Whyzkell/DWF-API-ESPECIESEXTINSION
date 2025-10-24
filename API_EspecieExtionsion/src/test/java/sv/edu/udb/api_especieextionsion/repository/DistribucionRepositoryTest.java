package sv.edu.udb.api_especieextionsion.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import sv.edu.udb.api_especieextionsion.repository.domain.DistribucionGeografica;
import sv.edu.udb.api_especieextionsion.repository.domain.Especie;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class DistribucionRepositoryTest {

    @Autowired
    private DistribucionRepository repo;

    @Autowired
    private TestEntityManager em;

    // ----- Helpers -----
    private Especie guardarEspecie(String nombreCientifico) {
        Especie e = new Especie();
        e.setNombreCientifico(nombreCientifico);
        e.setNombreComun("Comun " + nombreCientifico);
        e.setTipo("MAMIFERO");
        e.setEstadoConservacion("EN_PELIGRO");
        e.setDescripcion("Descripción de " + nombreCientifico);
        e.setEsEndemica(Boolean.TRUE);
        e.setFechaRegistro(LocalDate.now());
        return em.persistAndFlush(e);
    }

    private DistribucionGeografica construirDistribucion(Especie especie, String region) {
        DistribucionGeografica d = new DistribucionGeografica();
        d.setEspecie(especie);
        d.setRegion(region);
        d.setEcosistema("Bosque nublado");
        d.setLatitud(13.70);
        d.setLongitud(-89.20);
        d.setPrecisionMetros(50);
        d.setFechaObservacion(LocalDate.of(2025, 1, 1));
        return d;
    }

    // ----- Tests -----

    @Test
    @DisplayName("findByEspecieId: devuelve solo los registros de esa especie")
    void findByEspecieId_returns_only_for_that_species() {
        Especie e1 = guardarEspecie("Panthera onca");
        Especie e2 = guardarEspecie("Ara macao");

        repo.save(construirDistribucion(e1, "Ahuachapán"));
        repo.save(construirDistribucion(e1, "Sonsonate"));
        repo.save(construirDistribucion(e2, "La Unión"));
        repo.flush();

        List<DistribucionGeografica> deE1 = repo.findByEspecieId(e1.getId());
        assertThat(deE1).hasSize(2);
        assertThat(deE1).extracting(DistribucionGeografica::getRegion)
                .containsExactlyInAnyOrder("Ahuachapán", "Sonsonate");

        List<DistribucionGeografica> deE2 = repo.findByEspecieId(e2.getId());
        assertThat(deE2).hasSize(1);
        assertThat(deE2.get(0).getRegion()).isEqualTo("La Unión");
    }

    @Test
    @DisplayName("Guardar sin opcionales (precisionMetros, fechaObservacion) funciona")
    void save_without_optional_fields_ok() {
        Especie e = guardarEspecie("Crax rubra");
        DistribucionGeografica d = construirDistribucion(e, "Chalatenango");
        d.setPrecisionMetros(null);
        d.setFechaObservacion(null);

        DistribucionGeografica saved = repo.saveAndFlush(d);
        assertThat(saved.getId()).isNotNull();

        List<DistribucionGeografica> list = repo.findByEspecieId(e.getId());
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getPrecisionMetros()).isNull();
        assertThat(list.get(0).getFechaObservacion()).isNull();
    }

    @Test
    @DisplayName("FK especie NOT NULL: falla si especie es null")
    void fk_especie_notnull_violation() {
        DistribucionGeografica d = new DistribucionGeografica();
        d.setEspecie(null); // <= viola @ManyToOne(optional=false) + @JoinColumn(nullable=false)
        d.setRegion("Cuscatlán");
        d.setEcosistema("Bosque seco");
        d.setLatitud(13.8);
        d.setLongitud(-88.9);

        assertThatThrownBy(() -> repo.saveAndFlush(d))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Latitud NOT NULL: falla si latitud es null")
    void latitud_notnull_violation() {
        Especie e = guardarEspecie("Tayassu pecari");

        DistribucionGeografica d = new DistribucionGeografica();
        d.setEspecie(e);
        d.setRegion("Morazán");
        d.setEcosistema("Selva tropical");
        d.setLatitud(null); // NOT NULL
        d.setLongitud(-88.0);

        assertThatThrownBy(() -> repo.saveAndFlush(d))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Longitud NOT NULL: falla si longitud es null")
    void longitud_notnull_violation() {
        Especie e = guardarEspecie("Puma concolor");

        DistribucionGeografica d = new DistribucionGeografica();
        d.setEspecie(e);
        d.setRegion("Usulután");
        d.setEcosistema("Bosque húmedo");
        d.setLatitud(13.3);
        d.setLongitud(null); // NOT NULL

        assertThatThrownBy(() -> repo.saveAndFlush(d))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

