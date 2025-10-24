package sv.edu.udb.api_especieextionsion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
// Import para inyección de campos
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
// Este import ya estaba correcto
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import sv.edu.udb.api_especieextionsion.controller.dto.FuenteRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.FuenteResponse;
import sv.edu.udb.api_especieextionsion.security.JwtService;
import sv.edu.udb.api_especieextionsion.service.FuenteService;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FuenteController.class)
@AutoConfigureMockMvc(addFilters = false) // desactiva filtros de seguridad
@Import(FuenteControllerWebMvcTest.TestBeans.class)
class FuenteControllerWebMvcTest {

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

        // Bean “dummy” por si alguna config referencia JwtService
        @Bean @Primary
        JwtService jwtService() { return new JwtService() { /* no-op */ }; }

        // Servicio en memoria que respeta el contrato de FuenteService
        @Bean @Primary
        FuenteService fuenteService() { return new InMemoryFuenteService(); }

        static class InMemoryFuenteService implements FuenteService {
            private final AtomicLong seq = new AtomicLong(0);
            private final Map<Long, FuenteResponse> store = new ConcurrentHashMap<>();
            private final Set<String> nombres = Collections.synchronizedSet(new HashSet<>());

            void clear() { store.clear(); nombres.clear(); seq.set(0); }

            @Override
            public FuenteResponse crear(FuenteRequest r) {
                if (r.getNombre() == null || r.getNombre().isBlank()) {
                    throw new IllegalArgumentException("nombre requerido");
                }
                if (nombres.contains(r.getNombre())) {
                    throw new IllegalArgumentException("La fuente ya existe con ese nombre");
                }
                long id = seq.incrementAndGet();
                nombres.add(r.getNombre());
                FuenteResponse fr = FuenteResponse.builder()
                        .id(id)
                        .nombre(r.getNombre())
                        .descripcion(r.getDescripcion())
                        .tipo(r.getTipo())
                        .enlace(r.getEnlace())
                        .fechaPublicacion(r.getFechaPublicacion())
                        .build();
                store.put(id, fr);
                return fr;
            }

            @Override
            public List<FuenteResponse> listar() {
                return new ArrayList<>(store.values());
            }

            @Override
            public FuenteResponse obtener(Long id) {
                FuenteResponse f = store.get(id);
                if (f == null) throw new EntityNotFoundException("Fuente id " + id + " no existe");
                return f;
            }

            @Override
            public FuenteResponse actualizar(Long id, FuenteRequest r) {
                FuenteResponse existente = store.get(id);
                if (existente == null) throw new EntityNotFoundException("Fuente id " + id + " no existe");

                String nuevoNombre = r.getNombre();
                if (nuevoNombre != null && !Objects.equals(nuevoNombre, existente.getNombre())) {
                    if (nombres.contains(nuevoNombre)) {
                        throw new IllegalArgumentException("La fuente ya existe con ese nombre");
                    }
                    // actualizar set de nombres
                    nombres.remove(existente.getNombre());
                    nombres.add(nuevoNombre);
                    existente.setNombre(nuevoNombre);
                }
                if (r.getDescripcion() != null) existente.setDescripcion(r.getDescripcion());
                if (r.getTipo() != null) existente.setTipo(r.getTipo());
                if (r.getEnlace() != null) existente.setEnlace(r.getEnlace());
                if (r.getFechaPublicacion() != null) existente.setFechaPublicacion(r.getFechaPublicacion());

                store.put(id, existente);
                return existente;
            }

            @Override
            public void eliminar(Long id) {
                FuenteResponse removed = store.remove(id);
                if (removed == null) throw new EntityNotFoundException("Fuente id " + id + " no existe");
                nombres.remove(removed.getNombre());
            }
        }
    }

    // --- Inyección de dependencias por campos ---
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private FuenteService service;

    // (Constructor eliminado)

    @BeforeEach
    void clean() {
        ((TestBeans.InMemoryFuenteService) service).clear();
    }

    // helpers
    private byte[] body(String nombre, String descripcion, String tipo, String enlace, LocalDate fecha) throws Exception {
        Map<String, Object> m = new HashMap<>();
        if (nombre != null) m.put("nombre", nombre);
        if (descripcion != null) m.put("descripcion", descripcion);
        if (tipo != null) m.put("tipo", tipo);
        if (enlace != null) m.put("enlace", enlace);
        if (fecha != null) m.put("fechaPublicacion", fecha.toString());
        return json.writeValueAsBytes(m);
    }

    // tests

    @Test
    void get_lista_200() throws Exception {
        service.crear(FuenteRequest.builder().nombre("IUCN 2024").tipo("WEB").build());
        service.crear(FuenteRequest.builder().nombre("WWF Report").tipo("REPORTE").build());

        mvc.perform(get("/api/fuentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void get_porId_200() throws Exception {
        var r = service.crear(FuenteRequest.builder().nombre("IUCN 2024").tipo("WEB").build());

        mvc.perform(get("/api/fuentes/{id}", r.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("IUCN 2024"));
    }

    @Test
    void get_porId_404() throws Exception {
        mvc.perform(get("/api/fuentes/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void post_crea_201() throws Exception {
        var res = mvc.perform(post("/api/fuentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("IUCN 2025", "Ficha", "WEB",
                                "https://example.com", LocalDate.of(2025, 1, 1))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/fuentes/")))
                .andReturn();

        FuenteResponse resp = json.readValue(res.getResponse().getContentAsByteArray(), FuenteResponse.class);
        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getNombre()).isEqualTo("IUCN 2025");
    }

    @Test
    void post_409_si_nombre_duplicado() throws Exception {
        service.crear(FuenteRequest.builder().nombre("Duplicada").build());

        mvc.perform(post("/api/fuentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Duplicada", null, null, null, null)))
                .andExpect(status().isConflict());
    }

    @Test
    void put_actualiza_200() throws Exception {
        var r = service.crear(FuenteRequest.builder().nombre("IUCN 2024").descripcion("A").tipo("WEB").build());

        mvc.perform(put("/api/fuentes/{id}", r.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("IUCN 2024", "Actualizada", null, null, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Actualizada"));
    }

    @Test
    void put_404_si_no_existe() throws Exception {
        mvc.perform(put("/api/fuentes/{id}", 321L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("X", null, null, null, null)))
                .andExpect(status().isNotFound());
    }

    @Test
    void put_409_si_cambia_a_nombre_duplicado() throws Exception {
        var a = service.crear(FuenteRequest.builder().nombre("A").build());
        service.crear(FuenteRequest.builder().nombre("B").build());

        mvc.perform(put("/api/fuentes/{id}", a.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("B", null, null, null, null)))
                .andExpect(status().isConflict());
    }

    @Test
    void delete_204() throws Exception {
        var r = service.crear(FuenteRequest.builder().nombre("IUCN 2024").build());

        mvc.perform(delete("/api/fuentes/{id}", r.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_404() throws Exception {
        mvc.perform(delete("/api/fuentes/{id}", 777L))
                .andExpect(status().isNotFound());
    }
}
