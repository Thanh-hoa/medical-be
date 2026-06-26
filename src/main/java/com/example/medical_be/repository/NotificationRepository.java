package com.example.medical_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.medical_be.entity.Notification;
import com.example.medical_be.entity.Account;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserOrderByCreatedAtDesc(Account user, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.isRead = false ORDER BY n.createdAt DESC")
    Page<Notification> findUnreadByUser(@Param("user") Account user, Pageable pageable);

    long countByUserAndIsReadFalse(Account user);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndResourceTypeAndResourceIdAndIsReadFalse(
            Long userId,
            String resourceType,
            Long resourceId
    );
}
