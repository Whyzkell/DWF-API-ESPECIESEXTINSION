package sv.edu.udb.api_especieextionsion.controller.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DistribucionResponse {
    private Long id;
    private String region;
    private String ecosistema;
    private Double latitud;
    private Double longitud;
    private Integer precisionMetros;
    private LocalDate fechaObservacion;
}