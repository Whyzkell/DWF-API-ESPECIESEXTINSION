package sv.edu.udb.api_especieextionsion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/* DTOs reales (main) */
import sv.edu.udb.api_especieextionsion.controller.dto.AmenazaRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.DistribucionRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieAmenazaLinkRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.FuenteRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.UsuarioRequest;
import sv.edu.udb.api_especieextionsion.domain.Rol;

/**
 * Suite única: validación Bean Validation de TODOS los DTOs de controller.
 * - Requiere dependency: spring-boot-starter-validation (Hibernate Validator).
 */
class ControllerDtoValidationTest {

    private static final Validator VALIDATOR;
    static {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        VALIDATOR = vf.getValidator();
    }

    /* Helpers */
    private static <T> Set<ConstraintViolation<T>> v(T bean) {
        return VALIDATOR.validate(bean);
    }
    private static <T> boolean has(Set<ConstraintViolation<T>> v, String prop) {
        return v.stream().anyMatch(cv -> cv.getPropertyPath().toString().equals(prop));
    }
    private static <T> boolean hasMsg(Set<ConstraintViolation<T>> v, String contains) {
        return v.stream().anyMatch(cv -> (cv.getMessage() != null && cv.getMessage().contains(contains)));
    }

    /* ===================== AmenazaRequest ===================== */
    @Nested @DisplayName("AmenazaRequest")
    class AmenazaRequestTests {

        @Test @DisplayName("válido")
        void ok() {
            AmenazaRequest dto = AmenazaRequest.builder()
                    .codigo("DEFORESTACION")
                    .tipo("ACTIVIDADES_HUMANAS")
                    .descripcion("Eliminación de masa forestal.")
                    .build();

            assertThat(v(dto)).isEmpty();
        }

        @Test @DisplayName("inválido: blank y tamaños")
        void invalid_blankAndSize() {
            AmenazaRequest dto = AmenazaRequest.builder()
                    .codigo("")        // NotBlank + max 50
                    .tipo(" ")         // NotBlank + max 120
                    .descripcion("x".repeat(2001)) // > 2000
                    .build();

            var vs = v(dto);
            assertThat(vs).isNotEmpty();
            assertThat(has(vs, "codigo")).isTrue();
            assertThat(has(vs, "tipo")).isTrue();
            assertThat(has(vs, "descripcion")).isTrue();
        }
    }

    /* ===================== DistribucionRequest ===================== */
    @Nested @DisplayName("DistribucionRequest")
    class DistribucionRequestTests {

        @Test @DisplayName("válido")
        void ok() {
            DistribucionRequest dto = DistribucionRequest.builder()
                    .region("Centroamérica")
                    .ecosistema("Bosque tropical")
                    .latitud(13.6929)
                    .longitud(-89.2182)
                    .precisionMetros(50)
                    .fechaObservacion(LocalDate.now())
                    .build();

            assertThat(v(dto)).isEmpty();
        }

        @Test @DisplayName("inválido: NotBlank + rangos de lat/long + precision")
        void invalid_fields() {
            DistribucionRequest dto = DistribucionRequest.builder()
                    .region("")
                    .ecosistema(" ")
                    .latitud(120.0)        // > 90
                    .longitud(-200.0)      // < -180
                    .precisionMetros(-1)   // < 0
                    .fechaObservacion(null)
                    .build();

            var vs = v(dto);
            assertThat(vs).isNotEmpty();
            assertThat(has(vs, "region")).isTrue();
            assertThat(has(vs, "ecosistema")).isTrue();
            assertThat(has(vs, "latitud")).isTrue();
            assertThat(has(vs, "longitud")).isTrue();
            assertThat(has(vs, "precisionMetros")).isTrue();
        }
    }

    /* ===================== EspecieAmenazaLinkRequest ===================== */
    @Nested @DisplayName("EspecieAmenazaLinkRequest")
    class EspecieAmenazaLinkRequestTests {

        @Test @DisplayName("válido")
        void ok() {
            EspecieAmenazaLinkRequest dto = EspecieAmenazaLinkRequest.builder()
                    .amenazaId(9L)
                    .severidad("ALTA")
                    .build();

            assertThat(v(dto)).isEmpty();
        }

        @Test @DisplayName("inválido: null + pattern")
        void invalid() {
            EspecieAmenazaLinkRequest dto = EspecieAmenazaLinkRequest.builder()
                    .amenazaId(null)
                    .severidad("MUY_ALTA") // no coincide con BAJA|MEDIA|ALTA
                    .build();

            var vs = v(dto);
            assertThat(vs).isNotEmpty();
            assertThat(has(vs, "amenazaId")).isTrue();
            assertThat(has(vs, "severidad")).isTrue();
            assertThat(hasMsg(vs, "BAJA")).isTrue(); // mensaje del pattern menciona los permitidos
        }
    }

