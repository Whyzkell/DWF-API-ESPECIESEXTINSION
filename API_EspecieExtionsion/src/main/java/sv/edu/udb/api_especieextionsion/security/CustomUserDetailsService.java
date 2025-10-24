package sv.edu.udb.api_especieextionsion.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import sv.edu.udb.api_especieextionsion.repository.UsuarioRepository;
import sv.edu.udb.api_especieextionsion.repository.domain.Usuario;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UsuarioRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        String roleName = "ROLE_" + u.getRol().name(); // ADMIN->ROLE_ADMIN
        return new org.springframework.security.core.userdetails.User(
                u.getUsername(), u.getPassword(), List.of(new SimpleGrantedAuthority(roleName))
        );
    }
}

