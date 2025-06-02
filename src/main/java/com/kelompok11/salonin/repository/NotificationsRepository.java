package com.kelompok11.salonin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.kelompok11.salonin.model.Notifications;
import com.kelompok11.salonin.model.User;

@Repository
public interface NotificationsRepository extends JpaRepository<Notifications, Long>{
    List<Notifications> findByUserOrderByCreatedAtDesc(User user);
    List<Notifications> findByUserAndIsReadOrderByCreatedAtDesc(User user, Boolean isRead);
    long countByUserAndIsRead(User user, Boolean isRead);
}
