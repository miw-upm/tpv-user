package es.upm.api.configurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "miw.oauth2")
@Data
public class OAuth2Properties {
    private String clientId;
    private String clientSecret;
    private List<String> redirectUris = new ArrayList<>();
    private String issuer;
    private String apiClientId;
    private String apiClientSecret;
}

