// src/main/java/sv/edu/udb/api_especieextionsion/configuration/web/ApiError.java
package sv.edu.udb.api_especieextionsion.configuration.web;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ApiError {
    private int status;
    private String type;
    private String title;
    private String source;       // "base" o el campo
    private String description;  // mensaje

    @Builder.Default
    private List<FieldError> fields = new ArrayList<>();

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class FieldError {
        private String field;
        private String message;
    }
}

