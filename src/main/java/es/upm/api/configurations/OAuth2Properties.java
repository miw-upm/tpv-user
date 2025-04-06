package es.upm.api.configurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.clients")
@Data
public class OAuth2Properties {
    private String issuer;
    private String openApiClientId;
    private String openApiClientSecret;
    private List<String> openApiRedirectUris = new ArrayList<>();
    private String spaClientId;
    private String spaLoginRedirectUri;
    private String spaLogoutRedirectUri;
    private String apiClientId;
    private String apiClientSecret;
}

