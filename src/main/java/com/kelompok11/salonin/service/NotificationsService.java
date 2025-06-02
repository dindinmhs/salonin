package com.kelompok11.salonin.service;

import com.kelompok11.salonin.model.Notifications;
import com.kelompok11.salonin.model.User;
import com.kelompok11.salonin.repository.NotificationsRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationsService {
    @Autowired
    private NotificationsRepository notificationsRepository;
    
    public Notifications createNotification(User user, String title, String message) {
        Notifications notification = new Notifications();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        return notificationsRepository.save(notification);
    }
    
    public List<Notifications> getNotificationsByUser(User user) {
        return notificationsRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public List<Notifications> getUnreadNotificationsByUser(User user) {
        return notificationsRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false);
    }
    
    public long countUnreadNotifications(User user) {
        return notificationsRepository.countByUserAndIsRead(user, false);
    }
    
    public Notifications markAsRead(Long notificationId) {
        Notifications notification = notificationsRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setIsRead(true);
        return notificationsRepository.save(notification);
    }
    
    public void markAllAsRead(User user) {
        List<Notifications> unreadNotifications = getUnreadNotificationsByUser(user);
        for (Notifications notification : unreadNotifications) {
            notification.setIsRead(true);
            notificationsRepository.save(notification);
        }
    }
}