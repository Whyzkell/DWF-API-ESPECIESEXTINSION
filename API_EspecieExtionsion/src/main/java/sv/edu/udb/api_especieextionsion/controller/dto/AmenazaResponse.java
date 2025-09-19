package sv.edu.udb.api_especieextionsion.controller.dto;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con los detalles de una amenaza")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AmenazaResponse {
    @Schema(description = "Identificador único de la amenaza", example = "9")
    private Long id;

    @Schema(description = "Código único de la amenaza", example = "DEFORESTACION")
    private String codigo;

    @Schema(description = "Tipo de amenaza", example = "ACTIVIDADES_HUMANAS")
    private String tipo;

    @Schema(description = "Descripción de la amenaza", example = "Eliminación de la masa forestal.")
    private String descripcion;
}
