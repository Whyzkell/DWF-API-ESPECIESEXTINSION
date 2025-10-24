// AuthRequest.java
package sv.edu.udb.api_especieextionsion.controller.dto.auth;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthRequest {
    private String username;
    private String password;
}
