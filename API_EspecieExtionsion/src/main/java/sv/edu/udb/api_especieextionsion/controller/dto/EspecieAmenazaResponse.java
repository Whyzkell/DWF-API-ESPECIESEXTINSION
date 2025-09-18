package sv.edu.udb.api_especieextionsion.controller.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EspecieAmenazaResponse {
    private Long idVinculo;        // id de la tabla especie_amenaza
    private Long amenazaId;
    private String codigo;
    private String tipo;
    private String descripcion;
    private String severidad;      // BAJA/MEDIA/ALTA
}
