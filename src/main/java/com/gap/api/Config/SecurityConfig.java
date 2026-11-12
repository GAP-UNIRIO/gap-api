package com.gap.api.Config;

import com.gap.api.Service.CustomOidcUserService;
import com.gap.api.Model.DTO.BaseResponse;
import com.gap.api.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.security.frontend-url}")
    private String frontendUrl;

    private final CustomOidcUserService customOidcUserService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public SecurityConfig(CustomOidcUserService customOidcUserService, UserRepository userRepository,
                          ObjectMapper objectMapper) {
        this.customOidcUserService = customOidcUserService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
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
                        .requestMatchers("/users/me", "/users/me/complemento").authenticated()
                        .anyRequest().access((authentication, context) -> {
                            Authentication current = authentication.get();
                            if (current == null || !current.isAuthenticated()
                                    || !(current.getPrincipal() instanceof OidcUser oidcUser)) {
                                return new AuthorizationDecision(false);
                            }
                            String email = oidcUser.getEmail();
                            return new AuthorizationDecision(email != null
                                    && userRepository.isCadasterCompleted(email));
                        })
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            objectMapper.writeValue(response.getOutputStream(),
                                    BaseResponse.error("Complete seu cadastro de usuário para acessar este recurso."));
                        })
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(frontendUrl));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
