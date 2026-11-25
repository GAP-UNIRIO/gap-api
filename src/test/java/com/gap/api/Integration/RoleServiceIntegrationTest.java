package com.gap.api.Integration;

import com.gap.api.Model.Entity.Role;
import com.gap.api.Model.Entity.User;
import com.gap.api.Repository.RoleRepository;
import com.gap.api.Repository.UserRepository;
import com.gap.api.Service.Interface.IStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Import(PostgresTestContainerConfiguration.class)
class RoleServiceIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private IStorageService storageService;

    @Test
    @Transactional
    @DisplayName("@spec:AC-179 migration remove vínculos e papéis extras e preserva os centrais")
    void cleanupMigration_removesExtraRoleLinksAndKeepsCoreRoleLinks() {
        String suffix = UUID.randomUUID().toString();
        String extraAuthority = "ROLE_LEGACY_" + suffix;

        Role basic = roleRepository.findByAuthority(Role.BASIC).orElseThrow();
        Role admin = roleRepository.findByAuthority(Role.ADMIN).orElseThrow();
        Role extra = new Role();
        extra.setAuthority(extraAuthority);
        extra = roleRepository.saveAndFlush(extra);

        User user = new User();
        user.setEmail("role-migration-" + suffix + "@example.test");
        user.setName("Role migration test");
        user.addRole(basic);
        user.addRole(admin);
        user.addRole(extra);
        user = userRepository.saveAndFlush(user);
        Long userId = user.getId();
        Long extraRoleId = extra.getId();
        Long basicRoleId = basic.getId();
        Long adminRoleId = admin.getId();

        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/migration/V8__remove_non_core_roles.sql"));
            return null;
        });

        assertFalse(roleRepository.existsByAuthority(extraAuthority));
        assertNotNull(roleRepository.findByAuthority(Role.BASIC).orElse(null));
        assertNotNull(roleRepository.findByAuthority(Role.ADMIN).orElse(null));
        assertEquals(0, countRoleLinks(userId, extraRoleId));
        assertEquals(1, countRoleLinks(userId, basicRoleId));
        assertEquals(1, countRoleLinks(userId, adminRoleId));
        assertTrue(roleRepository.existsById(basic.getId()));
        assertTrue(roleRepository.existsById(admin.getId()));
    }

    private int countRoleLinks(Long userId, Long roleId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from users_roles where user_id = ? and role_id = ?", Integer.class, userId, roleId);
        return count == null ? 0 : count;
    }
}
