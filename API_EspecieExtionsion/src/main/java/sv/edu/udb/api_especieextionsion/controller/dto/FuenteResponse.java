package sv.edu.udb.api_especieextionsion.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "FuenteResponse", description = "Respuesta con datos de la fuente")
public class FuenteResponse {
    @Schema(example = "5")
    private Long id;

    @Schema(example = "IUCN Red List 2024 – Panthera onca")
    private String nombre;

    @Schema(example = "Ficha técnica de conservación")
    private String descripcion;

    @Schema(example = "WEB")
    private String tipo;

    @Schema(example = "https://www.iucnredlist.org/species/15953/123791436")
    private String enlace;

    @Schema(example = "2024-11-15")
    private LocalDate fechaPublicacion;
}
