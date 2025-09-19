package sv.edu.udb.api_especieextionsion.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import sv.edu.udb.api_especieextionsion.domain.Rol;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "UsuarioRequest", description = "Petición para crear/actualizar usuario")
public class UsuarioRequest {
    @NotBlank @Size(max = 40)
    @Schema(example = "dhernandez", description = "Username único")
    private String username;

    @NotBlank @Size(max = 120)
    @Schema(example = "Diego Hernández", description = "Nombre completo")
    private String nombreCompleto;

    @NotBlank @Email @Size(max = 120)
    @Schema(example = "diego@udb.edu.sv", description = "Correo electrónico único")
    private String email;

    @NotNull
    @Schema(example = "EDITOR", description = "Rol del usuario (ADMIN, EDITOR, LECTOR)")
    private Rol rol;

    @NotNull
    @Schema(example = "true", description = "Si el usuario está activo")
    private Boolean activo;

    @Schema(example = "2025-09-18", description = "Fecha de registro (opcional: si no se envía, se usará la fecha actual)")
    private LocalDate fechaRegistro;
}
