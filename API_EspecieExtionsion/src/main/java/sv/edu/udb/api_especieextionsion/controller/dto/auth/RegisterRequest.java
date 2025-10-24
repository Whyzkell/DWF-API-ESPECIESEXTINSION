package sv.edu.udb.api_especieextionsion.controller.dto.auth;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import sv.edu.udb.api_especieextionsion.repository.domain.Rol;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterRequest {
    @NotBlank
    private String username;
    @NotBlank private String password;
    @NotBlank private String nombreCompleto;
    @Email
    @NotBlank private String email;
    @NotNull
    private Rol rol;
}
