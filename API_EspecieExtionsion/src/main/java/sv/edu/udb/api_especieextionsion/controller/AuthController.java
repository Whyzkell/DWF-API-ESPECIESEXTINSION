package sv.edu.udb.api_especieextionsion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import sv.edu.udb.api_especieextionsion.controller.dto.auth.AuthRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.auth.AuthResponse;
import sv.edu.udb.api_especieextionsion.controller.dto.auth.RegisterRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.UsuarioResponse;
import sv.edu.udb.api_especieextionsion.mapping.UsuarioMapper;
import sv.edu.udb.api_especieextionsion.repository.UsuarioRepository;
import sv.edu.udb.api_especieextionsion.repository.domain.Usuario;
import sv.edu.udb.api_especieextionsion.security.JwtService;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        User user = (User) auth.getPrincipal();

        String accessToken  = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return ResponseEntity.ok(
                AuthResponse.builder()
                        .token(accessToken)
                        .refreshToken(refreshToken)
                        .build()
        );
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegisterRequest r) {
        if (usuarioRepo.existsByUsername(r.getUsername())) {
            // Tu GlobalExceptionHandler debería traducir IllegalArgumentException -> 409
            throw new IllegalArgumentException("Username ya existe");
        }
        if (usuarioRepo.existsByEmail(r.getEmail())) {
            throw new IllegalArgumentException("Email ya existe");
        }

        Usuario u = Usuario.builder()
                .username(r.getUsername())
                .password(passwordEncoder.encode(r.getPassword()))
                .nombreCompleto(r.getNombreCompleto())
                .email(r.getEmail())
                .rol(r.getRol())
                .activo(true)
                .fechaRegistro(java.time.LocalDate.now())
                .build();

        u = usuarioRepo.save(u);

        // mapear a response (sin password)
        UsuarioResponse body = usuarioMapper.toDto(u);

        // Location: /api/usuarios/{id} (si tienes ese recurso)
        URI location = URI.create("/api/usuarios/" + u.getId());
        return ResponseEntity.created(location).body(body);
    }


}


