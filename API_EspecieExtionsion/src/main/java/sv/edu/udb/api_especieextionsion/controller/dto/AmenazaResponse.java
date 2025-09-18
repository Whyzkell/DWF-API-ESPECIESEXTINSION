package sv.edu.udb.api_especieextionsion.controller.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AmenazaResponse {
    private Long id;
    private String codigo;
    private String tipo;
    private String descripcion;
}
