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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import sv.edu.udb.api_especieextionsion.controller.dto.UsuarioRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.UsuarioResponse;
import sv.edu.udb.api_especieextionsion.repository.domain.Rol;
import sv.edu.udb.api_especieextionsion.security.JwtService;
import sv.edu.udb.api_especieextionsion.service.UsuarioService;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false) // desactiva filtros de seguridad
@Import(UsuarioControllerWebMvcTest.TestBeans.class)
class UsuarioControllerWebMvcTest {

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

        // Bean dummy si alguna config referencia JwtService
        @Bean @Primary
        JwtService jwtService() { return new JwtService() { /* no-op */ }; }

        // Servicio en memoria que respeta el contrato de UsuarioService
        @Bean @Primary
        UsuarioService usuarioService() { return new InMemoryUsuarioService(); }

        static class InMemoryUsuarioService implements UsuarioService {
            private final AtomicLong seq = new AtomicLong(0);
            private final Map<Long, UsuarioResponse> store = new ConcurrentHashMap<>();
            private final Set<String> usernames = Collections.synchronizedSet(new HashSet<>());
            private final Set<String> emails = Collections.synchronizedSet(new HashSet<>());

            void clear() { store.clear(); usernames.clear(); emails.clear(); seq.set(0); }

            @Override
            public UsuarioResponse crear(UsuarioRequest r) {
                if (r.getUsername() == null || r.getUsername().isBlank())
                    throw new IllegalArgumentException("Username requerido");
                if (r.getEmail() == null || r.getEmail().isBlank())
                    throw new IllegalArgumentException("Email requerido");
                if (usernames.contains(r.getUsername()))
                    throw new IllegalArgumentException("Username ya existe");
                if (emails.contains(r.getEmail()))
                    throw new IllegalArgumentException("Email ya existe");

                long id = seq.incrementAndGet();
                usernames.add(r.getUsername());
                emails.add(r.getEmail());

                UsuarioResponse u = UsuarioResponse.builder()
                        .id(id)
                        .username(r.getUsername())
                        .nombreCompleto(r.getNombreCompleto())
                        .email(r.getEmail())
                        .rol(r.getRol())
                        .activo(r.getActivo() != null ? r.getActivo() : Boolean.TRUE)
                        .fechaRegistro(r.getFechaRegistro() != null ? r.getFechaRegistro() : LocalDate.now())
                        .build();
                store.put(id, u);
                return u;
            }

            @Override
            public List<UsuarioResponse> listar() {
                return new ArrayList<>(store.values());
            }

            @Override
            public UsuarioResponse obtener(Long id) {
                var u = store.get(id);
                if (u == null) throw new EntityNotFoundException("Usuario id " + id + " no existe");
                return u;
            }

            @Override
            public UsuarioResponse actualizar(Long id, UsuarioRequest r) {
                var existente = store.get(id);
                if (existente == null) throw new EntityNotFoundException("Usuario id " + id + " no existe");

                // username
                if (r.getUsername() != null && !Objects.equals(r.getUsername(), existente.getUsername())) {
                    if (usernames.contains(r.getUsername()))
                        throw new IllegalArgumentException("Username ya existe");
                    usernames.remove(existente.getUsername());
                    usernames.add(r.getUsername());
                    existente.setUsername(r.getUsername());
                }
                // email
                if (r.getEmail() != null && !Objects.equals(r.getEmail(), existente.getEmail())) {
                    if (emails.contains(r.getEmail()))
                        throw new IllegalArgumentException("Email ya existe");
                    emails.remove(existente.getEmail());
                    emails.add(r.getEmail());
                    existente.setEmail(r.getEmail());
                }

                if (r.getNombreCompleto() != null) existente.setNombreCompleto(r.getNombreCompleto());
                if (r.getRol() != null) existente.setRol(r.getRol());
                if (r.getActivo() != null) existente.setActivo(r.getActivo());
                if (r.getFechaRegistro() != null) existente.setFechaRegistro(r.getFechaRegistro());

                store.put(id, existente);
                return existente;
            }

