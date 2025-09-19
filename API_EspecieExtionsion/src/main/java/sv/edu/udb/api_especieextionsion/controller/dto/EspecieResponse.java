package sv.edu.udb.api_especieextionsion.controller.dto;

import lombok.*;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con los detalles de una especie")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EspecieResponse {

    @Schema(description = "Identificador único de la especie", example = "123")
    private Long id;

    @Schema(description = "Nombre científico de la especie", example = "Panthera onca")
    private String nombreCientifico;

    @Schema(description = "Nombre común de la especie", example = "Jaguar")
    private String nombreComun;

    @Schema(description = "Tipo de organismo (FLORA o FAUNA)", example = "FAUNA")
    private String tipo;

    @Schema(description = "Categoría de conservación de la IUCN", example = "EN")
    private String estadoConservacion;

    @Schema(description = "Descripción general de la especie", example = "Felino nativo de América")
    private String descripcion;

    @Schema(description = "¿Es la especie endémica de una región?", example = "true")
    private Boolean esEndemica;

    @Schema(description = "Fecha de registro de la especie", type = "string", format = "date", example = "2024-09-18")
    private LocalDate fechaRegistro;
}
