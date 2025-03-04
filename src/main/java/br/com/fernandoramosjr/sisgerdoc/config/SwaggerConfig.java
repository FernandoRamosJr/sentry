package br.com.fernandoramosjr.sisgerdoc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                    .title("API de Gerenciamento de Documentos")
                    .version("1.0")
                    .description("API de Gerenciamento de Documentos - Recrutamento Sentry"))
            .addSecurityItem(new SecurityRequirement().addList("ApiKeyAuth"))
            .components(new io.swagger.v3.oas.models.Components()
                    .addSecuritySchemes("ApiKeyAuth", new SecurityScheme()
                            .name("X-API-KEY")
                            .type(SecurityScheme.Type.APIKEY)
                            .in(SecurityScheme.In.HEADER)));
    }
}
