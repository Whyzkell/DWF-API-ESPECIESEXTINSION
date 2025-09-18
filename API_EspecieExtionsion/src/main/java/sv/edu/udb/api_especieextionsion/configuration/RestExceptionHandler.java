package sv.edu.udb.api_especieextionsion.configuration;

import jakarta.validation.ValidationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import sv.edu.udb.api_especieextionsion.configuration.web.ApiError;
import sv.edu.udb.api_especieextionsion.configuration.web.ApiErrorWrapper;

import java.util.NoSuchElementException;

@ControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorWrapper> handleValidation(MethodArgumentNotValidException ex){
        ApiErrorWrapper wrapper = new ApiErrorWrapper();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                wrapper.addFieldError("validation_error", "Dato inválido", fe.getField(), fe.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(wrapper); // 400
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiErrorWrapper> handleNotFound(NoSuchElementException ex){
        ApiErrorWrapper w = new ApiErrorWrapper();
        w.addApiError(ApiError.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .type("not_found")
                .title("Recurso no encontrado")
                .source("base")
                .description(ex.getMessage())
                .build());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(w); // 404
    }

    @ExceptionHandler({IllegalArgumentException.class, ValidationException.class})
    public ResponseEntity<ApiErrorWrapper> handleBadRequest(RuntimeException ex){
        ApiErrorWrapper w = new ApiErrorWrapper();
        w.addApiError(ApiError.builder()
                .status(HttpStatus.CONFLICT.value())
                .type("conflict")  // nombre científico duplicado, etc.
                .title("Conflicto en la solicitud")
                .source("base")
                .description(ex.getMessage())
                .build());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(w); // 409
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorWrapper> handleGeneric(Exception ex){
        ApiErrorWrapper w = new ApiErrorWrapper();
        w.addApiError(ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .type("internal_error")
                .title("Error interno")
                .source("base")
                .description("Ha ocurrido un error inesperado")
                .build());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(w); // 500
    }
}
