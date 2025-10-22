package sv.edu.udb.api_especieextionsion.configuration.web;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiErrorTest {

    @Test
    void builder_inicializaCamposBasicos_yFieldsPorDefecto() {
        ApiError error = ApiError.builder()
                .status(400)
                .type("https://example.com/validation-error")
                .title("Bad Request")
                .source("base")
                .description("Error de validación")
                .build();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getType()).isEqualTo("https://example.com/validation-error");
        assertThat(error.getTitle()).isEqualTo("Bad Request");
        assertThat(error.getSource()).isEqualTo("base");
        assertThat(error.getDescription()).isEqualTo("Error de validación");

        // @Builder.Default: fields no debe ser null y debe empezar vacío
        assertThat(error.getFields()).isNotNull();
        assertThat(error.getFields()).isEmpty();
    }

    @Test
    void settersYGetters_funcionanCorrectamente() {
        ApiError error = new ApiError();
        error.setStatus(404);
        error.setType("https://example.com/not-found");
        error.setTitle("Not Found");
        error.setSource("id");
        error.setDescription("Recurso no encontrado");

        ApiError.FieldError fe = ApiError.FieldError.builder()
                .field("id")
                .message("El id no existe")
                .build();

        error.setFields(List.of(fe));

        assertThat(error.getStatus()).isEqualTo(404);
        assertThat(error.getType()).isEqualTo("https://example.com/not-found");
        assertThat(error.getTitle()).isEqualTo("Not Found");
        assertThat(error.getSource()).isEqualTo("id");
        assertThat(error.getDescription()).isEqualTo("Recurso no encontrado");
        assertThat(error.getFields()).hasSize(1);
        assertThat(error.getFields().get(0).getField()).isEqualTo("id");
        assertThat(error.getFields().get(0).getMessage()).isEqualTo("El id no existe");
    }

    @Test
    void fieldError_constructorYSetters() {
        ApiError.FieldError fe = new ApiError.FieldError();
        fe.setField("nombre");
        fe.setMessage("No puede ser vacío");

        assertThat(fe.getField()).isEqualTo("nombre");
        assertThat(fe.getMessage()).isEqualTo("No puede ser vacío");
    }
}
