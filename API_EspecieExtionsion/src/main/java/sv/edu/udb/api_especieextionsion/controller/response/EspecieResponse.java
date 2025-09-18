package sv.edu.udb.api_especieextionsion.controller.response;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import java.time.LocalDate;

@Getter @Setter @Builder @FieldNameConstants

public class EspecieResponse {
    private Long id;
    private String nombreCientifico;
    private String nombreComun;
    private String tipo;
    private String estadoConservacion;
    private Boolean esEndemica;
    private LocalDate fechaRegistro;
    private String descripcion;
}
