package sv.edu.udb.api_especieextionsion.configuration;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import sv.edu.udb.api_especieextionsion.configuration.web.ApiError;
import sv.edu.udb.api_especieextionsion.configuration.web.ApiErrorWrapper;

import java.util.NoSuchElementException;

@RestControllerAdvice // <-- SOLO este, no @ControllerAdvice adicional
public class RestExceptionHandler {

    // 400: @Valid en el body
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorWrapper> handleValidation(MethodArgumentNotValidException ex){
        ApiErrorWrapper w = new ApiErrorWrapper();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                w.addFieldError("validation_error", "Dato inválido", fe.getField(), fe.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(w); // 400
    }

    // 400: validaciones en params/path (@Validated en controller)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorWrapper> handleConstraint(ConstraintViolationException ex){
        ApiErrorWrapper w = new ApiErrorWrapper();
        ex.getConstraintViolations().forEach(cv ->
                w.addFieldError("validation_error", "Parámetro inválido",
                        cv.getPropertyPath().toString(), cv.getMessage())
        );
        return ResponseEntity.badRequest().body(w); // 400
    }

    // 400: JSON mal formado / tipos erróneos
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorWrapper> handleBadJson(HttpMessageNotReadableException ex){
        ApiErrorWrapper w = new ApiErrorWrapper();
        w.addApiError(ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .type("bad_request")
                .title("JSON inválido")
                .source("body")
                .description("No se pudo parsear el cuerpo de la solicitud")
                .build());
        return ResponseEntity.badRequest().body(w);
    }

    // 404: no encontrado (usas EntityNotFoundException en los services)
    @ExceptionHandler({EntityNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<ApiErrorWrapper> handleNotFound(RuntimeException ex){
        ApiErrorWrapper w = new ApiErrorWrapper();
        w.addApiError(ApiError.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .type("not_found")
                .title("Recurso no encontrado")
                .source("base")
                .description(ex.getMessage())
                .build());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(w);
    }

    // 409: conflictos (duplicados, unicidad, negocio)
    @ExceptionHandler({IllegalArgumentException.class, ValidationException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiErrorWrapper> handleConflict(RuntimeException ex){
        ApiErrorWrapper w = new ApiErrorWrapper();
        w.addApiError(ApiError.builder()
                .status(HttpStatus.CONFLICT.value())
                .type("conflict")
                .title("Conflicto en la solicitud")
                .source("base")
                .description(ex.getMessage())
                .build());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(w);
    }

    // ⚠️ ÚNICO genérico
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorWrapper> handleUnexpected(Exception ex){
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

