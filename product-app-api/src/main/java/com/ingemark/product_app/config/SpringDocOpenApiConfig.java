package com.ingemark.product_app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocOpenApiConfig {

    @Bean(name = "com.ingemark.product_app.config.SpringDocConfiguration.apiInfo")
    OpenAPI apiInfo() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Product API")
                                .description("API for managing product entities.")
                                .version("1.0.0")
                )
                .components(
                        new Components()
                                .addSecuritySchemes("oAuthPassword", new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .flows(new OAuthFlows()
                                                .password(new OAuthFlow()
                                                        .tokenUrl("http://localhost:8080/realms/idm/protocol/openid-connect/token")
                                                        .scopes(new Scopes()
                                                                .addString("product_app_api:product.read","Read")
                                                                .addString("product_app_api:product.write","Write")
                                                        )
                                                )
                                        )
                                )
                )
                ;
    }
}
