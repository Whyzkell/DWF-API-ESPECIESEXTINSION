package sv.edu.udb.api_especieextionsion.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "FuenteRequest", description = "Petición para crear/actualizar una fuente")
public class FuenteRequest {
    @NotBlank @Size(max = 160)
    @Schema(example = "IUCN Red List 2024 – Panthera onca", description = "Nombre/título de la fuente")
    private String nombre;

    @Size(max = 500)
    @Schema(example = "Ficha técnica de conservación", description = "Descripción opcional")
    private String descripcion;

    @Size(max = 30)
    @Schema(example = "WEB", description = "Tipo de fuente: WEB | ARTICULO | LIBRO | REPORTE")
    private String tipo;

    @Size(max = 500) @URL
    @Schema(example = "https://www.iucnredlist.org/species/15953/123791436", description = "URL relacionada")
    private String enlace;

    @Schema(example = "2024-11-15", description = "Fecha de publicación")
    private LocalDate fechaPublicacion;
}
