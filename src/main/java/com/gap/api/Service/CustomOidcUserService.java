package com.gap.api.Service;

import com.gap.api.Model.Entity.User;
import com.gap.api.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    @Value("${app.security.allowed-domains}")
    private List<String> allowedDomains;

    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

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

        String name = oidcUser.getFullName();

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            return newUser;
        });

        user.setName(name);
        userRepository.save(user);

        return oidcUser;
    }

}
