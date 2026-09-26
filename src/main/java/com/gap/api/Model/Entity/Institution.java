package com.gap.api.Model.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.Map;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "institutions")
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length =  50)
    private String code; //cnpj

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private AuthProviderType authProviderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "signature_provider", nullable = false)
    private SignatureProviderType signatureProvider;

    // Configurações dinâmicas: cores da marca, URL do logo, textos institucionais
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> settings;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private ZonedDateTime createdAt;


    public enum AuthProviderType {
        LOCAL,
        CAFE_SHIBBOLETH,
        OAUTH2_CUSTOM
    }

    public enum SignatureProviderType {
        NONE,
        GOV_BR,
        ICP_BRASIL,
        INTERNAL_CERTIFICATE
    }
}
