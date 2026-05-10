package com.project.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableJpaAuditing
@EnableScheduling
public class AppConfig {
    // Activates @CreatedDate / @LastModifiedDate on BaseEntity
    // and @Scheduled on all @Component beans.
}
