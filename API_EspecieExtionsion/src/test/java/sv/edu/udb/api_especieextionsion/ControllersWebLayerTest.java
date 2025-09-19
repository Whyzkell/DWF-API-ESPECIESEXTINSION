package sv.edu.udb.api_especieextionsion;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Controllers
import sv.edu.udb.api_especieextionsion.controller.AmenazaController;
import sv.edu.udb.api_especieextionsion.controller.DistribucionController;
import sv.edu.udb.api_especieextionsion.controller.EspecieAmenazaController;
import sv.edu.udb.api_especieextionsion.controller.EspecieController;
import sv.edu.udb.api_especieextionsion.controller.UsuarioController;

// DTOs
import sv.edu.udb.api_especieextionsion.controller.dto.*;
import sv.edu.udb.api_especieextionsion.domain.Rol;

// Services (mockeados)
import sv.edu.udb.api_especieextionsion.service.AmenazaService;
import sv.edu.udb.api_especieextionsion.service.DistribucionService;
import sv.edu.udb.api_especieextionsion.service.EspecieAmenazaService;
import sv.edu.udb.api_especieextionsion.service.EspecieService;
import sv.edu.udb.api_especieextionsion.service.UsuarioService;

// Handler global
import sv.edu.udb.api_especieextionsion.configuration.RestExceptionHandler;

class ControllersWebLayerTest {

    private final ObjectMapper om = new ObjectMapper();

    private static LocalValidatorFactoryBean validator() {
        // Bean Validation para @Valid en body
        LocalValidatorFactoryBean v = new LocalValidatorFactoryBean();
        v.afterPropertiesSet();
        return v;
    }

    /* =============================== AmenazaController =============================== */
    @Nested
    @DisplayName("AmenazaController")
    class AmenazaControllerTests {

        AmenazaService service = Mockito.mock(AmenazaService.class);
        MockMvc mvc;

        @BeforeEach
        void setup() {
            mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                    .standaloneSetup(new AmenazaController(service))
                    .setControllerAdvice(new RestExceptionHandler())
                    .setValidator(validator())
                    .build();
        }

