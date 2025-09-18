package sv.edu.udb.api_especieextionsion.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EspecieAmenazaLinkRequest {
    @NotNull private Long amenazaId;
    @NotNull @Pattern(regexp = "BAJA|MEDIA|ALTA")
    private String severidad;
}
