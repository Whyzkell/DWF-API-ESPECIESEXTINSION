package sv.edu.udb.api_especieextionsion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.api_especieextionsion.controller.dto.AmenazaRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.AmenazaResponse;
import sv.edu.udb.api_especieextionsion.security.JwtService;
import sv.edu.udb.api_especieextionsion.service.AmenazaService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AmenazaController.class)
@AutoConfigureMockMvc(addFilters = false) // desactiva filtros de seguridad en el slice MVC
@Import(AmenazaControllerWebMvcTest.TestBeans.class)
class AmenazaControllerWebMvcTest {

    @TestConfiguration
    static class TestBeans {
        /** Advice mínimo para mapear excepciones como la API real */
        @RestControllerAdvice
        static class TestGlobalAdvice {
            @ExceptionHandler(EntityNotFoundException.class)
            ResponseEntity<Map<String, Object>> notFound(EntityNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", ex.getMessage()));
            }
            @ExceptionHandler(IllegalArgumentException.class)
            ResponseEntity<Map<String, Object>> conflict(IllegalArgumentException ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", ex.getMessage()));
            }
            @ExceptionHandler(MethodArgumentNotValidException.class)
            ResponseEntity<Map<String, Object>> badRequest(MethodArgumentNotValidException ex) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Validación fallida"));
            }
        }

        /** Implementación in-memory del servicio, sin JPA ni MapStruct. */
        @Bean @Primary
        AmenazaService amenazaService() {
            return new InMemoryAmenazaService();
        }

        /** Servicio en memoria que respeta el contrato del AmenazaServiceImpl */
        static class InMemoryAmenazaService implements AmenazaService {
            private final AtomicLong seq = new AtomicLong(0);
            private final Map<Long, AmenazaResponse> store = new ConcurrentHashMap<>();
            private final Set<String> codigos = Collections.synchronizedSet(new HashSet<>());

            // helpers para los tests
            void clear() { store.clear(); codigos.clear(); seq.set(0); }

            @Override
            public AmenazaResponse crear(AmenazaRequest req) {
                if (req.getCodigo() == null || req.getCodigo().isBlank()) {
                    throw new IllegalArgumentException("codigo requerido");
                }
                if (codigos.contains(req.getCodigo())) {
                    throw new IllegalArgumentException("El código de amenaza ya existe");
                }
                long id = seq.incrementAndGet();
                codigos.add(req.getCodigo());
                AmenazaResponse r = AmenazaResponse.builder()
                        .id(id)
                        .codigo(req.getCodigo())
                        .tipo(req.getTipo())
                        .descripcion(req.getDescripcion())
                        .build();
                store.put(id, r);
                return r;
            }

            @Override
            public List<AmenazaResponse> listar() {
                return new ArrayList<>(store.values());
            }

            @Override
            public AmenazaResponse buscarPorId(Long id) {
                AmenazaResponse r = store.get(id);
                if (r == null) throw new EntityNotFoundException("Amenaza no encontrada");
                return r;
            }

            @Override
            public AmenazaResponse actualizar(Long id, AmenazaRequest req) {
                AmenazaResponse existente = store.get(id);
                if (existente == null) throw new EntityNotFoundException("Amenaza no encontrada");

                // si el código cambia, validar unicidad
                String nuevoCodigo = req.getCodigo();
                if (nuevoCodigo != null && !Objects.equals(nuevoCodigo, existente.getCodigo())) {
                    if (codigos.contains(nuevoCodigo)) {
                        throw new IllegalArgumentException("El código de amenaza ya existe");
                    }
                    codigos.remove(existente.getCodigo());
                    codigos.add(nuevoCodigo);
                    existente.setCodigo(nuevoCodigo);
                }
                if (req.getTipo() != null) existente.setTipo(req.getTipo());
                if (req.getDescripcion() != null) existente.setDescripcion(req.getDescripcion());
                store.put(id, existente);
                return existente;
            }

            @Override
            public void eliminar(Long id) {
                AmenazaResponse removed = store.remove(id);
                if (removed == null) throw new EntityNotFoundException("Amenaza no encontrada");
                codigos.remove(removed.getCodigo());
            }
        }
    }



    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private AmenazaService service;

    /**
     * Se añade @MockBean para simular el JwtService.
     * @WebMvcTest carga la configuración de seguridad (que usa JwtAuthenticationFilter),
     * y ese filtro depende de JwtService. Como @WebMvcTest no carga los @Service
     * normales, debemos proveer un mock para que el contexto arranque.
     */
    @MockBean
    private JwtService jwtService;




    @BeforeEach
    void clean() {
        // limpia el store del fake service entre tests
        TestBeans.InMemoryAmenazaService s = (TestBeans.InMemoryAmenazaService) service;
        s.clear();
    }

    private AmenazaRequest req(String codigo, String tipo, String descripcion) {
        return AmenazaRequest.builder()
                .codigo(codigo)
                .tipo(tipo)
                .descripcion(descripcion)
                .build();
    }

    @Test
    void get_lista_200() throws Exception {
        service.crear(req("INCENDIO", "ACTIVIDADES_HUMANAS", "Incendios provocados"));
        service.crear(req("CAZA_ILEGAL", "ACTIVIDADES_HUMANAS", "Cacería"));

        mvc.perform(get("/api/amenazas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void get_porId_200_cuando_existe() throws Exception {
        var r = service.crear(req("INCENDIO", "ACTIVIDADES_HUMANAS", "Incendios"));

        mvc.perform(get("/api/amenazas/{id}", r.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("INCENDIO"));
    }

    @Test
    void get_porId_404_cuando_no_existe() throws Exception {
        mvc.perform(get("/api/amenazas/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void post_crea_201_con_body_valido() throws Exception {
        var body = req("NUEVA", "NATURALES", "Amenaza nueva");

        var res = mvc.perform(post("/api/amenazas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(body)))
                .andExpect(status().isCreated())
                .andReturn();

        // sanity: el body contiene el id generado
        var created = json.readValue(res.getResponse().getContentAsByteArray(), AmenazaResponse.class);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getCodigo()).isEqualTo("NUEVA");
    }

    @Test
    void post_409_cuando_codigo_duplicado() throws Exception {
        service.crear(req("DUP", "NATURALES", "x"));
        var body = req("DUP", "NATURALES", "y");

        mvc.perform(post("/api/amenazas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(body)))
                .andExpect(status().isConflict());
    }

    @Test
    void put_actualiza_200() throws Exception {
        var r = service.crear(req("INCENDIO", "HUMANAS", "a"));

        var patch = req("INCENDIO", "HUMANAS", "ACTUALIZADA");
        mvc.perform(put("/api/amenazas/{id}", r.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("ACTUALIZADA"));
    }

    @Test
    void delete_204_cuando_existe() throws Exception {
        var r = service.crear(req("INCENDIO", "HUMANAS", "a"));

        mvc.perform(delete("/api/amenazas/{id}", r.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_404_cuando_no_existe() throws Exception {
        mvc.perform(delete("/api/amenazas/{id}", 12345L))
                .andExpect(status().isNotFound());
    }
}

