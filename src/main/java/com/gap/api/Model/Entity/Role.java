package com.gap.api.Model.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role implements GrantedAuthority {

    /** Papel padrão de todo usuário (aluno e usuário comum). */
    public static final String BASIC = "ROLE_BASIC";
    /** Papel dos servidores: secretaria, coordenação e administração. */
    public static final String ADMIN = "ROLE_ADMIN";
    /** Papéis do núcleo, cadastrados pela migration V5 e protegidos contra exclusão. */
    public static final Set<String> CORE = Set.of(BASIC, ADMIN);

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "authority", nullable = false, unique = true)
    private String authority;

    @Override
    public String getAuthority() {
        return this.authority;
    }
}
