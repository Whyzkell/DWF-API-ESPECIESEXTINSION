package sv.edu.udb.api_especieextionsion.controller.dto;

import lombok.*;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con los detalles del vínculo entre una especie y una amenaza")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EspecieAmenazaResponse {
    @Schema(description = "Identificador único del vínculo", example = "45")
    private Long idVinculo;

    @Schema(description = "Identificador de la amenaza", example = "9")
    private Long amenazaId;

    @Schema(description = "Código de la amenaza", example = "INCENDIO_FORESTAL")
    private String codigo;

    @Schema(description = "Tipo de amenaza", example = "ACTIVIDADES_HUMANAS")
    private String tipo;

    @Schema(description = "Descripción de la amenaza", example = "Incendios provocados por la actividad humana.")
    private String descripcion;

    @Schema(description = "Nivel de severidad del vínculo", allowableValues = {"BAJA", "MEDIA", "ALTA"}, example = "ALTA")
    private String severidad;
}
