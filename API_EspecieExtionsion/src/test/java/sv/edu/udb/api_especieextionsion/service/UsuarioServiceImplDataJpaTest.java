// src/test/java/sv/edu/udb/api_especieextionsion/service/UsuarioServiceImplDataJpaTest.java
package sv.edu.udb.api_especieextionsion.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import sv.edu.udb.api_especieextionsion.mapping.FakeUsuarioMapper;
import sv.edu.udb.api_especieextionsion.service.impl.UsuarioServiceImpl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({UsuarioServiceImpl.class, FakeUsuarioMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.test.database.replace=ANY",
        "spring.jpa.show-sql=false",
        "spring.sql.init.mode=never"
})
@ActiveProfiles("test")
class UsuarioServiceImplDataJpaTest {

    @Autowired
    UsuarioService service;

    @Test
    @DisplayName("obtener: 404 si no existe")
    void obtener_notFound() {
        assertThatThrownBy(() -> service.obtener(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("eliminar: 404 cuando no existe")
    void eliminar_notFound() {
        assertThatThrownBy(() -> service.eliminar(12345L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
