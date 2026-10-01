package com.gap.api.Service;

import com.gap.api.Model.DTO.UserComplementRequest;
import com.gap.api.Model.Entity.User;
import com.gap.api.Repository.CourseRepository;
import com.gap.api.Repository.UserRepository;
import com.gap.api.Service.Interface.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository repository;
    private final CourseRepository courseRepository;

    public User findByEmail(String email) {
        return repository.findByEmail(email).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado: " + email));
    }

    @Transactional
    public User completeUserCadaster(UserComplementRequest userComplementRequest, OidcUser oidcUser) {
        User user = findByEmail(oidcUser.getEmail());

        user.setCourse(courseRepository.findById(userComplementRequest.courseId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado: " + userComplementRequest.courseId())));
        user.setRegistrationNumber(userComplementRequest.registrarionNumber());
        return repository.save(user);
    }

    private User getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Papel não encontrado: " + id));
    }

}
