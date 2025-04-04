package es.upm.api.configurations;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.*;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "Mi API", version = "v1"),
        security = @SecurityRequirement(name = "oauth2")
)
@SecurityScheme(
        name = "oauth2",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(
                authorizationCode = @OAuthFlow(
                        authorizationUrl = "http://localhost:8081/oauth2/authorize",
                        tokenUrl = "http://localhost:8081/oauth2/token",
                        scopes = {
                                @OAuthScope(name = "profile", description = "profile")
                        }
                )
        )
)
@Configuration
public class OpenApiConfig {
    // empty
}
