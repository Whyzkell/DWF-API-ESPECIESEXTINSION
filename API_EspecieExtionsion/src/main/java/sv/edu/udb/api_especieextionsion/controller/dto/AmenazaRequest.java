package sv.edu.udb.api_especieextionsion.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AmenazaRequest {
    @NotBlank @Size(max = 50)
    private String codigo;
    @NotBlank @Size(max = 120)
    private String tipo;
    @Size(max = 2000)
    private String descripcion;
}
