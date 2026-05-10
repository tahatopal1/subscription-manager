package com.project.notification.repository;

import com.project.notification.entity.Notification;
import com.project.notification.entity.enums.NotificationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long>,
        JpaSpecificationExecutor<Notification> {

    /**
     * Retry job: fetches the top N FAILED notifications that are still eligible
     * for automatic retry (retryCount below the configured ceiling).
     */
    @Query("""
            SELECT n FROM Notification n
            WHERE n.status = :status
              AND n.retryCount < :maxRetries
            ORDER BY n.createdAt ASC
            """)
    List<Notification> findRetryEligible(
            @Param("status")     NotificationStatus status,
            @Param("maxRetries") int maxRetries,
            Pageable pageable);
}
