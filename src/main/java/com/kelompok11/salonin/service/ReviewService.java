package com.kelompok11.salonin.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kelompok11.salonin.model.Booking;
import com.kelompok11.salonin.model.Review;
import com.kelompok11.salonin.repository.BookingRepository;
import com.kelompok11.salonin.repository.ReviewRepository;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    public Review createReview(Review review, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
            
        if (booking.getStatus() != Booking.Status.SELESAI) {
            throw new RuntimeException("Cannot review: Booking is not completed");
        }
        
        review.setBooking(booking);
        review.setUser(booking.getUser()); // Set user dari booking
        return reviewRepository.save(review);
    }
    
    public List<Review> getReviewsByBooking(Long bookingId) {
        return reviewRepository.findByBookingId(bookingId);
    }
    
    public List<Review> getReviewsByBranch(Long branchId) {
        return reviewRepository.findByBranchIdOrderByCreatedAtDesc(branchId);
    }
    
    public Optional<Review> findByBookingId(Long bookingId) {
        return reviewRepository.findByBookingId(bookingId).stream().findFirst();
    }
    
    public Double getBranchAverageRating(Long branchId) {
        Double average = reviewRepository.getAverageRatingByBranchId(branchId);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0;
    }
    
    public Long getBranchReviewCount(Long branchId) {
        return reviewRepository.getReviewCountByBranchId(branchId);
    }
    
    public Double getOverallAverageRating() {
        Double average = reviewRepository.getOverallAverageRating();
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0;
    }
    
    public static Double getAverageRatingForBranch(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAverageRatingForBranch'");
    }
}