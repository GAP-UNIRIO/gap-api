package com.gap.api.Service.Interface;

import com.gap.api.Model.DTO.UserComplementRequest;
import com.gap.api.Model.Entities.User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface IUserService {

    User findByEmail(String email);

    User completeUserCadaster(UserComplementRequest userComplementRequest, OidcUser oidcUser);
}
