package com.project.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA Auditing so that @CreatedDate and @LastModifiedDate
 * in BaseEntity are automatically populated on save/update.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
