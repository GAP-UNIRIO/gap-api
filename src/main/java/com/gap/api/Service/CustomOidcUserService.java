package com.gap.api.Service;

import com.gap.api.Model.Entity.Role;
import com.gap.api.Model.Entity.User;
import com.gap.api.Repository.RoleRepository;
import com.gap.api.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOidcUserService.class);

    @Value("${app.security.allowed-domains}")
    private List<String> allowedDomains;

    @Value("${app.security.admin-emails:}")
    private List<String> adminEmails;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        return enrich(super.loadUser(userRequest));
    }

    /**
     * Valida o domínio do e-mail, cria ou atualiza o usuário local e devolve o usuário do Google
     * com os papéis do banco como autoridades da sessão.
     */
    OidcUser enrich(OidcUser oidcUser) {
        String email = oidcUser.getEmail();
        String domain = oidcUser.getIdToken().getClaimAsString("hd");
        if (domain == null || domain.isEmpty()) {
            if (email != null && email.contains("@")) {
                domain = email.substring(email.indexOf("@") + 1);
            }
        }

        if (domain == null || !allowedDomains.contains(domain)) {
            OAuth2Error error = new OAuth2Error("invalid_domain", "Email domain:" + domain + "not allowed", null);
            throw new OAuth2AuthenticationException(error);
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.addRole(coreRole(Role.BASIC));
            log.info("Novo usuário {} criado com o papel {}", email, Role.BASIC);
            return newUser;
        });
        user.setName(oidcUser.getFullName());

        if (isAdminEmail(email) && !hasRole(user, Role.ADMIN)) {
            user.addRole(coreRole(Role.ADMIN));
            log.info("Usuário {} recebeu {} pela variável de ambiente de administradores", email, Role.ADMIN);
        }

        user = userRepository.save(user);

        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());
        user.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getAuthority())));
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }

    private boolean isAdminEmail(String email) {
        return adminEmails != null && adminEmails.stream()
                .map(String::trim)
                .anyMatch(admin -> !admin.isEmpty() && admin.equalsIgnoreCase(email));
    }

    private boolean hasRole(User user, String authority) {
        return user.getRoles().stream().anyMatch(role -> authority.equals(role.getAuthority()));
    }

    private Role coreRole(String authority) {
        return roleRepository.findByAuthority(authority)
                .orElseThrow(() -> new IllegalStateException("Papel do núcleo não cadastrado no banco: " + authority));
    }
}
