package com.gap.api.Service;

import com.gap.api.Model.DTO.UserComplementRequest;
import com.gap.api.Model.DTO.UserRolesRequest;
import com.gap.api.Model.DTO.UserResponse;
import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.Role;
import com.gap.api.Model.Entity.User;
import com.gap.api.Repository.CourseRepository;
import com.gap.api.Repository.RoleRepository;
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    private RoleRepository roleRepository;

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

    // ---------- papéis ----------

    @Test
    @DisplayName("@spec:AC-148 findByEmail deve retornar os papéis do usuário")
    void findByEmail_returnsRoles() {
        User existing = user(EMAIL);
        existing.setRoles(new HashSet<>(Set.of(new Role(2L, "ROLE_ADMIN"), new Role(1L, "ROLE_BASIC"))));
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));

        assertThat(service.findByEmail(EMAIL).roles()).containsExactly("ROLE_ADMIN", "ROLE_BASIC");
    }

    @Test
    @DisplayName("@spec:AC-155 updateRoles deve substituir os papéis do usuário")
    void updateRoles_replacesRoles() {
        User existing = user(EMAIL);
        existing.setRoles(new HashSet<>(Set.of(new Role(1L, "ROLE_BASIC"))));
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(roleRepository.findByAuthority("ROLE_ADMIN")).thenReturn(Optional.of(new Role(2L, "ROLE_ADMIN")));
        when(repository.save(existing)).thenReturn(existing);

        UserResponse response = service.updateRoles(1L, new UserRolesRequest(List.of("ROLE_ADMIN")));

        assertThat(response.roles()).containsExactly("ROLE_ADMIN");
        assertThat(existing.getRoles()).extracting(Role::getAuthority).containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("@spec:AC-157 updateRoles deve lançar 404 quando o usuário não existe")
    void updateRoles_userNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateRoles(99L, new UserRolesRequest(List.of("ROLE_ADMIN"))))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("@spec:AC-157 updateRoles deve lançar 404 e manter os papéis quando o papel não existe")
    void updateRoles_roleNotFound() {
        User existing = user(EMAIL);
        existing.setRoles(new HashSet<>(Set.of(new Role(1L, "ROLE_BASIC"))));
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(roleRepository.findByAuthority("ROLE_INEXISTENTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateRoles(1L, new UserRolesRequest(List.of("ROLE_INEXISTENTE"))))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND));
        assertThat(existing.getRoles()).extracting(Role::getAuthority).containsExactly("ROLE_BASIC");
        verify(repository, never()).save(any());
    }

    // ---------- findByEmail ----------

    @Test
    @DisplayName("@spec:AC-061 findByEmail deve retornar o usuário quando existe")
    void findByEmail_found() {
        User existing = user(EMAIL);
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));

        assertThat(service.findByEmail(EMAIL)).isEqualTo(UserResponse.from(existing));
    }

    @Test
    @DisplayName("@spec:AC-062 findByEmail deve lançar 404 quando não existe")
    void findByEmail_notFound() {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByEmail(EMAIL))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND))
                .hasMessageContaining(EMAIL);
    }

    // ---------- completeUserCadaster ----------

    @Test
    @DisplayName("@spec:AC-064 @spec:AC-067 completeUserCadaster atualiza a conta localizada pelo principal OIDC")
    void completeUserCadaster_success() {
        User existing = user(EMAIL);
        Course course = course(5L);
        when(oidcUser.getEmail()).thenReturn(EMAIL);
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
        when(courseRepository.findById(5L)).thenReturn(Optional.of(course));
        when(repository.save(existing)).thenReturn(existing);

        UserResponse result = service.completeUserCadaster(new UserComplementRequest("20260001", 5L), oidcUser);
        UserResponse expected = UserResponse.from(existing);
        assertThat(result).isEqualTo(expected);
        assertThat(result.courseId()).isSameAs(course.getId());
        assertThat(result.registrationNumber()).isEqualTo("20260001");
        assertThat(result.isCompletedCadaster()).isTrue();
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("@spec:AC-065 completeUserCadaster deve lançar 404 quando o usuário não existe")
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
    @DisplayName("@spec:AC-066 completeUserCadaster deve lançar 404 quando o curso não existe")
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

    @Test
    @DisplayName("@spec:AC-063 cadastro completo exige matrícula não vazia e curso")
    void completedCadaster_requiresRegistrationAndCourse() {
        User existing = user(EMAIL);
        assertThat(UserResponse.from(existing).isCompletedCadaster()).isFalse();

        existing.setRegistrationNumber("20260001");
        assertThat(UserResponse.from(existing).isCompletedCadaster()).isFalse();

        existing.setCourse(course(5L));
        assertThat(UserResponse.from(existing).isCompletedCadaster()).isTrue();

        existing.setRegistrationNumber("");
        assertThat(UserResponse.from(existing).isCompletedCadaster()).isFalse();
    }
}
