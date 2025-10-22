package sv.edu.udb.api_especieextionsion.configuration.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiErrorWrapperTest {

    @Test
    void constructor_porDefecto_inicializaListaVacia() {
        ApiErrorWrapper wrapper = new ApiErrorWrapper();
        assertThat(wrapper.getErrors()).isNotNull();
        assertThat(wrapper.getErrors()).isEmpty();
    }

    @Test
    void addApiError_agregaElementoALaLista() {
        ApiErrorWrapper wrapper = new ApiErrorWrapper();

        ApiError error = ApiError.builder()
                .status(422)
                .type("https://example.com/unprocessable")
                .title("Unprocessable Entity")
                .source("base")
                .description("Datos inválidos")
                .build();

        wrapper.addApiError(error);

        assertThat(wrapper.getErrors()).hasSize(1);
        assertThat(wrapper.getErrors().get(0).getStatus()).isEqualTo(422);
        assertThat(wrapper.getErrors().get(0).getTitle()).isEqualTo("Unprocessable Entity");
    }

    @Test
    void addFieldError_creaApiErrorConStatus400_yUnFieldError() {
        ApiErrorWrapper wrapper = new ApiErrorWrapper();

        wrapper.addFieldError(
                "https://example.com/validation-error",
                "Bad Request",
                "nombre",
                "No puede ser vacío"
        );

        assertThat(wrapper.getErrors()).hasSize(1);

        ApiError err = wrapper.getErrors().get(0);
        assertThat(err.getStatus()).isEqualTo(400);
        assertThat(err.getType()).isEqualTo("https://example.com/validation-error");
        assertThat(err.getTitle()).isEqualTo("Bad Request");
        assertThat(err.getSource()).isEqualTo("nombre");
        assertThat(err.getDescription()).isEqualTo("No puede ser vacío");

        assertThat(err.getFields()).hasSize(1);
        ApiError.FieldError fe = err.getFields().get(0);
        assertThat(fe.getField()).isEqualTo("nombre");
        assertThat(fe.getMessage()).isEqualTo("No puede ser vacío");
    }

    @Test
    void addFieldError_variasVeces_acumulaErrores() {
        ApiErrorWrapper wrapper = new ApiErrorWrapper();

        wrapper.addFieldError("t", "Bad Request", "nombre", "no vacío");
        wrapper.addFieldError("t", "Bad Request", "email", "formato inválido");

        assertThat(wrapper.getErrors()).hasSize(2);
        assertThat(wrapper.getErrors())
                .extracting(ApiError::getSource)
                .containsExactly("nombre", "email");
    }
}
