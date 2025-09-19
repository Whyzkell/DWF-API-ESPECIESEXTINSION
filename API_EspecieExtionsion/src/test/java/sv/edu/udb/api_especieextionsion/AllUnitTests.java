package sv.edu.udb.api_especieextionsion;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.bind.annotation.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// IMPORTA tus clases reales (tu código tiene la “o”): ...extionsion.configuration
import sv.edu.udb.api_especieextionsion.configuration.OpenApiConfig;
import sv.edu.udb.api_especieextionsion.configuration.RestExceptionHandler;

/**
 * Suite única de tests que puedes ejecutar tal cual:
 *  - OpenApiConfig (configuración de OpenAPI)
 *  - RestExceptionHandler (mapeo de excepciones)
 *
 * Colocar en: src/test/java/sv/edu/udb/api_especieextinsion/AllUnitTests.java
 */
class AllUnitTests {

    /* ===================== OpenApiConfig ===================== */
    @Nested
    @DisplayName("OpenApiConfig")
    class OpenApiConfigTests {

        private final ApplicationContextRunner runner =
                new ApplicationContextRunner().withUserConfiguration(OpenApiConfig.class);

        @Test
        @DisplayName("Crea OpenAPI con perfil dev y app.version desde properties")
        void devProfile_and_versionFromProps() {
            runner.withPropertyValues("app.version=2.3.4", "spring.profiles.active=dev")
                    .run(ctx -> {
                        assertThat(ctx).hasSingleBean(OpenAPI.class);
                        OpenAPI api = ctx.getBean(OpenAPI.class);
                        Info info = api.getInfo();

                        assertThat(info.getTitle()).isEqualTo("API de Especies – DWF");
                        assertThat(info.getDescription()).isEqualTo("API REST para gestión de especies, distribuciones y amenazas (según guías).");
                        assertThat(info.getVersion()).isEqualTo("2.3.4");

                        Contact c = info.getContact();
                        assertThat(c).isNotNull();
                        assertThat(c.getName()).isEqualTo("Equipo DWF");
                        assertThat(c.getEmail()).isEqualTo("soporte@udb.edu.sv");

                        License lic = info.getLicense();
                        assertThat(lic).isNotNull();
                        assertThat(lic.getName()).isEqualTo("MIT");

                        assertThat(api.getServers()).hasSize(1);
                        Server s = api.getServers().get(0);
                        assertThat(s.getUrl()).isEqualTo("http://localhost:8080");
                        assertThat(s.getDescription()).isEqualTo("Local (dev)");
                    });
        }

        @Test
        @DisplayName("Usa versión por defecto (1.0.0) y sin perfiles activos")
        void defaultVersion_and_noProfiles() {
            runner.run(ctx -> {
                OpenAPI api = ctx.getBean(OpenAPI.class);
                assertThat(api.getInfo().getVersion()).isEqualTo("1.0.0");
                assertThat(api.getServers()).hasSize(1);
                assertThat(api.getServers().get(0).getDescription()).isEqualTo("Local ()");
            });
        }

        @Test
        @DisplayName("Refleja perfil test")
        void testProfile() {
            runner.withPropertyValues("spring.profiles.active=test", "app.version=9.9.9")
                    .run(ctx -> {
                        OpenAPI api = ctx.getBean(OpenAPI.class);
                        assertThat(api.getServers()).hasSize(1);
                        assertThat(api.getServers().get(0).getDescription()).isEqualTo("Local (test)");
                        assertThat(api.getInfo().getVersion()).isEqualTo("9.9.9");
                    });
        }

        @Test
        @DisplayName("Soporta múltiples perfiles dev,prod")
        void multipleProfiles() {
            runner.withPropertyValues("spring.profiles.active=dev,prod", "app.version=3.0.0")
                    .run(ctx -> {
                        OpenAPI api = ctx.getBean(OpenAPI.class);
                        assertThat(api.getServers()).hasSize(1);
                        assertThat(api.getServers().get(0).getDescription()).isEqualTo("Local (dev,prod)");
                        assertThat(api.getInfo().getVersion()).isEqualTo("3.0.0");
                    });
        }
    }

    /* ================ RestExceptionHandler =================== */
    @Nested
    @DisplayName("RestExceptionHandler")
    class RestExceptionHandlerTests {

