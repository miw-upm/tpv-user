package es.upm.api.configurations;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import es.upm.api.data.entities.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Configuration
public class AuthorizationServerConfig {  // Generate tokens OAuth2

    private final PasswordEncoder passwordEncoder;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String issuer;
    private final String apiClientId;
    private final String apiClientSecret;

    @Autowired
    public AuthorizationServerConfig(
            PasswordEncoder passwordEncoder,
            @Value("${miw.oauth2.client-id}") String clientId,
            @Value("${miw.oauth2.client-secret}") String clientSecret,
            @Value("${miw.oauth2.redirect-uri}") String redirectUri,
            @Value("${miw.oauth2.issuer}") String issuer,
            @Value("${miw.oauth2.api-client-id}") String apiClientId,
            @Value("${miw.oauth2.api-client-secret}") String apiClientSecret) {
        this.passwordEncoder = passwordEncoder;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.issuer = issuer;
        this.apiClientId = apiClientId;
        this.apiClientSecret = apiClientSecret;
    }

    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();
        authorizationServerConfigurer.oidc(Customizer.withDefaults()); //.well-known/openid-configuration
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults())
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .with(authorizationServerConfigurer, Customizer.withDefaults())
                .build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        TokenSettings tokenSettings = TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(60))
                .refreshTokenTimeToLive(Duration.ofDays(30))
                .build();

        RegisteredClient userClient =
                RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId(clientId)
                        .clientSecret(passwordEncoder.encode(clientSecret))
                        .clientAuthenticationMethods(methods -> methods.addAll(Set.of(
                                ClientAuthenticationMethod.CLIENT_SECRET_BASIC,
                                ClientAuthenticationMethod.CLIENT_SECRET_POST
                        )))
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                        .redirectUris(uris -> uris.addAll(Set.of(
                                "http://localhost:8082/swagger-ui/oauth2-redirect.html",
                                "http://localhost:8081/swagger-ui/oauth2-redirect.html"
                        )))

                        .redirectUri(redirectUri) // añadir angular client
                        .scopes(scopes -> scopes.addAll(Scope.allValues()))
                        .tokenSettings(tokenSettings)
                        .build();

        RegisteredClient apiClient =
                RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId(apiClientId)
                        .clientSecret(passwordEncoder.encode(apiClientSecret))
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .scopes(scopes -> scopes.addAll(Scope.allValues()))
                        .tokenSettings(tokenSettings)
                        .build();

        return new InMemoryRegisteredClientRepository(userClient, apiClient);
    }

    // AUTHORIZATION_CODE
    // 1º- Se inicia: http://localhost:8080/oauth2/authorize?response_type=code&client_id=client-id
    // 2º- Se redirige a la ruta programada, el usuario se logea y se redirije a la url programada
    // http://localhost:8080/login/oauth2/code/cliente-oidc?code=4mnIudIk-YKKyFI3B6L6tztFAP7Xz90fqQ_NbxHE....
    // 3º http://localhost:8080/oauth2/token
    //      Header: Auth Basic cliente-id:client-secret & "Content-Type" = "application/x-www-form-urlencoded"
    //      Body: "grant_type=authorization_code &code=$code"
    // 4º - $token = response.token_access
    // 5º - Para invocar un recurso:
    //      Header: Bearer $Token....

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = generateRsa(); // Generas el par de claves
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    private RSAKey generateRsa() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(issuer) //Emisor
                .build();
    }

}

