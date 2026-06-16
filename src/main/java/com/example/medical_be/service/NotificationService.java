package com.example.medical_be.service;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.medical_be.dto.res.PagedResponse;
import com.example.medical_be.entity.Account;
import com.example.medical_be.entity.Notification;
import com.example.medical_be.entity.Notification.NotificationType;
import com.example.medical_be.exception.ApplicationException;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.repository.AccountRepository;
import com.example.medical_be.repository.NotificationRepository;
import com.example.medical_be.support.AccountSupport;
import com.example.medical_be.support.PaginationUtils;

import java.util.LinkedHashMap;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class NotificationService {

    final NotificationRepository notificationRepository;
    final AccountRepository accountRepository;
    final AccountSupport accountSupport;
    final IMessageTranslator messageTranslator;

    @Transactional
    public Notification createNotification(
            Account user,
            String title,
            String message,
            NotificationType type,
            String resourceType,
            Long resourceId) {

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .isRead(false)
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public PagedResponse<LinkedHashMap<String, Object>> listByCurrentUser(Integer page, Integer limit) {
        Long currentUserId = accountSupport.getCurrentAccountId();
        Account currentUser = accountRepository.findById(currentUserId)
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("account.not_found")));

        int p = PaginationUtils.normalizePage(page, messageTranslator);
        int l = PaginationUtils.normalizeLimit(limit, messageTranslator);
        Pageable pageable = PageRequest.of(p, l, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> pageRes = notificationRepository.findByUserOrderByCreatedAtDesc(currentUser, pageable);

        List<LinkedHashMap<String, Object>> items = pageRes.getContent().stream()
                .map(this::toRes)
                .toList();

        return PaginationUtils.buildPagedResponse(items, pageRes, page, l);
    }

    @Transactional(readOnly = true)
    public long countUnreadByCurrentUser() {
        Long currentUserId = accountSupport.getCurrentAccountId();
        Account currentUser = accountRepository.findById(currentUserId)
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("account.not_found")));
        return notificationRepository.countByUserAndIsReadFalse(currentUser);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Long currentUserId = accountSupport.getCurrentAccountId();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("notification.not_found")));

        if (!notification.getUser().getId().equals(currentUserId)) {
            throw new ApplicationException(messageTranslator.getMessage("notification.not_found"));
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead() {
        Long currentUserId = accountSupport.getCurrentAccountId();

        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .filter(n -> !n.getIsRead())
                .toList();

        unreadNotifications.forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(LocalDateTime.now());
        });

        notificationRepository.saveAll(unreadNotifications);
    }

    @Transactional
    public void markResourceAsReadForCurrentUser(String resourceType, Long resourceId) {
        Long currentUserId = accountSupport.getCurrentAccountId();

        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndResourceTypeAndResourceIdAndIsReadFalse(currentUserId, resourceType, resourceId);

        if (unreadNotifications.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        unreadNotifications.forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(now);
        });

        notificationRepository.saveAll(unreadNotifications);
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        Long currentUserId = accountSupport.getCurrentAccountId();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("notification.not_found")));

        if (!notification.getUser().getId().equals(currentUserId)) {
            throw new ApplicationException(messageTranslator.getMessage("notification.not_found"));
        }

        notificationRepository.delete(notification);
    }

    private LinkedHashMap<String, Object> toRes(Notification notification) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("id", notification.getId());
        map.put("title", notification.getTitle());
        map.put("message", notification.getMessage());
        map.put("type", notification.getType().toString());
        map.put("resourceType", notification.getResourceType());
        map.put("resourceId", notification.getResourceId());
        map.put("isRead", notification.getIsRead());
        map.put("createdAt", notification.getCreatedAt());
        map.put("readAt", notification.getReadAt());
        return map;
    }
}
