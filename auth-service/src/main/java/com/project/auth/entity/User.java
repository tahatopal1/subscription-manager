package com.project.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity representing an identity account in the system.
 *
 * Rules applied (auth-service spec):
 *  - Single User entity: no separate Admin/Customer entities.
 *  - UUID primary key via GenerationType.UUID (String-based).
 *  - Roles mapped with @ElementCollection + FetchType.EAGER (always needed for JWT generation).
 *  - No @Data: uses @Getter/@Setter/@Builder/@NoArgsConstructor/@AllArgsConstructor.
 *  - Extends BaseEntity for auditing + optimistic locking (04-data-layer.md §2).
 *
 * Security rules (auth-service spec §1):
 *  - password is the ONLY place BCrypt hashes are stored in the entire architecture.
 *  - isLocked allows instant revocation of JWT generation rights.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "surname", length = 100)
    private String surname;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private boolean isLocked = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role", nullable = false, length = 50)
    @Builder.Default
    private Set<String> roles = new HashSet<>();
}
