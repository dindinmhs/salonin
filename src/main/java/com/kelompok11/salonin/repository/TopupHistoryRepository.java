package com.kelompok11.salonin.repository;

import com.kelompok11.salonin.model.TopupHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopupHistoryRepository extends JpaRepository<TopupHistory, Long> {
    TopupHistory findByMidtransId(String midtransId);
}