            @Override
            public void eliminar(Long id) {
                var removed = store.remove(id);
                if (removed == null) throw new EntityNotFoundException("Usuario id " + id + " no existe");
                usernames.remove(removed.getUsername());
                emails.remove(removed.getEmail());
            }
        }
    }

    // --- Inyección de dependencias por campos ---
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private UsuarioService service;

    // (Constructor eliminado)

    @BeforeEach
    void clean() {
        ((TestBeans.InMemoryUsuarioService) service).clear();
    }

    // Helpers
    private byte[] body(String username, String nombreCompleto, String email,
                        Rol rol, Boolean activo, LocalDate fecha) throws Exception {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("username", username);
        m.put("nombreCompleto", nombreCompleto);
        m.put("email", email);
        m.put("rol", rol != null ? rol.name() : null);
        m.put("activo", activo);
        if (fecha != null) m.put("fechaRegistro", fecha.toString());
        return json.writeValueAsBytes(m);
    }

    // Tests

    @Test
    void get_lista_200() throws Exception {
        service.crear(UsuarioRequest.builder()
                .username("u1").nombreCompleto("U No 1").email("u1@x.com")
                .rol(Rol.EDITOR).activo(true).build());
        service.crear(UsuarioRequest.builder()
                .username("u2").nombreCompleto("U No 2").email("u2@x.com")
                .rol(Rol.ADMIN).activo(true).build());

        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void get_porId_200() throws Exception {
        var r = service.crear(UsuarioRequest.builder()
                .username("diego").nombreCompleto("Diego H")
                .email("d@udb.edu.sv").rol(Rol.EDITOR).activo(true).build());

        mvc.perform(get("/api/usuarios/{id}", r.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("diego"));
    }

    @Test
    void get_porId_404() throws Exception {
        mvc.perform(get("/api/usuarios/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void post_crea_201() throws Exception {
        var res = mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("nuevo", "Nuevo User", "n@x.com",
                                Rol.LECTOR, true, LocalDate.of(2025,1,1))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/usuarios/")))
                .andReturn();

        var created = json.readValue(res.getResponse().getContentAsByteArray(), UsuarioResponse.class);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getUsername()).isEqualTo("nuevo");
        assertThat(created.getRol()).isEqualTo(Rol.LECTOR);
    }

    @Test
    void post_409_username_duplicado() throws Exception {
        service.crear(UsuarioRequest.builder()
                .username("dup").nombreCompleto("A").email("a@x.com")
                .rol(Rol.EDITOR).activo(true).build());

        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("dup", "B", "b@x.com", Rol.EDITOR, true, null)))
                .andExpect(status().isConflict());
    }

    @Test
    void post_409_email_duplicado() throws Exception {
        service.crear(UsuarioRequest.builder()
                .username("a").nombreCompleto("A").email("dup@x.com")
                .rol(Rol.EDITOR).activo(true).build());

        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("b", "B", "dup@x.com", Rol.EDITOR, true, null)))
                .andExpect(status().isConflict());
    }

    @Test
    void put_actualiza_200() throws Exception {
        var u = service.crear(UsuarioRequest.builder()
                .username("ed").nombreCompleto("Ed Uno").email("ed@x.com")
                .rol(Rol.LECTOR).activo(true).build());

        // actualiza nombre y rol (manteniendo username/email)
        mvc.perform(put("/api/usuarios/{id}", u.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("ed", "Ed Dos", "ed@x.com", Rol.EDITOR, true, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCompleto").value("Ed Dos"))
                .andExpect(jsonPath("$.rol").value("EDITOR"));
    }

    @Test
    void put_404_no_existe() throws Exception {
        mvc.perform(put("/api/usuarios/{id}", 1234L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("x", "X", "x@x.com", Rol.LECTOR, true, null)))
                .andExpect(status().isNotFound());
    }

    @Test
    void put_409_username_duplicado() throws Exception {
        var a = service.crear(UsuarioRequest.builder()
                .username("u1").nombreCompleto("A").email("a@x.com")
                .rol(Rol.LECTOR).activo(true).build());
        service.crear(UsuarioRequest.builder()
                .username("u2").nombreCompleto("B").email("b@x.com")
                .rol(Rol.LECTOR).activo(true).build());

        mvc.perform(put("/api/usuarios/{id}", a.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("u2", "A", "a@x.com", Rol.LECTOR, true, null)))
                .andExpect(status().isConflict());
    }

    @Test
    void put_409_email_duplicado() throws Exception {
        var a = service.crear(UsuarioRequest.builder()
                .username("u1").nombreCompleto("A").email("a@x.com")
                .rol(Rol.LECTOR).activo(true).build());
        service.crear(UsuarioRequest.builder()
                .username("u2").nombreCompleto("B").email("b@x.com")
                .rol(Rol.LECTOR).activo(true).build());

        mvc.perform(put("/api/usuarios/{id}", a.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("u1", "A", "b@x.com", Rol.LECTOR, true, null)))
                .andExpect(status().isConflict());
    }

    @Test
    void delete_204() throws Exception {
        var u = service.crear(UsuarioRequest.builder()
                .username("x").nombreCompleto("X").email("x@x.com")
                .rol(Rol.ADMIN).activo(true).build());

        mvc.perform(delete("/api/usuarios/{id}", u.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_404() throws Exception {
        mvc.perform(delete("/api/usuarios/{id}", 9999L))
                .andExpect(status().isNotFound());
    }
}