package com.kelompok11.salonin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kelompok11.salonin.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>{
    
}
