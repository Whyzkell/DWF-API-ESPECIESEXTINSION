package sv.edu.udb.api_especieextionsion.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Solicitud para crear o actualizar una amenaza")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AmenazaRequest {
    @Schema(description = "Código único de la amenaza", example = "DEFORESTACION")
    @NotBlank @Size(max = 50)
    private String codigo;

    @Schema(description = "Tipo de amenaza", example = "ACTIVIDADES_HUMANAS")
    @NotBlank @Size(max = 120)
    private String tipo;

    @Schema(description = "Descripción detallada de la amenaza", example = "Eliminación de la masa forestal para la expansión agrícola o urbana.")
    @Size(max = 2000)
    private String descripcion;
}
