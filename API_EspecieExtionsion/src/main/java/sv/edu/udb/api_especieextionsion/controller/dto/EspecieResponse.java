package sv.edu.udb.api_especieextionsion.controller.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EspecieResponse {
    private Long id;
    private String nombreCientifico;
    private String nombreComun;
    private String tipo;
    private String estadoConservacion;
    private String descripcion;
    private Boolean esEndemica;
    private LocalDate fechaRegistro;
}
