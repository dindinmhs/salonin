package com.kelompok11.salonin.repository;

import com.kelompok11.salonin.model.TopupHistory;
import com.kelompok11.salonin.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TopupHistoryRepository extends JpaRepository<TopupHistory, Long> {
    List<TopupHistory> findByUserOrderByCreatedAtDesc(User user);
    TopupHistory findByMidtransId(String midtransId);
}
