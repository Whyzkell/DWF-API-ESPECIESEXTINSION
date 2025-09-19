package sv.edu.udb.api_especieextionsion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.jdbc.Sql;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/* ====== Entidades reales (main) ====== */
import sv.edu.udb.api_especieextionsion.domain.Amenaza;
import sv.edu.udb.api_especieextionsion.domain.DistribucionGeografica;
import sv.edu.udb.api_especieextionsion.domain.Especie;
import sv.edu.udb.api_especieextionsion.domain.EspecieAmenaza;
import sv.edu.udb.api_especieextionsion.domain.Fuente;
import sv.edu.udb.api_especieextionsion.domain.Rol;
import sv.edu.udb.api_especieextionsion.domain.Usuario;

/**
 * Pruebas JPA para TODAS las entidades de domain usando H2 en memoria.
 * Define repos de prueba inline para no tocar producción.
 */
@DataJpaTest
class DomainsJpaTest {

    /* ====== Repos de prueba (inline) ====== */
    interface AmenazaRepo extends JpaRepository<Amenaza, Long> {}
    interface EspecieRepo extends JpaRepository<Especie, Long> {}
    interface DistribucionRepo extends JpaRepository<DistribucionGeografica, Long> {}
    interface EspecieAmenazaRepo extends JpaRepository<EspecieAmenaza, Long> {}
    interface FuenteRepo extends JpaRepository<Fuente, Long> {}
    interface UsuarioRepo extends JpaRepository<Usuario, Long> {}

    @Autowired AmenazaRepo amenazaRepo;
    @Autowired EspecieRepo especieRepo;
    @Autowired DistribucionRepo distribucionRepo;
    @Autowired EspecieAmenazaRepo especieAmenazaRepo;
    @Autowired FuenteRepo fuenteRepo;
    @Autowired UsuarioRepo usuarioRepo;

    @PersistenceContext EntityManager em;

    /* ========================= Amenaza ========================= */
    @Nested @DisplayName("Amenaza")
    class AmenazaTests {

        @Test @DisplayName("Persist y recuperar")
        void persistAndFind() {
            Amenaza a = Amenaza.builder()
                    .codigo("DEFORESTACION")
                    .tipo("ACTIVIDADES_HUMANAS")
                    .descripcion("Eliminación de masa forestal")
                    .build();

            a = amenazaRepo.saveAndFlush(a);
            Amenaza got = amenazaRepo.findById(a.getId()).orElseThrow();
            assertThat(got.getCodigo()).isEqualTo("DEFORESTACION");
            assertThat(got.getTipo()).isEqualTo("ACTIVIDADES_HUMANAS");
        }

