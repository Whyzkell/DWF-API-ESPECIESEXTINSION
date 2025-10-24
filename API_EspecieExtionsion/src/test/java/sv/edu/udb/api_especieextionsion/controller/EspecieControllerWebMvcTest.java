package sv.edu.udb.api_especieextionsion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
// 1. Import corregido:
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
// 2. Import añadido para @Autowired:
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
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieResponse;
import sv.edu.udb.api_especieextionsion.security.JwtService;
import sv.edu.udb.api_especieextionsion.service.EspecieService;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EspecieController.class)
@AutoConfigureMockMvc(addFilters = false) // sin filtros de seguridad
@Import(EspecieControllerWebMvcTest.TestBeans.class)
class EspecieControllerWebMvcTest {

    @TestConfiguration
    static class TestBeans {

        // Advice simple para mapear excepciones a códigos esperados
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

        // Bean “dummy” por si tu config carga algo de JWT en el slice MVC
        @Bean @Primary
        JwtService jwtService() { return new JwtService() { /* no-op */ }; }

        // Servicio en memoria que respeta el contrato de EspecieService
        @Bean @Primary
        EspecieService especieService() { return new InMemoryEspecieService(); }

        static class InMemoryEspecieService implements EspecieService {
            private final AtomicLong seq = new AtomicLong(0);
            private final Map<Long, EspecieResponse> store = new ConcurrentHashMap<>();
            private final Set<String> nombresCientificos = Collections.synchronizedSet(new HashSet<>());

            void clear() { store.clear(); nombresCientificos.clear(); seq.set(0); }

            @Override
            public EspecieResponse crear(EspecieRequest r) {
                if (r.getNombreCientifico() == null || r.getNombreCientifico().isBlank()) {
                    throw new IllegalArgumentException("nombreCientifico requerido");
                }
                if (nombresCientificos.contains(r.getNombreCientifico())) {
                    throw new IllegalArgumentException("Nombre científico ya en uso");
                }
                long id = seq.incrementAndGet();
                nombresCientificos.add(r.getNombreCientifico());
                EspecieResponse resp = EspecieResponse.builder()
                        .id(id)
                        .nombreCientifico(r.getNombreCientifico())
                        .nombreComun(r.getNombreComun())
                        .tipo(r.getTipo())
                        .estadoConservacion(r.getEstadoConservacion())
                        .descripcion(r.getDescripcion())
                        .esEndemica(r.getEsEndemica())
                        .fechaRegistro(r.getFechaRegistro())
                        .build();
                store.put(id, resp);
                return resp;
            }

            @Override
            public EspecieResponse actualizar(Long id, EspecieRequest r) {
                EspecieResponse existente = store.get(id);
                if (existente == null) throw new EntityNotFoundException("Especie no encontrada");

                String nuevoNC = r.getNombreCientifico();
                if (nuevoNC != null && !nuevoNC.equals(existente.getNombreCientifico())) {
                    if (nombresCientificos.contains(nuevoNC)) {
                        throw new IllegalArgumentException("Nombre científico ya en uso");
                    }
                    nombresCientificos.remove(existente.getNombreCientifico());
                    nombresCientificos.add(nuevoNC);
                    existente.setNombreCientifico(nuevoNC);
                }
                if (r.getNombreComun() != null) existente.setNombreComun(r.getNombreComun());
                if (r.getTipo() != null) existente.setTipo(r.getTipo());
                if (r.getEstadoConservacion() != null) existente.setEstadoConservacion(r.getEstadoConservacion());
                if (r.getDescripcion() != null) existente.setDescripcion(r.getDescripcion());
                if (r.getEsEndemica() != null) existente.setEsEndemica(r.getEsEndemica());
                if (r.getFechaRegistro() != null) existente.setFechaRegistro(r.getFechaRegistro());
                store.put(id, existente);
                return existente;
            }

            @Override
            public EspecieResponse obtener(Long id) {
                EspecieResponse r = store.get(id);
                if (r == null) throw new EntityNotFoundException("Especie no encontrada");
                return r;
            }

            @Override
            public List<EspecieResponse> listar() {
                return new ArrayList<>(store.values());
            }

            @Override
            public void eliminar(Long id) {
                EspecieResponse removed = store.remove(id);
                if (removed == null) throw new EntityNotFoundException("Especie no encontrada");
                nombresCientificos.remove(removed.getNombreCientifico());
            }
        }
    }

