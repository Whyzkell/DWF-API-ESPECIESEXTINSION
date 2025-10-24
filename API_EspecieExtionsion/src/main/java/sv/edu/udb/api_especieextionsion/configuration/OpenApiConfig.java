package sv.edu.udb.api_especieextionsion.configuration;

// Importa estas 3 clases para la seguridad
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

// Tus imports existentes
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

        // Definimos el nombre del esquema de seguridad (como en las instrucciones)
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("API de Especies – DWF")
                        .description("API REST para gestión de especies, distribuciones y amenazas (según guías).")
                        .version(appVersion)
                        .contact(new Contact().name("Equipo DWF").email("soporte@udb.edu.sv"))
                        .license(new License().name("MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local (" + profile + ")")
                ))


                // 1. Añade el requisito de seguridad global (el candadito en los endpoints)
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))

                // 2. Define el esquema de seguridad (lo que pasa al pulsar "Authorize")
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP) // Tipo HTTP
                                        .scheme("bearer")               // Esquema Bearer
                                        .bearerFormat("JWT")            // Formato JWT
                        )
                );

    }
}
