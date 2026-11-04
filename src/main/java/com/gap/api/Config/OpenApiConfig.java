package com.gap.api.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gapOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("GAP API")
                        .description("API de solicitações acadêmicas da UNIRIO. "
                                + "As rotas exigem sessão autenticada via Google (OIDC); "
                                + "faça login em /api/oauth2/authorization/google antes de usar o \"Try it out\".")
                        .version("v1"));
    }
}