    // 3. Cambiado a inyección por campos (como en AmenazaControllerWebMvcTest)
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private EspecieService service;

    // 4. Constructor eliminado

    @BeforeEach
    void clean() {
        ((TestBeans.InMemoryEspecieService) service).clear();
    }

    // -------- helpers --------
    private EspecieRequest req(String nc, String ncomun) {
        return EspecieRequest.builder()
                .nombreCientifico(nc)
                .nombreComun(ncomun)
                .tipo("FAUNA")
                .estadoConservacion("EN")
                .descripcion("desc")
                .esEndemica(Boolean.FALSE)
                .fechaRegistro(LocalDate.now().minusDays(1))
                .build();
    }
    private byte[] bodyValido(String nc, String ncomun) throws Exception {
        Map<String, Object> m = new HashMap<>();
        m.put("nombreCientifico", nc);
        m.put("nombreComun", ncomun);
        m.put("tipo", "FAUNA");
        m.put("estadoConservacion", "EN");
        m.put("descripcion", "desc");
        m.put("esEndemica", false);
        m.put("fechaRegistro", LocalDate.now().minusDays(1).toString());
        return json.writeValueAsBytes(m);
    }

    // -------- tests --------

    @Test
    void get_lista_200() throws Exception {
        service.crear(req("Panthera onca", "Jaguar"));
        service.crear(req("Ara macao", "Guacamaya"));

        mvc.perform(get("/api/especies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void get_porId_200() throws Exception {
        var creada = service.crear(req("Panthera onca", "Jaguar"));

        mvc.perform(get("/api/especies/{id}", creada.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCientifico").value("Panthera onca"))
                .andExpect(jsonPath("$.nombreComun").value("Jaguar"));
    }

    @Test
    void get_porId_404() throws Exception {
        mvc.perform(get("/api/especies/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void post_crea_201() throws Exception {
        var res = mvc.perform(post("/api/especies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValido("Panthera onca", "Jaguar")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();

        var body = json.readValue(res.getResponse().getContentAsByteArray(), EspecieResponse.class);
        assertThat(body.getId()).isNotNull();
        assertThat(body.getNombreCientifico()).isEqualTo("Panthera onca");
    }

    @Test
    void post_409_nombreCientifico_duplicado() throws Exception {
        service.crear(req("Panthera onca", "Jaguar"));

        mvc.perform(post("/api/especies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValido("Panthera onca", "Jaguar 2")))
                .andExpect(status().isConflict());
    }

    @Test
    void put_actualiza_200() throws Exception {
        var creada = service.crear(req("Panthera onca", "Jaguar"));

        // Cambiamos el nombre común y la descripción
        Map<String, Object> patch = new HashMap<>();
        patch.put("nombreCientifico", "Panthera onca");
        patch.put("nombreComun", "Jaguar actualizado");
        patch.put("tipo", "FAUNA");
        patch.put("estadoConservacion", "EN");
        patch.put("descripcion", "nueva desc");
        patch.put("esEndemica", false);
        patch.put("fechaRegistro", LocalDate.now().minusDays(1).toString());

        mvc.perform(put("/api/especies/{id}", creada.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreComun").value("Jaguar actualizado"))
                .andExpect(jsonPath("$.descripcion").value("nueva desc"));
    }

    @Test
    void put_409_si_cambia_nombreCientifico_a_duplicado() throws Exception {
        var e1 = service.crear(req("Panthera onca", "Jaguar"));
        var e2 = service.crear(req("Ara macao", "Guacamaya"));

        Map<String, Object> patch = new HashMap<>();
        patch.put("nombreCientifico", "Panthera onca"); // intenta duplicar
        patch.put("nombreComun", "Guacamaya");
        patch.put("tipo", "FAUNA");
        patch.put("estadoConservacion", "EN");
        patch.put("esEndemica", false);
        patch.put("fechaRegistro", LocalDate.now().minusDays(1).toString());

        mvc.perform(put("/api/especies/{id}", e2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(patch)))
                .andExpect(status().isConflict());
    }

    @Test
    void delete_204() throws Exception {
        var creada = service.crear(req("Panthera onca", "Jaguar"));

        mvc.perform(delete("/api/especies/{id}", creada.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_404() throws Exception {
        mvc.perform(delete("/api/especies/{id}", 111L))
                .andExpect(status().isNotFound());
    }
}
