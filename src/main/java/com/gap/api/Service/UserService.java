package com.gap.api.Service;

import com.gap.api.Model.DTO.UserComplementRequest;
import com.gap.api.Model.DTO.UserResponse;
import com.gap.api.Model.DTO.UserRolesRequest;
import com.gap.api.Model.Entity.Role;
import com.gap.api.Model.Entity.User;
import com.gap.api.Repository.CourseRepository;
import com.gap.api.Repository.RoleRepository;
import com.gap.api.Repository.UserRepository;
import com.gap.api.Service.Interface.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository repository;
    private final CourseRepository courseRepository;
    private final RoleRepository roleRepository;
    private final String USER_NOT_FOUND_MESSAGE = "Usuário não encontrado: ";
    private final String ROLE_NOT_FOUND_MESSAGE = "Papel não encontrado: ";

    public UserResponse findByEmail(String email) {
        return UserResponse.from(repository.findByEmail(email).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_MESSAGE + email)));
    }

    @Transactional
    public UserResponse completeUserCadaster(UserComplementRequest userComplementRequest, OidcUser oidcUser) {
        User user = repository.findByEmail(oidcUser.getEmail()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_MESSAGE + oidcUser.getEmail()));

        user.setCourse(courseRepository.findById(userComplementRequest.courseId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado: " + userComplementRequest.courseId())));
        user.setRegistrationNumber(userComplementRequest.registrationNumber());
        return UserResponse.from(repository.save(user));
    }

    @Transactional
    public UserResponse updateRoles(Long id, UserRolesRequest request) {
        for (String authority : request.roles()) {
            if (!Role.CORE.contains(authority)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Papel não permitido: " + authority + ". Permitidos: " + Role.BASIC + " e " + Role.ADMIN + ".");
            }
        }
        User user = repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_MESSAGE + id));
        Set<Role> roles = new HashSet<>();
        for (String authority : request.roles()) {
            roles.add(roleRepository.findByAuthority(authority).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, ROLE_NOT_FOUND_MESSAGE + authority)));
        }
        user.setRoles(roles);
        return UserResponse.from(repository.save(user));
    }

}
