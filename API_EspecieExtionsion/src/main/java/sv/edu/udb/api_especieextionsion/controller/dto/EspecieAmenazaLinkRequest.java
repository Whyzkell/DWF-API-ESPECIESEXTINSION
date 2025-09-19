package sv.edu.udb.api_especieextionsion.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Solicitud para vincular una amenaza a una especie con un nivel de severidad")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EspecieAmenazaLinkRequest {
    @Schema(description = "Identificador de la amenaza a asociar", example = "9")
    @NotNull
    private Long amenazaId;

    @Schema(description = "Nivel de severidad del vínculo", allowableValues = {"BAJA", "MEDIA", "ALTA"}, example = "ALTA")
    @NotNull @Pattern(regexp = "BAJA|MEDIA|ALTA")
    private String severidad;
}
