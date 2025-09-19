// controller/dto/DistribucionRequest.java
package sv.edu.udb.api_especieextionsion.controller.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Solicitud para registrar la distribución geográfica de una especie")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DistribucionRequest {
    @Schema(description = "Nombre de la región o país donde se ha observado la especie", example = "Centroamérica")
    @NotBlank
    private String region;

    @Schema(description = "Tipo de ecosistema de la ubicación", example = "Bosque tropical")
    @NotBlank
    private String ecosistema;

    @Schema(description = "Latitud de la ubicación en grados decimales (-90 a 90)", example = "13.6929")
    @NotNull @DecimalMin(value="-90") @DecimalMax(value="90")
    private Double latitud;

    @Schema(description = "Longitud de la ubicación en grados decimales (-180 a 180)", example = "-89.2182")
    @NotNull @DecimalMin(value="-180") @DecimalMax(value="180")
    private Double longitud;

    @Schema(description = "Precisión de la ubicación en metros (mayor o igual a 0)", example = "50")
    @PositiveOrZero
    private Integer precisionMetros;

    @Schema(description = "Fecha de la observación. Puede ser nula.", type = "string", format = "date", example = "2024-09-18")
    private LocalDate fechaObservacion;
}
