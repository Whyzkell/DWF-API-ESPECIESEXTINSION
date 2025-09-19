// src/main/java/sv/edu/udb/api_especieextionsion/configuration/web/ApiErrorWrapper.java
package sv.edu.udb.api_especieextionsion.configuration.web;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class ApiErrorWrapper {

    private List<ApiError> errors = new ArrayList<>();

    public void addApiError(ApiError err){
        this.errors.add(err);
    }

    // Atajo para errores de validación de campos (agrega una entrada por campo)
    public void addFieldError(String type, String title, String field, String message){
        ApiError.FieldError fe = ApiError.FieldError.builder()
                .field(field)
                .message(message)
                .build();

        ApiError err = ApiError.builder()
                .status(400)
                .type(type)
                .title(title)
                .source(field)
                .description(message)
                .fields(new ArrayList<>(List.of(fe)))
                .build();

        this.errors.add(err);
    }
}

