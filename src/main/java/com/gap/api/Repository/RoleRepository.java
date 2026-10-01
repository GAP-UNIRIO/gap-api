package com.gap.api.Repository;

import com.gap.api.Model.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    boolean existsByAuthority(String authority);
}
