// controller/dto/DistribucionRequest.java
package sv.edu.udb.api_especieextionsion.controller.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DistribucionRequest {
    @NotBlank private String region;
    @NotBlank private String ecosistema;

    @NotNull @DecimalMin(value="-90")  @DecimalMax(value="90")
    private Double latitud;

    @NotNull @DecimalMin(value="-180") @DecimalMax(value="180")
    private Double longitud;

    @PositiveOrZero private Integer precisionMetros;
    private LocalDate fechaObservacion; // puede ser nula
}
