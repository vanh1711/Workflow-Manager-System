package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Notification;
import com.example.taskmanager.enums.NotificationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    long countByRecipientIdAndReadFalse(Long recipientId);

    boolean existsByRecipientIdAndTypeAndTargetUrlAndCreatedAtAfter(
            Long recipientId,
            NotificationType type,
            String targetUrl,
            LocalDateTime after
    );

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipient.id = :recipientId AND n.read = false")
    int markAllAsReadByRecipientId(@Param("recipientId") Long recipientId);
}
