package com.gap.api.Service;

import com.gap.api.Model.DTO.CourseRequest;
import com.gap.api.Model.DTO.CourseResponse;
import com.gap.api.Model.Entities.Course;
import com.gap.api.Model.Entities.User;
import com.gap.api.Repository.CourseRepository;
import com.gap.api.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    private static final Long DIRECTOR_ID = 10L;
    private static final Long COORDINATOR_ID = 20L;
    private static final Long SECRETARY_ID = 30L;

    @Mock
    private CourseRepository repository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CourseService service;

    private User director;
    private User coordinator;
    private User secretary;

    @BeforeEach
    void setUp() {
        director = user(DIRECTOR_ID, "diretor@gap.com");
        coordinator = user(COORDINATOR_ID, "coordenador@gap.com");
        secretary = user(SECRETARY_ID, "secretaria@gap.com");
    }

    private User user(Long id, String email) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setName(email);
        return u;
    }

    private Course course(Long id, String name) {
        Course c = new Course();
        c.setId(id);
        c.setName(name);
        c.setDirector(director);
        c.setCoordinator(coordinator);
        c.setSecretary(secretary);
        return c;
    }

    private CourseRequest request(String name) {
        return new CourseRequest(name, DIRECTOR_ID, COORDINATOR_ID, SECRETARY_ID);
    }

    private void stubUserReferences() {
        when(userRepository.getReferenceById(DIRECTOR_ID)).thenReturn(director);
        when(userRepository.getReferenceById(COORDINATOR_ID)).thenReturn(coordinator);
        when(userRepository.getReferenceById(SECRETARY_ID)).thenReturn(secretary);
    }

    private void assertStatus(Throwable ex, HttpStatus expected) {
        assertThat(ex).isInstanceOfSatisfying(ResponseStatusException.class,
                e -> assertThat(e.getStatusCode()).isEqualTo(expected));
    }

    // ---------- findAll / findById ----------

    @Test
    @DisplayName("findAll deve retornar os cursos ordenados por id")
    void findAll_returnsMappedList() {
        when(repository.findAll(Sort.by("id"))).thenReturn(List.of(
                course(1L, "Sistemas de Informação"),
                course(2L, "Engenharia")));

        List<CourseResponse> result = service.findAll();

        assertThat(result).extracting(CourseResponse::name)
                .containsExactly("Sistemas de Informação", "Engenharia");
        assertThat(result.get(0).director()).isSameAs(director);
        assertThat(result.get(0).coordinator()).isSameAs(coordinator);
        assertThat(result.get(0).secretary()).isSameAs(secretary);
    }

    @Test
    @DisplayName("findAll deve retornar lista vazia quando não há cursos")
    void findAll_empty() {
        when(repository.findAll(Sort.by("id"))).thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
    }

    @Test
    @DisplayName("findById deve retornar a entidade Course quando existe")
    void findById_found() {
        Course existing = course(1L, "Sistemas de Informação");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThat(service.findById(1L)).isSameAs(existing);
    }

    @Test
    @DisplayName("findById deve lançar 404 quando não existe")
    void findById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND));
    }

    // ---------- create ----------

    @Test
    @DisplayName("create deve montar o curso com nome, diretor, coordenador e secretário")
    void create_success() {
        stubUserReferences();
        when(repository.save(any(Course.class))).thenAnswer(inv -> {
            Course c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CourseResponse result = service.create(request("Sistemas de Informação"));

        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(repository).save(captor.capture());
        Course saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Sistemas de Informação");
        assertThat(saved.getDirector()).isSameAs(director);
        assertThat(saved.getCoordinator()).isSameAs(coordinator);
        assertThat(saved.getSecretary()).isSameAs(secretary);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Sistemas de Informação");
    }

    // ---------- update ----------

    @Test
    @DisplayName("update deve alterar nome e responsáveis do curso existente")
    void update_success() {
        Course existing = course(1L, "Nome antigo");
        User newDirector = user(11L, "novo.diretor@gap.com");
        User newCoordinator = user(21L, "novo.coordenador@gap.com");
        User newSecretary = user(31L, "nova.secretaria@gap.com");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.getReferenceById(11L)).thenReturn(newDirector);
        when(userRepository.getReferenceById(21L)).thenReturn(newCoordinator);
        when(userRepository.getReferenceById(31L)).thenReturn(newSecretary);
        when(repository.save(existing)).thenReturn(existing);

        CourseResponse result = service.update(1L, new CourseRequest("Nome novo", 11L, 21L, 31L));

        assertThat(existing.getName()).isEqualTo("Nome novo");
        assertThat(existing.getDirector()).isSameAs(newDirector);
        assertThat(existing.getCoordinator()).isSameAs(newCoordinator);
        assertThat(existing.getSecretary()).isSameAs(newSecretary);
        assertThat(result.name()).isEqualTo("Nome novo");
        assertThat(result.director()).isSameAs(newDirector);
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("update deve lançar 404 e não salvar quando o curso não existe")
    void update_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request("X")))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND));

        verify(repository, never()).save(any());
        verify(userRepository, never()).getReferenceById(anyLong());
    }

    // ---------- delete ----------

    @Test
    @DisplayName("delete deve remover e dar flush, nessa ordem")
    void delete_success() {
        Course existing = course(1L, "Sistemas de Informação");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        InOrder order = inOrder(repository);
        order.verify(repository).delete(existing);
        order.verify(repository).flush();
    }

    @Test
    @DisplayName("delete deve lançar 404 e não remover quando o curso não existe")
    void delete_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND));

        verify(repository, never()).delete(any());
        verify(repository, never()).flush();
    }
}
