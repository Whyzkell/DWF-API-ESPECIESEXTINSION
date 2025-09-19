package sv.edu.udb.api_especieextionsion.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import sv.edu.udb.api_especieextionsion.domain.Rol;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "UsuarioResponse", description = "Respuesta con datos del usuario")
public class UsuarioResponse {
    @Schema(example = "10")
    private Long id;

    @Schema(example = "dhernandez")
    private String username;

    @Schema(example = "Diego Hernández")
    private String nombreCompleto;

    @Schema(example = "diego@udb.edu.sv")
    private String email;

    @Schema(example = "EDITOR")
    private Rol rol;

    @Schema(example = "true")
    private Boolean activo;

    @Schema(example = "2025-09-18")
    private LocalDate fechaRegistro;
}
