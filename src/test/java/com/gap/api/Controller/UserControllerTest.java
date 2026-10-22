package com.gap.api.Controller;

import com.gap.api.Config.SecurityConfig;
import com.gap.api.Model.DTO.UserComplementRequest;
import com.gap.api.Model.DTO.UserResponse;
import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.User;
import com.gap.api.Service.CustomOidcUserService;
import com.gap.api.Service.Interface.ICourseService;
import com.gap.api.Service.Interface.IUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IUserService userService;

    @MockitoBean
    private ICourseService courseService;

    @MockitoBean
    private CustomOidcUserService customOidcUserService;

    // Métodos auxiliares
    private User userMock(Long id, String email, String name) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName(name);
        return user;
    }

    private UserResponse userResponseMock(Long id, String email, String name, boolean active) {
        return new UserResponse(id, email, name, null, null, active);
    }

    private UserComplementRequest complementRequest(String registrationNumber, Long courseId) {
        return new UserComplementRequest(registrationNumber, courseId);
    }

    // ---------- GET /users/me ----------

    @Test
    @DisplayName("@spec:AC-061 GET /users/me retorna dados do usuário autenticado")
    void getLoggedUser_returnsUser() throws Exception {
        UserResponse user = userResponseMock(1L, "aluno@edu.unirio.br", "Aluno Silva", false);

        when(userService.findByEmail("aluno@edu.unirio.br")).thenReturn(user);

        mockMvc.perform(get("/users/me")
                        .with(oidcLogin().idToken(token -> token.claim("email", "aluno@edu.unirio.br"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.email").value("aluno@edu.unirio.br"))
                .andExpect(jsonPath("$.data.name").value("Aluno Silva"));
    }

    @Test
    @DisplayName("@spec:AC-062 GET /users/me retorna 404 para conta não cadastrada")
    void getLoggedUser_notFound() throws Exception {
        when(userService.findByEmail("aluno@edu.unirio.br"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mockMvc.perform(get("/users/me")
                        .with(oidcLogin().idToken(token -> token.claim("email", "aluno@edu.unirio.br"))))
                .andExpect(status().isNotFound());
    }

    // ---------- PUT /users/me/complemento ----------

    @Test
    @DisplayName("@spec:AC-064 @spec:AC-067 PUT /users/me/complemento completa o cadastro da identidade autenticada")
    void complementUser_success() throws Exception {
        UserComplementRequest request = complementRequest("20260001", 10L);

        Course course = new Course();
        course.setId(10L);

        User updatedUser = userMock(1L, "aluno@edu.unirio.br", "Aluno Silva");
        updatedUser.setRegistrationNumber("20260001");
        updatedUser.setCourse(course);
        UserResponse updatedUserResponse = UserResponse.from(updatedUser);

        when(userService.completeUserCadaster(any(UserComplementRequest.class), any())).thenReturn(updatedUserResponse);

        mockMvc.perform(put("/users/me/complemento")
                        .with(oidcLogin().idToken(token -> token.claim("email", "aluno@edu.unirio.br")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.registrationNumber").value("20260001"))
                .andExpect(jsonPath("$.data.courseId").value(10L))
                .andExpect(jsonPath("$.data.isCompletedCadaster").value(true));
    }

    @Test
    @DisplayName("@spec:AC-065 PUT /users/me/complemento retorna 404 se a conta não existe")
    void complementUser_userNotFound() throws Exception {
        when(userService.completeUserCadaster(any(UserComplementRequest.class), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mockMvc.perform(put("/users/me/complemento")
                        .with(oidcLogin().idToken(token -> token.claim("email", "aluno@edu.unirio.br")))
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(complementRequest("20260001", 10L))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("@spec:AC-066 PUT /users/me/complemento retorna 404 se o curso não existe")
    void complementUser_courseNotFound() throws Exception {
        when(userService.completeUserCadaster(any(UserComplementRequest.class), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mockMvc.perform(put("/users/me/complemento")
                        .with(oidcLogin().idToken(token -> token.claim("email", "aluno@edu.unirio.br")))
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(complementRequest("20260001", 99L))))
                .andExpect(status().isNotFound());
    }
}
