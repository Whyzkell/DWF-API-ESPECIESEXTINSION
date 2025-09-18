package sv.edu.udb.api_especieextionsion.configuration.web;
import lombok.Builder;
import lombok.Getter;
@Getter @Builder
public class ApiError {
    private Integer status;
    private String type;       // p.ej. "validation_error", "not_found", "conflict"
    private String title;      // resumen breve
    private String source;     // p.ej. "EspecieController" o "base"
    private String description;// detalle para el cliente
}