        @Test @DisplayName("Unique codigo -> 409 DB (DataIntegrityViolationException)")
        void uniqueCodigo() {
            amenazaRepo.saveAndFlush(Amenaza.builder().codigo("X").tipo("T").build());
            Amenaza dup = Amenaza.builder().codigo("X").tipo("T2").build();

            assertThatThrownBy(() -> {
                amenazaRepo.saveAndFlush(dup);
                em.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    /* ========================= Especie ========================= */
    @Nested @DisplayName("Especie")
    class EspecieTests {

        private Especie nuevaValida(String cientifico) {
            return Especie.builder()
                    .nombreCientifico(cientifico)
                    .nombreComun("Jaguar")
                    .tipo("FAUNA")
                    .estadoConservacion("EN")
                    .descripcion("Felino nativo de América")
                    .esEndemica(Boolean.TRUE)
                    .fechaRegistro(LocalDate.now())
                    .build();
        }

        @Test @DisplayName("Persist básico")
        void persist() {
            Especie e = especieRepo.saveAndFlush(nuevaValida("Panthera onca"));
            Especie got = especieRepo.findById(e.getId()).orElseThrow();
            assertThat(got.getNombreCientifico()).isEqualTo("Panthera onca");
            assertThat(got.getTipo()).isEqualTo("FAUNA");
        }

        @Test @DisplayName("Unique nombre_cientifico")
        void uniqueNombreCientifico() {
            especieRepo.saveAndFlush(nuevaValida("Panthera onca"));
            Especie dup = nuevaValida("Panthera onca");

            assertThatThrownBy(() -> {
                especieRepo.saveAndFlush(dup);
                em.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    /* ================== DistribucionGeografica ================== */
    @Nested @DisplayName("DistribucionGeografica")
    class DistribucionTests {

        @Test @DisplayName("Persist con relación a Especie")
        void persistWithEspecie() {
            Especie e = especieRepo.saveAndFlush(Especie.builder()
                    .nombreCientifico("Testus spec")
                    .nombreComun("Test")
                    .tipo("FAUNA")
                    .estadoConservacion("VU")
                    .esEndemica(false)
                    .fechaRegistro(LocalDate.now())
                    .build());

            DistribucionGeografica d = DistribucionGeografica.builder()
                    .especie(e)
                    .region("Centroamérica")
                    .ecosistema("Bosque")
                    .latitud(13.6)
                    .longitud(-89.2)
                    .precisionMetros(20)
                    .fechaObservacion(LocalDate.now())
                    .build();

            d = distribucionRepo.saveAndFlush(d);
            DistribucionGeografica got = distribucionRepo.findById(d.getId()).orElseThrow();
            assertThat(got.getEspecie().getId()).isEqualTo(e.getId());
            assertThat(got.getRegion()).isEqualTo("Centroamérica");
        }
    }

    /* ===================== EspecieAmenaza ===================== */
    @Nested @DisplayName("EspecieAmenaza")
    class EspecieAmenazaTests {

        private Especie especie() {
            return especieRepo.saveAndFlush(Especie.builder()
                    .nombreCientifico("Spec1").nombreComun("Com1")
                    .tipo("FAUNA").estadoConservacion("EN")
                    .esEndemica(true).fechaRegistro(LocalDate.now()).build());
        }
        private Amenaza amenaza(String cod) {
            return amenazaRepo.saveAndFlush(Amenaza.builder().codigo(cod).tipo("T").build());
        }

        @Test @DisplayName("Persist con relaciones")
        void persistLink() {
            Especie e = especie();
            Amenaza a = amenaza("A-1");

            EspecieAmenaza link = EspecieAmenaza.builder()
                    .especie(e).amenaza(a).severidad("ALTA").build();

            link = especieAmenazaRepo.saveAndFlush(link);
            EspecieAmenaza got = especieAmenazaRepo.findById(link.getId()).orElseThrow();

            assertThat(got.getEspecie().getId()).isEqualTo(e.getId());
            assertThat(got.getAmenaza().getId()).isEqualTo(a.getId());
            assertThat(got.getSeveridad()).isEqualTo("ALTA");
        }

        @Test @DisplayName("Unique (especie, amenaza)")
        void uniqueComposite() {
            Especie e = especie();
            Amenaza a = amenaza("A-2");

            especieAmenazaRepo.saveAndFlush(EspecieAmenaza.builder()
                    .especie(e).amenaza(a).severidad("ALTA").build());

            EspecieAmenaza dup = EspecieAmenaza.builder()
                    .especie(e).amenaza(a).severidad("BAJA").build();

            assertThatThrownBy(() -> {
                especieAmenazaRepo.saveAndFlush(dup);
                em.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    /* ========================= Fuente ========================= */
    @Nested @DisplayName("Fuente")
    class FuenteTests {

        @Test @DisplayName("Persist ok y unique nombre")
        void persistAndUnique() {
            Fuente f1 = fuenteRepo.saveAndFlush(Fuente.builder()
                    .nombre("IUCN Red List 2024 – Panthera onca")
                    .descripcion("Ficha")
                    .tipo("WEB")
                    .enlace("https://example.org/x")
                    .fechaPublicacion(LocalDate.now())
                    .build());
            assertThat(f1.getId()).isNotNull();

            Fuente dup = Fuente.builder()
                    .nombre("IUCN Red List 2024 – Panthera onca")
                    .build();

            assertThatThrownBy(() -> {
                fuenteRepo.saveAndFlush(dup);
                em.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    /* ========================= Usuario ========================= */
    @Nested @DisplayName("Usuario")
    class UsuarioTests {

        private Usuario nuevo(String user, String email) {
            return Usuario.builder()
                    .username(user)
                    .nombreCompleto("Nombre Apellido")
                    .email(email)
                    .rol(Rol.EDITOR)
                    .activo(true)
                    .fechaRegistro(LocalDate.now())
                    .build();
        }

        @Test @DisplayName("Persist y recuperación")
        void persist() {
            Usuario u = usuarioRepo.saveAndFlush(nuevo("dhernandez", "diego@udb.edu.sv"));
            Usuario got = usuarioRepo.findById(u.getId()).orElseThrow();

            assertThat(got.getUsername()).isEqualTo("dhernandez");
            assertThat(got.getRol()).isEqualTo(Rol.EDITOR);
            assertThat(got.getActivo()).isTrue();
        }

        @Test @DisplayName("Unique username y unique email")
        void uniqueUsernameAndEmail() {
            usuarioRepo.saveAndFlush(nuevo("u1", "u1@x.com"));

            // duplicado username
            assertThatThrownBy(() -> {
                usuarioRepo.saveAndFlush(nuevo("u1", "otro@x.com"));
                em.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);

            // duplicado email (primero nuevo username válido)
            usuarioRepo.deleteAll(); // limpiar para no arrastrar unique anterior
            usuarioRepo.saveAndFlush(nuevo("u2", "u2@x.com"));
            assertThatThrownBy(() -> {
                usuarioRepo.saveAndFlush(nuevo("u3", "u2@x.com"));
                em.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}
