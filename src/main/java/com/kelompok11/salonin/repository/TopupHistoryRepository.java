package com.kelompok11.salonin.repository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kelompok11.salonin.model.TopupHistory;

@Repository
public interface TopupHistoryRepository extends JpaRepository<TopupHistory, Long> {
}
