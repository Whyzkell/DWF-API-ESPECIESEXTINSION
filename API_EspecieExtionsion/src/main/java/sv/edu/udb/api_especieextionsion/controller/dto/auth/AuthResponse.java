// AuthResponse.java
package sv.edu.udb.api_especieextionsion.controller.dto.auth;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    private String token;
    private String refreshToken; // opcional; si no usas refresh, puedes omitir
}