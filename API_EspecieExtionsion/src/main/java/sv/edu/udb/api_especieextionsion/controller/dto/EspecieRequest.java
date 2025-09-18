package sv.edu.udb.api_especieextionsion.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EspecieRequest {
    @NotBlank @Size(min = 3, max = 120)
    private String nombreCientifico;

    @NotBlank @Size(min = 2, max = 120)
    private String nombreComun;

    @NotBlank @Pattern(regexp = "FLORA|FAUNA", message = "tipo debe ser FLORA o FAUNA")
    private String tipo;

    @NotBlank @Size(min = 2, max = 10)  // p.ej. EN, VU, CR, etc.
    private String estadoConservacion;

    private String descripcion;

    @NotNull
    private Boolean esEndemica;

    @PastOrPresent
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaRegistro;
}
