package com.gap.api.Config;

import com.gap.api.Model.DTO.BaseResponse;
import com.gap.api.Model.Entity.Role;
import com.gap.api.Service.CustomOidcUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Cadastros: leitura para qualquer usuário logado, escrita só para administrador. */
    private static final String[] REGISTRY_PATHS = {
            "/institutions/**", "/org-units/**", "/courses/**", "/academic-calendars/**"};
    /** Endpoints exclusivos do administrador, inclusive para leitura. */
    private static final String[] ADMIN_ONLY_PATHS = {
            "/roles/**", "/workflow-template/**", "/users/*/roles"};
    private static final ObjectMapper JSON = new ObjectMapper();

    @Value("${app.security.frontend-url}")
    private String frontendUrl;

    private final CustomOidcUserService customOidcUserService;

    public SecurityConfig(CustomOidcUserService customOidcUserService) {
        this.customOidcUserService = customOidcUserService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(requestHandler)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**").permitAll() // Rotas abertas, se houver
                        .requestMatchers("/doc", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll() // Documentação (Swagger)
                        .requestMatchers(HttpMethod.GET, REGISTRY_PATHS).authenticated()
                        .requestMatchers(REGISTRY_PATHS).hasAuthority(Role.ADMIN)
                        .requestMatchers(ADMIN_ONLY_PATHS).hasAuthority(Role.ADMIN)
                        .anyRequest().authenticated() // Restante exige login
                )

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, failure) -> writeError(response,
                                HttpStatus.UNAUTHORIZED, "É necessário fazer login para acessar este recurso."))
                        .accessDeniedHandler((request, response, failure) -> writeError(response,
                                HttpStatus.FORBIDDEN, failure instanceof CsrfException
                                        ? "Token CSRF ausente ou inválido. Recarregue a página e tente novamente."
                                        : "Você não tem permissão para acessar este recurso."))
                )

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService) // Nossa validação de domínio
                        )
                        .successHandler(new SimpleUrlAuthenticationSuccessHandler(frontendUrl + "/home"))
                        .failureHandler(new SimpleUrlAuthenticationFailureHandler(frontendUrl + "/login?error=unauthorized"))
                )

                .logout(logout -> logout
                        .logoutSuccessUrl(frontendUrl + "/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                );

        return http.build();
    }

    private static void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JSON.writeValueAsString(BaseResponse.error(message)));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(frontendUrl));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}