        @Test
        @DisplayName("POST /api/amenazas -> 201 Created")
        void crear_201() throws Exception {
            var req = AmenazaRequest.builder()
                    .codigo("DEFORESTACION").tipo("ACTIVIDADES_HUMANAS").descripcion("desc").build();
            var res = AmenazaResponse.builder()
                    .id(9L).codigo("DEFORESTACION").tipo("ACTIVIDADES_HUMANAS").descripcion("desc").build();

            when(service.crear(any())).thenReturn(res);

            mvc.perform(post("/api/amenazas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(9)))
                    .andExpect(jsonPath("$.codigo", is("DEFORESTACION")));
        }

        @Test
        @DisplayName("POST /api/amenazas body inválido -> 400")
        void crear_400() throws Exception {
            // codigo vacío y tipo en blanco
            var bad = AmenazaRequest.builder().codigo("").tipo(" ").descripcion("x".repeat(2001)).build();

            mvc.perform(post("/api/amenazas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(bad)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("validation_error")));
        }

        @Test
        @DisplayName("GET /api/amenazas -> 200 OK")
        void listar_200() throws Exception {
            when(service.listar()).thenReturn(List.of(
                    AmenazaResponse.builder().id(1L).codigo("A").tipo("T").build(),
                    AmenazaResponse.builder().id(2L).codigo("B").tipo("T").build()
            ));

            mvc.perform(get("/api/amenazas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("GET /api/amenazas/{id} -> 200 OK")
        void obtener_200() throws Exception {
            when(service.buscarPorId(9L)).thenReturn(
                    AmenazaResponse.builder().id(9L).codigo("DEFORESTACION").tipo("H").build()
            );

            mvc.perform(get("/api/amenazas/9"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(9)));
        }

        @Test
        @DisplayName("GET /api/amenazas/{id} inexistente -> 404")
        void obtener_404() throws Exception {
            when(service.buscarPorId(99L)).thenThrow(new EntityNotFoundException("No existe"));

            mvc.perform(get("/api/amenazas/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("not_found")));
        }

        @Test
        @DisplayName("PUT /api/amenazas/{id} -> 200")
        void actualizar_200() throws Exception {
            var req = AmenazaRequest.builder().codigo("X").tipo("Y").build();
            var res = AmenazaResponse.builder().id(5L).codigo("X").tipo("Y").build();

            when(service.actualizar(eq(5L), any())).thenReturn(res);

            mvc.perform(put("/api/amenazas/5")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(5)));
        }

        @Test
        @DisplayName("PUT /api/amenazas/{id} conflicto -> 409")
        void actualizar_409() throws Exception {
            var req = AmenazaRequest.builder().codigo("X").tipo("Y").build();
            when(service.actualizar(eq(5L), any())).thenThrow(new IllegalArgumentException("Duplicado"));

            mvc.perform(put("/api/amenazas/5")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isConflict())
                    .andExpect(content().string(containsString("conflict")));
        }

        @Test
        @DisplayName("DELETE /api/amenazas/{id} -> 204")
        void eliminar_204() throws Exception {
            mvc.perform(delete("/api/amenazas/5"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("DELETE /api/amenazas/{id} inexistente -> 404")
        void eliminar_404() throws Exception {
            Mockito.doThrow(new EntityNotFoundException("No existe")).when(service).eliminar(99L);

            mvc.perform(delete("/api/amenazas/99"))
                    .andExpect(status().isNotFound());
        }
    }

    /* =============================== DistribucionController =============================== */
    @Nested
    @DisplayName("DistribucionController")
    class DistribucionControllerTests {

        DistribucionService service = Mockito.mock(DistribucionService.class);
        MockMvc mvc;

        @BeforeEach
        void setup() {
            mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                    .standaloneSetup(new DistribucionController(service))
                    .setControllerAdvice(new RestExceptionHandler())
                    .setValidator(validator())
                    .build();
        }



        @Test
        @DisplayName("POST /api/especies/{id}/distribuciones body inválido -> 400")
        void crear_400() throws Exception {
            var bad = DistribucionRequest.builder()
                    .region("").ecosistema(" ").latitud(200.0).longitud(-200.0).precisionMetros(-1).build();

            mvc.perform(post("/api/especies/1/distribuciones")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(bad)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("validation_error")));
        }

        @Test
        @DisplayName("GET /api/especies/{id}/distribuciones -> 200")
        void listar_200() throws Exception {
            when(service.listarPorEspecie(1L)).thenReturn(List.of(
                    DistribucionResponse.builder().id(1L).region("R1").ecosistema("E1").build(),
                    DistribucionResponse.builder().id(2L).region("R2").ecosistema("E2").build()
            ));

            mvc.perform(get("/api/especies/1/distribuciones"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }
    }

    /* =============================== EspecieAmenazaController =============================== */
    @Nested
    @DisplayName("EspecieAmenazaController")
    class EspecieAmenazaControllerTests {

        EspecieAmenazaService service = Mockito.mock(EspecieAmenazaService.class);
        MockMvc mvc;

        @BeforeEach
        void setup() {
            mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                    .standaloneSetup(new EspecieAmenazaController(service))
                    .setControllerAdvice(new RestExceptionHandler())
                    .setValidator(validator())
                    .build();
        }

        @Test
        @DisplayName("POST /api/especies/{id}/amenazas -> 201")
        void asociar_201() throws Exception {
            var req = EspecieAmenazaLinkRequest.builder().amenazaId(9L).severidad("ALTA").build();
            var res = EspecieAmenazaResponse.builder().idVinculo(5L).amenazaId(9L).severidad("ALTA").build();

            when(service.asociar(eq(2L), any())).thenReturn(res);

            mvc.perform(post("/api/especies/2/amenazas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.idVinculo", is(5)));
        }

        @Test
        @DisplayName("POST /api/especies/{id}/amenazas body inválido -> 400")
        void asociar_400() throws Exception {
            var bad = EspecieAmenazaLinkRequest.builder().amenazaId(null).severidad("MUY_ALTA").build();

            mvc.perform(post("/api/especies/2/amenazas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(bad)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("validation_error")));
        }

        @Test
        @DisplayName("GET /api/especies/{id}/amenazas -> 200")
        void listar_200() throws Exception {
            when(service.listarPorEspecie(2L)).thenReturn(List.of(
                    EspecieAmenazaResponse.builder().idVinculo(1L).amenazaId(9L).severidad("ALTA").build()
            ));

            mvc.perform(get("/api/especies/2/amenazas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        @DisplayName("PUT /api/especies/{id}/amenazas/{amenazaId} conflicto -> 409 (desde service)")
        void cambiarSeveridad_409() throws Exception {
            when(service.actualizarSeveridad(eq(2L), eq(9L), anyString()))
                    .thenThrow(new IllegalArgumentException("conflicto"));

            mvc.perform(put("/api/especies/2/amenazas/9")
                            .param("severidad", "ALTA"))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("DELETE /api/especies/{id}/amenazas/{amenazaId} -> 204")
        void desasociar_204() throws Exception {
            mvc.perform(delete("/api/especies/2/amenazas/9"))
                    .andExpect(status().isNoContent());
        }
    }

    /* =============================== EspecieController =============================== */
    @Nested
    @DisplayName("EspecieController")
    class EspecieControllerTests {

        EspecieService service = Mockito.mock(EspecieService.class);
        MockMvc mvc;

        @BeforeEach
        void setup() {
            mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                    .standaloneSetup(new EspecieController(service))
                    .setControllerAdvice(new RestExceptionHandler())
                    .setValidator(validator())
                    .build();
        }

        @Test
        @DisplayName("GET /api/especies -> 200")
        void listar_200() throws Exception {
            when(service.listar()).thenReturn(List.of(
                    EspecieResponse.builder().id(1L).nombreCientifico("A").nombreComun("a").build()
            ));

            mvc.perform(get("/api/especies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id", is(1)));
        }

        @Test
        @DisplayName("GET /api/especies/{id} -> 200")
        void obtener_200() throws Exception {
            when(service.obtener(3L)).thenReturn(
                    EspecieResponse.builder().id(3L).nombreCientifico("P. onca").build()
            );

            mvc.perform(get("/api/especies/3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(3)));
        }

        @Test
        @DisplayName("GET /api/especies/{id} inexistente -> 404")
        void obtener_404() throws Exception {
            when(service.obtener(99L)).thenThrow(new EntityNotFoundException("No existe"));

            mvc.perform(get("/api/especies/99"))
                    .andExpect(status().isNotFound());
        }




        @Test
        @DisplayName("PUT /api/especies/{id} -> 200")
        void actualizar_200() throws Exception {
            var req = EspecieRequest.builder()
                    .nombreCientifico("Panthera onca").nombreComun("Jaguar")
                    .tipo("FAUNA").estadoConservacion("EN").esEndemica(true).build();

            var res = EspecieResponse.builder().id(11L).nombreCientifico("Panthera onca").build();
            when(service.actualizar(eq(11L), any())).thenReturn(res);

            mvc.perform(put("/api/especies/11")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(11)));
        }

        @Test
        @DisplayName("DELETE /api/especies/{id} -> 204")
        void eliminar_204() throws Exception {
            mvc.perform(delete("/api/especies/5"))
                    .andExpect(status().isNoContent());
        }
    }

    /* =============================== UsuarioController =============================== */
    @Nested
    @DisplayName("UsuarioController")
    class UsuarioControllerTests {

        UsuarioService service = Mockito.mock(UsuarioService.class);
        MockMvc mvc;

        @BeforeEach
        void setup() {
            mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                    .standaloneSetup(new UsuarioController(service))
                    .setControllerAdvice(new RestExceptionHandler())
                    .setValidator(validator())
                    .build();
        }


        @Test
        @DisplayName("POST /api/usuarios body inválido -> 400")
        void crear_400() throws Exception {
            var bad = UsuarioRequest.builder()
                    .username("")
                    .nombreCompleto(" ")
                    .email("no-email")
                    .rol(null)
                    .activo(null)
                    .build();

            mvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(bad)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("validation_error")));
        }

        @Test
        @DisplayName("GET /api/usuarios -> 200")
        void listar_200() throws Exception {
            when(service.listar()).thenReturn(List.of(
                    UsuarioResponse.builder().id(1L).username("u1").email("u1@x.com").rol(Rol.LECTOR).activo(true).build()
            ));

            mvc.perform(get("/api/usuarios"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].username", is("u1")));
        }

        @Test
        @DisplayName("GET /api/usuarios/{id} -> 200")
        void obtener_200() throws Exception {
            when(service.obtener(1L)).thenReturn(
                    UsuarioResponse.builder().id(1L).username("u1").build()
            );

            mvc.perform(get("/api/usuarios/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)));
        }

        @Test
        @DisplayName("PUT /api/usuarios/{id} conflicto -> 409")
        void actualizar_409() throws Exception {
            var req = UsuarioRequest.builder()
                    .username("dhernandez").nombreCompleto("Diego").email("diego@udb.edu.sv")
                    .rol(Rol.EDITOR).activo(true).build();

            when(service.actualizar(eq(10L), any())).thenThrow(new IllegalArgumentException("duplicado"));

            mvc.perform(put("/api/usuarios/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isConflict())
                    .andExpect(content().string(containsString("conflict")));
        }

        @Test
        @DisplayName("DELETE /api/usuarios/{id} -> 204")
        void eliminar_204() throws Exception {
            mvc.perform(delete("/api/usuarios/3"))
                    .andExpect(status().isNoContent());
        }
    }
}
