package sv.edu.udb.api_especieextionsion.controller.dto.auth;
import lombok.*;
import sv.edu.udb.api_especieextionsion.repository.domain.Rol;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterRequest {
    private String username;
    private String password;
    private String nombreCompleto;
    private String email;
    private Rol rol;  // ADMIN / EDITOR / LECTOR
}
