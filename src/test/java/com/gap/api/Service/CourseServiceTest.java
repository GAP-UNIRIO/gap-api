package com.gap.api.Service;

import com.gap.api.Model.DTO.CourseRequest;
import com.gap.api.Model.DTO.CourseResponse;
import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.OrgUnit;
import com.gap.api.Model.Entity.Role;
import com.gap.api.Model.Entity.User;
import com.gap.api.Repository.CourseRepository;
import com.gap.api.Repository.OrgUnitRepository;
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
    private static final Long DEPARTMENT_ID = 40L;
    private static final Long REITORIA_ID = 41L;
    private static final Long COORDENACAO_ID = 42L;
    private static final Long DIRECAO_ID = 43L;
    private static final Long SECRETARIA_UNIT_ID = 44L;
    private static final Long ESCOLA_ID = 45L;

    @Mock
    private CourseRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrgUnitRepository orgUnitRepository;

    @InjectMocks
    private CourseService service;

    private User director;
    private User coordinator;
    private User secretary;
    private OrgUnit department;
    private OrgUnit reitoria;
    private OrgUnit coordenacao;
    private OrgUnit direcao;
    private OrgUnit secretariaUnit;
    private OrgUnit escola;

    @BeforeEach
    void setUp() {
        director = userWithRole(DIRECTOR_ID, "diretor@gap.com", "DIRECTOR");
        coordinator = userWithRole(COORDINATOR_ID, "coordenador@gap.com", "COORDINATOR");
        secretary = userWithRole(SECRETARY_ID, "secretaria@gap.com", "SECRETARY");
        department = new OrgUnit();
        department.setId(DEPARTMENT_ID);
        department.setName("Departamento de Computação");
        department.setType(OrgUnit.OrgUnitType.DEPARTAMENTO);
        reitoria = unit(REITORIA_ID, OrgUnit.OrgUnitType.REITORIA, "Reitoria");
        coordenacao = unit(COORDENACAO_ID, OrgUnit.OrgUnitType.COORDENACAO, "Coordenação");
        direcao = unit(DIRECAO_ID, OrgUnit.OrgUnitType.DIRECAO, "Direção");
        secretariaUnit = unit(SECRETARIA_UNIT_ID, OrgUnit.OrgUnitType.SECRETARIA, "Secretaria");
        escola = unit(ESCOLA_ID, OrgUnit.OrgUnitType.ESCOLA, "Escola");
    }

    private User user(Long id, String email) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setName(email);
        return u;
    }

    private User userWithRole(Long id, String email, String authority) {
        User user = user(id, email);
        user.addRole(new Role(null, authority));
        return user;
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
        return new CourseRequest(name, DIRECTOR_ID, COORDINATOR_ID, SECRETARY_ID, DEPARTMENT_ID,
                REITORIA_ID, COORDENACAO_ID, DIRECAO_ID, SECRETARIA_UNIT_ID, ESCOLA_ID);
    }

    private OrgUnit unit(Long id, OrgUnit.OrgUnitType type, String name) {
        OrgUnit unit = new OrgUnit();
        unit.setId(id);
        unit.setType(type);
        unit.setName(name);
        return unit;
    }

    private void stubUserReferences() {
        when(userRepository.findById(DIRECTOR_ID)).thenReturn(Optional.of(director));
        when(userRepository.findById(COORDINATOR_ID)).thenReturn(Optional.of(coordinator));
        when(userRepository.findById(SECRETARY_ID)).thenReturn(Optional.of(secretary));
        when(orgUnitRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(department));
        when(orgUnitRepository.findById(REITORIA_ID)).thenReturn(Optional.of(reitoria));
        when(orgUnitRepository.findById(COORDENACAO_ID)).thenReturn(Optional.of(coordenacao));
        when(orgUnitRepository.findById(DIRECAO_ID)).thenReturn(Optional.of(direcao));
        when(orgUnitRepository.findById(SECRETARIA_UNIT_ID)).thenReturn(Optional.of(secretariaUnit));
        when(orgUnitRepository.findById(ESCOLA_ID)).thenReturn(Optional.of(escola));
    }

    private void assertStatus(Throwable ex, HttpStatus expected) {
        assertThat(ex).isInstanceOfSatisfying(ResponseStatusException.class,
                e -> assertThat(e.getStatusCode()).isEqualTo(expected));
    }

    // ---------- findAll / findById ----------

    @Test
    @DisplayName("@spec:AC-031 findAll retorna os cursos ordenados por id")
    void findAll_returnsMappedList() {
        when(repository.findAll(Sort.by("id"))).thenReturn(List.of(
                course(1L, "Sistemas de Informação"),
                course(2L, "Engenharia")));

        List<CourseResponse> result = service.findAll();

        assertThat(result).extracting(CourseResponse::name)
                .containsExactly("Sistemas de Informação", "Engenharia");
        assertThat(result.get(0).director()).isEqualTo(new CourseResponse.ResponsibleSummary(
                DIRECTOR_ID, "diretor@gap.com", "diretor@gap.com"));
        assertThat(result.get(0).coordinator()).isEqualTo(new CourseResponse.ResponsibleSummary(
                COORDINATOR_ID, "coordenador@gap.com", "coordenador@gap.com"));
        assertThat(result.get(0).secretary()).isEqualTo(new CourseResponse.ResponsibleSummary(
                SECRETARY_ID, "secretaria@gap.com", "secretaria@gap.com"));
    }

    @Test
    @DisplayName("@spec:AC-032 findAll retorna lista vazia quando não há cursos")
    void findAll_empty() {
        when(repository.findAll(Sort.by("id"))).thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
    }

    @Test
    @DisplayName("@spec:AC-033 findById retorna o curso quando existe")
    void findById_found() {
        Course existing = course(1L, "Sistemas de Informação");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThat(service.findById(1L)).isEqualTo(CourseResponse.from(existing));
    }

    @Test
    @DisplayName("@spec:AC-034 findById lança 404 quando não existe")
    void findById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND));
    }

    // ---------- create ----------

    @Test
    @DisplayName("@spec:AC-035 @spec:AC-108 create associa nome, responsáveis e departamento")
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
        assertThat(saved.getDepartment()).isSameAs(department);
        assertThat(saved.getReitoria()).isSameAs(reitoria);
        assertThat(saved.getCoordenacao()).isSameAs(coordenacao);
        assertThat(saved.getDirecao()).isSameAs(direcao);
        assertThat(saved.getSecretaria()).isSameAs(secretariaUnit);
        assertThat(saved.getEscola()).isSameAs(escola);
        assertThat(result.department()).isEqualTo(new CourseResponse.OrgUnitSummary(DEPARTMENT_ID, "Departamento de Computação"));
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Sistemas de Informação");
    }

    @Test
    @DisplayName("@spec:AC-123 @spec:AC-124 create rejeita responsável inexistente ou com papel incompatível sem salvar")
    void create_rejectsMissingOrInvalidResponsible() {
        when(userRepository.findById(COORDINATOR_ID)).thenReturn(Optional.of(coordinator));
        when(userRepository.findById(DIRECTOR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request("Curso")))
                .satisfies(ex -> {
                    assertStatus(ex, HttpStatus.BAD_REQUEST);
                    assertThat(((ResponseStatusException) ex).getReason()).contains("Director", "10");
                });

        User wrongDirector = userWithRole(DIRECTOR_ID, "diretor@gap.com", "COORDINATOR");
        when(userRepository.findById(DIRECTOR_ID)).thenReturn(Optional.of(wrongDirector));
        assertThatThrownBy(() -> service.create(request("Curso")))
                .satisfies(ex -> {
                    assertStatus(ex, HttpStatus.BAD_REQUEST);
                    assertThat(((ResponseStatusException) ex).getReason()).contains("Director", "DIRECTOR");
                });
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("@spec:AC-109 create e update rejeitam unidade inexistente ou que não seja departamento")
    void create_rejectsInvalidDepartment() {
        stubUserReferences();
        OrgUnit otherUnit = new OrgUnit();
        otherUnit.setId(DEPARTMENT_ID);
        otherUnit.setType(OrgUnit.OrgUnitType.COORDENACAO);
        when(orgUnitRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.empty(), Optional.of(otherUnit));

        assertThatThrownBy(() -> service.create(request("Curso")))
                .satisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> service.create(request("Curso")))
                .satisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST));
        when(repository.findById(1L)).thenReturn(Optional.of(course(1L, "Curso atual")));
        assertThatThrownBy(() -> service.update(1L, request("Curso novo")))
                .satisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("@spec:AC-121 create e update rejeitam unidade organizacional com tipo incompatível sem salvar")
    void write_rejectsIncompatibleOrganizationalUnit() {
        when(userRepository.findById(COORDINATOR_ID)).thenReturn(Optional.of(coordinator));
        when(userRepository.findById(DIRECTOR_ID)).thenReturn(Optional.of(director));
        when(userRepository.findById(SECRETARY_ID)).thenReturn(Optional.of(secretary));
        OrgUnit wrongType = unit(REITORIA_ID, OrgUnit.OrgUnitType.ESCOLA, "Unidade errada");
        when(orgUnitRepository.findById(REITORIA_ID)).thenReturn(Optional.of(wrongType));

        assertThatThrownBy(() -> service.create(request("Curso")))
                .satisfies(ex -> {
                    assertStatus(ex, HttpStatus.BAD_REQUEST);
                    assertThat(((ResponseStatusException) ex).getReason())
                            .contains("reitoriaId", "REITORIA", REITORIA_ID.toString());
                });

        when(repository.findById(1L)).thenReturn(Optional.of(course(1L, "Curso atual")));
        assertThatThrownBy(() -> service.update(1L, request("Curso novo")))
                .satisfies(ex -> {
                    assertStatus(ex, HttpStatus.BAD_REQUEST);
                    assertThat(((ResponseStatusException) ex).getReason())
                            .contains("reitoriaId", "REITORIA", REITORIA_ID.toString());
                });
        verify(repository, never()).save(any());
    }

    // ---------- update ----------

    @Test
    @DisplayName("@spec:AC-038 update altera nome e responsáveis do curso existente")
    void update_success() {
        Course existing = course(1L, "Nome antigo");
        User newDirector = user(11L, "novo.diretor@gap.com");
        User newCoordinator = user(21L, "novo.coordenador@gap.com");
        User newSecretary = user(31L, "nova.secretaria@gap.com");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        newDirector.addRole(new Role(null, "DIRECTOR"));
        newCoordinator.addRole(new Role(null, "COORDINATOR"));
        newSecretary.addRole(new Role(null, "SECRETARY"));
        when(userRepository.findById(11L)).thenReturn(Optional.of(newDirector));
        when(userRepository.findById(21L)).thenReturn(Optional.of(newCoordinator));
        when(userRepository.findById(31L)).thenReturn(Optional.of(newSecretary));
        when(orgUnitRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(department));
        when(orgUnitRepository.findById(REITORIA_ID)).thenReturn(Optional.of(reitoria));
        when(orgUnitRepository.findById(COORDENACAO_ID)).thenReturn(Optional.of(coordenacao));
        when(orgUnitRepository.findById(DIRECAO_ID)).thenReturn(Optional.of(direcao));
        when(orgUnitRepository.findById(SECRETARIA_UNIT_ID)).thenReturn(Optional.of(secretariaUnit));
        when(orgUnitRepository.findById(ESCOLA_ID)).thenReturn(Optional.of(escola));
        when(repository.save(existing)).thenReturn(existing);

        CourseResponse result = service.update(1L, new CourseRequest("Nome novo", 11L, 21L, 31L, DEPARTMENT_ID,
                REITORIA_ID, COORDENACAO_ID, DIRECAO_ID, SECRETARIA_UNIT_ID, ESCOLA_ID));

        assertThat(existing.getName()).isEqualTo("Nome novo");
        assertThat(existing.getDirector()).isSameAs(newDirector);
        assertThat(existing.getCoordinator()).isSameAs(newCoordinator);
        assertThat(existing.getSecretary()).isSameAs(newSecretary);
        assertThat(existing.getDepartment()).isSameAs(department);
        assertThat(existing.getReitoria()).isSameAs(reitoria);
        assertThat(existing.getCoordenacao()).isSameAs(coordenacao);
        assertThat(existing.getDirecao()).isSameAs(direcao);
        assertThat(existing.getSecretaria()).isSameAs(secretariaUnit);
        assertThat(existing.getEscola()).isSameAs(escola);
        assertThat(result.name()).isEqualTo("Nome novo");
        assertThat(result.director()).isEqualTo(new CourseResponse.ResponsibleSummary(
                11L, "novo.diretor@gap.com", "novo.diretor@gap.com"));
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("@spec:AC-039 update lança 404 e não salva quando o curso não existe")
    void update_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request("X")))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND));

        verify(repository, never()).save(any());
        verify(userRepository, never()).findById(anyLong());
    }

    // ---------- delete ----------

    @Test
    @DisplayName("@spec:AC-041 delete remove o curso")
    void delete_success() {
        Course existing = course(1L, "Sistemas de Informação");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        InOrder order = inOrder(repository);
        order.verify(repository).delete(existing);
        order.verify(repository).flush();
    }

    @Test
    @DisplayName("@spec:AC-042 delete lança 404 e não remove quando o curso não existe")
    void delete_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND));

        verify(repository, never()).delete(any());
        verify(repository, never()).flush();
    }
}