        // Controller/Dtos de prueba para provocar cada excepción
        @RestController
        @RequestMapping("/demo")
        @org.springframework.validation.annotation.Validated // activa validación en params/path
        static class DemoController {

            @PostMapping("/valid")
            public String create(@Valid @RequestBody DemoDTO dto) {
                return "ok";
            }

            @GetMapping("/cv")
            public String constraint(@RequestParam @Min(value = 1, message = "page debe ser >= 1") int page) {
                return "ok" + page;
            }

            @GetMapping("/nf")
            public String notFound() {
                throw new EntityNotFoundException("Especie no existe");
            }

            @GetMapping("/conflict-illegal-arg")
            public String conflictIllegalArg() {
                throw new IllegalArgumentException("Conflicto de negocio");
            }

            @GetMapping("/boom")
            public String boom() {
                throw new RuntimeException("boom");
            }
        }

        static class DemoDTO {
            @NotBlank(message = "nombre es obligatorio")
            public String nombre;
            public String getNombre() { return nombre; }
            public void setNombre(String nombre) { this.nombre = nombre; }
        }

        /** MockMvc configurado para:
         *  - validar @Valid (bean) con LocalValidatorFactoryBean
         *  - validar @Validated (método) vía MethodValidationPostProcessor (proxy)
         */
        private MockMvc buildMvc() {
            // 1) Bean Validation
            LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
            validator.afterPropertiesSet();

            // 2) Method validation (para @Validated en parámetros)
            MethodValidationPostProcessor mvp = new MethodValidationPostProcessor();
            mvp.setValidator(validator);

            DemoController target = new DemoController();
            Object proxied = mvp.postProcessAfterInitialization(target, "demoController");

            return org.springframework.test.web.servlet.setup.MockMvcBuilders
                    .standaloneSetup(proxied) // usa el proxy para que aplique la validación de método
                    .setControllerAdvice(new RestExceptionHandler())
                    .setValidator(validator)  // y también valida @Valid en el body
                    .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter())
                    .build();
        }

        @Test
        @DisplayName("400: @Valid body -> MethodArgumentNotValidException")
        void badRequest_bodyBeanValidation() throws Exception {
            MockMvc mvc = buildMvc();
            mvc.perform(post("/demo/valid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}")) // falta 'nombre'
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("validation_error")))
                    .andExpect(content().string(containsString("Dato inválido")))
                    .andExpect(content().string(containsString("nombre")))
                    .andExpect(content().string(containsString("obligatorio")));
        }



        @Test
        @DisplayName("400: JSON mal formado -> HttpMessageNotReadableException")
        void badRequest_malformedJson() throws Exception {
            MockMvc mvc = buildMvc();
            mvc.perform(post("/demo/valid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{")) // JSON inválido
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("bad_request")))
                    .andExpect(content().string(containsString("JSON inválido")))
                    .andExpect(content().string(containsString("No se pudo parsear el cuerpo de la solicitud")));
        }

        @Test
        @DisplayName("404: EntityNotFoundException/NoSuchElementException")
        void notFound_mapsTo404() throws Exception {
            MockMvc mvc = buildMvc();
            mvc.perform(get("/demo/nf"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("not_found")))
                    .andExpect(content().string(containsString("Recurso no encontrado")))
                    .andExpect(content().string(containsString("Especie no existe")));
        }

        @Test
        @DisplayName("409: IllegalArgumentException -> CONFLICT")
        void conflict_illegalArgument_mapsTo409() throws Exception {
            MockMvc mvc = buildMvc();
            mvc.perform(get("/demo/conflict-illegal-arg"))
                    .andExpect(status().isConflict())
                    .andExpect(content().string(containsString("conflict")))
                    .andExpect(content().string(containsString("Conflicto en la solicitud")))
                    .andExpect(content().string(containsString("Conflicto de negocio")));
        }

        @Test
        @DisplayName("500: Exception genérica -> INTERNAL_SERVER_ERROR")
        void internalError_mapsTo500() throws Exception {
            MockMvc mvc = buildMvc();
            mvc.perform(get("/demo/boom"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string(containsString("internal_error")))
                    .andExpect(content().string(containsString("Error interno")))
                    .andExpect(content().string(containsString("Ha ocurrido un error inesperado")));
        }
    }
}


