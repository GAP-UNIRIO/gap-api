package com.gap.api.Controller;

import com.gap.api.Model.DTO.BaseResponse;
import com.gap.api.Model.DTO.UserComplementRequest;
import com.gap.api.Model.DTO.UserResponse;
import com.gap.api.Service.Interface.ICourseService;
import com.gap.api.Service.Interface.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final ICourseService courseService;

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserResponse>> getLoggedUser(@AuthenticationPrincipal OidcUser oidcUser) {
        if(oidcUser == null) {
            throw new RuntimeException("Usuário não autenticado.");
        }
        return ResponseEntity.ok(BaseResponse.success("Consulta realizada com sucesso.", userService.findByEmail(oidcUser.getEmail())));
    }

    @PutMapping("/me/complemento")
    public ResponseEntity<BaseResponse<UserResponse>> complementUser(@RequestBody UserComplementRequest userComplementRequest, @AuthenticationPrincipal OidcUser oidcUser) {
        return ResponseEntity.ok(BaseResponse.success("User cadaster completed successfully" , userService.completeUserCadaster(userComplementRequest, oidcUser)));
    }
}
