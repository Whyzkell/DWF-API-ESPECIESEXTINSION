package sv.edu.udb.api_especieextionsion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
// 1. Import corregido:
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
// 2. Import añadido:
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import sv.edu.udb.api_especieextionsion.controller.dto.EspecieAmenazaLinkRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieAmenazaResponse;
import sv.edu.udb.api_especieextionsion.security.JwtService;
import sv.edu.udb.api_especieextionsion.service.EspecieAmenazaService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EspecieAmenazaController.class)
@AutoConfigureMockMvc(addFilters = false) // desactiva filtros de seguridad
@Import(EspecieAmenazaControllerWebMvcTest.TestBeans.class)
class EspecieAmenazaControllerWebMvcTest {

    @TestConfiguration
    static class TestBeans {

        // Advice mínimo para mapear excepciones como tu API real
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

        // Bean “dummy” por si tu configuración carga algo de JWT en el slice MVC
        @Bean @Primary
        JwtService jwtService() { return new JwtService() { /* no-op */ }; }

        // Servicio en memoria que respeta el contrato de EspecieAmenazaService
        @Bean @Primary
        EspecieAmenazaService especieAmenazaService() { return new InMemoryLinkService(); }

        /**
         * Implementación in-memory:
         * - Por cada especieId mantiene un mapa de amenazas (amenazaId -> vínculo)
         * - Valida duplicados por (especieId, amenazaId)
         * - Valida severidad (en el update también)
         */
        static class InMemoryLinkService implements EspecieAmenazaService {
            private final AtomicLong seq = new AtomicLong(0);
            private final Map<Long, Map<Long, EspecieAmenazaResponse>> store = new ConcurrentHashMap<>();

            void clear() { store.clear(); seq.set(0); }

            @Override
            public EspecieAmenazaResponse asociar(Long especieId, EspecieAmenazaLinkRequest r) {
                if (r.getAmenazaId() == null) {
                    throw new IllegalArgumentException("amenazaId requerido");
                }
                store.putIfAbsent(especieId, new ConcurrentHashMap<>());
                Map<Long, EspecieAmenazaResponse> byAmenaza = store.get(especieId);
                if (byAmenaza.containsKey(r.getAmenazaId())) {
                    throw new IllegalArgumentException("La especie ya tiene asociada esta amenaza");
                }
                long idLink = seq.incrementAndGet();
                // Solo usamos los campos que el controlador expone/usa
                EspecieAmenazaResponse resp = EspecieAmenazaResponse.builder()
                        .idVinculo(idLink)
                        .amenazaId(r.getAmenazaId())
                        .codigo(null)       // opcional
                        .tipo(null)         // opcional
                        .descripcion(null)    // opcional
                        .severidad(r.getSeveridad())
                        .build();
                byAmenaza.put(r.getAmenazaId(), resp);
                return resp;
            }

            @Override
            public List<EspecieAmenazaResponse> listarPorEspecie(Long especieId) {
                Map<Long, EspecieAmenazaResponse> byAmenaza = store.get(especieId);
                if (byAmenaza == null) {
                    // Simula que la especie no existe
                    throw new EntityNotFoundException("Especie no encontrada");
                }
                return new ArrayList<>(byAmenaza.values());
            }

            @Override
            public EspecieAmenazaResponse actualizarSeveridad(Long especieId, Long amenazaId, String severidad) {
                Map<Long, EspecieAmenazaResponse> byAmenaza = store.get(especieId);
                if (byAmenaza == null || !byAmenaza.containsKey(amenazaId)) {
                    throw new EntityNotFoundException("La especie no tiene asociada esa amenaza");
                }
                if (severidad == null || !severidad.matches("BAJA|MEDIA|ALTA")) {
                    throw new IllegalArgumentException("Severidad inválida (use BAJA, MEDIA o ALTA)");
                }
                EspecieAmenazaResponse v = byAmenaza.get(amenazaId);
                v.setSeveridad(severidad);
                byAmenaza.put(amenazaId, v);
                return v;
            }

