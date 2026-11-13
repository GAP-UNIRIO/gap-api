package com.gap.api.Controller;

import com.gap.api.Config.SecurityConfig;
import com.gap.api.Model.DTO.UserResponse;
import com.gap.api.Model.DTO.UserRolesRequest;
import com.gap.api.Repository.UserRepository;
import com.gap.api.Service.CustomOidcUserService;
import com.gap.api.Service.Interface.ICourseService;
import com.gap.api.Service.Interface.IInstitutionService;
import com.gap.api.Service.Interface.IOrderService;
import com.gap.api.Service.Interface.IRoleService;
import com.gap.api.Service.Interface.IUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {InstitutionController.class, CourseController.class, RoleController.class,
        UserController.class, OrderController.class})
@Import(SecurityConfig.class)
class AccessControlTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private IInstitutionService institutionService;
    @MockitoBean private ICourseService courseService;
    @MockitoBean private IRoleService roleService;
    @MockitoBean private IUserService userService;
    @MockitoBean private IOrderService orderService;
    @MockitoBean private CustomOidcUserService customOidcUserService;
    @MockitoBean private UserRepository userRepository;

    @BeforeEach
    void allowCompletedUsersByDefault() {
        when(userRepository.isCadasterCompleted(anyString())).thenReturn(true);
    }

    @Test
    @DisplayName("@spec:AC-149 usuário BASIC não altera cadastros")
    void basicUser_cannotWriteRegistries() throws Exception {
        mockMvc.perform(delete("/institutions/1").with(basic()).with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/courses/1").with(basic()).with(csrf()))
                .andExpect(status().isForbidden());

        verify(institutionService, never()).delete(anyLong());
        verify(courseService, never()).delete(anyLong());
    }

    @Test
    @DisplayName("@spec:AC-150 administrador altera cadastros")
    void adminUser_canWriteRegistries() throws Exception {
        mockMvc.perform(delete("/institutions/1").with(admin()).with(csrf()))
                .andExpect(status().isOk());

        verify(institutionService).delete(1L);
    }

    @Test
    @DisplayName("@spec:AC-151 aplica a matriz de acesso por papel")
    void accessMatrix_isEnforced() throws Exception {
        when(courseService.findAll()).thenReturn(List.of());
        when(roleService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/courses").with(basic())).andExpect(status().isOk());
        mockMvc.perform(get("/roles").with(basic())).andExpect(status().isForbidden());
        mockMvc.perform(get("/roles").with(admin())).andExpect(status().isOk());
        mockMvc.perform(get("/users/me").with(basic())).andExpect(status().isOk());
    }

    @Test
    @DisplayName("@spec:AC-152 acesso negado responde 403 no padrão BaseResponse")
    void accessDenied_returnsBaseResponse() throws Exception {
        mockMvc.perform(delete("/institutions/1").with(basic()).with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value(not(emptyOrNullString())))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("@spec:AC-153 chamada sem login responde 401 no padrão BaseResponse")
    void unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value(not(emptyOrNullString())));
    }

    @Test
    @DisplayName("@spec:AC-155 administrador atribui papéis a um usuário")
    void admin_updatesUserRoles() throws Exception {
        UserResponse response = new UserResponse(5L, "servidor@edu.unirio.br", "Servidor", null, null, false,
                List.of("ROLE_ADMIN"));
        when(userService.updateRoles(eq(5L), any(UserRolesRequest.class))).thenReturn(response);

        mockMvc.perform(put("/users/5/roles").with(admin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserRolesRequest(List.of("ROLE_ADMIN")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("@spec:AC-156 usuário BASIC não atribui papéis")
    void basic_cannotUpdateUserRoles() throws Exception {
        mockMvc.perform(put("/users/5/roles").with(basic()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserRolesRequest(List.of("ROLE_ADMIN")))))
                .andExpect(status().isForbidden());

        verify(userService, never()).updateRoles(anyLong(), any(UserRolesRequest.class));
    }

    @Test
    @DisplayName("@spec:AC-157 atribuição com usuário ou papel inexistente responde 404")
    void admin_updateRolesNotFound_returns404() throws Exception {
        when(userService.updateRoles(eq(99L), any(UserRolesRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado: 99"));

        mockMvc.perform(put("/users/99/roles").with(admin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserRolesRequest(List.of("ROLE_ADMIN")))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário não encontrado: 99"));
    }

    private RequestPostProcessor basic() {
        return oidcLogin().idToken(token -> token.claim("email", "aluno@edu.unirio.br"))
                .authorities(new SimpleGrantedAuthority("ROLE_BASIC"));
    }

    private RequestPostProcessor admin() {
        return oidcLogin().idToken(token -> token.claim("email", "servidor@edu.unirio.br"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
