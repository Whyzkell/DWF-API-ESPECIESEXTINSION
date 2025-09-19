package sv.edu.udb.api_especieextionsion.controller.dto;

import lombok.*;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con los detalles de una distribución geográfica de una especie")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DistribucionResponse {
    @Schema(description = "Identificador único de la distribución", example = "789")
    private Long id;

    @Schema(description = "Nombre de la región o país donde se encuentra la especie", example = "Centroamérica")
    private String region;

    @Schema(description = "Tipo de ecosistema donde se ha observado la especie", example = "Bosque tropical")
    private String ecosistema;

    @Schema(description = "Latitud de la ubicación (en grados decimales)", example = "13.6929")
    private Double latitud;

    @Schema(description = "Longitud de la ubicación (en grados decimales)", example = "-89.2182")
    private Double longitud;

    @Schema(description = "Precisión de la ubicación en metros", example = "50")
    private Integer precisionMetros;

    @Schema(description = "Fecha de la observación de la especie en esa ubicación", type = "string", format = "date", example = "2024-09-18")
    private LocalDate fechaObservacion;
}