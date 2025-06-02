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
        return reviewRepository.save(review);
    }
    
    public List<Review> getReviewsByBooking(Long bookingId) {
        return reviewRepository.findByBookingId(bookingId);
    }
    
    public Optional<Review> findByBookingId(Long bookingId) {
        return reviewRepository.findByBookingId(bookingId).stream().findFirst();
    }
}