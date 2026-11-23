package com.gap.api.Config;

import com.gap.api.Repository.UserRepository;
import com.gap.api.Service.CustomOidcUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

    private UserRepository userRepository;
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        securityConfig = new SecurityConfig(mock(CustomOidcUserService.class), userRepository);
    }

    @Test
    @DisplayName("cadastro não está completo sem autenticação")
    void isRegistrationComplete_nullAuthentication_returnsFalse() {
        assertFalse(securityConfig.isRegistrationComplete(null));

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("cadastro não está completo para autenticação não validada")
    void isRegistrationComplete_unauthenticated_returnsFalse() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        assertFalse(securityConfig.isRegistrationComplete(authentication));

        verify(authentication).isAuthenticated();
        verify(authentication, never()).getPrincipal();
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("cadastro não está completo para principal que não é OIDC")
    void isRegistrationComplete_nonOidcPrincipal_returnsFalse() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        assertFalse(securityConfig.isRegistrationComplete(authentication));

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("cadastro não está completo quando o usuário OIDC não tem e-mail")
    void isRegistrationComplete_oidcWithoutEmail_returnsFalse() {
        Authentication authentication = mock(Authentication.class);
        OidcUser oidcUser = mock(OidcUser.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(oidcUser);

        assertFalse(securityConfig.isRegistrationComplete(authentication));

        verify(userRepository, never()).isCadasterCompleted(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("cadastro está completo quando o repositório confirma os dados")
    void isRegistrationComplete_completedRegistration_returnsTrue() {
        Authentication authentication = mock(Authentication.class);
        OidcUser oidcUser = mock(OidcUser.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getEmail()).thenReturn("complete@example.test");
        when(userRepository.isCadasterCompleted("complete@example.test")).thenReturn(true);

        assertTrue(securityConfig.isRegistrationComplete(authentication));

        verify(userRepository).isCadasterCompleted("complete@example.test");
    }

    @Test
    @DisplayName("cadastro permanece incompleto quando faltam dados no repositório")
    void isRegistrationComplete_incompleteRegistration_returnsFalse() {
        Authentication authentication = mock(Authentication.class);
        OidcUser oidcUser = mock(OidcUser.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getEmail()).thenReturn("incomplete@example.test");

        assertFalse(securityConfig.isRegistrationComplete(authentication));

        verify(userRepository).isCadasterCompleted("incomplete@example.test");
    }
}
