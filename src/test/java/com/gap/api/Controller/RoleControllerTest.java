package com.gap.api.Controller;

import com.gap.api.Config.SecurityConfig;
import com.gap.api.Model.Entity.Role;
import com.gap.api.Repository.UserRepository;
import com.gap.api.Service.CustomOidcUserService;
import com.gap.api.Service.Interface.ICourseService;
import com.gap.api.Service.Interface.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private IUserService userService;

    @MockitoBean
    private ICourseService courseService;

    @MockitoBean
    private CustomOidcUserService customOidcUserService;

    @BeforeEach
    void allowCompletedUser() {
        when(userRepository.isCadasterCompleted(anyString())).thenReturn(true);
    }

    @Test
    @DisplayName("@spec:AC-175 todas as rotas de administração de roles respondem 404")
    void roleCrudRoutes_areNotMapped() throws Exception {
        var admin = oidcLogin()
                .idToken(token -> token.claim("email", "admin@example.test"))
                .authorities(new SimpleGrantedAuthority(Role.ADMIN));

        mockMvc.perform(get("/roles").with(admin)).andExpect(status().isNotFound());
        mockMvc.perform(get("/roles/1").with(admin)).andExpect(status().isNotFound());
        mockMvc.perform(post("/roles").with(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authority\":\"ROLE_CUSTOM\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/roles/1").with(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authority\":\"ROLE_CUSTOM\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/roles/1").with(admin).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
