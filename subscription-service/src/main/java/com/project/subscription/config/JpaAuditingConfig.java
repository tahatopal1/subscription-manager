package com.project.subscription.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA Auditing so that @CreatedDate and @LastModifiedDate
 * annotations in BaseEntity are automatically populated on save.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
