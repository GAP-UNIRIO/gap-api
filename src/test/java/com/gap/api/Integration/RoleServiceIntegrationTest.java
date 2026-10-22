package com.gap.api.Integration;

import com.gap.api.Model.Entity.Role;
import com.gap.api.Model.Entity.User;
import com.gap.api.Repository.RoleRepository;
import com.gap.api.Repository.UserRepository;
import com.gap.api.Service.Interface.IRoleService;
import com.gap.api.Service.Interface.IStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class RoleServiceIntegrationTest {

    @Autowired
    private IRoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private IStorageService storageService;

    @Test
    @DisplayName("@spec:AC-092 banco impede excluir papel associado a usuário")
    void delete_roleAssignedToUser_isRejectedByDatabase() {
        String suffix = UUID.randomUUID().toString();
        Role role = new Role();
        role.setAuthority("ROLE_TEST_" + suffix);
        role = roleRepository.saveAndFlush(role);

        User user = new User();
        user.setEmail("role-test-" + suffix + "@example.test");
        user.setName("Role integration test");
        user.addRole(role);
        user = userRepository.saveAndFlush(user);

        Long roleId = role.getId();
        Long userId = user.getId();
        try {
            assertThrows(DataIntegrityViolationException.class, () -> roleService.delete(roleId));
        } finally {
            userRepository.deleteById(userId);
            userRepository.flush();
            roleRepository.deleteById(roleId);
            roleRepository.flush();
        }
    }
}