    /* ===================== EspecieRequest ===================== */
    @Nested @DisplayName("EspecieRequest")
    class EspecieRequestTests {

        @Test @DisplayName("válido")
        void ok() {
            EspecieRequest dto = EspecieRequest.builder()
                    .nombreCientifico("Panthera onca")
                    .nombreComun("Jaguar")
                    .tipo("FAUNA")
                    .estadoConservacion("EN")
                    .descripcion("Felino nativo de América")
                    .esEndemica(Boolean.TRUE)
                    .fechaRegistro(LocalDate.now()) // @PastOrPresent
                    .build();

            assertThat(v(dto)).isEmpty();
        }

        @Test @DisplayName("inválido: blanks, pattern, tamaños, fecha futura")
        void invalid() {
            EspecieRequest dto = EspecieRequest.builder()
                    .nombreCientifico("ab") // min 3
                    .nombreComun("")        // NotBlank
                    .tipo("ANIMAL")         // pattern FLORA|FAUNA
                    .estadoConservacion("A".repeat(11)) // > 10
                    .descripcion(null)
                    .esEndemica(null)       // NotNull
                    .fechaRegistro(LocalDate.now().plusDays(1)) // futuro
                    .build();

            var vs = v(dto);
            assertThat(vs).isNotEmpty();
            assertThat(has(vs, "nombreCientifico")).isTrue();
            assertThat(has(vs, "nombreComun")).isTrue();
            assertThat(has(vs, "tipo")).isTrue();
            assertThat(has(vs, "estadoConservacion")).isTrue();
            assertThat(has(vs, "esEndemica")).isTrue();
            assertThat(has(vs, "fechaRegistro")).isTrue();
        }
    }

    /* ===================== FuenteRequest ===================== */
    @Nested @DisplayName("FuenteRequest")
    class FuenteRequestTests {

        @Test @DisplayName("válido")
        void ok() {
            FuenteRequest dto = FuenteRequest.builder()
                    .nombre("IUCN Red List 2024 – Panthera onca")
                    .descripcion("Ficha técnica de conservación")
                    .tipo("WEB")
                    .enlace("https://www.iucnredlist.org/species/15953/123791436")
                    .fechaPublicacion(LocalDate.now())
                    .build();

            assertThat(v(dto)).isEmpty();
        }

        @Test @DisplayName("inválido: tamaños, URL, blank")
        void invalid() {
            FuenteRequest dto = FuenteRequest.builder()
                    .nombre("") // NotBlank
                    .descripcion("x".repeat(501))
                    .tipo("x".repeat(31))
                    .enlace("no-es-url")
                    .fechaPublicacion(null)
                    .build();

            var vs = v(dto);
            assertThat(vs).isNotEmpty();
            assertThat(has(vs, "nombre")).isTrue();
            assertThat(has(vs, "descripcion")).isTrue();
            assertThat(has(vs, "tipo")).isTrue();
            assertThat(has(vs, "enlace")).isTrue();
        }
    }

    /* ===================== UsuarioRequest ===================== */
    @Nested @DisplayName("UsuarioRequest")
    class UsuarioRequestTests {

        @Test @DisplayName("válido")
        void ok() {
            UsuarioRequest dto = UsuarioRequest.builder()
                    .username("dhernandez")
                    .nombreCompleto("Diego Hernández")
                    .email("diego@udb.edu.sv")
                    .rol(Rol.EDITOR)
                    .activo(Boolean.TRUE)
                    .fechaRegistro(LocalDate.now())
                    .build();

            assertThat(v(dto)).isEmpty();
        }

        @Test @DisplayName("inválido: blanks, email, nulls")
        void invalid() {
            UsuarioRequest dto = UsuarioRequest.builder()
                    .username("")              // NotBlank
                    .nombreCompleto(" ")       // NotBlank
                    .email("no-email")         // @Email
                    .rol(null)                 // NotNull
                    .activo(null)              // NotNull
                    .fechaRegistro(null)
                    .build();

            var vs = v(dto);
            assertThat(vs).isNotEmpty();
            assertThat(has(vs, "username")).isTrue();
            assertThat(has(vs, "nombreCompleto")).isTrue();
            assertThat(has(vs, "email")).isTrue();
            assertThat(has(vs, "rol")).isTrue();
            assertThat(has(vs, "activo")).isTrue();
        }
    }
}
