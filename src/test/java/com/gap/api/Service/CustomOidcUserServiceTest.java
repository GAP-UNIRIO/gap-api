package com.gap.api.Service;

import com.gap.api.Model.Entity.Role;
import com.gap.api.Model.Entity.User;
import com.gap.api.Repository.RoleRepository;
import com.gap.api.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOidcUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @InjectMocks private CustomOidcUserService service;

    private final Role basicRole = new Role(1L, "ROLE_BASIC");
    private final Role adminRole = new Role(2L, "ROLE_ADMIN");

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "allowedDomains", List.of("edu.unirio.br"));
        ReflectionTestUtils.setField(service, "adminEmails", List.of("chefe@edu.unirio.br"));
    }

    @Test
    @DisplayName("@spec:AC-145 carrega os papéis do banco como autoridades da sessão")
    void enrich_existingUser_loadsRolesAsAuthorities() {
        User existing = user("servidor@edu.unirio.br", adminRole);
        when(userRepository.findByEmail("servidor@edu.unirio.br")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OidcUser result = service.enrich(googleUser("servidor@edu.unirio.br"));

        assertThat(authorities(result)).contains("ROLE_ADMIN");
        assertThat(result.getEmail()).isEqualTo("servidor@edu.unirio.br");
    }

    @Test
    @DisplayName("@spec:AC-146 cria o usuário novo com ROLE_BASIC")
    void enrich_newUser_receivesBasicRole() {
        when(userRepository.findByEmail("novo@edu.unirio.br")).thenReturn(Optional.empty());
        when(roleRepository.findByAuthority("ROLE_BASIC")).thenReturn(Optional.of(basicRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OidcUser result = service.enrich(googleUser("novo@edu.unirio.br"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getRoles()).extracting(Role::getAuthority).containsExactly("ROLE_BASIC");
        assertThat(authorities(result)).contains("ROLE_BASIC").doesNotContain("ROLE_ADMIN");
    }

    @Test
    @DisplayName("@spec:AC-147 e-mail listado na variável de ambiente recebe ROLE_ADMIN")
    void enrich_adminEmail_receivesAdminRole() {
        User existing = user("chefe@edu.unirio.br", basicRole);
        when(userRepository.findByEmail("chefe@edu.unirio.br")).thenReturn(Optional.of(existing));
        when(roleRepository.findByAuthority("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OidcUser result = service.enrich(googleUser("chefe@edu.unirio.br"));

        assertThat(existing.getRoles()).extracting(Role::getAuthority)
                .containsExactlyInAnyOrder("ROLE_BASIC", "ROLE_ADMIN");
        assertThat(authorities(result)).contains("ROLE_ADMIN");
    }

    @Test
    @DisplayName("@spec:AC-147 não duplica ROLE_ADMIN de quem já é administrador")
    void enrich_adminEmailAlreadyAdmin_keepsSingleAdminRole() {
        User existing = user("chefe@edu.unirio.br", adminRole);
        when(userRepository.findByEmail("chefe@edu.unirio.br")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.enrich(googleUser("chefe@edu.unirio.br"));

        assertThat(existing.getRoles()).extracting(Role::getAuthority).containsExactly("ROLE_ADMIN");
        verify(roleRepository, never()).findByAuthority("ROLE_ADMIN");
    }

    @Test
    @DisplayName("enrich recusa domínio de e-mail não permitido")
    void enrich_domainNotAllowed_throws() {
        assertThatThrownBy(() -> service.enrich(googleUser("alguem@gmail.com")))
                .isInstanceOf(OAuth2AuthenticationException.class);
        verify(userRepository, never()).save(any());
    }

    private OidcUser googleUser(String email) {
        OidcIdToken token = OidcIdToken.withTokenValue("token")
                .claim("sub", "google-" + email)
                .claim("email", email)
                .claim("name", "Pessoa Teste")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        return new DefaultOidcUser(List.of(new SimpleGrantedAuthority("OIDC_USER")), token);
    }

    private User user(String email, Role role) {
        User user = new User();
        user.setId(10L);
        user.setEmail(email);
        user.setRoles(new HashSet<>(Set.of(role)));
        return user;
    }

    private List<String> authorities(OidcUser user) {
        return user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    }
}
