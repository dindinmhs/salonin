package com.kelompok11.salonin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kelompok11.salonin.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBookingId(Long bookingId);
    
    @Query("SELECT AVG(r.rate) FROM Review r WHERE r.booking.branch.id = :branchId")
    Double getAverageRatingByBranchId(@Param("branchId") Long branchId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.booking.branch.id = :branchId")
    Long getReviewCountByBranchId(@Param("branchId") Long branchId);
    
    @Query("SELECT r FROM Review r WHERE r.booking.branch.id = :branchId ORDER BY r.createdAt DESC")
    List<Review> findByBranchIdOrderByCreatedAtDesc(@Param("branchId") Long branchId);
    
    @Query("SELECT AVG(r.rate) FROM Review r")
    Double getOverallAverageRating();
}
