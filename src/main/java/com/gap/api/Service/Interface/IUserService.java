package com.gap.api.Service.Interface;

import com.gap.api.Model.DTO.UserComplementRequest;
import com.gap.api.Model.DTO.UserResponse;
import com.gap.api.Model.Entity.User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface IUserService {

    UserResponse findByEmail(String email);

    UserResponse completeUserCadaster(UserComplementRequest userComplementRequest, OidcUser oidcUser);
}
