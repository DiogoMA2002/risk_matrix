package ipleiria.risk_matrix.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI riskMatrixOpenAPI() {
        final String cookieAuthScheme = "cookieAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Risk Matrix API")
                        .description("REST API for the Risk Matrix application. " +
                                "Manages questionnaires, questions, answers, categories, feedback, glossary, and admin users.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("IPLeiria")
                                .url("https://www.ipleiria.pt"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList(cookieAuthScheme))
                .components(new Components()
                        .addSecuritySchemes(cookieAuthScheme, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("admin_access_token")
                                .description("JWT token stored as an HttpOnly cookie. " +
                                        "Obtained via POST /api/auth/login (admin) or POST /api/auth/request-token (public).")));
    }
}
