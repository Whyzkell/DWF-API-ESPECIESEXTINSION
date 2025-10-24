package sv.edu.udb.api_especieextionsion.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.api_especieextionsion.controller.dto.auth.*;
import sv.edu.udb.api_especieextionsion.repository.UsuarioRepository;
import sv.edu.udb.api_especieextionsion.repository.domain.Usuario;
import sv.edu.udb.api_especieextionsion.security.JwtService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req){
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        var user = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
        String token = jwtService.generateToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        return ResponseEntity.ok(AuthResponse.builder().token(token).refreshToken(refresh).build());
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest r){
        if (usuarioRepo.existsByUsername(r.getUsername())) return ResponseEntity.status(409).body("Username ya existe");
        Usuario u = Usuario.builder()
                .username(r.getUsername())
                .password(passwordEncoder.encode(r.getPassword()))
                .nombreCompleto(r.getNombreCompleto())
                .email(r.getEmail())
                .rol(r.getRol())
                .activo(true)
                .fechaRegistro(java.time.LocalDate.now())
                .build();
        return ResponseEntity.ok(usuarioRepo.save(u));
    }
}

