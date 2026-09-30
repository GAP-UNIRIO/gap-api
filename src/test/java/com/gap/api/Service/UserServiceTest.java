package com.gap.api.Service;

import com.gap.api.Model.DTO.UserComplementRequest;
import com.gap.api.Model.Entities.Course;
import com.gap.api.Model.Entities.User;
import com.gap.api.Repository.CourseRepository;
import com.gap.api.Repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String EMAIL = "aluno@gap.com";

    @Mock
    private UserRepository repository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private OidcUser oidcUser;

    @InjectMocks
    private UserService service;

    private User user(String email) {
        User u = new User();
        u.setId(1L);
        u.setEmail(email);
        u.setName("Aluno");
        return u;
    }

    private Course course(Long id) {
        Course c = new Course();
        c.setId(id);
        c.setName("Sistemas de Informação");
        return c;
    }

    private void assertStatus(Throwable ex, HttpStatus expected) {
        assertThat(ex).isInstanceOfSatisfying(ResponseStatusException.class,
                e -> assertThat(e.getStatusCode()).isEqualTo(expected));
    }

    // ---------- findByEmail ----------

    @Test
    @DisplayName("findByEmail deve retornar o usuário quando existe")
    void findByEmail_found() {
        User existing = user(EMAIL);
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));

        assertThat(service.findByEmail(EMAIL)).isSameAs(existing);
    }

    @Test
    @DisplayName("findByEmail deve lançar 404 quando não existe")
    void findByEmail_notFound() {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByEmail(EMAIL))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND))
                .hasMessageContaining(EMAIL);
    }

    // ---------- completeUserCadaster ----------

    @Test
    @DisplayName("completeUserCadaster deve preencher curso e matrícula e salvar")
    void completeUserCadaster_success() {
        User existing = user(EMAIL);
        Course course = course(5L);
        when(oidcUser.getEmail()).thenReturn(EMAIL);
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
        when(courseRepository.findById(5L)).thenReturn(Optional.of(course));
        when(repository.save(existing)).thenReturn(existing);

        User result = service.completeUserCadaster(new UserComplementRequest("20260001", 5L), oidcUser);

        assertThat(result).isSameAs(existing);
        assertThat(result.getCourse()).isSameAs(course);
        assertThat(result.getRegistrationNumber()).isEqualTo("20260001");
        assertThat(result.isCompletedCadaster()).isTrue();
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("completeUserCadaster deve lançar 404 quando o usuário não existe")
    void completeUserCadaster_userNotFound() {
        when(oidcUser.getEmail()).thenReturn(EMAIL);
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.completeUserCadaster(new UserComplementRequest("20260001", 5L), oidcUser))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND));

        verifyNoInteractions(courseRepository);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("completeUserCadaster deve lançar 404 quando o curso não existe")
    void completeUserCadaster_courseNotFound() {
        User existing = user(EMAIL);
        when(oidcUser.getEmail()).thenReturn(EMAIL);
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.completeUserCadaster(new UserComplementRequest("20260001", 99L), oidcUser))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND))
                .hasMessageContaining("99");

        assertThat(existing.getCourse()).isNull();
        assertThat(existing.getRegistrationNumber()).isNull();
        verify(repository, never()).save(any());
    }
}
