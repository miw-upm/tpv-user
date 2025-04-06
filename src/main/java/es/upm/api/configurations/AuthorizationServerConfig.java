package es.upm.api.configurations;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.Role;
import es.upm.api.services.exceptions.NotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientCredentialsAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.oidc.web.authentication.OidcLogoutAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Log4j2
@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {  // Generate tokens OAuth2

    private final PasswordEncoder passwordEncoder;
    private final OAuth2Properties oAuth2Properties;
    private final UserRepository userRepository;

    @Autowired
    public AuthorizationServerConfig(PasswordEncoder passwordEncoder, OAuth2Properties oAuth2Properties, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.oAuth2Properties = oAuth2Properties;
        this.userRepository = userRepository;
    }

    // Conversor para el logout que registra los datos recibidos (Pre-procesamiento)
    public static class LoggingLogoutRequestConverter implements AuthenticationConverter {
        private final OidcLogoutAuthenticationConverter delegate = new OidcLogoutAuthenticationConverter();

        @Override
        public Authentication convert(HttpServletRequest request) {
            log.info("Logout Request recibida con parámetros: {}", request.getParameterMap());
            Authentication auth = delegate.convert(request);
            log.info("Logout Request convertida: {}", auth);
            return auth;
        }
    }

    // Handler para logout exitoso que registra la acción (Post-procesamiento)
    // Validador personalizado que puede registrar y validar el logout (Procesamiento principal)
    public static class CustomPostLogoutRedirectUriValidator implements Consumer<OidcLogoutAuthenticationContext> {
        @Override
        public void accept(OidcLogoutAuthenticationContext context) {
            OidcLogoutAuthenticationToken token = context.getAuthentication();
            RegisteredClient registeredClient = context.getRegisteredClient();
            log.info("Validando logout para el cliente: {}", registeredClient.getClientId());
            // Implementa aquí la lógica de validación deseada.
            // Ejemplo:
            // if (!esValido(token.getPostLogoutRedirectUri(), registeredClient)) {
            //     throw new OAuth2AuthenticationException(new OAuth2Error("invalid_request", "Post logout redirect URI inválido", null));
            // }
        }
    }

    // Handler para logout exitoso que registra la acción (Post-procesamiento)
    // Ahora implementa AuthenticationSuccessHandler en lugar de LogoutResponseHandler.
    public static class LoggingLogoutResponseHandler implements AuthenticationSuccessHandler {
        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
                throws IOException, ServletException {
            log.info("Logout exitoso para el usuario: {}", authentication.getName());
            // Invalida la sesión actual
            if (request.getSession(false) != null) {
                request.getSession().invalidate();
                log.info("Sesión HTTP invalidada.");
            }

            // Eliminar cookies de sesión (ejemplo: JSESSIONID)
            Cookie cookie = new Cookie("JSESSIONID", null);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            // Redirige o responde según la necesidad
            response.sendRedirect("http://localhost:4200");
        }
    }

    // Handler para errores en el logout que registra el error (Post-procesamiento)
    public static class LoggingLogoutErrorResponseHandler implements AuthenticationFailureHandler {
        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
                throws IOException, ServletException {
            log.error("Error durante el proceso de logout: {}", exception.getMessage(), exception);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        }
    }


    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();

//        authorizationServerConfigurer.oidc(Customizer.withDefaults()); //.well-known/openid-configuration

        // Configuramos OIDC, personalizando el endpoint de logout
        authorizationServerConfigurer.oidc(oidc ->
                oidc.logoutEndpoint(logoutEndpoint ->
                        logoutEndpoint
                                // Pre-procesamiento: Conversor que registra los parámetros y resultado de la conversión
                                .logoutRequestConverter(new LoggingLogoutRequestConverter())
                                // Procesamiento: Personalización de AuthenticationProviders para incluir nuestro validador
                                .authenticationProviders(providers -> {
                                    providers.forEach(provider -> {
                                        if (provider instanceof OidcLogoutAuthenticationProvider oidcProvider) {
                                            oidcProvider.setAuthenticationValidator(new CustomPostLogoutRedirectUriValidator());
                                            log.info("Validador personalizado asignado al provider: {}", oidcProvider);
                                        }
                                    });
                                })
                                // Post-procesamiento: Handlers para logout exitoso y manejo de error
                                .logoutResponseHandler(new LoggingLogoutResponseHandler())
                                .errorResponseHandler(new LoggingLogoutErrorResponseHandler())
                )
        );



























        return http
//                .csrf(AbstractHttpConfigurer::disable)
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

        RegisteredClient spaClient =
                RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId(this.oAuth2Properties.getSpaClientId())
                        .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                        .redirectUris(uris -> uris.addAll(this.oAuth2Properties.getRedirectUris()))
                        .scopes(scopes -> scopes.addAll(Scope.allValues()))
                        .tokenSettings(tokenSettings)
                        .clientSettings(ClientSettings.builder()
                                .requireAuthorizationConsent(false)
                                .build())
                        .build();

        RegisteredClient userClient =
                RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId(this.oAuth2Properties.getClientId())
                        .clientSecret(passwordEncoder.encode(this.oAuth2Properties.getClientSecret()))
                        .clientAuthenticationMethods(methods -> methods.addAll(Set.of(
                                ClientAuthenticationMethod.CLIENT_SECRET_BASIC,
                                ClientAuthenticationMethod.CLIENT_SECRET_POST
                        )))
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                        .redirectUris(uris -> uris.addAll(this.oAuth2Properties.getRedirectUris()))
                        .scopes(scopes -> scopes.addAll(Scope.allValues()))
                        .tokenSettings(tokenSettings)
                        .build();

        RegisteredClient apiClient =
                RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId(this.oAuth2Properties.getApiClientId())
                        .clientSecret(passwordEncoder.encode(this.oAuth2Properties.getApiClientSecret()))
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .scopes(scopes -> scopes.addAll(Scope.allValues()))
                        .tokenSettings(tokenSettings)
                        .build();

        return new InMemoryRegisteredClientRepository(userClient, apiClient, spaClient);
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
                .issuer(this.oAuth2Properties.getIssuer()) //Emisor
                .build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizerRoleByScope() {
        return context -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())
                    || "id_token".equals(context.getTokenType().getValue())
            ) {
                Set<String> roles = new HashSet<>();
                if (context.getPrincipal() != null
                        && context.getPrincipal().getAuthorities() != null
                        && !context.getPrincipal().getAuthorities().isEmpty()) {
                    roles.addAll(
                            context.getPrincipal().getAuthorities().stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .map(Role::of)
                                    .map(Role::value)
                                    .collect(Collectors.toSet())
                    ); //Scope of user
                    String mobile = context.getPrincipal().getName();
                    context.getClaims().claim("name", this.userRepository.findByMobile(mobile)
                            .orElseThrow(() -> new NotFoundException("Mobile not found: " + mobile)).getFirstName());
                } else if (context.getAuthorizationGrant() instanceof OAuth2ClientCredentialsAuthenticationToken clientCredentialsToken) {
                    String role = (String) clientCredentialsToken.getAdditionalParameters().get("role");
                    roles.add(role);
                }
                context.getClaims().claim("roles", String.join(" ", roles));
            }
        };
    }

}
