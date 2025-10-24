package sv.edu.udb.api_especieextionsion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sv.edu.udb.api_especieextionsion.controller.dto.DistribucionRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.DistribucionResponse;
import sv.edu.udb.api_especieextionsion.security.JwtService;
import sv.edu.udb.api_especieextionsion.service.DistribucionService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DistribucionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(DistribucionControllerWebMvcTest.TestBeans.class)
class DistribucionControllerWebMvcTest {

    @TestConfiguration
    static class TestBeans {

        // Advice mínimo para status esperados
        @RestControllerAdvice
        static class TestAdvice {
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

        // JwtService mockeado como bean vacío
        @Bean @Primary
        JwtService jwtService() {
            return new JwtService() {
                // Métodos vacíos
            };
        }

        // Servicio en memoria
        @Bean @Primary
        DistribucionService distribucionService() {
            return new InMemoryDistribucionService();
        }

        /** Implementación en memoria del contrato DistribucionService */
        static class InMemoryDistribucionService implements DistribucionService {
            private final Set<Long> especies = ConcurrentHashMap.newKeySet();
            private final Map<Long, List<DistribucionResponse>> data = new ConcurrentHashMap<>();

            void clear() { especies.clear(); data.clear(); }
            void addEspecie(Long id) { especies.add(id); }

            @Override
            public DistribucionResponse crear(Long especieId, DistribucionRequest r) {
                if (!especies.contains(especieId)) {
                    throw new EntityNotFoundException("Especie no encontrada");
                }
                DistribucionResponse resp = new DistribucionResponse();
                data.computeIfAbsent(especieId, k -> new ArrayList<>()).add(resp);
                return resp;
            }

            @Override
            public List<DistribucionResponse> listarPorEspecie(Long especieId) {
                if (!especies.contains(especieId)) {
                    throw new EntityNotFoundException("Especie no encontrada");
                }
                return new ArrayList<>(data.getOrDefault(especieId, List.of()));
            }
        }
    }

    // --- Inyección de dependencias ---

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private DistribucionService service;

    // (Constructor eliminado, usando inyección de campos)

    @BeforeEach
    void setup() {
        var mem = (TestBeans.InMemoryDistribucionService) service;
        mem.clear();
        mem.addEspecie(10L); // especie "existente" para los tests felices
    }

    // helper: body válido que pasa las validaciones del DTO
    private String bodyValido() throws Exception {
        Map<String, Object> m = new HashMap<>();
        m.put("region", "Centro");
        m.put("pais", "SV");
        m.put("descripcion", "Bosque nuboso");

        // Campos requeridos por las validaciones @NotNull/@NotBlank del DTO
        m.put("latitud", 13.6929);
        m.put("longitud", -89.2182);
        m.put("ecosistema", "Bosque Seco Tropical");

        return json.writeValueAsString(m);
    }

    @Test
    void post_crear_201_cuando_especie_existe() throws Exception {
        mvc.perform(post("/api/especies/{id}/distribuciones", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValido()))
                .andExpect(status().isCreated());
    }

    @Test
    void get_listar_200() throws Exception {
        // Pre-cargamos 2 registros llamando al servicio fake
        // Nota: Esto se salta la validación del DTO del controlador
        service.crear(10L, new DistribucionRequest());
        service.crear(10L, new DistribucionRequest());

        mvc.perform(get("/api/especies/{id}/distribuciones", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void get_listar_404_si_especie_no_existe() throws Exception {
        mvc.perform(get("/api/especies/{id}/distribuciones", 999L))
                .andExpect(status().isNotFound());
    }
}

