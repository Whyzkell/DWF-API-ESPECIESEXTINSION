package sv.edu.udb.api_especieextionsion.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import io.swagger.v3.oas.annotations.media.Schema;



import java.time.LocalDate;

@Schema(description = "Solicitud para crear/actualizar una especie")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EspecieRequest {
    @Schema(description = "Nombre científico de la especie", example = "Panthera onca")
    @NotBlank @Size(min = 3, max = 120)
    private String nombreCientifico;

    @Schema(description = "Nombre común de la especie", example = "Jaguar")
    @NotBlank @Size(min = 2, max = 120)
    private String nombreComun;

    @Schema(description = "Tipo de organismo (FLORA o FAUNA)", allowableValues = {"FLORA", "FAUNA"}, example = "FAUNA")
    @NotBlank @Pattern(regexp = "FLORA|FAUNA", message = "tipo debe ser FLORA o FAUNA")
    private String tipo;

    @Schema(description = "Categoría de conservación de la IUCN (p. ej. EN, VU, CR)", example = "EN")
    @NotBlank @Size(min = 2, max = 10)  // p.ej. EN, VU, CR, etc.
    private String estadoConservacion;

    @Schema(description = "Descripción general de la especie", example = "Felino nativo de América")
    private String descripcion;

    @Schema(description = "¿Es la especie endémica de una región?", example = "true")
    @NotNull
    private Boolean esEndemica;

    @Schema(description = "Fecha de registro de la especie", type = "string", format = "date", example = "2024-09-18")
    @PastOrPresent
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaRegistro;
}