            @Override
            public void desasociar(Long especieId, Long amenazaId) {
                Map<Long, EspecieAmenazaResponse> byAmenaza = store.get(especieId);
                if (byAmenaza == null || byAmenaza.remove(amenazaId) == null) {
                    throw new EntityNotFoundException("La especie no tiene asociada esa amenaza");
                }
                if (byAmenaza.isEmpty()) {
                    store.remove(especieId);
                }
            }
        }
    }

    // ----- 3. Cambiado a inyección por campos -----
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private EspecieAmenazaService service;

    // 4. Constructor eliminado

    @BeforeEach
    void clean() {
        ((TestBeans.InMemoryLinkService) service).clear();
    }

    // ----- helpers -----
    private byte[] linkBody(Long amenazaId, String severidad) throws Exception {
        Map<String, Object> m = new HashMap<>();
        m.put("amenazaId", amenazaId);
        m.put("severidad", severidad);
        return json.writeValueAsBytes(m);
    }

    // ----- tests -----

    @Test
    void post_asociar_201() throws Exception {
        long especieId = 10L;

        var res = mvc.perform(post("/api/especies/{especieId}/amenazas", especieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(linkBody(5L, "ALTA")))
                .andExpect(status().isCreated())
                .andReturn();

        EspecieAmenazaResponse body =
                json.readValue(res.getResponse().getContentAsByteArray(), EspecieAmenazaResponse.class);
        assertThat(body.getIdVinculo()).isNotNull();
        assertThat(body.getAmenazaId()).isEqualTo(5L);
        assertThat(body.getSeveridad()).isEqualTo("ALTA");
    }

    @Test
    void get_listar_200() throws Exception {
        long especieId = 1L;
        // Cargar datos previos usando el propio servicio en memoria
        service.asociar(especieId, EspecieAmenazaLinkRequest.builder().amenazaId(2L).severidad("BAJA").build());
        service.asociar(especieId, EspecieAmenazaLinkRequest.builder().amenazaId(3L).severidad("MEDIA").build());

        mvc.perform(get("/api/especies/{especieId}/amenazas", especieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void post_409_si_duplicado() throws Exception {
        long especieId = 7L;
        service.asociar(especieId, EspecieAmenazaLinkRequest.builder().amenazaId(99L).severidad("ALTA").build());

        mvc.perform(post("/api/especies/{especieId}/amenazas", especieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(linkBody(99L, "MEDIA")))
                .andExpect(status().isConflict());
    }

    @Test
    void put_actualiza_severidad_200() throws Exception {
        long especieId = 4L;
        long amenazaId = 20L;
        var r = service.asociar(especieId, EspecieAmenazaLinkRequest.builder().amenazaId(amenazaId).severidad("BAJA").build());
        assertThat(r.getSeveridad()).isEqualTo("BAJA");

        mvc.perform(put("/api/especies/{especieId}/amenazas/{amenazaId}", especieId, amenazaId)
                        .param("severidad", "ALTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.severidad").value("ALTA"));
    }

    @Test
    void put_404_si_vinculo_no_existe() throws Exception {
        mvc.perform(put("/api/especies/{especieId}/amenazas/{amenazaId}", 1L, 999L)
                        .param("severidad", "MEDIA"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_204() throws Exception {
        long especieId = 5L;
        long amenazaId = 50L;
        service.asociar(especieId, EspecieAmenazaLinkRequest.builder().amenazaId(amenazaId).severidad("MEDIA").build());

        mvc.perform(delete("/api/especies/{especieId}/amenazas/{amenazaId}", especieId, amenazaId))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_404_si_no_existe() throws Exception {
        mvc.perform(delete("/api/especies/{especieId}/amenazas/{amenazaId}", 2L, 77L))
                .andExpect(status().isNotFound());
    }


}
