package sv.edu.udb.api_especieextionsion;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base para pruebas de Repositorios con JPA + Testcontainers.
 * Levanta PostgreSQL y configura el datasource de Spring.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class RepositoryTestBase {

    // PostgreSQL 16 en contenedor
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("bio_db_test")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @BeforeAll
    static void startContainer() { POSTGRES.start(); }

    @AfterAll
    static void stopContainer() { POSTGRES.stop(); }

    // Inyecta las propiedades del contenedor al contexto de Spring
    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        // Para tests: que JPA cree/borre el esquema automáticamente
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }
}

