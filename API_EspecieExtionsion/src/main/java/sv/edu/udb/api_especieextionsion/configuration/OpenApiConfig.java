package sv.edu.udb.api_especieextionsion.configuration;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import java.util.List;

@Configuration

public class OpenApiConfig {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI apiInfo(Environment env) {
        String profile = String.join(",", env.getActiveProfiles());
        return new OpenAPI()
                .info(new Info()
                        .title("API de Especies – DWF")
                        .description("API REST para gestión de especies, distribuciones y amenazas (según guías).")
                        .version(appVersion)
                        .contact(new Contact().name("Equipo DWF").email("soporte@udb.edu.sv"))
                        .license(new License().name("MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local (" + profile + ")")
                ));
    }
}
